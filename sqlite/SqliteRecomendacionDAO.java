package com.example.cinetracker.data.sqlite;

import android.content.Context;
import com.example.cinetracker.data.RecomendacionDAO;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Contenido;
import java.util.List;

public class SqliteRecomendacionDAO implements RecomendacionDAO {

    private final DatabaseHelper db;

    public SqliteRecomendacionDAO(Context ctx) {
        this.db = new DatabaseHelper(ctx);
    }

    @Override public void clearForUser(int usuarioId) { db.clearRecomendacionesUsuario(usuarioId); }
    @Override public void insert(int usuarioId, int contenidoId, String criterio) { db.insertRecomendacion(usuarioId, contenidoId, criterio); }
    @Override public List<Contenido> getRecomendacionesContenido(int usuarioId) { return db.getRecomendacionesContenido(usuarioId); }

    @Override public void generarRecomendacionesBasicas(int usuarioId, int topGeneros, int maxRecs) {
        db.generarRecomendacionesBasicas(usuarioId, topGeneros, maxRecs);
    }
}

