package com.example.movu.player.datasource

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.source.MediaSource

class DashHandler : MediaSourceHandler {
    override fun createMediaSource(
        context: Context,
        mediaItem: MediaItem,
        httpDataSourceFactory: HttpDataSource.Factory,
        drmSessionManager: DrmSessionManager?
    ): MediaSource {
        val factory = DashMediaSource.Factory(httpDataSourceFactory)
        if (drmSessionManager != null) {
            factory.setDrmSessionManagerProvider { drmSessionManager }
        }
        return factory.createMediaSource(mediaItem)
    }
}
