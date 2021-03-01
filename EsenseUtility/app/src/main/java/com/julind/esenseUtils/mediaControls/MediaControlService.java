package com.julind.esenseUtils.mediaControls;

import android.media.AudioManager;
import android.view.KeyEvent;

public class MediaControlService {
    private static AudioManager audioManager = null;

    private static final int volumeStepChange = 5;


    private static final KeyEvent nextMediaKeyEventPressed = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
    private static final KeyEvent nextMediaKeyEventReleased = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT);

    private static final KeyEvent prevMediaKeyEventPressed = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    private static final KeyEvent prevMediaKeyEventReleased = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS);



    private static final KeyEvent pauseMediaKeyEventPressed = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
    private static final KeyEvent pauseMediaKeyEventReleased = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE);

    private static Boolean toggle;
    private static final KeyEvent playMediaKeyEventPressed = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
    private static final KeyEvent playMediaKeyEventReleased = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY);

    private MediaControlService() {
    }

    public static void setAudioManager(AudioManager audioManager) {
        if (MediaControlService.audioManager == null) {
            MediaControlService.audioManager = audioManager;
        }
    }

    public static void volumeUp() {
        for (int i = 0; i < MediaControlService.volumeStepChange; i++) {
            MediaControlService.audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }

    }

    public static void volumeDown() {
        for (int i = 0; i < MediaControlService.volumeStepChange; i++) {
            MediaControlService.audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        }
    }

    public static void nextMedia() {
        MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.nextMediaKeyEventPressed);
        MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.nextMediaKeyEventReleased);
    }

    public static void prevMedia() {
        MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.prevMediaKeyEventPressed);
        MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.prevMediaKeyEventReleased);
    }

    public static void playPause() {
        if (toggle == null) {
            toggle = audioManager.isMusicActive();
        } else {
            toggle = !toggle;
        }

        if (toggle) {
            MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.pauseMediaKeyEventPressed);
            MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.pauseMediaKeyEventReleased);
        } else {
            MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.playMediaKeyEventPressed);
            MediaControlService.audioManager.dispatchMediaKeyEvent(MediaControlService.playMediaKeyEventReleased);
        }
    }
}
