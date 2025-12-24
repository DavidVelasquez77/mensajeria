package service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import model.*;
import repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private DataStore dataStore;

    public String generarXmlSalida() {
        System.out.println("ReporteService: Calculando estadisticas y generando XML...");

        try {

            ResultadoLogitrack resultado = new ResultadoLogitrack();

            // Calculamos las Estadisticas
            Estadisticas stats = new Estadisticas();

            // Contamos paquetes que ya no estan pendientes
            long procesados = dataStore.getPaquetes().stream()
                    .filter(p -> !"PENDIENTE".equals(p.getEstado()))
                    .count();
            stats.setPaquetesProcesados((int) procesados);

            stats.setSolicitudesAtendidas(dataStore.getSolicitudes().size());

            // Mensajeros totales
            stats.setMensajerosActivos(dataStore.getMensajeros().size());

            resultado.setEstadisticas(stats);

            // Agregamos las listas de datos actuales
            resultado.setCentros(dataStore.getCentros());
            resultado.setMensajeros(dataStore.getMensajeros());
            resultado.setPaquetes(dataStore.getPaquetes());
            resultado.setSolicitudes(dataStore.getSolicitudes());

            // Convertimos a texto XML
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

            return xmlMapper.writeValueAsString(resultado);

        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Error generando reporte: " + e.getMessage() + "</error>";
        }
    }


    @JacksonXmlRootElement(localName = "resultadoLogitrack")
    static class ResultadoLogitrack {
        @JacksonXmlProperty(localName = "estadisticas")
        private Estadisticas estadisticas;

        @JacksonXmlProperty(localName = "centros")
        private List<Centro> centros;

        @JacksonXmlProperty(localName = "mensajeros")
        private List<Mensajero> mensajeros;

        @JacksonXmlProperty(localName = "paquetes")
        private List<Paquete> paquetes;

        @JacksonXmlProperty(localName = "solicitudes")
        private List<Solicitud> solicitudes;

        // Getters y Setters
        public Estadisticas getEstadisticas() { return estadisticas; }
        public void setEstadisticas(Estadisticas estadisticas) { this.estadisticas = estadisticas; }
        public List<Centro> getCentros() { return centros; }
        public void setCentros(List<Centro> centros) { this.centros = centros; }
        public List<Mensajero> getMensajeros() { return mensajeros; }
        public void setMensajeros(List<Mensajero> mensajeros) { this.mensajeros = mensajeros; }
        public List<Paquete> getPaquetes() { return paquetes; }
        public void setPaquetes(List<Paquete> paquetes) { this.paquetes = paquetes; }
        public List<Solicitud> getSolicitudes() { return solicitudes; }
        public void setSolicitudes(List<Solicitud> solicitudes) { this.solicitudes = solicitudes; }
    }

    static class Estadisticas {
        private int paquetesProcesados;
        private int solicitudesAtendidas;
        private int mensajerosActivos;

        public int getPaquetesProcesados() { return paquetesProcesados; }
        public void setPaquetesProcesados(int paquetesProcesados) { this.paquetesProcesados = paquetesProcesados; }
        public int getSolicitudesAtendidas() { return solicitudesAtendidas; }
        public void setSolicitudesAtendidas(int solicitudesAtendidas) { this.solicitudesAtendidas = solicitudesAtendidas; }
        public int getMensajerosActivos() { return mensajerosActivos; }
        public void setMensajerosActivos(int mensajerosActivos) { this.mensajerosActivos = mensajerosActivos; }
    }
}