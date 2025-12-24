package controller;

import model.Mensajero;
import model.Paquete;
import model.Solicitud;
import repository.DataStore;
import service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    @Autowired
    DataStore dataStore;

    @Autowired
    RutaService servicioRutas;

    // Clase auxiliar para controlar la carga del mensajero en memoria durante el proceso
    private static class EstadoMensajeroTemporal {
        Mensajero mensajeroReal;
        double capacidadRestante;
        String destinoActual; // Para asegurar que agrupe paquetes al mismo destino

        public EstadoMensajeroTemporal(Mensajero m) {
            this.mensajeroReal = m;
            this.capacidadRestante = m.getCapacidad();
            this.destinoActual = null;
        }
    }

    // 1. LISTAR LA COLA (GET)
    @GetMapping
    public List<Solicitud> verCola() {
        List<Solicitud> cola = dataStore.getSolicitudes();
        if (cola == null) return new ArrayList<>();

        // Ordenamos Mayor prioridad primero
        cola.sort((s1, s2) -> Integer.compare(s2.getPrioridad(), s1.getPrioridad()));
        return cola;
    }

    // 2. CREAR SOLICITUD
    @PostMapping
    public ResponseEntity<String> crearSolicitud(@RequestBody Solicitud nueva) {
        boolean paqueteExiste = false;
        if (dataStore.getPaquetes() != null) {
            for (Paquete p : dataStore.getPaquetes()) {
                if (p.getId().equals(nueva.getPaquete())) {
                    paqueteExiste = true;
                    if (!p.getEstado().equals("PENDIENTE")) {
                        return ResponseEntity.badRequest().body("Error: El paquete ya no esta pendiente.");
                    }
                    break;
                }
            }
        }
        if (!paqueteExiste) {
            return ResponseEntity.badRequest().body("Error: El paquete no existe.");
        }
        dataStore.getSolicitudes().add(nueva);
        return ResponseEntity.ok("Solicitud agregada a la cola.");
    }

    // 3. ELIMINAR SOLICITUD
    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarSolicitud(@PathVariable String id) {
        Solicitud aBorrar = null;
        for (Solicitud s : dataStore.getSolicitudes()) {
            if (s.getId().equals(id)) {
                aBorrar = s;
                break;
            }
        }
        if (aBorrar != null) {
            dataStore.getSolicitudes().remove(aBorrar);
            return ResponseEntity.ok("Solicitud eliminada.");
        }
        return ResponseEntity.status(404).body("No encontrada.");
    }

    // 4. PROCESAR LA DE MAYOR PRIORIDAD
    @PostMapping("/procesar")
    public ResponseEntity<String> procesarTop1() {
        return procesarLogica(1);
    }

    // 5. PROCESAR LAS N MAS PRIORITARIAS
    @PostMapping("/procesar/{n}")
    public ResponseEntity<String> procesarTopN(@PathVariable int n) {
        return procesarLogica(n);
    }

    // --- LOGICA DE PROCESAMIENTO MEJORADA (CARGA MÚLTIPLE) ---
    private ResponseEntity<String> procesarLogica(int cantidadAProcesar) {
        List<Solicitud> cola = dataStore.getSolicitudes();
        if (cola == null || cola.isEmpty()) {
            return ResponseEntity.ok("La cola esta vacia.");
        }

        // 1. Ordenar por prioridad (Lo mas urgente primero)
        cola.sort((s1, s2) -> Integer.compare(s2.getPrioridad(), s1.getPrioridad()));

        int procesados = 0;
        int errores = 0;
        List<Solicitud> completadas = new ArrayList<>();

        // MAPA TEMPORAL: ID Mensajero -> Estado (cuanto espacio le queda en este viaje)
        Map<String, EstadoMensajeroTemporal> usoMensajeros = new HashMap<>();

        // Iteramos las solicitudes
        for (int i = 0; i < cola.size(); i++) {
            if (procesados >= cantidadAProcesar) break;

            Solicitud sol = cola.get(i);

            // A. Buscar paquete
            Paquete elPaquete = null;
            if (dataStore.getPaquetes() != null) {
                for (Paquete p : dataStore.getPaquetes()) {
                    if (p.getId().equals(sol.getPaquete())) {
                        elPaquete = p;
                        break;
                    }
                }
            }

            if (elPaquete == null || !elPaquete.getEstado().equals("PENDIENTE")) {
                errores++; continue;
            }

            String origen = elPaquete.getCentroActual();
            String destino = elPaquete.getDestino();
            double pesoPaquete = elPaquete.getPeso();

            // Validar ruta
            if (servicioRutas.buscarRuta(origen, destino).isEmpty()) {
                System.out.println("Salto solicitud: Sin ruta.");
                errores++; continue;
            }

            // --- AQUI ESTA LA MAGIA: ASIGNACIÓN OPTIMIZADA ---
            EstadoMensajeroTemporal candidato = null;

            // Opción 1: Buscar si ya estamos llenando un mensajero para esa ruta
            for (EstadoMensajeroTemporal temp : usoMensajeros.values()) {
                if (temp.mensajeroReal.getCentro().equals(origen) &&    // Mismo origen
                        temp.destinoActual.equals(destino) &&               // Mismo destino (Agrupación)
                        temp.capacidadRestante >= pesoPaquete) {            // Cabe el paquete

                    candidato = temp;
                    break;
                }
            }

            // Opción 2: Si no hay nadie saliendo, buscar uno nuevo DISPONIBLE
            if (candidato == null) {
                if (dataStore.getMensajeros() != null) {
                    for (Mensajero m : dataStore.getMensajeros()) {
                        // Importante: Que no lo hayamos usado ya en el mapa temporal (usoMensajeros.containsKey)
                        if (!usoMensajeros.containsKey(m.getId()) &&
                                m.getCentro().equals(origen) &&
                                m.getEstado().equals("DISPONIBLE") &&
                                m.getCapacidad() >= pesoPaquete) {

                            // Creamos un nuevo estado temporal
                            candidato = new EstadoMensajeroTemporal(m);
                            candidato.destinoActual = destino; // Definimos a dónde va este viaje
                            usoMensajeros.put(m.getId(), candidato);
                            break;
                        }
                    }
                }
            }

            // --- RESULTADO DE LA BÚSQUEDA ---
            if (candidato != null) {
                // Asignamos
                candidato.capacidadRestante -= pesoPaquete;
                elPaquete.setEstado("EN_TRANSITO");

                System.out.println("Asignado P: " + elPaquete.getId() + " ("+pesoPaquete+"kg) a Mensajero: " + candidato.mensajeroReal.getNombre() + " | Espacio restante: " + candidato.capacidadRestante);

                completadas.add(sol);
                procesados++;
            } else {
                System.out.println("Salto solicitud: No hay mensajero con capacidad o ruta disponible.");
                errores++;
            }
        }

        // --- FINALIZAR: ACTUALIZAR ESTADOS DE MENSAJEROS ---
        // Ahora si, a todos los mensajeros que usamos, los ponemos EN_TRANSITO real
        for (EstadoMensajeroTemporal estado : usoMensajeros.values()) {
            estado.mensajeroReal.setEstado("EN_TRANSITO");
            // Opcional: Podrías guardar en el mensajero hacia dónde va, si tuvieras ese campo.
        }

        cola.removeAll(completadas);
        return ResponseEntity.ok("Procesamiento inteligente finalizado. Atendidas: " + procesados + ". Fallidas/Saltadas: " + errores);
    }
}