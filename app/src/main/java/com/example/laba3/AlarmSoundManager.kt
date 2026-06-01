package com.example.laba3

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager


object AlarmSoundManager {

    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context) {
        stop()
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}
