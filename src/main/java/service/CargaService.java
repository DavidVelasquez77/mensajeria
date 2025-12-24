package service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import model.ConfiguracionLogitrack;
import repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class CargaService {

    @Autowired
    private DataStore dataStore;


    public String procesarArchivo(MultipartFile archivo) {
        System.out.println("CargaService: Iniciando lectura del archivo...");

        try {

            XmlMapper xmlMapper = new XmlMapper();

            ConfiguracionLogitrack datos = xmlMapper.readValue(archivo.getInputStream(), ConfiguracionLogitrack.class);

            // verificamos que traiga algo
            if (datos == null || datos.getConfiguracion() == null) {
                System.out.println("Error: El XML viene vacio o mal formado");
                return "Error: El XML no tiene el formato correcto o esta vacio.";
            }

            dataStore.limpiarTodo();

            // obtenemos la config interna
            var config = datos.getConfiguracion();



            // Centros
            if (config.getListaCentros() != null) {
                dataStore.getCentros().addAll(config.getListaCentros());
            }

            // Rutas
            if (config.getListaRutas() != null) {
                dataStore.getRutas().addAll(config.getListaRutas());
            }

            // Mensajeros
            if (config.getListaMensajeros() != null) {
                dataStore.getMensajeros().addAll(config.getListaMensajeros());
            }

            // Paquetes
            if (config.getListaPaquetes() != null) {
                dataStore.getPaquetes().addAll(config.getListaPaquetes());
            }

            // Solicitudes
            if (config.getListaSolicitudes() != null) {
                dataStore.getSolicitudes().addAll(config.getListaSolicitudes());
            }

            dataStore.imprimirEstadisticas();

            String reporte = "Carga Exitosa. Objetos creados en memoria: ";
            reporte += "\n - Centros: " + dataStore.getCentros().size();
            reporte += "\n - Rutas: " + dataStore.getRutas().size();
            reporte += "\n - Mensajeros: " + dataStore.getMensajeros().size();
            reporte += "\n - Paquetes: " + dataStore.getPaquetes().size();
            reporte += "\n - Solicitudes: " + dataStore.getSolicitudes().size();

            return reporte;

        } catch (IOException e) {
            e.printStackTrace();
            return "Error al leer el archivo: " + e.getMessage();
        } catch (Exception e) {
            System.out.println("Ocurrio un error inesperado: " + e.getMessage());
            return "Error inesperado: " + e.getMessage();
        }
    }
}