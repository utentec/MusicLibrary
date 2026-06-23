package com.musiclibrary.controller;

import com.musiclibrary.command.AddTrackCommand;
import com.musiclibrary.command.Command;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import com.musiclibrary.repository.PlaylistRepository;
import com.musiclibrary.repository.TrackRepository;

import java.util.List;

/**
 * Facade del dominio: punto d'accesso unico con cui la UI interagisce con la
 * libreria musicale. Centralizza le validazioni e nasconde repository, comandi
 * e gestione dell'undo.
 */
public class MusicLibraryFacade {

    // ── Attributi ─────────────────────────────────────────
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final UndoManager undoManager = new UndoManager();

    // ── Costruttore ───────────────────────────────────────
    /**
     * @param trackRepository    repository dei brani
     * @param playlistRepository repository delle playlist
     */
    public MusicLibraryFacade(TrackRepository trackRepository,
                              PlaylistRepository playlistRepository) {
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
    }

    // ── Metodi pubblici ───────────────────────────────────

    /**
     * Crea un nuovo brano con validazione dei campi.
     * @param title  titolo del brano
     * @param author autore/artista
     * @param year   anno di pubblicazione
     * @param length durata in secondi
     * @param genre  genere musicale
     * @return il brano creato
     * @throws IllegalArgumentException se i dati non sono validi
     */
    public Track createTrack(String title, String author,
                             int year, int length, String genre) {
        validateTrackData(title, author, year, length);
        Track track = new Track(title, author, year, length, genre);
        trackRepository.addTrack(track);
        return track;
    }

    /**
     * Modifica i dati di un brano esistente.
     * @param id     identificativo del brano
     * @param title  nuovo titolo
     * @param author nuovo autore/artista
     * @param year   nuovo anno
     * @param length nuova durata
     * @param genre  nuovo genere
     * @throws IllegalArgumentException se i dati non sono validi
     * @throws IllegalStateException    se il brano non esiste
     */
    public void updateTrack(String id, String title, String author,
                            int year, int length, String genre) {
        validateTrackData(title, author, year, length);
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Brano non trovato: " + id));
        track.setTitle(title);
        track.setAuthor(author);
        track.setYear(year);
        track.setLength(length);
        track.setGenre(genre);
        trackRepository.updateTrack(track);
    }

    /**
     * Elimina un brano dalla libreria e da tutte le playlist che lo contengono.
     * @param id identificativo del brano
     * @throws IllegalStateException se il brano non esiste
     */
    public void deleteTrack(String id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Brano non trovato: " + id));
        for (Playlist playlist : playlistRepository.findAll()) {
            playlist.removeTrack(track);
        }
        trackRepository.removeTrack(id);
    }

    /**
     * Crea una nuova playlist con nome univoco.
     * @param name nome della playlist
     * @return la playlist creata
     * @throws IllegalArgumentException se il nome è vuoto o già esistente
     */
    public Playlist createPlaylist(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }
        if (playlistRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException(
                    "Esiste già una playlist con il nome: " + name);
        }
        Playlist playlist = new Playlist(name);
        playlistRepository.addPlaylist(playlist);
        return playlist;
    }

    /**
     * Aggiunge un brano a una playlist tramite Command (supporta l'undo).
     * @param playlist playlist di destinazione
     * @param track    brano da aggiungere
     */
    public void addTrackToPlaylist(Playlist playlist, Track track) {
        Command cmd = new AddTrackCommand(playlist, track);
        undoManager.executeCommand(cmd);
    }

    /** Annulla l'ultima operazione annullabile (es. un'aggiunta a playlist). */
    public void undo() {
        undoManager.undo();
    }

    /**
     * Indica se esiste un'operazione annullabile.
     * @return {@code true} se l'undo è disponibile
     */
    public boolean canUndo() {
        return undoManager.canUndo();
    }

    // ── Metodi privati ────────────────────────────────────
    /**
     * Valida i campi comuni di un brano (titolo, autore, anno, durata),
     * condivisi tra creazione e modifica.
     * @throws IllegalArgumentException se un vincolo non è rispettato
     */
    private void validateTrackData(String title, String author, int year, int length) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto.");
        }
        if (title.length() > Track.MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Il titolo non può superare " + Track.MAX_TITLE_LENGTH + " caratteri.");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("L'autore non può essere vuoto.");
        }
        if (year < Track.MIN_YEAR || year > Track.MAX_YEAR) {
            throw new IllegalArgumentException(
                    "L'anno deve essere compreso tra "
                            + Track.MIN_YEAR + " e " + Track.MAX_YEAR + ".");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("La durata deve essere maggiore di 0.");
        }
    }

    // ── Getter ────────────────────────────────────────────
    /**
     * Restituisce tutti i brani della libreria.
     * @return la lista dei brani
     */
    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    /**
     * Restituisce tutte le playlist.
     * @return la lista delle playlist
     */
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }
}
