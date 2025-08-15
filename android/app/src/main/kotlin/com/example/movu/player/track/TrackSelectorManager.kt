package com.example.movu.player.track

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@OptIn(UnstableApi::class)
class TrackSelectorManager(context: Context) {
    val trackSelector = DefaultTrackSelector(context)
    
    /**
     * Get available video tracks from the current player
     */
    fun getVideoTracks(player: ExoPlayer): List<Map<String, Any>> {
        val videoTracks = mutableListOf<Map<String, Any>>()
        val tracks = player.currentTracks
        
        for (trackGroup in tracks.groups) {
            if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getTrackFormat(i)
                    videoTracks.add(mapOf(
                        "index" to i,
                        "width" to format.width,
                        "height" to format.height,
                        "bitrate" to format.bitrate,
                        "frameRate" to format.frameRate,
                        "codecs" to (format.codecs ?: "unknown")
                    ))
                }
            }
        }
        return videoTracks
    }
    
    /**
     * Set a specific video track by index
     */
    fun setVideoTrack(player: ExoPlayer, trackIndex: Int): Boolean {
        val videoTrackGroup = player.currentTracks.groups.firstOrNull { 
            it.type == C.TRACK_TYPE_VIDEO 
        }
        
        return if (videoTrackGroup != null && trackIndex < videoTrackGroup.length) {
            val trackOverride = TrackSelectionOverride(
                videoTrackGroup.mediaTrackGroup, 
                listOf(trackIndex)
            )
            trackSelector.parameters = trackSelector.parameters
                .buildUpon()
                .addOverride(trackOverride)
                .build()
            true
        } else {
            false
        }
    }
    
    /**
     * Clear all track overrides (auto selection)
     */
    fun clearTrackOverrides() {
        trackSelector.parameters = trackSelector.parameters
            .buildUpon()
            .clearOverrides()
            .build()
    }
    
    /**
     * Get available audio tracks
     */
    fun getAudioTracks(player: ExoPlayer): List<Map<String, Any>> {
        val audioTracks = mutableListOf<Map<String, Any>>()
        val tracks = player.currentTracks
        
        for (trackGroup in tracks.groups) {
            if (trackGroup.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getTrackFormat(i)
                    audioTracks.add(mapOf(
                        "index" to i,
                        "language" to (format.language ?: "unknown"),
                        "bitrate" to format.bitrate,
                        "sampleRate" to format.sampleRate,
                        "channelCount" to format.channelCount
                    ))
                }
            }
        }
        return audioTracks
    }
    
    /**
     * Set a specific audio track by index
     */
    fun setAudioTrack(player: ExoPlayer, trackIndex: Int): Boolean {
        val audioTrackGroup = player.currentTracks.groups.firstOrNull { 
            it.type == C.TRACK_TYPE_AUDIO 
        }
        
        return if (audioTrackGroup != null && trackIndex < audioTrackGroup.length) {
            val trackOverride = TrackSelectionOverride(
                audioTrackGroup.mediaTrackGroup, 
                listOf(trackIndex)
            )
            trackSelector.parameters = trackSelector.parameters
                .buildUpon()
                .addOverride(trackOverride)
                .build()
            true
        } else {
            false
        }
    }
}
