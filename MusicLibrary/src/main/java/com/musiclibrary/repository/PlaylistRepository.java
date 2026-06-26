package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import java.util.List;
import java.util.Optional;

/**
 * Repository delle playlist: astrae la memorizzazione delle istanze di
 * {@link Playlist}, così da poterne cambiare l'implementazione senza
 * impattare la logica applicativa.
 */
public interface PlaylistRepository {

    /**
     * Aggiunge una playlist al repository.
     * @param playlist la playlist da memorizzare
     */
    void addPlaylist(Playlist playlist);

    /**
     * Rimuove la playlist con l'identificativo indicato.
     * @param id identificativo della playlist da rimuovere
     */
    void removePlaylist(String id);

    /**
     * Persiste lo stato corrente di una playlist già esistente (ad esempio dopo
     * l'aggiunta o la rimozione di un brano dal suo contenuto). Simmetrico a
     * {@link TrackRepository#updateTrack}.
     * @param playlist la playlist da aggiornare
     */
    void update(Playlist playlist);

    /**
     * Cerca una playlist per identificativo.
     * @param id identificativo della playlist
     * @return la playlist, se presente
     */
    Optional<Playlist> findById(String id);

    /**
     * Cerca una playlist per nome.
     * @param name nome della playlist
     * @return la playlist, se presente
     */
    Optional<Playlist> findByName(String name);

    /**
     * Restituisce tutte le playlist memorizzate.
     * @return la lista di tutte le playlist
     */
    List<Playlist> findAll();
}
