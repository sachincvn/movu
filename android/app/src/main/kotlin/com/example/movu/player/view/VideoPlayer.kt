package com.example.movu.player.view

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.movu.player.datasource.MediaSourceFactoryProvider
import com.example.movu.player.drm.DrmManager
import com.example.movu.player.track.TrackSelectorManager
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

@OptIn(UnstableApi::class)
class VideoPlayer(private val context: Context, id: Int, creationParams: Map<String?, Any?>?, messenger: BinaryMessenger) : PlatformView, MethodChannel.MethodCallHandler {
    private val playerView: PlayerView = PlayerView(context)
    private val trackSelectorManager = TrackSelectorManager(context)
    private val exoPlayer: ExoPlayer
    private val methodChannel: MethodChannel
    private val handler = Handler(Looper.getMainLooper())
    private val drmManager = DrmManager()

    init {
        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelectorManager.trackSelector)
            .build()
        playerView.player = exoPlayer
        playerView.useController = false // Disable default controls

        // Configure player view for DRM content with secure surface
        playerView.setUseController(false)

        // Set black background for the player view
        playerView.setBackgroundColor(Color.BLACK)

        // Enable secure rendering for DRM content
        configureSecureSurface()

        methodChannel = MethodChannel(messenger, "movu/video_player_" + id)
        methodChannel.setMethodCallHandler(this)

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                methodChannel.invokeMethod("onPlayerError", mapOf("errorCode" to error.errorCode, "errorMessage" to error.message))
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                methodChannel.invokeMethod("onIsPlayingChanged", isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                methodChannel.invokeMethod("onIsBufferingChanged", isBuffering)
                if (playbackState == Player.STATE_READY) {
                    methodChannel.invokeMethod("onDuration", exoPlayer.duration)
                }
            }

            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                val videoTracks = trackSelectorManager.getVideoTracks(exoPlayer)
                methodChannel.invokeMethod("onTracks", videoTracks)
            }
        })

        handler.post(object : Runnable {
            override fun run() {
                if (exoPlayer.isPlaying) {
                    methodChannel.invokeMethod("onPosition", exoPlayer.currentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun getView() = playerView

    override fun dispose() {
        exoPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    private fun configureSecureSurface() {
        try {
            // Configure surface view for secure content
            val videoSurfaceView = playerView.videoSurfaceView
            if (videoSurfaceView is SurfaceView) {
                // Enable secure surface for DRM content
                videoSurfaceView.setSecure(true)
                android.util.Log.d("VideoPlayer", "Secure surface configured for DRM content")
            } else {
                android.util.Log.w("VideoPlayer", "VideoSurfaceView is not a SurfaceView, secure surface not configured")
            }
        } catch (e: Exception) {
            // Log error but don't crash - some devices may not support secure surfaces
            android.util.Log.w("VideoPlayer", "Failed to configure secure surface: ${e.message}")
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "initialize" -> {
                val args = call.arguments as Map<*, *>
                val url = args["url"] as String
                val headers = args["headers"] as? Map<String, String>
                val drmScheme = args["drm_scheme"] as? String
                val drmLicenseUrl = args["drm_license_url"] as? String
                val licenseKeys = args["drm_license_keys"] as? List<String>

                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                if (headers != null) {
                    httpDataSourceFactory.setDefaultRequestProperties(headers)
                }

                android.util.Log.d("VideoPlayer", "Initializing with URL: $url")
                android.util.Log.d("VideoPlayer", "DRM Scheme: $drmScheme")
                android.util.Log.d("VideoPlayer", "DRM License URL: $drmLicenseUrl")
                android.util.Log.d("VideoPlayer", "License Keys: $licenseKeys")

                val drmSessionManager = drmManager.buildDrmSessionManager(context, drmScheme, drmLicenseUrl, httpDataSourceFactory, licenseKeys)
                android.util.Log.d("VideoPlayer", "DRM Session Manager created: ${drmSessionManager != null}")

                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .build()

                val mediaSourceFactory = MediaSourceFactoryProvider.getFactory(url)
                val mediaSource = mediaSourceFactory.createMediaSource(context, mediaItem, httpDataSourceFactory, drmSessionManager)

                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.play()
                result.success(null)
            }
            "play" -> {
                exoPlayer.play()
                result.success(null)
            }
            "pause" -> {
                exoPlayer.pause()
                result.success(null)
            }
            "seekTo" -> {
                val position = call.argument<Int>("position")
                if (position != null) {
                    exoPlayer.seekTo(position.toLong())
                }
                result.success(null)
            }
            "setVolume" -> {
                val volume = call.argument<Double>("volume")
                if (volume != null) {
                    exoPlayer.volume = volume.toFloat()
                }
                result.success(null)
            }
            "setPlaybackSpeed" -> {
                val speed = call.argument<Double>("speed")?.toFloat() ?: 1.0f
                exoPlayer.setPlaybackSpeed(speed)
                result.success(null)
            }
            "setTrack" -> {
                val trackIndex = call.argument<Int>("trackIndex")
                if (trackIndex != null) {
                    val success = trackSelectorManager.setVideoTrack(exoPlayer, trackIndex)
                    result.success(success)
                } else {
                    result.success(false)
                }
            }
            "setAudioTrack" -> {
                val trackIndex = call.argument<Int>("trackIndex")
                if (trackIndex != null) {
                    val success = trackSelectorManager.setAudioTrack(exoPlayer, trackIndex)
                    result.success(success)
                } else {
                    result.success(false)
                }
            }
            "getAudioTracks" -> {
                val audioTracks = trackSelectorManager.getAudioTracks(exoPlayer)
                result.success(audioTracks)
            }
            "clearTrackOverrides" -> {
                trackSelectorManager.clearTrackOverrides()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }
}
