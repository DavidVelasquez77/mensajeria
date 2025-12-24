package service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import model.*;
import repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private DataStore dataStore;

    public String generarXmlSalida() {
        try {
            // 1. Instanciamos la raiz del XML
            ResultadoLogitrack resultado = new ResultadoLogitrack();

            // 2. CALCULO DE ESTADISTICAS (Dinámico)
            Estadisticas stats = new Estadisticas();

            // Paquetes procesados = Aquellos que ya no estan PENDIENTE
            long conteoPaquetesProcesados = dataStore.getPaquetes().stream()
                    .filter(p -> !"PENDIENTE".equals(p.getEstado()))
                    .count();
            stats.setPaquetesProcesados((int) conteoPaquetesProcesados);

            // Solicitudes atendidas = Aquellas cuyo paquete asociado ya se movió
            long conteoSolicitudesAtendidas = 0;
            if (dataStore.getSolicitudes() != null) {
                for (Solicitud s : dataStore.getSolicitudes()) {
                    Paquete p = buscarPaquete(s.getPaquete());
                    if (p != null && !p.getEstado().equals("PENDIENTE")) {
                        conteoSolicitudesAtendidas++;
                    }
                }
            }
            stats.setSolicitudesAtendidas((int) conteoSolicitudesAtendidas);

            // Mensajeros Totales
            stats.setMensajerosActivos(dataStore.getMensajeros().size());

            resultado.setEstadisticas(stats);

            // 3. PROCESAR CENTROS (Calculando inventario actual)
            List<CentroReporte> listaCentros = new ArrayList<>();
            if (dataStore.getCentros() != null) {
                for (Centro c : dataStore.getCentros()) {
                    CentroReporte cr = new CentroReporte();
                    cr.setId(c.getId());

                    // Cuenta paquetes que están fisicamente en este centro ahora mismo
                    long paqEnCentro = dataStore.getPaquetes().stream()
                            .filter(p -> p.getCentroActual().equals(c.getId()))
                            .count();

                    // Cuenta mensajeros que están en este centro y disponibles
                    long mensajerosDisp = dataStore.getMensajeros().stream()
                            .filter(m -> m.getCentro().equals(c.getId()) && "DISPONIBLE".equals(m.getEstado()))
                            .count();

                    cr.setPaquetesActuales((int) paqEnCentro);
                    cr.setMensajerosDisponibles((int) mensajerosDisp);
                    listaCentros.add(cr);
                }
            }
            resultado.setCentros(listaCentros);

            // 4. PROCESAR MENSAJEROS
            List<MensajeroReporte> listaMensajeros = new ArrayList<>();
            if (dataStore.getMensajeros() != null) {
                for (Mensajero m : dataStore.getMensajeros()) {
                    MensajeroReporte mr = new MensajeroReporte();
                    mr.setId(m.getId());
                    mr.setEstado(m.getEstado());
                    listaMensajeros.add(mr);
                }
            }
            resultado.setMensajeros(listaMensajeros);

            // 5. PROCESAR PAQUETES
            List<PaqueteReporte> listaPaquetes = new ArrayList<>();
            if (dataStore.getPaquetes() != null) {
                for (Paquete p : dataStore.getPaquetes()) {
                    PaqueteReporte pr = new PaqueteReporte();
                    pr.setId(p.getId());
                    pr.setEstado(p.getEstado());
                    pr.setCentroActual(p.getCentroActual());
                    listaPaquetes.add(pr);
                }
            }
            resultado.setPaquetes(listaPaquetes);

            // 6. PROCESAR SOLICITUDES (Logica Dinámica de Estado)
            List<SolicitudReporte> listaSolicitudes = new ArrayList<>();
            if (dataStore.getSolicitudes() != null) {
                for (Solicitud s : dataStore.getSolicitudes()) {
                    SolicitudReporte sr = new SolicitudReporte();
                    sr.setId(s.getId());

                    // BUSCAMOS EL PAQUETE PARA VER SI LA SOLICITUD YA FUE ATENDIDA
                    Paquete p = buscarPaquete(s.getPaquete());

                    // Si el paquete ya se mueve (no es PENDIENTE), la solicitud se considera ATENDIDA
                    if (p != null && !p.getEstado().equals("PENDIENTE")) {
                        sr.setEstado("ATENDIDA");
                    } else {
                        sr.setEstado("PENDIENTE");
                    }

                    sr.setPaquete(s.getPaquete());
                    listaSolicitudes.add(sr);
                }
            }
            resultado.setSolicitudes(listaSolicitudes);

            // GENERACION FINAL DEL XML
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            return xmlMapper.writeValueAsString(resultado);

        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Error generando reporte XML: " + e.getMessage() + "</error>";
        }
    }

    // Helper para buscar paquete seguro
    private Paquete buscarPaquete(String id) {
        if (dataStore.getPaquetes() == null) return null;
        return dataStore.getPaquetes().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // ==========================================
    // CLASES INTERNAS (DTOs) PARA LA ESTRUCTURA XML
    // ==========================================

    @JacksonXmlRootElement(localName = "resultadoLogitrack")
    static class ResultadoLogitrack {
        @JacksonXmlProperty(localName = "estadisticas")
        private Estadisticas estadisticas;

        @JacksonXmlElementWrapper(localName = "centros")
        @JacksonXmlProperty(localName = "centro")
        private List<CentroReporte> centros;

        @JacksonXmlElementWrapper(localName = "mensajeros")
        @JacksonXmlProperty(localName = "mensajero")
        private List<MensajeroReporte> mensajeros;

        @JacksonXmlElementWrapper(localName = "paquetes")
        @JacksonXmlProperty(localName = "paquete")
        private List<PaqueteReporte> paquetes;

        @JacksonXmlElementWrapper(localName = "solicitudes")
        @JacksonXmlProperty(localName = "solicitud")
        private List<SolicitudReporte> solicitudes;

        // Getters y Setters
        public Estadisticas getEstadisticas() { return estadisticas; }
        public void setEstadisticas(Estadisticas e) { this.estadisticas = e; }
        public List<CentroReporte> getCentros() { return centros; }
        public void setCentros(List<CentroReporte> c) { this.centros = c; }
        public List<MensajeroReporte> getMensajeros() { return mensajeros; }
        public void setMensajeros(List<MensajeroReporte> m) { this.mensajeros = m; }
        public List<PaqueteReporte> getPaquetes() { return paquetes; }
        public void setPaquetes(List<PaqueteReporte> p) { this.paquetes = p; }
        public List<SolicitudReporte> getSolicitudes() { return solicitudes; }
        public void setSolicitudes(List<SolicitudReporte> s) { this.solicitudes = s; }
    }

    static class Estadisticas {
        private int paquetesProcesados;
        private int solicitudesAtendidas;
        private int mensajerosActivos;

        public int getPaquetesProcesados() { return paquetesProcesados; }
        public void setPaquetesProcesados(int p) { this.paquetesProcesados = p; }
        public int getSolicitudesAtendidas() { return solicitudesAtendidas; }
        public void setSolicitudesAtendidas(int s) { this.solicitudesAtendidas = s; }
        public int getMensajerosActivos() { return mensajerosActivos; }
        public void setMensajerosActivos(int m) { this.mensajerosActivos = m; }
    }

    static class CentroReporte {
        @JacksonXmlProperty(isAttribute = true)
        private String id;
        private int paquetesActuales;
        private int mensajerosDisponibles;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getPaquetesActuales() { return paquetesActuales; }
        public void setPaquetesActuales(int p) { this.paquetesActuales = p; }
        public int getMensajerosDisponibles() { return mensajerosDisponibles; }
        public void setMensajerosDisponibles(int m) { this.mensajerosDisponibles = m; }
    }

    static class MensajeroReporte {
        @JacksonXmlProperty(isAttribute = true)
        private String id;
        @JacksonXmlProperty(isAttribute = true)
        private String estado;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEstado() { return estado; }
        public void setEstado(String e) { this.estado = e; }
    }

    static class PaqueteReporte {
        @JacksonXmlProperty(isAttribute = true)
        private String id;
        @JacksonXmlProperty(isAttribute = true)
        private String estado;
        @JacksonXmlProperty(isAttribute = true)
        private String centroActual;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEstado() { return estado; }
        public void setEstado(String e) { this.estado = e; }
        public String getCentroActual() { return centroActual; }
        public void setCentroActual(String c) { this.centroActual = c; }
    }

    static class SolicitudReporte {
        @JacksonXmlProperty(isAttribute = true)
        private String id;
        @JacksonXmlProperty(isAttribute = true)
        private String estado;
        @JacksonXmlProperty(isAttribute = true)
        private String paquete;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEstado() { return estado; }
        public void setEstado(String e) { this.estado = e; }
        public String getPaquete() { return paquete; }
        public void setPaquete(String p) { this.paquete = p; }
    }
}