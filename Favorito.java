package com.example.cinetracker.models;

public class Favorito {
    private int usuarioId;
    private int contenidoId;
    private Long fechaMarcado;

    public Favorito() { }

    public Favorito(int usuarioId, int contenidoId, Long fechaMarcado) {
        this.usuarioId = usuarioId;
        this.contenidoId = contenidoId;
        this.fechaMarcado = fechaMarcado;
    }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getContenidoId() { return contenidoId; }
    public void setContenidoId(int contenidoId) { this.contenidoId = contenidoId; }

    public Long getFechaMarcado() { return fechaMarcado; }
    public void setFechaMarcado(Long fechaMarcado) { this.fechaMarcado = fechaMarcado; }
}
