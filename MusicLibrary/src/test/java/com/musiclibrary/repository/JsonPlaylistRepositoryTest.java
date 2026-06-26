package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del JsonPlaylistRepository: persistenza delle playlist, condivisione dei
 * riferimenti con la libreria e gestione delle referenze pendenti (Scenari 1-4
 * della US-Persistence, lato playlist).
 */
class JsonPlaylistRepositoryTest {

    @TempDir Path tempDir;

    @Test
    void saveAndReload_sharesTrackInstancesWithLibrary() {
        // Il brano nella playlist DEVE essere
        // la stessa istanza presente nella libreria.
        Path tracksFile    = tempDir.resolve("tracks.json");
        Path playlistsFile = tempDir.resolve("playlists.json");

        JsonTrackRepository trackRepo = new JsonTrackRepository(tracksFile);
        Track t = new Track("One", "Metallica", 1988, 446, "Metal");
        trackRepo.addTrack(t);

        JsonPlaylistRepository playlistRepo = new JsonPlaylistRepository(playlistsFile, trackRepo);
        Playlist p = new Playlist("Preferiti");
        playlistRepo.addPlaylist(p);
        p.addTrack(t);
        playlistRepo.update(p);

        // Nuova sessione: ricarico da zero
        JsonTrackRepository trackRepo2 = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo2 = new JsonPlaylistRepository(playlistsFile, trackRepo2);

        Playlist reloaded = playlistRepo2.findByName("Preferiti").orElseThrow();
        assertEquals(1, reloaded.size());

        Track inPlaylist = reloaded.getTrackAt(0);
        Track inLibrary  = trackRepo2.findById(t.getId()).orElseThrow();
        assertSame(inLibrary, inPlaylist); // stessa istanza condivisa
    }

    @Test
    void load_discardsPendingTrackReferences() {
        // Una playlist referenzia un brano non più esistente.
        Path tracksFile    = tempDir.resolve("tracks.json");
        Path playlistsFile = tempDir.resolve("playlists.json");

        JsonTrackRepository trackRepo = new JsonTrackRepository(tracksFile);
        Track t = new Track("Ghost", "Artist", 2000, 200, "Pop");
        trackRepo.addTrack(t);

        JsonPlaylistRepository playlistRepo = new JsonPlaylistRepository(playlistsFile, trackRepo);
        Playlist p = new Playlist("Mix");
        playlistRepo.addPlaylist(p);
        p.addTrack(t);
        playlistRepo.update(p);

        trackRepo.removeTrack(t.getId()); // il brano sparisce dalla libreria

        JsonTrackRepository trackRepo2 = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo2 = new JsonPlaylistRepository(playlistsFile, trackRepo2);

        Playlist reloaded = playlistRepo2.findByName("Mix").orElseThrow();
        assertEquals(0, reloaded.size()); // referenza pendente scartata, nessun crash
    }

    @Test
    void saveAndReload_preservesEmptyPlaylist() {
        Path tracksFile    = tempDir.resolve("tracks.json");
        Path playlistsFile = tempDir.resolve("playlists.json");
        JsonTrackRepository trackRepo = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo = new JsonPlaylistRepository(playlistsFile, trackRepo);
        playlistRepo.addPlaylist(new Playlist("Vuota"));

        JsonTrackRepository trackRepo2 = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo2 = new JsonPlaylistRepository(playlistsFile, trackRepo2);
        assertEquals(0, playlistRepo2.findByName("Vuota").orElseThrow().size());
    }

    @Test
    void load_whenFileAbsent_startsEmpty() {
        // File inesistente -> nessuna playlist, nessun errore
        JsonTrackRepository trackRepo = new JsonTrackRepository(tempDir.resolve("tracks.json"));
        JsonPlaylistRepository playlistRepo =
                new JsonPlaylistRepository(tempDir.resolve("nope.json"), trackRepo);
        assertTrue(playlistRepo.findAll().isEmpty());
    }

    @Test
    void removePlaylist_isPersisted() {
        Path tracksFile    = tempDir.resolve("tracks.json");
        Path playlistsFile = tempDir.resolve("playlists.json");
        JsonTrackRepository trackRepo = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo = new JsonPlaylistRepository(playlistsFile, trackRepo);
        Playlist p = new Playlist("Temp");
        playlistRepo.addPlaylist(p);
        playlistRepo.removePlaylist(p.getId());

        JsonTrackRepository trackRepo2 = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo2 = new JsonPlaylistRepository(playlistsFile, trackRepo2);
        assertTrue(playlistRepo2.findAll().isEmpty());
    }

    @Test
    void update_persistsAddedTrack() {
        Path tracksFile    = tempDir.resolve("tracks.json");
        Path playlistsFile = tempDir.resolve("playlists.json");
        JsonTrackRepository trackRepo = new JsonTrackRepository(tracksFile);
        Track t = new Track("Song", "Artist", 2010, 200, "Pop");
        trackRepo.addTrack(t);
        JsonPlaylistRepository playlistRepo = new JsonPlaylistRepository(playlistsFile, trackRepo);
        Playlist p = new Playlist("List");
        playlistRepo.addPlaylist(p);  // salvata vuota
        p.addTrack(t);
        playlistRepo.update(p);       // persiste l'aggiunta

        JsonTrackRepository trackRepo2 = new JsonTrackRepository(tracksFile);
        JsonPlaylistRepository playlistRepo2 = new JsonPlaylistRepository(playlistsFile, trackRepo2);
        assertEquals(1, playlistRepo2.findByName("List").orElseThrow().size());
    }
}
