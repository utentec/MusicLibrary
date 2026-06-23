package com.musiclibrary.repository;

import com.musiclibrary.model.Track;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryTrackRepository implements TrackRepository {

    private List<Track> tracks = new ArrayList<>();

    @Override
    public void addTrack(Track track) {
        tracks.add(track);
    }

    @Override
    public void removeTrack(String id) {
        tracks.removeIf(t -> t.getId().equals(id));
    }

    @Override
    public void updateTrack(Track track) {
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId().equals(track.getId())) {
                tracks.set(i, track);
                return;
            }
        }
    }

    @Override
    public Optional<Track> findById(String id) {
        return tracks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Track> findByName(String title) {
        return tracks.stream()
                .filter(t -> t.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    @Override
    public List<Track> findAll() {
        return new ArrayList<>(tracks);
    }
}