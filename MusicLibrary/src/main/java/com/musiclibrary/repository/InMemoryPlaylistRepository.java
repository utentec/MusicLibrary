package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryPlaylistRepository implements PlaylistRepository {

    private List<Playlist> playlists = new ArrayList<>();

    @Override
    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    @Override
    public void removePlaylist(String id) {
        playlists.removeIf(p -> p.getId().equals(id));
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