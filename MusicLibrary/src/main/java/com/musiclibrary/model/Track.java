package com.musiclibrary.model;

import com.musiclibrary.model.enums.Tag;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Rappresenta un brano musicale della libreria, con i suoi metadati,
 * i tag associati e il contatore delle riproduzioni.
 */
public class Track {

    // ── Costanti ──────────────────────────────────────────
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MIN_YEAR = 1900;
    public static final int MAX_YEAR = 2026;

    // ── Attributi ─────────────────────────────────────────
    private String id;
    private String title;
    private String author;
    private int year;
    private int length;
    private String genre;
    private Set<Tag> tags;
    private int playCount;
    private String filePath;

    // ── Costruttore ───────────────────────────────────────
    /**
     * Crea un brano generando automaticamente un identificativo univoco.
     * @param title  titolo del brano
     * @param author autore/artista
     * @param year   anno di pubblicazione
     * @param length durata in secondi
     * @param genre  genere musicale
     */
    public Track(String title, String author, int year, int length, String genre) {
        this.id        = UUID.randomUUID().toString();
        this.title     = title;
        this.author    = author;
        this.year      = year;
        this.length    = length;
        this.genre     = genre;
        this.tags      = new HashSet<>();
        this.playCount = 0;
        this.filePath  = "";
    }

    // ── Metodi pubblici ───────────────────────────────────
    /**
     * Assegna un tag al brano (operazione idempotente).
     * @param tag il tag da aggiungere
     */
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    /**
     * Rimuove un tag dal brano, se presente.
     * @param tag il tag da rimuovere
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    /** Incrementa di uno il contatore delle riproduzioni. */
    public void incrementPlayCount() {
        playCount++;
    }

    // ── Getter ────────────────────────────────────────────
    /** @return l'identificativo univoco del brano */
    public String getId() {
        return id;
    }
    /** @return il titolo */
    public String getTitle() {
        return title;
    }
    /** @return l'autore/artista */
    public String getAuthor() {
        return author;
    }
    /** @return l'anno di pubblicazione */
    public int getYear() {
        return year;
    }
    /** @return la durata in secondi */
    public int getLength() {
        return length;
    }
    /** @return il genere musicale */
    public String getGenre() {
        return genre;
    }
    /** @return l'insieme dei tag associati al brano */
    public Set<Tag> getTags() {
        return tags;
    }
    /** @return il numero di riproduzioni */
    public int getPlayCount() {
        return playCount;
    }
    /** @return il percorso del file audio (stringa vuota se non impostato) */
    public String getFilePath() {
        return filePath;
    }

    // ── Setter ────────────────────────────────────────────
    /** @param title il nuovo titolo */
    public void setTitle(String title) {
        this.title = title;
    }
    /** @param author il nuovo autore/artista */
    public void setAuthor(String author)  {
        this.author = author;
    }
    /** @param year il nuovo anno di pubblicazione */
    public void setYear(int year) {
        this.year = year;
    }
    /** @param length la nuova durata in secondi */
    public void setLength(int length) {
        this.length = length;
    }
    /** @param genre il nuovo genere musicale */
    public void setGenre(String genre) {
        this.genre = genre;
    }
    /** @param path il percorso del file audio */
    public void setFilePath(String path) {
        this.filePath = path;
    }

    /** @return una rappresentazione testuale del tipo "Titolo — Autore (anno)" */
    @Override
    public String toString() {
        return title + " — " + author + " (" + year + ")";
    }
}
