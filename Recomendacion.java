package com.example.cinetracker.models;

public class Recomendacion {
    private int id;
    private int usuarioId;
    private int contenidoId;
    private String criterio;
    private long fechaGenerada;

    public Recomendacion() { }


    public Recomendacion(int id, int usuarioId, int contenidoId, String criterio, long fechaGenerada) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.contenidoId = contenidoId;
        this.criterio = criterio;
        this.fechaGenerada = fechaGenerada;
    }

    // getters / setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getContenidoId() { return contenidoId; }
    public void setContenidoId(int contenidoId) { this.contenidoId = contenidoId; }

    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }

    public long getFechaGenerada() { return fechaGenerada; }
    public void setFechaGenerada(long fechaGenerada) { this.fechaGenerada = fechaGenerada; }
}
