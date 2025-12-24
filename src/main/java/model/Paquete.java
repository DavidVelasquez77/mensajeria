package model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Paquete {

    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlProperty(isAttribute = true)
    private String cliente;

    @JacksonXmlProperty(isAttribute = true)
    private double peso;

    @JacksonXmlProperty(isAttribute = true)
    private String destino;

    @JacksonXmlProperty(isAttribute = true)
    private String estado;

    @JacksonXmlProperty(isAttribute = true)
    private String centroActual;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public double getPeso() { return peso; }
    public void setPeso(double peso) {
        // el peso viene en el xml
        this.peso = peso;
    }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCentroActual() { return centroActual; }
    public void setCentroActual(String centroActual) { this.centroActual = centroActual; }
}