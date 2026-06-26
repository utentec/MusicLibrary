package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione di {@link PlaylistRepository} che mantiene le playlist
 * soltanto in memoria (i dati non sopravvivono alla chiusura).
 */
public class InMemoryPlaylistRepository implements PlaylistRepository {

    private final List<Playlist> playlists = new ArrayList<>();

    @Override
    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    @Override
    public void removePlaylist(String id) {
        playlists.removeIf(p -> p.getId().equals(id));
    }

    @Override
    public void update(Playlist playlist) {
        // No-op: la playlist è già memorizzata per riferimento, quindi le sue
        // modifiche sono immediatamente visibili senza dover persistere nulla.
    }

    @Override
    public Optional<Playlist> findById(String id) {
        return playlists.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Playlist> findByName(String name) {
        return playlists.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Playlist> findAll() {
        return new ArrayList<>(playlists);
    }
}
