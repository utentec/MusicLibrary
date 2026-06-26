package com.musiclibrary.player;

import com.musiclibrary.model.Track;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test della logica di PlayerService, isolando l'audio reale con un doppio di
 * test ({@link FakeAudioPlayer}). Verifica avvio, stop, brano senza file e
 * fine naturale del brano, senza riprodurre alcun suono.
 */
class PlayerServiceTest {

    /** Doppio di test: registra le chiamate e permette di simulare la fine del brano. */
    private static final class FakeAudioPlayer implements AudioPlayer {
        private String lastPlayedPath;
        private int playCount;
        private int stopCount;
        private Runnable onEnd;

        @Override
        public void play(String filePath) {
            lastPlayedPath = filePath;
            playCount++;
        }

        @Override
        public void stop() {
            stopCount++;
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
    void stop_stopsPlaybackAndDelegates() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        service.play(trackWithFile());

        service.stop();

        assertEquals(PlaybackStatus.STOPPED, service.getPlaybackState().getStatus());
        assertEquals(1, audio.stopCount);
    }

    @Test
    void endOfMedia_returnsToStoppedState() {
        FakeAudioPlayer audio = new FakeAudioPlayer();
        PlayerService service = new PlayerService(audio);
        service.play(trackWithFile());

        audio.simulateEndOfMedia(); // il brano finisce da solo (Scenario 2)

        assertEquals(PlaybackStatus.STOPPED, service.getPlaybackState().getStatus());
        assertNull(service.getPlaybackState().getCurrentTrack());
    }
}
