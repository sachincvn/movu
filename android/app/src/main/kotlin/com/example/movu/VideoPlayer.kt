package com.example.movu

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import io.flutter.plugin.common.BinaryMessenger
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import org.json.JSONArray
import org.json.JSONObject
import android.util.Base64
import androidx.media3.common.util.Util
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class VideoPlayer(private val context: Context, id: Int, creationParams: Map<String?, Any?>?, messenger: BinaryMessenger) : PlatformView, MethodChannel.MethodCallHandler {
    private val playerView: PlayerView = PlayerView(context)
    private val trackSelector = DefaultTrackSelector(context)
    private val exoPlayer: ExoPlayer
    private val methodChannel: MethodChannel
    private val handler = Handler(Looper.getMainLooper())

    init {
        // 1. Create ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
        playerView.player = exoPlayer
        playerView.useController = false

        // 2. Set up MethodChannel
        methodChannel = MethodChannel(messenger, "video_player_$id")
        methodChannel.setMethodCallHandler(this)

        // 3. Build MediaItem
        val url = creationParams?.get("url") as? String
        if (url != null) {
            val mediaItemBuilder = MediaItem.Builder().setUri(url)

            // 3a. Configure MediaSource with custom headers
            val headers = creationParams["headers"] as? Map<String, String>
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            if (headers != null) {
                httpDataSourceFactory.setDefaultRequestProperties(headers)
            }

            // 3b. Configure DRM
            val drmScheme = creationParams["drm_scheme"] as? String
            if (drmScheme != null) {
                val drmUuid = when (drmScheme) {
                    "widevine" -> C.WIDEVINE_UUID
                    "clearkey" -> C.CLEARKEY_UUID
                    else -> null
                }
                if (drmUuid != null) {
                    val drmConfigBuilder = MediaItem.DrmConfiguration.Builder(drmUuid)
                    val licenseKeys = creationParams["drm_license_keys"] as? List<String>
                    if (licenseKeys != null && licenseKeys.isNotEmpty()) {
                        val keyResponse = createClearKeyKeyResponse(licenseKeys)
                        drmConfigBuilder.setLicenseRequestHeaders(mapOf("X-AxDRM-Message" to String(keyResponse)))
                    }
                    mediaItemBuilder.setDrmConfiguration(drmConfigBuilder.build())
                }
            }

            // 4. Set MediaItem and prepare player
            val mediaSource = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(httpDataSourceFactory)
                .createMediaSource(mediaItemBuilder.build())
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
        }

        // 4. Set up player event listener
        setupPlayerListener()
        startPositionUpdates()
    }

    override fun getView(): View = playerView

    override fun dispose() {
        exoPlayer.release()
        handler.removeCallbacksAndMessages(null)
        methodChannel.setMethodCallHandler(null)
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                methodChannel.invokeMethod("onBuffering", playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    methodChannel.invokeMethod("onDuration", exoPlayer.duration)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                methodChannel.invokeMethod("onIsPlaying", isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                methodChannel.invokeMethod("onError", error.message)
            }

            override fun onTracksChanged(tracks: Tracks) {
                val videoTracks = mutableListOf<Map<String, Any>>()
                for (trackGroup in tracks.groups) {
                    if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until trackGroup.length) {
                            val format = trackGroup.getTrackFormat(i)
                            videoTracks.add(mapOf("width" to format.width, "height" to format.height, "bitrate" to format.bitrate))
                        }
                    }
                }
                methodChannel.invokeMethod("onTracks", videoTracks)
            }
        })
    }

    private fun startPositionUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                if (exoPlayer.playbackState != Player.STATE_IDLE) {
                    methodChannel.invokeMethod("onPosition", exoPlayer.currentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun createClearKeyKeyResponse(keys: List<String>): ByteArray {
        val keysArray = JSONArray()
        for (key in keys) {
            val parts = key.split(":")
            if (parts.size == 2) {
                val kidHex = parts[0]
                val keyHex = parts[1]
                val kidBytes = Util.getBytesFromHexString(kidHex)
                val keyBytes = Util.getBytesFromHexString(keyHex)
                val kidBase64 = Base64.encodeToString(kidBytes, Base64.NO_PADDING or Base64.NO_WRAP)
                val keyBase64 = Base64.encodeToString(keyBytes, Base64.NO_PADDING or Base64.NO_WRAP)
                val keyObject = JSONObject()
                keyObject.put("kty", "oct")
                keyObject.put("k", keyBase64)
                keyObject.put("kid", kidBase64)
                keysArray.put(keyObject)
            }
        }
        val finalJsonObject = JSONObject()
        finalJsonObject.put("keys", keysArray)
        return finalJsonObject.toString().toByteArray()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "play" -> exoPlayer.play()
            "pause" -> exoPlayer.pause()
            "seekTo" -> {
                val position = call.argument<Int>("position")
                exoPlayer.seekTo(position?.toLong() ?: 0L)
            }
            "setTrack" -> {
                val bitrate = call.argument<Int>("bitrate")
                if (bitrate != null) {
                    trackSelector.parameters = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(bitrate)
                        .build()
                }
            }
            "setSpeed" -> {
                val speed = call.argument<Double>("speed")
                if (speed != null) {
                    exoPlayer.setPlaybackSpeed(speed.toFloat())
                }
            }
            else -> {
                result.notImplemented()
                return
            }
        }
        result.success(null)
    }
}
