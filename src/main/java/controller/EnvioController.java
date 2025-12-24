package com.usac.logitrack.backend.controller;

import com.usac.logitrack.backend.model.Mensajero;
import com.usac.logitrack.backend.model.Paquete;
import com.usac.logitrack.backend.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    @Autowired
    DataStore datos;

    //  1. ASIGNACIÓN DIRECTA
    // Endpoint: PUT /api/envios/asignar
    @PutMapping("/asignar")
    public ResponseEntity<String> asignarManual(@RequestBody Map<String, String> body) {

        String idPaquete = body.get("paqueteId");
        String idMensajero = body.get("mensajeroId");

        System.out.println("Intento de asignacion manual: Paquete " + idPaquete + " -> Mensajero " + idMensajero);

        // busca el paquete
        Paquete elPaquete = null;
        for (Paquete p : datos.getPaquetes()) {
            if (p.getId().equals(idPaquete)) {
                elPaquete = p;
                break;
            }
        }

        if (elPaquete == null) {
            return ResponseEntity.badRequest().body("Error: El paquete no existe.");
        }

        // buca el mensajero
        Mensajero elMensajero = null;
        for (Mensajero m : datos.getMensajeros()) {
            if (m.getId().equals(idMensajero)) {
                elMensajero = m;
                break;
            }
        }

        if (elMensajero == null) {
            return ResponseEntity.badRequest().body("Error: El mensajero no existe.");
        }

        // Deben estar en el mismo centro
        String centroPaquete = elPaquete.getCentroActual();
        String centroMensajero = elMensajero.getCentro();

        if (!centroPaquete.equals(centroMensajero)) {
            System.out.println("Fallo ubicacion: Paquete en " + centroPaquete + " vs Mensajero en " + centroMensajero);
            return ResponseEntity.badRequest().body("Error: El paquete y el mensajero deben estar en el mismo centro.");
        }

        if (!elPaquete.getEstado().equals("PENDIENTE")) {
            return ResponseEntity.badRequest().body("Error: El paquete no esta PENDIENTE (ya fue enviado o entregado).");
        }
        if (!elMensajero.getEstado().equals("DISPONIBLE")) {
            return ResponseEntity.badRequest().body("Error: El mensajero no esta DISPONIBLE.");
        }

        elPaquete.setEstado("EN_TRANSITO");
        elMensajero.setEstado("EN_TRANSITO");

        System.out.println("Asignacion manual exitosa.");
        return ResponseEntity.ok("Asignacion completada: Paquete y Mensajero ahora estan EN_TRANSITO.");
    }

    // GESTIÓN DEL ESTADO DEL ENVÍO
    // Endpoint: PUT /api/envios/{id}/estado
    // Recibe un string simple en el body, ejemplo: "ENTREGADO"
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> actualizarEstado(@PathVariable String id, @RequestBody String nuevoEstado) {

        System.out.println("Solicitud de cambio de estado para paquete ID: " + id);

        String estadoLimpio = nuevoEstado.replace("\"", "").trim();

        // Buscar Paquete
        Paquete paqueteEncontrado = null;
        for (Paquete p : datos.getPaquetes()) {
            if (p.getId().equals(id)) {
                paqueteEncontrado = p;
                break;
            }
        }

        if (paqueteEncontrado == null) {
            return ResponseEntity.status(404).body("Error: Paquete no encontrado.");
        }

        String estadoActual = paqueteEncontrado.getEstado();


        // No se puede modificar si ya se entregó
        if (estadoActual.equals("ENTREGADO")) {
            return ResponseEntity.badRequest().body("Error: El paquete ya fue ENTREGADO, no se puede cambiar mas.");
        }

        // No se puede saltar pasos (PENDIENTE -> ENTREGADO prohibido)
        if (estadoActual.equals("PENDIENTE") && estadoLimpio.equals("ENTREGADO")) {
            return ResponseEntity.badRequest().body("Error: Transicion invalida. Debe pasar por EN_TRANSITO primero.");
        }

        // No se puede retroceder (EN_TRANSITO -> PENDIENTE prohibido)
        if (estadoActual.equals("EN_TRANSITO") && estadoLimpio.equals("PENDIENTE")) {
            return ResponseEntity.badRequest().body("Error: No se puede revertir un envio en transito.");
        }


        paqueteEncontrado.setEstado(estadoLimpio);

        // Registro del timestamp en memoria
        String timeStamp = LocalDateTime.now().toString();
        System.out.println("Cambio registrado a las: " + timeStamp + " | Nuevo estado: " + estadoLimpio);

        // liberar al mensajero si se entrega
        if (estadoLimpio.equals("ENTREGADO")) {
            System.out.println("Paquete entregado. Buscando mensajero para liberar...");

            boolean mensajeroLiberado = false;

            for (Mensajero m : datos.getMensajeros()) {
                // Verificamos si esta ocupado y si coincide con el destino del paquete
                if (m.getEstado().equals("EN_TRANSITO") && m.getCentro().equals(paqueteEncontrado.getDestino())) {
                    m.setEstado("DISPONIBLE");
                    System.out.println("Mensajero " + m.getNombre() + " ahora esta DISPONIBLE.");
                    mensajeroLiberado = true;
                    break; // Liberamos solo a uno
                }
            }

            if (!mensajeroLiberado) {
                System.out.println("Advertencia: No se encontro mensajero exacto para liberar en el destino.");
            }
        }

        return ResponseEntity.ok("Estado actualizado correctamente a " + estadoLimpio);
    }
}