package com.example.cinetracker.models;

public class Contenido {
    private int id;
    private String titulo;
    private String tipo;      // "Serie" o "Película"
    private String genero;
    private int año;
    private int duracion;     // minutos

    // Campos específicos opcionales para subclases
    private String director;      // Solo Película
    private Integer temporadas;   // Solo Serie
    private Integer episodios;    // Solo Serie

    public Contenido() {}

    public Contenido(int id, String titulo, String tipo, String genero, int año, int duracion) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.genero = genero;
        this.año = año;
        this.duracion = duracion;
    }

    //  Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public int getAño() { return año; }
    public void setAño(int año) { this.año = año; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    // Campos específicos (usados por subclases)
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public Integer getTemporadas() { return temporadas; }
    public void setTemporadas(Integer temporadas) { this.temporadas = temporadas; }

    public Integer getEpisodios() { return episodios; }
    public void setEpisodios(Integer episodios) { this.episodios = episodios; }

    public boolean esSerie()     { return "Serie".equalsIgnoreCase(tipo); }
    public boolean esPelicula()  { return "Película".equalsIgnoreCase(tipo) || "Pelicula".equalsIgnoreCase(tipo); }

    public String mostrarDetalles() {
        if (esSerie()) {
            int t = (temporadas == null ? 0 : temporadas);
            int e = (episodios  == null ? 0 : episodios);
            return "Serie | " + t + " temp · " + e + " eps";
        } else {
            return "Película | " + duracion + " min";
        }
    }

    @Override
    public String toString() { return titulo + " (" + tipo + ")"; }
}
