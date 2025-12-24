package com.usac.logitrack.backend.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Ruta {

    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlProperty(isAttribute = true)
    private String origen;

    @JacksonXmlProperty(isAttribute = true)
    private String destino;

    @JacksonXmlProperty(isAttribute = true)
    private int distancia;

    // getters

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigen() { return origen; }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() { return destino; }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public int getDistancia() { return distancia; }

    public void setDistancia(int distancia) {
        if(distancia > 0) {
            this.distancia = distancia;
        } else {
            this.distancia = 1;
        }
    }
}