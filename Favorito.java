package com.example.cinetracker.models;

public class Favorito {
    private int usuarioId;
    private int contenidoId;
    private long fechaMarcado;

    public Favorito() {}

    public Favorito(int usuarioId, int contenidoId, long fechaMarcado) {
        this.usuarioId = usuarioId;
        this.contenidoId = contenidoId;
        this.fechaMarcado = fechaMarcado;
    }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getContenidoId() { return contenidoId; }
    public void setContenidoId(int contenidoId) { this.contenidoId = contenidoId; }

    public long getFechaMarcado() { return fechaMarcado; }
    public void setFechaMarcado(long fechaMarcado) { this.fechaMarcado = fechaMarcado; }
    public void marcarFavorito()  { this.fechaMarcado = System.currentTimeMillis(); }
    public void eliminarFavorito() { /* en DB se borra; aquí queda como DTO */ }
}
