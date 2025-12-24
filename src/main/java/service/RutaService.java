package com.usac.logitrack.backend.service;

import com.usac.logitrack.backend.model.Ruta;
import com.usac.logitrack.backend.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RutaService {

    @Autowired
    private DataStore dataStore;


    public List<String> buscarRuta(String inicio, String fin) {
        System.out.println("Calculando ruta de " + inicio + " a " + fin);

        // validaciones iniciales
        if (inicio.equals(fin)) {
            List<String> sola = new ArrayList<>();
            sola.add(inicio);
            return sola;
        }

        List<Ruta> todasLasRutas = dataStore.getRutas();
        if (todasLasRutas == null || todasLasRutas.isEmpty()) {
            return new ArrayList<>();
        }


        // usamos mapas porque es mas facil guardar los costos
        Map<String, Integer> costos = new HashMap<>();
        Map<String, String> predecesores = new HashMap<>();
        List<String> pendientes = new ArrayList<>();
        List<String> visitados = new ArrayList<>();

        // inicializamos los costos en un valor muy alto y recorremos todas las rutas para saber que nodos existen
        for (Ruta r : todasLasRutas) {
            costos.put(r.getOrigen(), 1000000);
            costos.put(r.getDestino(), 1000000);
        }

        costos.put(inicio, 0);
        pendientes.add(inicio);

        // Bucle Principal (
        while (!pendientes.isEmpty()) {

            //  Busca el nodo con menor costo en la lista de pendientes
            String actual = null;
            int menorCosto = 1000000;

            for (String nodo : pendientes) {
                int costoNodo = costos.get(nodo);
                if (costoNodo < menorCosto) {
                    menorCosto = costoNodo;
                    actual = nodo;
                }
            }

            // Si no encontramos nada valido, salimos
            if (actual == null) {
                break;
            }

            // Movemos de pendientes a visitados
            pendientes.remove(actual);
            visitados.add(actual);

            // Si llegamos al destino, terminamos antes para ahorrar tiempo
            if (actual.equals(fin)) {
                break;
            }

            // buscamos vecinos y recorremos la lista completa de rutas para buscar coincidencias
            for (Ruta r : todasLasRutas) {

                // Si la ruta sale del nodo actual
                if (r.getOrigen().equals(actual)) {
                    String vecino = r.getDestino();

                    // Si ya lo visitamos, no hacemos nada
                    if (visitados.contains(vecino)) {
                        continue;
                    }

                    int nuevoCosto = costos.get(actual) + r.getDistancia();
                    int costoActualVecino = costos.get(vecino);

                    // Si encontramos un camino mas corto
                    if (nuevoCosto < costoActualVecino) {
                        costos.put(vecino, nuevoCosto);
                        predecesores.put(vecino, actual);

                        // Agregamos a pendientes si no estaba
                        if (!pendientes.contains(vecino)) {
                            pendientes.add(vecino);
                        }
                    }
                }
            }
        }

        // armado del resultado final
        List<String> caminoFinal = new ArrayList<>();

        // Verificamos si logramos llegar al destino
        if (!predecesores.containsKey(fin)) {
            System.out.println("No se encontro camino.");
            return caminoFinal; // Lista vacia
        }

        // Vamos hacia atras desde el fin hasta el inicio
        String paso = fin;
        while (paso != null) {
            caminoFinal.add(0, paso); // Insertamos al principio
            paso = predecesores.get(paso);
        }

        System.out.println("Ruta encontrada: " + caminoFinal);
        return caminoFinal;
    }
}