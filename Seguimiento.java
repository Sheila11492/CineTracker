package com.example.cinetracker.models;

public class Seguimiento {
    private int usuarioId;
    private int contenidoId;
    private String estado;       // "Pendiente", "Viendo", "Visto" o null
    private Long fechaInicio;
    private Long fechaFin;
    private Integer capitulosVistos;

    public Seguimiento() { }

    public Seguimiento(int usuarioId, int contenidoId, String estado, Long fechaInicio, Long fechaFin, Integer capitulosVistos) {
        this.usuarioId = usuarioId;
        this.contenidoId = contenidoId;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.capitulosVistos = capitulosVistos;
    }

    // getters/setters
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

    public Integer getCapitulosVistos() { return capitulosVistos; }
    public void setCapitulosVistos(Integer capitulosVistos) { this.capitulosVistos = capitulosVistos; }

    public void cambiarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
        if ("Visto".equalsIgnoreCase(nuevoEstado)) this.fechaFin = System.currentTimeMillis();
    }
}
