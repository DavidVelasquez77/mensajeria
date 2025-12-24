package model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Centro {

    // atributos del xml
    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlProperty(localName = "nombre")
    private String nombre;

    @JacksonXmlProperty(localName = "ciudad")
    private String ciudad;

    @JacksonXmlProperty(localName = "capacidad")
    private int capacidad;

    // control interno
    private int paquetesActuales;

    // constructor basico
    public Centro() {
        this.paquetesActuales = 0;
    }

    // --- Metodos Get y Set ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        // validamos que no sea negativo por si acaso
        if (capacidad < 0) {
            System.out.println("Advertencia: Capacidad negativa en centro " + this.id);
            this.capacidad = 0;
        } else {
            this.capacidad = capacidad;
        }
    }

    public int getPaquetesActuales() {
        return paquetesActuales;
    }

    public void setPaquetesActuales(int paquetesActuales) {
        this.paquetesActuales = paquetesActuales;
    }
}