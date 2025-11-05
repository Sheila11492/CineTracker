package com.example.cinetracker.data;

public interface SeguimientoDAO {
    void upsert(int usuarioId, int contenidoId, String estado, Long fechaInicioMs, Long fechaFinMs);
    String getEstado(int usuarioId, int contenidoId);
}
