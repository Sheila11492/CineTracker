package com.example.cinetracker.models;

public class Seguimiento {
    private int usuarioId;
    private int contenidoId;
    private String estado;     // "Pendiente", "Viendo", "Visto" o null
    private Long fechaInicio;
    private Long fechaFin;

    public Seguimiento() {}

    public Seguimiento(int usuarioId, int contenidoId, String estado, Long fechaInicio, Long fechaFin) {
        this.usuarioId = usuarioId;
        this.contenidoId = contenidoId;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Getters / Setters
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getContenidoId() { return contenidoId; }
    public void setContenidoId(int contenidoId) { this.contenidoId = contenidoId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Long fechaInicio) { this.fechaInicio = fechaInicio; }

    public Long getFechaFin() { return fechaFin; }
    public void setFechaFin(Long fechaFin) { this.fechaFin = fechaFin; }
    public void registrarInicio() { this.fechaInicio = System.currentTimeMillis(); }
    public void registrarFin()    { this.fechaFin    = System.currentTimeMillis(); }

    public void cambiarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
        if ("Visto".equalsIgnoreCase(nuevoEstado)) registrarFin();
    }
}
