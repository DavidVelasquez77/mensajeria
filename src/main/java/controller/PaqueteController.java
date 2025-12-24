package com.usac.logitrack.backend.controller;

import com.usac.logitrack.backend.model.Paquete;
import com.usac.logitrack.backend.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/paquetes")
@CrossOrigin(origins = "*")
public class PaqueteController {

    @Autowired
    DataStore datosMemoria;

    // 1. Listar todos los paquetes
    @GetMapping
    public List<Paquete> listarPaquetes() {
        System.out.println("Solicitud recibida: Listar todos los paquetes.");

        List<Paquete> lista = datosMemoria.getPaquetes();

        if (lista == null) {
            return new ArrayList<>();
        }

        return lista;
    }

    // 2. Buscar paquete por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPaquete(@PathVariable String id) {
        System.out.println("Buscando paquete ID: " + id);

        List<Paquete> lista = datosMemoria.getPaquetes();
        Paquete encontrado = null;

        // Bucle for clasico para buscar
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(id)) {
                encontrado = lista.get(i);
                break;
            }
        }

        if (encontrado != null) {
            return ResponseEntity.ok(encontrado);
        }
        return ResponseEntity.status(404).body("Error: No se encontro el paquete.");
    }

    // 3. Crear paquete
    @PostMapping
    public ResponseEntity<String> crearPaquete(@RequestBody Paquete nuevo) {
        System.out.println("Intentando registrar paquete: " + nuevo.getId());


        if (nuevo.getPeso() <= 0) {
            return ResponseEntity.badRequest().body("Error: El peso debe ser mayor a 0.");
        }

        // Validacion de duplicados
        List<Paquete> lista = datosMemoria.getPaquetes();
        for (int k = 0; k < lista.size(); k++) {
            if (lista.get(k).getId().equals(nuevo.getId())) {
                return ResponseEntity.badRequest().body("Error: Ya existe un paquete con ese ID.");
            }
        }

        // Guardar
        datosMemoria.getPaquetes().add(nuevo);
        System.out.println("Paquete guardado con exito.");

        return ResponseEntity.ok("Paquete creado exitosamente.");
    }

    // 4. Actualizar Paquete
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPaquete(@PathVariable String id, @RequestBody Paquete datosNuevos) {
        System.out.println("Actualizando paquete: " + id);

        Paquete p = null;
        List<Paquete> lista = datosMemoria.getPaquetes();

        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(id)) {
                p = lista.get(i);
                break;
            }
        }

        if (p == null) {
            return ResponseEntity.notFound().build();
        }

        // Actualizamos datos
        p.setPeso(datosNuevos.getPeso());
        p.setDestino(datosNuevos.getDestino());

        // Si viene el estado, lo actualizamos tambien
        if (datosNuevos.getEstado() != null) {
            p.setEstado(datosNuevos.getEstado());
        }

        return ResponseEntity.ok("Paquete actualizado correctamente.");
    }

    // 5. Eliminar Paquete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarPaquete(@PathVariable String id) {
        System.out.println("Solicitud de eliminacion para paquete: " + id);

        Paquete aBorrar = null;
        List<Paquete> lista = datosMemoria.getPaquetes();


        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(id)) {
                aBorrar = lista.get(i);
                break;
            }
        }

        if (aBorrar == null) {
            return ResponseEntity.status(404).body("Paquete no encontrado.");
        }


        String estado = aBorrar.getEstado();
        if (estado.equals("EN_TRANSITO") || estado.equals("ENTREGADO")) {
            System.out.println("Error: Paquete activo, no se puede borrar.");
            return ResponseEntity.badRequest().body("No se puede eliminar: El paquete esta en camino o ya fue entregado.");
        }

        lista.remove(aBorrar);
        return ResponseEntity.ok("Paquete eliminado del sistema.");
    }
}