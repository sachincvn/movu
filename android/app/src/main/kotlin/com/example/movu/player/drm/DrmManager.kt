package com.example.movu.player.drm

import android.util.Base64
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.util.Util
import org.json.JSONArray
import org.json.JSONObject

import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import android.content.Context

class DrmManager {
    fun buildDrmSessionManager(
        context: Context,
        drmScheme: String?,
        drmLicenseUrl: String?,
        httpDataSourceFactory: HttpDataSource.Factory
    ): DrmSessionManager? {
        if (drmScheme == null || drmLicenseUrl == null) {
            return null
        }

        val drmUuid = when (drmScheme) {
            "widevine" -> C.WIDEVINE_UUID
            else -> null
        }

        return if (drmUuid != null) {
            val mediaDrmCallback = HttpMediaDrmCallback(drmLicenseUrl, httpDataSourceFactory)
            DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(drmUuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(mediaDrmCallback)
        } else {
            null
        }
    }

    private fun createClearKeyLicenseUri(licenseKeys: List<String>): String {
        val keysList = mutableListOf<ByteArray>()
        for (key in licenseKeys) {
            val keyParts = key.split(":")
            if (keyParts.size == 2) {
                // Convert hex strings to byte arrays for ClearKey
                val keyId = hexStringToByteArray(keyParts[0])
                val keyValue = hexStringToByteArray(keyParts[1])
                keysList.add(keyId)
                keysList.add(keyValue)
            }
        }

        val jsonObject = JSONObject()
        val keysArray = JSONArray()
        for (i in keysList.indices step 2) {
            val keyId = Base64.encodeToString(keysList[i], Base64.NO_WRAP or Base64.URL_SAFE)
            val keyValue = Base64.encodeToString(keysList[i + 1], Base64.NO_WRAP or Base64.URL_SAFE)
            val keyObject = JSONObject()
            keyObject.put("kty", "oct")
            keyObject.put("k", keyValue)
            keyObject.put("kid", keyId)
            keysArray.put(keyObject)
        }
        jsonObject.put("keys", keysArray)

        val jsonData = jsonObject.toString()
        val base64Data = Base64.encodeToString(Util.getUtf8Bytes(jsonData), Base64.NO_WRAP)
        return "data:application/json;base64,$base64Data"
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
