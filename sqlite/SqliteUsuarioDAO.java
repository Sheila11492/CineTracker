package com.example.cinetracker.data.sqlite;

import android.content.Context;
import com.example.cinetracker.data.UsuarioDAO;
import com.example.cinetracker.database.DatabaseHelper;
import com.example.cinetracker.models.Usuario;

public class SqliteUsuarioDAO implements UsuarioDAO {

    private final DatabaseHelper db;

    public SqliteUsuarioDAO(Context ctx) {
        this.db = new DatabaseHelper(ctx);
    }

    @Override
    public long insert(Usuario u) {
        return db.insertUsuario(u.getNombre(), u.getEmail(), u.getContrasenaHash());
    }

    @Override
    public boolean isEmailTaken(String email) {
        return db.isEmailTaken(email);
    }

    @Override
    public int login(String email, String password) {
        return db.loginUser(email, password);
    }
}

