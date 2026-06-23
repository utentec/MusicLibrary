package com.musiclibrary.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Rappresenta una playlist: una sequenza ordinata di brani, con nome e
 * contatore delle riproduzioni. Lo stesso brano può comparire più volte.
 */
public class Playlist {

    // ── Attributi ─────────────────────────────────────────
    private String id;
    private String name;
    private List<Track> tracks;
    private int playCount;

    // ── Costruttore ───────────────────────────────────────
    /**
     * Crea una playlist vuota con identificativo univoco generato automaticamente.
     * @param name nome della playlist
     */
    public Playlist(String name) {
        this.id        = UUID.randomUUID().toString();
        this.name      = name;
        this.tracks    = new ArrayList<>();
        this.playCount = 0;
    }

    // ── Metodi pubblici ───────────────────────────────────
    /**
     * Aggiunge un brano in fondo alla playlist.
     * @param track il brano da aggiungere
     */
    public void addTrack(Track track) {
        tracks.add(track);
    }

    /**
     * Inserisce un brano in una posizione specifica.
     * @param track    il brano da aggiungere
     * @param position indice in cui inserirlo
     */
    public void addTrack(Track track, int position) {
        tracks.add(position, track);
    }

    /**
     * Rimuove la prima occorrenza del brano indicato.
     * @param track il brano da rimuovere
     */
    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    /**
     * Sposta un brano da una posizione a un'altra.
     * @param fromIndex posizione di partenza
     * @param toIndex   posizione di destinazione
     */
    public void moveTrack(int fromIndex, int toIndex) {
        Track track = tracks.remove(fromIndex);
        tracks.add(toIndex, track);
    }

    /**
     * Restituisce il brano alla posizione indicata.
     * @param index posizione del brano
     * @return il brano in quella posizione
     */
    public Track getTrackAt(int index) {
        return tracks.get(index);
    }

    /**
     * Restituisce il numero di brani nella playlist.
     * @return la dimensione della playlist
     */
    public int size() {
        return tracks.size();
    }

    /** Incrementa di uno il contatore delle riproduzioni. */
    public void incrementPlayCount() {
        playCount++;
    }

    // ── Getter ────────────────────────────────────────────
    /** @return l'identificativo univoco della playlist */
    public String getId()          { return id; }
    /** @return il nome della playlist */
    public String getName()        { return name; }
    /** @return una copia della lista dei brani (modificarla non altera la playlist) */
    public List<Track> getTracks() { return new ArrayList<>(tracks); }
    /** @return il numero di riproduzioni */
    public int getPlayCount()      { return playCount; }

    // ── Setter ────────────────────────────────────────────
    /** @param name il nuovo nome della playlist */
    public void setName(String name) { this.name = name; }

    /** @return una rappresentazione testuale del tipo "nome (N brani)" */
    @Override
    public String toString() {
        return name + " (" + tracks.size() + " brani)";
    }
}
