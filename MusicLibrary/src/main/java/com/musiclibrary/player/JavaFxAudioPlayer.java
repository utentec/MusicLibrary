package com.musiclibrary.player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * Implementazione di {@link AudioPlayer} basata sul MediaPlayer di JavaFX:
 * riproduce realmente i file audio. Non è coperta da test automatici perché
 * richiede il toolkit JavaFX e un file reale, e va verificata manualmente;
 * la logica testabile vive invece in {@link PlayerService}.
 */
public class JavaFxAudioPlayer implements AudioPlayer {

    private MediaPlayer mediaPlayer;
    private Runnable onEndOfMedia;

    @Override
    public void play(String filePath) {
        stop(); // ferma e libera l'eventuale riproduzione precedente
        Media media = new Media(new File(filePath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        if (onEndOfMedia != null) {
            mediaPlayer.setOnEndOfMedia(onEndOfMedia);
        }
        mediaPlayer.play();
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    @Override
    public void setOnEndOfMedia(Runnable callback) {
        this.onEndOfMedia = callback;
    }
}
