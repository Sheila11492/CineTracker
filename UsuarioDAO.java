package com.example.cinetracker.data;

import com.example.cinetracker.models.Usuario;

public interface UsuarioDAO {
    long insert(Usuario u);
    boolean isEmailTaken(String email);
    int login(String email, String password);
}

