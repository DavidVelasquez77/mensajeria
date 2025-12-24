package repository;

import model.*;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataStore {

    // --- Variables de almacenamiento en memoria ---

    private List<Centro> listaDeCentros;
    private List<Ruta> listaDeRutas;
    private List<Mensajero> listaDeMensajeros;

    // constructor
    public DataStore() {
        System.out.println("Inicializando DataStore...");
        this.listaDeCentros = new ArrayList<>();
        this.listaDeRutas = new ArrayList<>();
        this.listaDeMensajeros = new ArrayList<>();
        this.listaDePaquetes = new ArrayList<>();
        this.listaDeSolicitudes = new ArrayList<>();
    }

    private List<Paquete> listaDePaquetes;
    private List<Solicitud> listaDeSolicitudes;

    public void limpiarTodo() {
        // System.out.println("Limpiando datos anteriores...");
        if(this.listaDeCentros != null) this.listaDeCentros.clear();
        if(this.listaDeRutas != null) this.listaDeRutas.clear();
        if(this.listaDeMensajeros != null) this.listaDeMensajeros.clear();
        if(this.listaDePaquetes != null) this.listaDePaquetes.clear();
        if(this.listaDeSolicitudes != null) this.listaDeSolicitudes.clear();
    }

    // --- Accesos (Getters) ---

    public List<Centro> getCentros() {
        if(listaDeCentros == null) {
            return new ArrayList<>(); // evitar nulos
        }
        return listaDeCentros;
    }

    public List<Mensajero> getMensajeros() {
        return listaDeMensajeros;
    }

    // metodo extra para debug este imprime un resumen
    public void imprimirEstadisticas() {
        System.out.println("Centros cargados: " + listaDeCentros.size());
        System.out.println("Rutas cargadas: " + listaDeRutas.size());
    }

    public List<Ruta> getRutas() {
        return listaDeRutas;
    }

    public List<Paquete> getPaquetes() {
        // validar
        return listaDePaquetes;
    }

    public List<Solicitud> getSolicitudes() {
        return listaDeSolicitudes;
    }
}