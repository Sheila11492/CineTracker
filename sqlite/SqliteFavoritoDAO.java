package com.example.cinetracker.data.sqlite;

import android.content.Context;

import com.example.cinetracker.data.FavoritoDAO;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Contenido;

import java.util.List;

public class SqliteFavoritoDAO implements FavoritoDAO {

    private final DatabaseHelper db;

    public SqliteFavoritoDAO(Context ctx) {
        this.db = new DatabaseHelper(ctx);
    }

    @Override
    public void addFavorito(int usuarioId, int contenidoId) {
        db.addFavorito(usuarioId, contenidoId);
    }

    @Override
    public void removeFavorito(int usuarioId, int contenidoId) {
        db.removeFavorito(usuarioId, contenidoId);
    }

    @Override
    public boolean isFavorito(int usuarioId, int contenidoId) {
        return db.isFavorito(usuarioId, contenidoId);
    }

    @Override
    public boolean toggleFavorito(int usuarioId, int contenidoId) {
        return db.toggleFavorito(usuarioId, contenidoId);
    }

    @Override
    public Long getFechaFavorito(int usuarioId, int contenidoId) {
        return db.getFechaFavorito(usuarioId, contenidoId);
    }

    @Override
    public List<Contenido> getFavoritosUsuario(int usuarioId) {
        return db.getFavoritos(usuarioId);
    }
}
