package com.example.movu.player.datasource

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource

class HlsHandler : MediaSourceHandler {
    override fun createMediaSource(
        context: Context,
        mediaItem: MediaItem,
        httpDataSourceFactory: HttpDataSource.Factory
    ): MediaSource {
        return HlsMediaSource.Factory(httpDataSourceFactory)
            .createMediaSource(mediaItem)
    }
}
