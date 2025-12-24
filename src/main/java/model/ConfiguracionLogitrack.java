package com.usac.logitrack.backend.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "logitrack")
public class ConfiguracionLogitrack {

    @JacksonXmlProperty(localName = "configuracion")
    private DatosConfig configuracion;

    public DatosConfig getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(DatosConfig configuracion) {
        this.configuracion = configuracion;
    }

    // clase interna para agrupar las listas
    public static class DatosConfig {

        @JacksonXmlElementWrapper(localName = "centros")
        @JacksonXmlProperty(localName = "centro")
        private List<Centro> listaCentros = new ArrayList<>();

        @JacksonXmlElementWrapper(localName = "rutas")
        @JacksonXmlProperty(localName = "ruta")
        private List<Ruta> listaRutas = new ArrayList<>();

        @JacksonXmlElementWrapper(localName = "mensajeros")
        @JacksonXmlProperty(localName = "mensajero")
        private List<Mensajero> listaMensajeros = new ArrayList<>();

        @JacksonXmlElementWrapper(localName = "paquetes")
        @JacksonXmlProperty(localName = "paquete")
        private List<Paquete> listaPaquetes = new ArrayList<>();

        @JacksonXmlElementWrapper(localName = "solicitudes")
        @JacksonXmlProperty(localName = "solicitud")
        private List<Solicitud> listaSolicitudes = new ArrayList<>();

        // --- Getters y Setters de las listas ---

        public List<Centro> getListaCentros() { return listaCentros; }
        public void setListaCentros(List<Centro> listaCentros) { this.listaCentros = listaCentros; }

        public List<Ruta> getListaRutas() { return listaRutas; }
        public void setListaRutas(List<Ruta> listaRutas) { this.listaRutas = listaRutas; }

        public List<Mensajero> getListaMensajeros() { return listaMensajeros; }
        public void setListaMensajeros(List<Mensajero> listaMensajeros) { this.listaMensajeros = listaMensajeros; }

        public List<Paquete> getListaPaquetes() { return listaPaquetes; }
        public void setListaPaquetes(List<Paquete> listaPaquetes) { this.listaPaquetes = listaPaquetes; }

        public List<Solicitud> getListaSolicitudes() { return listaSolicitudes; }
        public void setListaSolicitudes(List<Solicitud> listaSolicitudes) { this.listaSolicitudes = listaSolicitudes; }
    }
}