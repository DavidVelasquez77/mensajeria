package controller;

import model.Mensajero;
import model.Paquete;
import repository.DataStore;
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
    @PutMapping("/asignar")
    public ResponseEntity<String> asignarManual(@RequestBody Map<String, String> body) {
        String idPaquete = body.get("paqueteId");
        String idMensajero = body.get("mensajeroId");

        System.out.println("Asignacion manual: " + idPaquete + " -> " + idMensajero);

        Paquete elPaquete = null;
        for (Paquete p : datos.getPaquetes()) {
            if (p.getId().equals(idPaquete)) {
                elPaquete = p;
                break;
            }
        }
        if (elPaquete == null) return ResponseEntity.badRequest().body("Error: Paquete no existe.");

        Mensajero elMensajero = null;
        for (Mensajero m : datos.getMensajeros()) {
            if (m.getId().equals(idMensajero)) {
                elMensajero = m;
                break;
            }
        }
        if (elMensajero == null) return ResponseEntity.badRequest().body("Error: Mensajero no existe.");

        // Validaciones
        if (!elPaquete.getCentroActual().equals(elMensajero.getCentro())) {
            return ResponseEntity.badRequest().body("Error: Deben estar en el mismo centro.");
        }
        if (!elPaquete.getEstado().equals("PENDIENTE")) {
            return ResponseEntity.badRequest().body("Error: El paquete no esta PENDIENTE.");
        }
        if (!elMensajero.getEstado().equals("DISPONIBLE")) {
            return ResponseEntity.badRequest().body("Error: El mensajero no esta DISPONIBLE.");
        }

        // Ejecutar
        elPaquete.setEstado("EN_TRANSITO");
        elMensajero.setEstado("EN_TRANSITO");

        return ResponseEntity.ok("Asignacion completada.");
    }

    // 2. GESTIÓN DEL ESTADO DEL ENVÍO (CORREGIDO)
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> actualizarEstado(@PathVariable String id, @RequestBody String nuevoEstado) {

        System.out.println("Cambio de estado para paquete: " + id);

        String estadoLimpio = nuevoEstado.replace("\"", "").trim();

        Paquete paqueteEncontrado = null;
        for (Paquete p : datos.getPaquetes()) {
            if (p.getId().equals(id)) {
                paqueteEncontrado = p;
                break;
            }
        }

        if (paqueteEncontrado == null) return ResponseEntity.status(404).body("Error: Paquete no encontrado.");

        String estadoActual = paqueteEncontrado.getEstado();

        // Validaciones de Transición
        if (estadoActual.equals("ENTREGADO")) {
            return ResponseEntity.badRequest().body("Error: Ya fue entregado.");
        }
        if (estadoActual.equals("PENDIENTE") && estadoLimpio.equals("ENTREGADO")) {
            return ResponseEntity.badRequest().body("Error: Debe pasar por EN_TRANSITO.");
        }
        if (estadoActual.equals("EN_TRANSITO") && estadoLimpio.equals("PENDIENTE")) {
            return ResponseEntity.badRequest().body("Error: No se puede revertir.");
        }

        // --- APLICAR CAMBIO ---
        paqueteEncontrado.setEstado(estadoLimpio);
        String timeStamp = LocalDateTime.now().toString();
        System.out.println("Nuevo estado: " + estadoLimpio + " | Hora: " + timeStamp);

        // --- REGLA: FINALIZAR VIAJE Y LIBERAR MENSAJERO ---
        if (estadoLimpio.equals("ENTREGADO")) {
            System.out.println("Paquete entregado. Finalizando logistica...");

            // Guardamos donde estaba el paquete antes de actualizarlo
            String centroOrigenViaje = paqueteEncontrado.getCentroActual();
            String centroDestinoViaje = paqueteEncontrado.getDestino();

            // 1. Mover el paquete al destino final
            paqueteEncontrado.setCentroActual(centroDestinoViaje);

            // 2. Buscar al mensajero que venia del origen
            boolean mensajeroLiberado = false;

            for (Mensajero m : datos.getMensajeros()) {
                // Buscamos un mensajero EN_TRANSITO que siga registrado en el origen del viaje
                if (m.getEstado().equals("EN_TRANSITO") && m.getCentro().equals(centroOrigenViaje)) {

                    // SIMULACION DE LLEGADA:
                    m.setCentro(centroDestinoViaje); // Lo movemos al destino
                    m.setEstado("DISPONIBLE");       // Lo liberamos

                    System.out.println("Mensajero " + m.getNombre() + " viajo de " + centroOrigenViaje + " a " + centroDestinoViaje + " y esta LIBRE.");
                    mensajeroLiberado = true;
                    break;
                }
            }

            if (!mensajeroLiberado) {
                System.out.println("Nota: No se encontro mensajero en el origen para mover. (Tal vez ya estaba en destino)");
            }
        }

        return ResponseEntity.ok("Estado actualizado a " + estadoLimpio);
    }
}