package com.example.cinetracker.data.sqlite;

import android.content.Context;

import com.example.cinetracker.data.SeguimientoDAO;
import com.example.cinetracker.database.DatabaseHelper;

public class SqliteSeguimientoDAO implements SeguimientoDAO {

    private final DatabaseHelper db;

    public SqliteSeguimientoDAO(Context ctx) {
        this.db = new DatabaseHelper(ctx);
    }

    @Override
    public void upsert(int usuarioId, int contenidoId, String estado, Long fechaInicioMs, Long fechaFinMs) {
        db.upsertSeguimiento(usuarioId, contenidoId, estado, fechaInicioMs, fechaFinMs);
    }

    @Override
    public String getEstado(int usuarioId, int contenidoId) {
        return db.getEstadoSeguimiento(usuarioId, contenidoId);
    }

    @Override
    public void registrarInicio(int usuarioId, int contenidoId) {
        db.registrarInicio(usuarioId, contenidoId);
    }

    @Override
    public void registrarFin(int usuarioId, int contenidoId) {
        db.registrarFin(usuarioId, contenidoId);
    }

    @Override
    public Long getFechaInicio(int usuarioId, int contenidoId) {
        return db.getFechaInicioSeguimiento(usuarioId, contenidoId);
    }

    @Override
    public Long getFechaFin(int usuarioId, int contenidoId) {
        return db.getFechaFinSeguimiento(usuarioId, contenidoId);
    }
}
