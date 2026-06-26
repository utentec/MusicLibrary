package com.musiclibrary.player;

import com.musiclibrary.model.Track;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test delle transizioni di stato di PlaybackState (logica del player)
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
        Track first  = new Track("One", "Metallica", 1988, 446, "Metal");
        Track second = new Track("Two", "Metallica", 1991, 300, "Metal");

        state.startTrack(first);
        state.startTrack(second);

        assertSame(second, state.getCurrentTrack());
        assertEquals(PlaybackStatus.PLAYING, state.getStatus());
    }
}
