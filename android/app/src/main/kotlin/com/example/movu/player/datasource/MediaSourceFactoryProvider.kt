package com.example.movu.player.datasource

import android.net.Uri
import com.example.movu.player.datasource.DashHandler
import com.example.movu.player.datasource.DefaultHandler
import com.example.movu.player.datasource.HlsHandler
import com.example.movu.player.datasource.MediaSourceHandler

object MediaSourceFactoryProvider {
    fun getFactory(url: String): MediaSourceHandler {
        val uri = Uri.parse(url)
        val path = uri.path?.lowercase()

        return when {
            path?.endsWith(".mpd") == true -> DashHandler()
            path?.endsWith(".m3u8") == true -> HlsHandler()
            else -> DefaultHandler()
        }
    }
}
