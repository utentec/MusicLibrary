package com.musiclibrary.repository;

import com.musiclibrary.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrackRepositoryTest {

    private TrackRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryTrackRepository();
    }

    // Creazione valida
    @Test
    void addTrack_validTrack_isPresentInFindAll() {
        Track track = new Track("Bohemian Rhapsody", "Queen", 1975, 355, "Rock");
        repo.addTrack(track);

        List<Track> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("Bohemian Rhapsody", all.get(0).getTitle());
    }

    // findById — trovato
    @Test
    void findById_existingTrack_returnsTrack() {
        Track track = new Track("Levitating", "Dua Lipa", 2020, 203, "Pop");
        repo.addTrack(track);

        Optional<Track> result = repo.findById(track.getId());
        assertTrue(result.isPresent());
        assertEquals("Levitating", result.get().getTitle());
    }

    // findById — non trovato
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<Track> result = repo.findById("id-inesistente");
        assertTrue(result.isEmpty());
    }

    // findAll — lista vuota
    @Test
    void findAll_emptyRepo_returnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    // removeTrack
    @Test
    void removeTrack_existingTrack_isRemovedFromList() {
        Track track = new Track("Shape of You", "Ed Sheeran", 2017, 234, "Pop");
        repo.addTrack(track);
        repo.removeTrack(track.getId());

        assertTrue(repo.findAll().isEmpty());
    }

    // updateTrack
    @Test
    void updateTrack_changesTitle_updatedInList() {
        Track track = new Track("Titolo Vecchio", "Artista", 2000, 200, "Pop");
        repo.addTrack(track);

        track.setTitle("Titolo Nuovo");
        repo.updateTrack(track);

        Optional<Track> updated = repo.findById(track.getId());
        assertTrue(updated.isPresent());
        assertEquals("Titolo Nuovo", updated.get().getTitle());
    }
}