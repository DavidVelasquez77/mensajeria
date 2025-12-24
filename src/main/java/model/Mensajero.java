package model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Mensajero {

    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlProperty(isAttribute = true)
    private String nombre;

    @JacksonXmlProperty(isAttribute = true)
    private int capacidad;

    @JacksonXmlProperty(isAttribute = true)
    private String centro;

    // estado interno
    private String estado;

    public Mensajero() {
        // siempre inician disponibles segun reglas
        this.estado = "DISPONIBLE";
    }

    // metodos de acceso

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getCentro() { return centro; }

    public void setCentro(String centro) {
        // aqui podriamos validar si el centro existe despues
        this.centro = centro;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}