package com.musiclibrary.repository;

import com.musiclibrary.model.Track;
import java.util.List;
import java.util.Optional;

/**
 * Repository dei brani: astrae la memorizzazione delle istanze di {@link Track},
 * così da poterne cambiare l'implementazione (in memoria, su file, su DB)
 * senza impattare la logica applicativa.
 */
public interface TrackRepository {

    /**
     * Aggiunge un brano al repository.
     * @param track il brano da memorizzare
     */
    void addTrack(Track track);

    /**
     * Rimuove il brano con l'identificativo indicato.
     * @param id identificativo del brano da rimuovere
     */
    void removeTrack(String id);

    /**
     * Aggiorna un brano già presente, identificato dal suo id.
     * @param track il brano con i dati aggiornati
     */
    void updateTrack(Track track);

    /**
     * Cerca un brano per identificativo.
     * @param id identificativo del brano
     * @return il brano, se presente
     */
    Optional<Track> findById(String id);

    /**
     * Cerca un brano per titolo.
     * @param title titolo del brano
     * @return il brano, se presente
     */
    Optional<Track> findByName(String title);

    /**
     * Restituisce tutti i brani memorizzati.
     * @return la lista di tutti i brani
     */
    List<Track> findAll();
}
