package com.example.cinetracker.data;


public interface SeguimientoDAO {
    void upsert(int usuarioId, int contenidoId, String estado, Long fechaInicioMs, Long fechaFinMs);
    String getEstado(int usuarioId, int contenidoId);

    // Helpers añadidos para la UI / lógica de dominio
    void registrarInicio(int usuarioId, int contenidoId);
    void registrarFin(int usuarioId, int contenidoId);

    // Lectura de fechas
    Long getFechaInicio(int usuarioId, int contenidoId);
    Long getFechaFin(int usuarioId, int contenidoId);
}
