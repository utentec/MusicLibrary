package com.musiclibrary.controller;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import com.musiclibrary.repository.InMemoryPlaylistRepository;
import com.musiclibrary.repository.InMemoryTrackRepository;
import com.musiclibrary.repository.PlaylistRepository;
import com.musiclibrary.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicLibraryFacadeTest {

    private MusicLibraryFacade facade;

    @BeforeEach
    void setUp() {
        TrackRepository trackRepo = new InMemoryTrackRepository();
        PlaylistRepository playlistRepo = new InMemoryPlaylistRepository();
        facade = new MusicLibraryFacade(trackRepo, playlistRepo);
    }

    // ── US-H1.1 — Creazione brano (partizionamento in classi di equivalenza) ──

    // Creazione valida
    @Test
    void createTrack_validData_trackIsSaved() {
        Track track = facade.createTrack("Bohemian Rhapsody", "Queen", 1975, 355, "Rock");

        assertNotNull(track);
        assertEquals("Bohemian Rhapsody", track.getTitle());
        assertEquals(1, facade.getAllTracks().size());
    }

    // Titolo vuoto
    @Test
    void createTrack_emptyTitle_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("", "Queen", 1975, 355, "Rock")
        );
    }

    // Autore vuoto
    @Test
    void createTrack_emptyAuthor_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("Titolo", "", 2000, 200, "Rock")
        );
    }

    // Anno non valido (rappresentante della classe "fuori range")
    @Test
    void createTrack_invalidYear_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("Titolo", "Artista", 1800, 355, "Rock")
        );
    }

    // Durata non valida (rappresentante della classe "<= 0")
    @Test
    void createTrack_zeroDuration_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("Titolo", "Artista", 2000, 0, "Rock")
        );
    }


    // ── Anno: intervallo valido [1900, 2026] ──────────────────────────────

    @Test
    void createTrack_yearJustBelowMin_throwsException() {   // 1899 → non valido
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("Titolo", "Artista", 1899, 200, "Rock")
        );
    }

    @Test
    void createTrack_yearAtMin_isValid() {                  // 1900 → valido (confine)
        Track t = facade.createTrack("Titolo", "Artista", 1900, 200, "Rock");
        assertNotNull(t);
        assertEquals(1900, t.getYear());
    }

    @Test
    void createTrack_yearAtMax_isValid() {                  // 2026 → valido (confine)
        Track t = facade.createTrack("Titolo", "Artista", 2026, 200, "Rock");
        assertNotNull(t);
        assertEquals(2026, t.getYear());
    }

    @Test
    void createTrack_yearJustAboveMax_throwsException() {   // 2027 → non valido
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("Titolo", "Artista", 2027, 200, "Rock")
        );
    }

    // ── Durata: valida se > 0 (confine 0 / 1) ─────────────────────────────
    //  (il caso durata = 0 è già coperto da createTrack_zeroDuration_...)

    @Test
    void createTrack_durationOne_isValid() {                // 1 → valido (appena sopra 0)
        Track t = facade.createTrack("Titolo", "Artista", 2000, 1, "Rock");
        assertNotNull(t);
        assertEquals(1, t.getLength());
    }

    @Test
    void createTrack_negativeDuration_throwsException() {   // -1 → non valido
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack("Titolo", "Artista", 2000, -1, "Rock")
        );
    }

    // ── Lunghezza titolo: massimo 200 caratteri (confine 200 / 201) ────────

    @Test
    void createTrack_titleAtMaxLength_isValid() {           // 200 char → valido (confine)
        String title200 = "a".repeat(200);
        Track t = facade.createTrack(title200, "Artista", 2000, 200, "Rock");
        assertNotNull(t);
        assertEquals(200, t.getTitle().length());
    }

    @Test
    void createTrack_titleOverMaxLength_throwsException() { // 201 char → non valido
        String title201 = "a".repeat(201);
        assertThrows(IllegalArgumentException.class, () ->
                facade.createTrack(title201, "Artista", 2000, 200, "Rock")
        );
    }

    // ── US-H1.4 — Visualizzazione lista brani ─────────────────────────────

    @Test
    void getAllTracks_afterAddingTracks_returnsAll() {
        facade.createTrack("Bohemian Rhapsody", "Queen", 1975, 355, "Rock");
        facade.createTrack("Levitating", "Dua Lipa", 2020, 203, "Pop");

        assertEquals(2, facade.getAllTracks().size());
    }

    @Test
    void getAllTracks_emptyLibrary_returnsEmptyList() {
        assertTrue(facade.getAllTracks().isEmpty());
    }

    // ── US-H1.5 — Creazione playlist ──────────────────────────────────────

    @Test
    void createPlaylist_validName_playlistIsSaved() {
        facade.createPlaylist("Preferiti");
        assertEquals(1, facade.getAllPlaylists().size());
    }

    @Test
    void createPlaylist_emptyName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                facade.createPlaylist("")
        );
    }

    @Test
    void createPlaylist_duplicateName_throwsException() {
        facade.createPlaylist("Rock");
        assertThrows(IllegalArgumentException.class, () ->
                facade.createPlaylist("Rock")
        );
    }

    // ── US-H1.2 — Modifica brano ──────────────────────────────────────────

    @Test
    void updateTrack_validData_trackIsUpdated() { // Cambio titolo
        Track t = facade.createTrack("Titolo Vecchio", "Artista", 2000, 200, "Pop");
        facade.updateTrack(t.getId(), "Titolo Nuovo", "Artista", 2000, 200, "Pop");

        Track updated = facade.getAllTracks().get(0);
        assertEquals("Titolo Nuovo", updated.getTitle());
    }

    @Test
    void updateTrack_invalidYear_throwsException() { // Cambio con data non valida
        Track t = facade.createTrack("Titolo", "Artista", 2000, 200, "Pop");
        assertThrows(IllegalArgumentException.class, () ->
                facade.updateTrack(t.getId(), "Titolo", "Artista", 1800, 200, "Pop")
        );
    }

    @Test
    void updateTrack_zeroDuration_throwsException() { // Cambio con durata non valida
        Track t = facade.createTrack("Titolo", "Artista", 2000, 200, "Pop");
        assertThrows(IllegalArgumentException.class, () ->
                facade.updateTrack(t.getId(), "Titolo", "Artista", 2000, 0, "Pop")
        );
    }

    @Test
    void updateTrack_nonExistingId_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                facade.updateTrack("id-falso", "Titolo", "Artista", 2000, 200, "Pop")
        );
    }

    // ── US-H1.3 — Eliminazione brano ──────────────────────────────────────

    @Test
    void deleteTrack_existing_isRemovedFromLibrary() {
        Track t = facade.createTrack("Shape of You", "Ed Sheeran", 2017, 234, "Pop");
        facade.deleteTrack(t.getId());

        assertTrue(facade.getAllTracks().isEmpty());
    }

    @Test
    void deleteTrack_nonExisting_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                facade.deleteTrack("id-falso")
        );
    }

    @Test
    void deleteTrack_removesFromAllPlaylists() {
        Track t = facade.createTrack("Levitating", "Dua Lipa", 2020, 203, "Pop");
        Playlist p = facade.createPlaylist("Preferiti");
        p.addTrack(t);

        facade.deleteTrack(t.getId());

        assertTrue(facade.getAllTracks().isEmpty());
        assertTrue(p.getTracks().isEmpty());
    }

    // ── US-H1.6 — Aggiunta brani a una playlist ───────────────────────────

    @Test
    void addTrackToPlaylist_trackAppearsInPlaylist() {
        Track t = facade.createTrack("Bohemian Rhapsody", "Queen", 1975, 355, "Rock");
        Playlist p = facade.createPlaylist("Preferiti");

        facade.addTrackToPlaylist(p, t);

        assertEquals(1, p.getTracks().size());
        assertEquals("Bohemian Rhapsody", p.getTracks().get(0).getTitle());
    }

    // Stesso brano in due playlist diverse, senza conflitti
    @Test
    void addTrackToPlaylist_sameTrackInTwoPlaylists_appearsInBoth() {
        Track t = facade.createTrack("Bohemian Rhapsody", "Queen", 1975, 355, "Rock");
        Playlist pop = facade.createPlaylist("Pop");
        Playlist preferiti = facade.createPlaylist("Preferiti");

        facade.addTrackToPlaylist(pop, t);
        facade.addTrackToPlaylist(preferiti, t);

        assertEquals(1, pop.getTracks().size());
        assertEquals(1, preferiti.getTracks().size());
        assertTrue(pop.getTracks().contains(t));
        assertTrue(preferiti.getTracks().contains(t));
    }

    // Stesso brano due volte nella stessa playlist, in fondo
    @Test
    void addTrackToPlaylist_sameTrackTwice_appearsTwiceInOrder() {
        Track t = facade.createTrack("Levitating", "Dua Lipa", 2020, 203, "Pop");
        Playlist p = facade.createPlaylist("Mix");

        facade.addTrackToPlaylist(p, t);
        facade.addTrackToPlaylist(p, t);

        assertEquals(2, p.getTracks().size());
        assertSame(t, p.getTracks().get(0));   // appare in posizione 0
        assertSame(t, p.getTracks().get(1));   // e di nuovo in fondo (posizione 1)
    }

    @Test
    void addTrackToPlaylist_undo_removesTrackFromPlaylist() {
        Track t = facade.createTrack("Shape of You", "Ed Sheeran", 2017, 234, "Pop");
        Playlist p = facade.createPlaylist("Pop");

        facade.addTrackToPlaylist(p, t);
        assertTrue(facade.canUndo());

        facade.undo();
        assertTrue(p.getTracks().isEmpty());
    }
}
