package com.example.movu.player.datasource

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.MediaSource

interface MediaSourceHandler {
    fun createMediaSource(
        context: Context,
        mediaItem: MediaItem,
        httpDataSourceFactory: HttpDataSource.Factory
    ): MediaSource
}
