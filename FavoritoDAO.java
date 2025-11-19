package com.example.cinetracker.data;

import com.example.cinetracker.models.Contenido;
import java.util.List;

public interface FavoritoDAO {
    void addFavorito(int usuarioId, int contenidoId);
    void removeFavorito(int usuarioId, int contenidoId);
    boolean isFavorito(int usuarioId, int contenidoId);
    boolean toggleFavorito(int usuarioId, int contenidoId);
    Long getFechaFavorito(int usuarioId, int contenidoId);
    List<Contenido> getFavoritosUsuario(int usuarioId);
}
