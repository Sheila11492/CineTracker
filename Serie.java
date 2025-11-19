package com.example.cinetracker.models;

public class Serie extends Contenido {

    public Serie() {
        setTipo("Serie");
    }

    public Serie(int id, String titulo, String genero, int año, int duracion,
                 Integer temporadas, Integer episodios) {
        super(id, titulo, "Serie", genero, año, duracion);
        setTemporadas(temporadas);
        setEpisodios(episodios);
    }

    public Integer getTemporadas() { return super.getTemporadas(); }
    public void setTemporadas(Integer temporadas) { super.setTemporadas(temporadas); }

    public Integer getEpisodios() { return super.getEpisodios(); }
    public void setEpisodios(Integer episodios) { super.setEpisodios(episodios); }


    public int getTotalEpisodios() {
        return (getEpisodios() == null ? 0 : getEpisodios());
    }

    @Override
    public String mostrarDetalles() {
        int t = (getTemporadas() == null ? 0 : getTemporadas());
        int e = (getEpisodios()  == null ? 0 : getEpisodios());
        return "Serie | " + t + " temp · " + e + " eps";
    }

    @Override
    public String toString() { return getTitulo() + " (Serie)"; }
}
