package com.example.cinetracker.data.sqlite;

import android.content.Context;
import com.example.cinetracker.data.EstadisticaService;
import com.example.cinetracker.database.DatabaseHelper;
import java.util.HashMap;

public class SqliteEstadisticaService implements EstadisticaService {

    private final DatabaseHelper db;

    public SqliteEstadisticaService(Context ctx) {
        this.db = new DatabaseHelper(ctx);
    }

    @Override public int contarVistosPorTipo(int usuarioId, String tipo) { return db.contarVistosPorTipo(usuarioId, tipo); }
    @Override public HashMap<String, Integer> contarVistosPorGenero(int usuarioId) { return db.contarVistosPorGenero(usuarioId); }
    @Override public int minutosTotalesVistos(int usuarioId) { return db.minutosTotalesVistos(usuarioId); }
}

