package com.example.cinetracker.data;

import com.example.cinetracker.models.Contenido;
import java.util.List;

public interface RecomendacionDAO {
    void clearForUser(int usuarioId);
    void insert(int usuarioId, int contenidoId, String criterio);
    List<Contenido> getRecomendacionesContenido(int usuarioId);
    void generarRecomendacionesBasicas(int usuarioId, int topGeneros, int maxRecs);
}
