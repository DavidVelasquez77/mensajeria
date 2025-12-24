package controller;

import model.Ruta;
import service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@CrossOrigin(origins = "*")
public class RutaController {

    @Autowired
    RutaService servicio; // Le quite el private para probar

    // METODO 1: Traer todas las rutas
    @GetMapping
    public List<Ruta> getRutas() {
        System.out.println("--- Peticion recibida: Listar Rutas ---");

        // Guardo la lista en una variable antes de mandarla
        List<Ruta> listadoFinal = servicio.obtenerTodas();

        // Solo para ver cuantos hay
        System.out.println("Se encontraron " + listadoFinal.size() + " rutas en memoria.");

        return listadoFinal;
    }

    // METODO 2: Buscar una sola ruta por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRutaIndividual(@PathVariable String id) {
        System.out.println("Buscando ruta especifica: " + id);

        Ruta r = servicio.buscarPorId(id);

        // validacion simple
        if (r == null) {
            System.out.println("Ups, esa ruta no existe.");
            return ResponseEntity.status(404).body("Error: No existe la ruta con id " + id);
        }

        // Si existe, la devolvemos con estado OK
        return ResponseEntity.ok(r);
    }

    // METODO 3: Guardar nueva ruta
    @PostMapping
    public ResponseEntity<String> guardarRuta(@RequestBody Ruta body) {
        System.out.println("Intentando guardar ruta: " + body.getId());

        try {
            // Llamamos al servicio
            servicio.crearRuta(body);

            System.out.println("Todo bien, ruta creada.");
            return ResponseEntity.ok("La ruta se creo correctamente.");

        } catch (Exception e) {

            System.out.println("Fallo al crear: " + e.getMessage());
            return ResponseEntity.status(400).body("No se pudo crear: " + e.getMessage());
        }
    }

    // METODO 4: Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<String> updateRuta(@PathVariable String id, @RequestBody Ruta datos) {

        // Probamos actualizar
        boolean resultado = servicio.actualizarRuta(id, datos);

        if (resultado == true) {
            return ResponseEntity.ok("Actualizacion exitosa.");
        } else {
            return ResponseEntity.status(404).body("No se encontro esa ruta para editar.");
        }
    }

    // METODO 5: Borrar
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRuta(@PathVariable String id) {
        System.out.println("Solicitando eliminar: " + id);

        boolean seBorro = servicio.eliminarRuta(id);

        if (seBorro) {
            return ResponseEntity.ok("Ruta eliminada del sistema.");
        }

        // Si llegamos aqui es que no existia
        return ResponseEntity.status(404).body("No se pudo eliminar, id no valido.");
    }
}