package com.usac.logitrack.backend.service;

import com.usac.logitrack.backend.model.*;
import com.usac.logitrack.backend.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DespachoService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private RutaService rutaService;

    // Metodo unico para procesar todo el lote de solicitudes
    public String procesarDespacho() {
        System.out.println("DespachoService: Iniciando proceso de asignacion...");

        // Traemos todas las listas a variables locales
        List<Solicitud> listaSolicitudes = dataStore.getSolicitudes();
        List<Paquete> listaPaquetes = dataStore.getPaquetes();
        List<Mensajero> listaMensajeros = dataStore.getMensajeros();

        if (listaSolicitudes.isEmpty()) {
            return "No hay solicitudes pendientes para procesar.";
        }

        int contadorExitos = 0;
        int contadorFallos = 0;

        // Recorremos cada solicitud
        for (int i = 0; i < listaSolicitudes.size(); i++) {
            Solicitud solicitud = listaSolicitudes.get(i);

            //  Buscar el Paquete correspondien
            Paquete paqueteEncontrado = null;
            for (Paquete p : listaPaquetes) {
                if (p.getId().equals(solicitud.getPaquete())) {
                    paqueteEncontrado = p;
                    break;
                }
            }

            if (paqueteEncontrado == null) {
                System.out.println("Error: La solicitud " + solicitud.getId() + " busca un paquete que no existe.");
                contadorFallos++;
                continue;
            }

            if ("ENTREGADO".equals(paqueteEncontrado.getEstado()) || "EN_CAMINO".equals(paqueteEncontrado.getEstado())) {
                // Si ya se despacho, no hacemos nada
                continue;
            }

            //  Calcular la ruta para saber si es posible entregarlo
            String origen = paqueteEncontrado.getCentroActual();
            String destino = paqueteEncontrado.getDestino();

            List<String> ruta = rutaService.buscarRuta(origen, destino);

            if (ruta.isEmpty()) {
                System.out.println("Imposible despachar paquete " + paqueteEncontrado.getId() + ": No existe ruta entre " + origen + " y " + destino);
                contadorFallos++;
                continue;
            }

            // Buscar un mensajero disponible en el origen
            Mensajero mensajeroElegido = null;
            for (Mensajero m : listaMensajeros) {
                // Verificamos que este en el mismo lugar
                if (m.getCentro().equals(origen)) {
                    mensajeroElegido = m;
                    break; // Agarramos el primero que aparezca y salimos
                }
            }

            if (mensajeroElegido == null) {
                System.out.println("Fallo: No hay mensajeros en " + origen + " para llevar el paquete.");
                contadorFallos++;
                continue;
            }

            // Si todo esta bien, actualizamos el estado
            System.out.println("Asignando paquete " + paqueteEncontrado.getId() + " al mensajero " + mensajeroElegido.getNombre());

            paqueteEncontrado.setEstado("EN_CAMINO");

            contadorExitos++;
        }

        return "Proceso finalizado. Paquetes despachados: " + contadorExitos + ". Errores: " + contadorFallos;
    }
}