package com.example.cinetracker.models;

public class Pelicula extends Contenido {

    public Pelicula() {
        setTipo("Película");
    }

    public Pelicula(int id, String titulo, String genero, int año, int duracion, String director) {
        super(id, titulo, "Película", genero, año, duracion);
        setDirector(director);
    }

    public String getDirector() { return super.getDirector(); }
    public void setDirector(String director) { super.setDirector(director); }

    @Override
    public String mostrarDetalles() {
        return "Película | " + getDuracion() + " min";
    }

    @Override
    public String toString() { return getTitulo() + " (Película)"; }
}
