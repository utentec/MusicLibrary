package com.musiclibrary.player;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Track;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test della logica di PlayerService, isolando l'audio reale con un doppio di
 * test ({@link FakeAudioPlayer}). Copre brano singolo e riproduzione playlist (avvio, avanzamento
 * automatico, fine, playlist vuota), senza riprodurre suono.
 */
class PlayerServiceTest {

    /** Registra le chiamate e permette di simulare la fine del brano. */
    private static final class FakeAudioPlayer implements AudioPlayer {
        private String lastPlayedPath;
        private int playCount;
        private Runnable onEnd;

        @Override
        public void play(String filePath) {
            lastPlayedPath = filePath;
            playCount++;
        }

        @Override
        public void stop() {
            //Richiesto dall'interfaccia AudioPlayer
        }

        @Override
        public void setOnEndOfMedia(Runnable callback) {
            this.onEnd = callback;
        }

        void simulateEndOfMedia() {
            if (onEnd != null) onEnd.run();
        }
    }

    private static Track trackWithFile() {
        Track t = new Track("One", "Metallica", 1988, 446, "Metal");
        t.setFilePath("/music/one.mp3");
        return t;
    }

    private static Playlist playlistWith(String... filePaths) {
        Playlist p = new Playlist("Mix");
        int i = 1;
        for (String path : filePaths) {
            Track t = new Track("Track " + i, "Artist", 2020, 200, "Pop");
            t.setFilePath(path);
            p.addTrack(t);
            i++;
        }
        return p;
    }

    /**
     * Brano singolo
      */

    @Test
    void play_startsPlaybackAndDelegatesToAudioPlayer() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        Track t = trackWithFile();

        service.play(t);

        assertEquals(PlaybackStatus.PLAYING, service.getPlaybackState().getStatus());
        assertSame(t, service.getPlaybackState().getCurrentTrack());
        assertEquals(1, audio.playCount);
        assertEquals("/music/one.mp3", audio.lastPlayedPath);
    }

    @Test
    void play_trackWithoutFile_doesNotStart() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        Track t = new Track("One", "Metallica", 1988, 446, "Metal"); // filePath vuoto

        service.play(t);

        assertEquals(PlaybackStatus.STOPPED, service.getPlaybackState().getStatus());
        assertNull(service.getPlaybackState().getCurrentTrack());
        assertEquals(0, audio.playCount); // non ha tentato di riprodurre
    }

    @Test
    void endOfMedia_returnsToStoppedState() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        service.play(trackWithFile());

        audio.simulateEndOfMedia(); // il brano finisce da solo

        assertEquals(PlaybackStatus.STOPPED, service.getPlaybackState().getStatus());
        assertNull(service.getPlaybackState().getCurrentTrack());
    }

    /**
     * Riproduzione di playlist
     */

    @Test
    void playPlaylist_startsAtFirstTrack() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        Playlist p = playlistWith("/a.mp3", "/b.mp3");

        service.playPlaylist(p);

        assertEquals(PlaybackStatus.PLAYING, service.getPlaybackState().getStatus());
        assertEquals(0, service.getPlaybackState().getCurrentIndex());
        assertEquals("/a.mp3", audio.lastPlayedPath);
    }

    @Test
    void endOfMedia_inPlaylist_advancesToNextTrack() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        Playlist p = playlistWith("/a.mp3", "/b.mp3");
        service.playPlaylist(p);

        audio.simulateEndOfMedia(); // finisce il primo brano

        assertEquals(PlaybackStatus.PLAYING, service.getPlaybackState().getStatus());
        assertEquals(1, service.getPlaybackState().getCurrentIndex());
        assertEquals("/b.mp3", audio.lastPlayedPath); // sta suonando il secondo
    }

    @Test
    void endOfMedia_atLastTrackOfPlaylist_stops() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        Playlist p = playlistWith("/only.mp3");
        service.playPlaylist(p);

        audio.simulateEndOfMedia(); // finisce l'ultimo brano

        assertEquals(PlaybackStatus.STOPPED, service.getPlaybackState().getStatus());
    }

    @Test
    void playPlaylist_emptyPlaylist_doesNotStart() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        Playlist empty = new Playlist("Vuota");

        service.playPlaylist(empty); // Playlist vuota

        assertEquals(PlaybackStatus.STOPPED, service.getPlaybackState().getStatus());
        assertEquals(0, audio.playCount);
    }
}


