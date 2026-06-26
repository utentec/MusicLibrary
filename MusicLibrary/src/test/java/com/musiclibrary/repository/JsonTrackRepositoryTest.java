package com.musiclibrary.repository;

import com.musiclibrary.model.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del JsonTrackRepository: verificano la persistenza su file
 * (Scenari 1, 2 e 3 della US-Persistence, limitatamente ai brani).
 */
class JsonTrackRepositoryTest {

    @TempDir Path tempDir;

    @Test
    void saveAndReload_preservesTracks() {
        // Salvataggio e ricaricamento da una nuova istanza
        Path file = tempDir.resolve("tracks.json");
        JsonTrackRepository repo = new JsonTrackRepository(file);
        Track t = new Track("Bohemian Rhapsody", "Queen", 1975, 355, "Rock");
        repo.addTrack(t);

        JsonTrackRepository reloaded = new JsonTrackRepository(file);
        List<Track> all = reloaded.findAll();

        assertEquals(1, all.size());
        Track r = all.get(0);
        assertEquals(t.getId(),  r.getId());      // id preservato
        assertEquals("Bohemian Rhapsody", r.getTitle());
        assertEquals("Queen", r.getAuthor());
        assertEquals(1975, r.getYear());
        assertEquals(355,  r.getLength());
        assertEquals("Rock", r.getGenre());
    }

    @Test
    void load_whenFileAbsent_startsEmpty() {
        // File inesistente -> libreria vuota, nessun errore
        Path file = tempDir.resolve("does-not-exist.json");
        JsonTrackRepository repo = new JsonTrackRepository(file);
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void removeTrack_isPersisted() {
        Path file = tempDir.resolve("tracks.json");
        JsonTrackRepository repo = new JsonTrackRepository(file);
        Track t = new Track("Imagine", "John Lennon", 1971, 183, "Rock");
        repo.addTrack(t);
        repo.removeTrack(t.getId());

        JsonTrackRepository reloaded = new JsonTrackRepository(file);
        assertTrue(reloaded.findAll().isEmpty());
    }

    @Test
    void updateTrack_isPersisted() {
        Path file = tempDir.resolve("tracks.json");
        JsonTrackRepository repo = new JsonTrackRepository(file);
        Track t = new Track("Imagin", "John Lennon", 1971, 183, "Rock");
        repo.addTrack(t);
        t.setTitle("Imagine");
        repo.updateTrack(t);

        JsonTrackRepository reloaded = new JsonTrackRepository(file);
        assertEquals("Imagine", reloaded.findAll().get(0).getTitle());
    }
}
