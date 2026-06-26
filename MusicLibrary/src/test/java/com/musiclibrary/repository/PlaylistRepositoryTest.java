package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaylistRepositoryTest {

    private PlaylistRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryPlaylistRepository();
    }

    @Test
    void addPlaylist_validPlaylist_isPresentInFindAll() {
        Playlist p = new Playlist("Preferiti");
        repo.addPlaylist(p);
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findByName_existingPlaylist_returnsPlaylist() {
        repo.addPlaylist(new Playlist("Rock"));
        assertTrue(repo.findByName("Rock").isPresent());
    }

    @Test
    void findByName_nonExisting_returnsEmpty() {
        assertTrue(repo.findByName("NonEsiste").isEmpty());
    }

    @Test
    void removePlaylist_existing_isRemovedFromList() {
        Playlist p = new Playlist("Pop");
        repo.addPlaylist(p);
        repo.removePlaylist(p.getId());
        assertTrue(repo.findAll().isEmpty());
    }
}
