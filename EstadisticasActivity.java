package com.example.cinetracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinetracker.auth.SessionManager;
import com.example.cinetracker.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EstadisticasActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView textResumen;
    private DatabaseHelper db;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        // Views
        textResumen = findViewById(R.id.textResumen);
        pieChart    = findViewById(R.id.pieChart);
        barChart    = findViewById(R.id.barChart);

        db = new DatabaseHelper(this);
        usuarioId = SessionManager.getUserId(this);

        mostrarResumen();
        mostrarPieChart();
        mostrarBarChart();
    }

    private void mostrarResumen() {
        // minutosTotalesVistos por usuario y estado = 'Visto'
        int minutos = db.minutosTotalesVistos(usuarioId);

        // Formato simple de horas:min
        int horas = minutos / 60;
        int mins  = minutos % 60;

        String resumen = "Tiempo total visto: " + horas + " h " + mins + " min";
        textResumen.setText(resumen);
    }

    private void mostrarPieChart() {
        // Pie global por tipo
        int peliculas = db.contarPorTipo("Película");
        int series    = db.contarPorTipo("Serie");

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(peliculas, "Películas"));
        entries.add(new PieEntry(series, "Series"));

        PieDataSet dataSet = new PieDataSet(entries, "Catálogo por tipo");
        // Colores del proyecto (estilos de MPAndroidChart)
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.teal_200));
        colors.add(getResources().getColor(R.color.purple_200));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);

        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void mostrarBarChart() {
        // Barras por género
        HashMap<String, Integer> generos = db.contarPorGenero();
        ArrayList<BarEntry> entries = new ArrayList<>();
        final ArrayList<String> etiquetas = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Integer> entry : generos.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            etiquetas.add(entry.getKey() == null ? "Sin género" : entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Contenidos por género");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.teal_200));
        colors.add(getResources().getColor(R.color.purple_200));
        colors.add(getResources().getColor(R.color.yellow_200));
        colors.add(getResources().getColor(R.color.green_200));
        dataSet.setColors(colors);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        barChart.setData(data);

        // Eje X con etiquetas de texto
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < etiquetas.size()) ? etiquetas.get(idx) : "";
            }
        });


        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.invalidate();
    }
}
