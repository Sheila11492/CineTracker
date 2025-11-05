package com.example.cinetracker.data;

import com.example.cinetracker.models.Contenido;
import java.util.List;

public interface ContenidoDAO {
    long add(Contenido c);
    void update(Contenido c);
    void delete(int contenidoId);

    List<Contenido> getAll();
    List<Contenido> getFavoritos(int usuarioId);

    void addFavorito(int usuarioId, int contenidoId);
    void removeFavorito(int usuarioId, int contenidoId);
    boolean isFavorito(int usuarioId, int contenidoId);
    boolean toggleFavorito(int usuarioId, int contenidoId);
}
