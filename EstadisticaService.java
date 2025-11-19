package com.example.cinetracker.data;

import java.util.HashMap;

public interface EstadisticaService {
    int contarVistosPorTipo(int usuarioId, String tipo);
    HashMap<String, Integer> contarVistosPorGenero(int usuarioId);
    int minutosTotalesVistos(int usuarioId);
}
