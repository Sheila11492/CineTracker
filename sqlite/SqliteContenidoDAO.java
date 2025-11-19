package com.example.cinetracker.data.sqlite;

import android.content.Context;
import com.example.cinetracker.data.ContenidoDAO;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Contenido;
import java.util.List;

public class SqliteContenidoDAO implements ContenidoDAO {

    private final DatabaseHelper db;


    public SqliteContenidoDAO(Context ctx) {
        this.db = new DatabaseHelper(ctx);
    }

    @Override public long add(Contenido c) { return db.addContenido(c); }
    @Override public void update(Contenido c) { db.updateContenido(c); }
    @Override public void delete(int contenidoId) { db.deleteContenido(contenidoId); }

    @Override public List<Contenido> getAll() { return db.getAllContenidoList(); }
    @Override public List<Contenido> getFavoritos(int usuarioId) { return db.getFavoritos(usuarioId); }

    @Override public void addFavorito(int usuarioId, int contenidoId) { db.addFavorito(usuarioId, contenidoId); }
    @Override public void removeFavorito(int usuarioId, int contenidoId) { db.removeFavorito(usuarioId, contenidoId); }
    @Override public boolean isFavorito(int usuarioId, int contenidoId) { return db.isFavorito(usuarioId, contenidoId); }
    @Override public boolean toggleFavorito(int usuarioId, int contenidoId) { return db.toggleFavorito(usuarioId, contenidoId); }
}

