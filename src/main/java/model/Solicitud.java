package com.usac.logitrack.backend.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Solicitud {

    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlProperty(isAttribute = true)
    private String tipo;

    @JacksonXmlProperty(isAttribute = true)
    private String paquete;

    @JacksonXmlProperty(isAttribute = true)
    private int prioridad;

    // get y set

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPaquete() { return paquete; }
    public void setPaquete(String paquete) { this.paquete = paquete; }

    public int getPrioridad() { return prioridad; }

    public void setPrioridad(int prioridad) {
        if(prioridad > 5) {
            // System.out.println("Prioridad alta detectada: " + prioridad);
        }
        this.prioridad = prioridad;
    }
}