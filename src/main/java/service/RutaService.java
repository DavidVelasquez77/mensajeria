package service;

import model.Ruta;
import repository.DataStore;
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


    //  Obtener todas
    public List<Ruta> obtenerTodas() {
        return dataStore.getRutas();
    }

    //  Buscar por ID
    public Ruta buscarPorId(String id) {
        for (Ruta r : dataStore.getRutas()) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    // Crear Ruta
    public void crearRuta(Ruta nueva) {
        // Validar duplicados de ID o de conexión (Origen-Destino)
        for (Ruta r : dataStore.getRutas()) {
            if (r.getId().equals(nueva.getId())) {
                throw new IllegalArgumentException("Ya existe una ruta con el ID " + nueva.getId());
            }
            if (r.getOrigen().equals(nueva.getOrigen()) && r.getDestino().equals(nueva.getDestino())) {
                throw new IllegalArgumentException("Ya existe una ruta entre " + nueva.getOrigen() + " y " + nueva.getDestino());
            }
        }
        dataStore.getRutas().add(nueva);
    }

    // Actualizar Ruta
    public boolean actualizarRuta(String id, Ruta nuevosDatos) {
        Ruta ruta = buscarPorId(id);
        if (ruta != null) {
            ruta.setDistancia(nuevosDatos.getDistancia());
            // Actualizar otros campos si es necesario
            return true;
        }
        return false;
    }

    //  Eliminar Ruta
    public boolean eliminarRuta(String id) {
        Ruta ruta = buscarPorId(id);
        if (ruta != null) {
            dataStore.getRutas().remove(ruta);
            return true;
        }
        return false;
    }

    // dijkstra

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

        // usamos mapas para los costos
        Map<String, Integer> costos = new HashMap<>();
        Map<String, String> predecesores = new HashMap<>();
        List<String> pendientes = new ArrayList<>();
        List<String> visitados = new ArrayList<>();

        // inicializamos costos altos
        for (Ruta r : todasLasRutas) {
            costos.put(r.getOrigen(), 1000000);
            costos.put(r.getDestino(), 1000000);
        }

        costos.put(inicio, 0);
        pendientes.add(inicio);

        while (!pendientes.isEmpty()) {

            // Buscar nodo con menor costo
            String actual = null;
            int menorCosto = 1000000;

            for (String nodo : pendientes) {
                int costoNodo = costos.getOrDefault(nodo, 1000000);
                if (costoNodo < menorCosto) {
                    menorCosto = costoNodo;
                    actual = nodo;
                }
            }

            if (actual == null) break;

            pendientes.remove(actual);
            visitados.add(actual);

            if (actual.equals(fin)) break;

            // Buscar vecinos
            for (Ruta r : todasLasRutas) {
                if (r.getOrigen().equals(actual)) {
                    String vecino = r.getDestino();

                    if (visitados.contains(vecino)) continue;

                    int nuevoCosto = costos.get(actual) + r.getDistancia();
                    int costoActualVecino = costos.getOrDefault(vecino, 1000000);

                    if (nuevoCosto < costoActualVecino) {
                        costos.put(vecino, nuevoCosto);
                        predecesores.put(vecino, actual);
                        if (!pendientes.contains(vecino)) {
                            pendientes.add(vecino);
                        }
                    }
                }
            }
        }

        // Construir camino final
        List<String> caminoFinal = new ArrayList<>();
        if (!predecesores.containsKey(fin)) {
            System.out.println("No se encontro camino.");
            return caminoFinal;
        }

        String paso = fin;
        while (paso != null) {
            caminoFinal.add(0, paso);
            paso = predecesores.get(paso);
        }

        System.out.println("Ruta encontrada: " + caminoFinal);
        return caminoFinal;
    }
}