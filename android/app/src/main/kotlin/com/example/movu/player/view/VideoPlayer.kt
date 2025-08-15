package com.example.movu.player.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverrides
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.example.movu.player.datasource.MediaSourceFactoryProvider
import com.example.movu.player.drm.DrmManager
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

@OptIn(UnstableApi::class)
class VideoPlayer(private val context: Context, id: Int, creationParams: Map<String?, Any?>?, messenger: BinaryMessenger) : PlatformView, MethodChannel.MethodCallHandler {
    private val playerView: PlayerView = PlayerView(context)
    private val trackSelector = DefaultTrackSelector(context)
    private val exoPlayer: ExoPlayer
    private val methodChannel: MethodChannel
    private val handler = Handler(Looper.getMainLooper())
    private val drmManager = DrmManager()

    init {
        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
        playerView.player = exoPlayer
        playerView.useController = false // Disable default controls
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
                val videoTracks = mutableListOf<Map<String, Any>>()
                for (trackGroup in tracks.groups) {
                    if (trackGroup.type == androidx.media3.common.C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until trackGroup.length) {
                            val format = trackGroup.getTrackFormat(i)
                            videoTracks.add(mapOf(
                                "width" to format.width,
                                "height" to format.height,
                                "bitrate" to format.bitrate
                            ))
                        }
                    }
                }
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

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "initialize" -> {
                val args = call.arguments as Map<*, *>
                val url = args["url"] as String
                val headers = args["headers"] as? Map<String, String>
                val drmScheme = args["drmScheme"] as? String
                val drmLicenseUrl = args["drmLicenseUrl"] as? String
                val licenseKeys = args["licenseKeys"] as? List<String>

                val mediaItemBuilder = MediaItem.Builder().setUri(url)
                drmManager.configureDrm(mediaItemBuilder, drmScheme, drmLicenseUrl, licenseKeys)
                val mediaItem = mediaItemBuilder.build()

                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                if (headers != null) {
                    httpDataSourceFactory.setDefaultRequestProperties(headers)
                }

                val mediaSourceFactory = MediaSourceFactoryProvider.getFactory(url)
                val mediaSource = mediaSourceFactory.createMediaSource(context, mediaItem, httpDataSourceFactory)

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
                    val videoTrackGroup = exoPlayer.currentTracks.groups.firstOrNull { it.type == androidx.media3.common.C.TRACK_TYPE_VIDEO }
                    if (videoTrackGroup != null) {
                        val trackOverride = TrackSelectionOverrides.TrackSelectionOverride(videoTrackGroup.mediaTrackGroup, listOf(trackIndex))
                        trackSelector.parameters = trackSelector.parameters
                            .buildUpon()
                            .setTrackSelectionOverrides(TrackSelectionOverrides.Builder().setOverrideForType(trackOverride).build())
                            .build()
                    }
                }
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }
}
