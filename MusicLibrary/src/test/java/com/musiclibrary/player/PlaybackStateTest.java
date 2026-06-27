package com.musiclibrary.player;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test delle transizioni di stato di PlaybackState (logica del player):
 * brano singolo e riproduzione playlist
 */
class PlaybackStateTest {

    @Test
    void initialState_isStoppedWithNoTrack() {
        PlaybackState state = new PlaybackState();
        assertEquals(PlaybackStatus.STOPPED, state.getStatus());
        assertNull(state.getCurrentTrack());
        assertNull(state.getCurrentPlaylist());
        assertEquals(-1, state.getCurrentIndex());
    }

    @Test
    void startTrack_setsPlayingAndCurrentTrack() {
        PlaybackState state = new PlaybackState();
        Track t = new Track("One", "Metallica", 1988, 446, "Metal");

        state.startTrack(t);

        assertEquals(PlaybackStatus.PLAYING, state.getStatus());
        assertSame(t, state.getCurrentTrack());
        // singolo brano: nessun contesto di playlist
        assertNull(state.getCurrentPlaylist());
        assertEquals(-1, state.getCurrentIndex());
    }

    @Test
    void stop_resetsToInitialState() {
        PlaybackState state = new PlaybackState();
        state.startTrack(new Track("One", "Metallica", 1988, 446, "Metal"));

        state.stop();

        assertEquals(PlaybackStatus.STOPPED, state.getStatus());
        assertNull(state.getCurrentTrack());
        assertEquals(-1, state.getCurrentIndex());
    }

    @Test
    void startTrack_whenAlreadyPlaying_switchesTrack() {
        PlaybackState state = new PlaybackState();
        Track first = new Track("One", "Metallica", 1988, 446, "Metal");
        Track second = new Track("Two", "Metallica", 1991, 300, "Metal");

        state.startTrack(first);
        state.startTrack(second);

        assertSame(second, state.getCurrentTrack());
        assertEquals(PlaybackStatus.PLAYING, state.getStatus());
    }

    /**
     * Riproduzione playlist
     */

    @Test
    void startPlaylist_startsAtFirstTrack() {
        Track t1 = new Track("A", "Artist", 2020, 200, "Pop");
        Track t2 = new Track("B", "Artist", 2021, 210, "Pop");
        Playlist p = new Playlist("Mix");
        p.addTrack(t1);
        p.addTrack(t2);
        PlaybackState state = new PlaybackState();

        state.startPlaylist(p);

        assertEquals(PlaybackStatus.PLAYING, state.getStatus());
        assertSame(t1, state.getCurrentTrack());
        assertSame(p, state.getCurrentPlaylist());
        assertEquals(0, state.getCurrentIndex());
    }

    @Test
    void advanceToNext_movesToNextTrack() {
        Track t1 = new Track("A", "Artist", 2020, 200, "Pop");
        Track t2 = new Track("B", "Artist", 2021, 210, "Pop");
        Playlist p = new Playlist("Mix");
        p.addTrack(t1);
        p.addTrack(t2);
        PlaybackState state = new PlaybackState();
        state.startPlaylist(p);

        boolean advanced = state.advanceToNext();

        assertTrue(advanced);
        assertSame(t2, state.getCurrentTrack());
        assertEquals(1, state.getCurrentIndex());
    }

    @Test
    void advanceToNext_atLastTrack_returnsFalse() {
        Track only = new Track("A", "Artist", 2020, 200, "Pop");
        Playlist p = new Playlist("Mix");
        p.addTrack(only);
        PlaybackState state = new PlaybackState();
        state.startPlaylist(p);

        boolean advanced = state.advanceToNext();

        assertFalse(advanced);
        assertSame(only, state.getCurrentTrack()); // resta sull'ultimo
        assertEquals(0, state.getCurrentIndex());
    }

    @Test
    void advanceToNext_withoutPlaylist_returnsFalse() {
        PlaybackState state = new PlaybackState();
        state.startTrack(new Track("A", "Artist", 2020, 200, "Pop")); // brano singolo

        assertFalse(state.advanceToNext());
    }
}