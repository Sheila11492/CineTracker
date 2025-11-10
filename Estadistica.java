package com.example.cinetracker.models;

import com.example.cinetracker.database.DatabaseHelper;
import java.util.HashMap;


public class Estadistica {
    private int id;
    private int usuarioId;

    private float horasVistas;
    private String generoMasVisto;
    private int totalContenidosVistos;

    public Estadistica(int usuarioId) { this.usuarioId = usuarioId; }

    public void recalcular(DatabaseHelper db) {
        calcularHoras(db);
        calcularGeneroFavorito(db);
        totalContenidosVistos =
                db.contarVistosPorTipo(usuarioId, "Película")
                        + db.contarVistosPorTipo(usuarioId, "Serie");
    }

    public float calcularHoras(DatabaseHelper db) {
        int minutos = db.minutosTotalesVistos(usuarioId);
        horasVistas = minutos / 60f;
        return horasVistas;
    }

    public String calcularGeneroFavorito(DatabaseHelper db) {
        HashMap<String, Integer> map = db.contarVistosPorGenero(usuarioId);
        String top = null; int max = -1;
        for (String g : map.keySet()) {
            int v = map.get(g);
            if (v > max) { max = v; top = g; }
        }
        generoMasVisto = (top == null ? "N/D" : top);
        return generoMasVisto;
    }

    public void generarGraficos() {
        // Los gráficos se pintan en EstadisticasActivity con MPAndroidChart.
        // Aquí no hay UI;
    }

    // Getters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }

    public float getHorasVistas() { return horasVistas; }
    public String getGeneroMasVisto() { return generoMasVisto; }
    public int getTotalContenidosVistos() { return totalContenidosVistos; }
}
