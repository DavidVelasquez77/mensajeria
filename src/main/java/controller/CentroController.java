package controller;

import model.Centro;
import model.Mensajero;
import model.Paquete;
import repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/centros")
@CrossOrigin(origins = "*")
public class CentroController {

    @Autowired
    DataStore baseDeDatosMemoria;

    // 1. Obtener listado
    @GetMapping
    public ResponseEntity<List<Centro>> getAllCentros() {
        System.out.println("Entrando a GET /api/centros...");

        // Verificacion manual por si acaso
        if (baseDeDatosMemoria.getCentros() == null) {
            System.out.println("Ojo: La lista de centros es nula.");
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<Centro> listaRespuesta = baseDeDatosMemoria.getCentros();
        System.out.println("Retornando un total de: " + listaRespuesta.size() + " centros.");

        return ResponseEntity.ok(listaRespuesta);
    }

    // 2. Buscar centro por ID
    @GetMapping("/{idCentro}")
    public ResponseEntity<?> getCentroPorId(@PathVariable String idCentro) {
        System.out.println("Buscando el ID: " + idCentro);

        List<Centro> listaCompleta = baseDeDatosMemoria.getCentros();
        Centro objetoEncontrado = null;

        // Bucle clasico con indice i
        for (int i = 0; i < listaCompleta.size(); i++) {
            Centro temp = listaCompleta.get(i);
            // Comparamos strings
            if (temp.getId().equals(idCentro)) {
                objetoEncontrado = temp;
                // Romper el ciclo porque ya lo hallamos
                break;
            }
        }

        if (objetoEncontrado != null) {
            return ResponseEntity.ok(objetoEncontrado);
        } else {
            System.out.println("No se encontro nada con ese ID.");
            return ResponseEntity.status(404).body("No existe el centro solicitado.");
        }
    }

    // 3. Ver el inventario de paquetes de un centro
    @GetMapping("/{id}/paquetes")
    public List<Paquete> getPaquetesDelCentro(@PathVariable String id) {
        System.out.println("Filtrando paquetes para centro: " + id);

        // Traigo todos primero
        List<Paquete> todos = baseDeDatosMemoria.getPaquetes();

        // Creo una lista vacia para ir metiendo los que coincidan
        List<Paquete> misPaquetes = new ArrayList<>();

        for (int k = 0; k < todos.size(); k++) {
            Paquete p = todos.get(k);

            // Validamos que sea del centro Y que no este entregado
            String ubicacionActual = p.getCentroActual();
            String estado = p.getEstado();

            if (ubicacionActual.equals(id)) {
                if (!estado.equals("ENTREGADO")) {
                    misPaquetes.add(p);
                }
            }
        }

        System.out.println("Encontre " + misPaquetes.size() + " paquetes pendientes aqui.");
        return misPaquetes;
    }

    // 4. Ver mensajeros asignados
    @GetMapping("/{id}/mensajeros")
    public List<Mensajero> getMensajerosDelCentro(@PathVariable String id) {
        System.out.println("--- Request: Mensajeros en " + id + " ---");

        List<Mensajero> listaFiltrada = new ArrayList<>();
        List<Mensajero> source = baseDeDatosMemoria.getMensajeros();

        for (int j = 0; j < source.size(); j++) {
            Mensajero m = source.get(j);
            if (m.getCentro().equals(id)) {
                listaFiltrada.add(m);
            }
        }

        return listaFiltrada;
    }
}