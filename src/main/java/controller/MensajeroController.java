package com.usac.logitrack.backend.controller;

import com.usac.logitrack.backend.model.Mensajero;
import com.usac.logitrack.backend.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/mensajeros")
@CrossOrigin(origins = "*")
public class MensajeroController {

    @Autowired
    DataStore datosMemoria;

    // 1. Ver todos los mensajeros
    @GetMapping
    public List<Mensajero> listarMensajeros() {
        System.out.println("Solicitud recibida: Listar todos los mensajeros.");

        List<Mensajero> lista = datosMemoria.getMensajeros();

        if (lista == null) {
            return new ArrayList<>();
        }

        return lista;
    }

    // 2. Buscar uno por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarMensajero(@PathVariable String id) {
        System.out.println("Buscando mensajero ID: " + id);

        List<Mensajero> todos = datosMemoria.getMensajeros();
        Mensajero encontrado = null;

        // Recorrido manual
        for (int i = 0; i < todos.size(); i++) {
            if (todos.get(i).getId().equals(id)) {
                encontrado = todos.get(i);
                break;
            }
        }

        if (encontrado != null) {
            return ResponseEntity.ok(encontrado);
        }
        return ResponseEntity.status(404).body("No se encontro el mensajero.");
    }

    // 3. Contratar nuevo mensajero
    @PostMapping
    public ResponseEntity<String> crearMensajero(@RequestBody Mensajero nuevo) {
        System.out.println("Intentando registrar mensajero: " + nuevo.getNombre());

        // Validacion rapida de duplicados
        List<Mensajero> lista = datosMemoria.getMensajeros();
        for (int k = 0; k < lista.size(); k++) {
            if (lista.get(k).getId().equals(nuevo.getId())) {
                return ResponseEntity.badRequest().body("Error: Ya existe un mensajero con ese ID.");
            }
        }

        // Si pasa, lo agregamos
        datosMemoria.getMensajeros().add(nuevo);
        System.out.println("Mensajero registrado con exito.");

        return ResponseEntity.ok("Mensajero creado exitosamente.");
    }

    // 4. Actualizar Estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id, @RequestBody String nuevoEstado) {
        System.out.println("Cambiando estado de " + id + " a " + nuevoEstado);

        Mensajero trabajador = null;
        List<Mensajero> lista = datosMemoria.getMensajeros();

        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(id)) {
                trabajador = lista.get(i);
                break;
            }
        }

        if (trabajador == null) {
            return ResponseEntity.notFound().build();
        }

        String estadoLimpio = nuevoEstado.replace("\"", "").trim();
        trabajador.setEstado(estadoLimpio);

        return ResponseEntity.ok("Estado actualizado.");
    }

    // 5. Mover de Centro
    @PutMapping("/{id}/centro")
    public ResponseEntity<?> reasignarCentro(@PathVariable String id, @RequestBody String nuevoCentroId) {
        System.out.println("Reasignando mensajero " + id + " al centro " + nuevoCentroId);

        Mensajero m = null;
        for (Mensajero temp : datosMemoria.getMensajeros()) {
            if (temp.getId().equals(id)) {
                m = temp;
                break;
            }
        }

        if (m == null) {
            return ResponseEntity.status(404).body("Mensajero no encontrado.");
        }

        // Validacion: Si esta ocupado no se mueve
        if (m.getEstado().equals("EN_TRANSITO")) {
            System.out.println("Fallo: El mensajero esta ocupado.");
            return ResponseEntity.badRequest().body("No se puede reasignar: El mensajero esta EN_TRANSITO.");
        }

        String idCentroLimpio = nuevoCentroId.replace("\"", "").trim();
        m.setCentro(idCentroLimpio);

        return ResponseEntity.ok("Mensajero traslado al centro " + idCentroLimpio);
    }
}