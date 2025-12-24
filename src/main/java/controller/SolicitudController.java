package controller;

import model.Mensajero;
import model.Paquete;
import model.Solicitud;
import repository.DataStore;
import service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    @Autowired
    DataStore dataStore;

    @Autowired
    RutaService servicioRutas;

    // 1. LISTAR LA COLA (GET)
    @GetMapping
    public List<Solicitud> verCola() {
        System.out.println("Solicitud: Ver cola de espera");

        List<Solicitud> cola = dataStore.getSolicitudes();

        // Ordenamos Mayor prioridad primero
        cola.sort(new Comparator<Solicitud>() {
            @Override
            public int compare(Solicitud s1, Solicitud s2) {
                return Integer.compare(s2.getPrioridad(), s1.getPrioridad());
            }
        });

        return cola;
    }

    // 2. CREAR SOLICITUD
    @PostMapping
    public ResponseEntity<String> crearSolicitud(@RequestBody Solicitud nueva) {
        System.out.println("Nueva solicitud para paquete: " + nueva.getPaquete());

        boolean paqueteExiste = false;
        for (Paquete p : dataStore.getPaquetes()) {
            if (p.getId().equals(nueva.getPaquete())) {
                paqueteExiste = true;

                if (!p.getEstado().equals("PENDIENTE")) {
                    return ResponseEntity.badRequest().body("Error: El paquete ya no esta pendiente.");
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
        System.out.println("Borrando solicitud: " + id);

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
        System.out.println("Procesando la solicitud mas urgente...");
        return procesarLogica(1); // Llamamos a una funcion auxiliar que procesa N cantidad
    }

    // 5. PROCESAR LAS N MAS PRIORITARIAS (POST)
    @PostMapping("/procesar/{n}")
    public ResponseEntity<String> procesarTopN(@PathVariable int n) {
        System.out.println("Procesando las top " + n + " solicitudes...");
        return procesarLogica(n);
    }

    //  LOGICA DE PROCESAMIENTO "MANUAL"
    private ResponseEntity<String> procesarLogica(int cantidadAProcesar) {
        List<Solicitud> cola = dataStore.getSolicitudes();

        if (cola.isEmpty()) {
            return ResponseEntity.ok("La cola esta vacia, nada que hacer.");
        }

        // 1. Ordenar por prioridad Mayor a menor
        cola.sort((s1, s2) -> Integer.compare(s2.getPrioridad(), s1.getPrioridad()));

        int procesados = 0;
        int errores = 0;
        List<Solicitud> completadas = new ArrayList<>();

        // iterar solo las veces que pidieron n
        for (int i = 0; i < cola.size(); i++) {

            // Validacion de cantidad procesada
            if (procesados >= cantidadAProcesar) {
                break;
            }

            Solicitud sol = cola.get(i);

            // Buscar paquete asociado
            Paquete elPaquete = null;
            for (Paquete p : dataStore.getPaquetes()) {
                if (p.getId().equals(sol.getPaquete())) {
                    elPaquete = p;
                    break;
                }
            }

            if (elPaquete == null || !elPaquete.getEstado().equals("PENDIENTE")) {
                System.out.println("Salto solicitud " + sol.getId() + ": Paquete no valido.");
                errores++;
                continue; // Saltamos a la siguiente
            }

            // Buscar Ruta
            String origen = elPaquete.getCentroActual();
            String destino = elPaquete.getDestino();
            List<String> ruta = servicioRutas.buscarRuta(origen, destino);

            if (ruta.isEmpty()) {
                System.out.println("Salto solicitud: No hay ruta.");
                errores++;
                continue;
            }

            // Buscar Mensajero en el origen
            Mensajero elMensajero = null;
            for (Mensajero m : dataStore.getMensajeros()) {
                if (m.getCentro().equals(origen) && m.getEstado().equals("DISPONIBLE")) {
                    elMensajero = m;
                    break;
                }
            }

            if (elMensajero == null) {
                System.out.println("Salto solicitud: No hay mensajero.");
                errores++;
                continue;
            }

            // Realizar cambios
            elPaquete.setEstado("EN_TRANSITO");
            elMensajero.setEstado("EN_TRANSITO");

            completadas.add(sol);
            procesados++;
        }

        // Limpiar las procesadas de la cola
        cola.removeAll(completadas);

        return ResponseEntity.ok("Procesamiento finalizado. Atendidas: " + procesados + ". Fallidas/Saltadas: " + errores);
    }
}