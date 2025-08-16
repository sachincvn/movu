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
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import android.content.Context

class DrmManager {
    fun buildDrmSessionManager(
        context: Context,
        drmScheme: String?,
        drmLicenseUrl: String?,
        httpDataSourceFactory: HttpDataSource.Factory,
        licenseKeys: List<String>? = null
    ): DrmSessionManager? {
        if (drmScheme == null) {
            return null
        }

        return when (drmScheme.lowercase()) {
            "widevine" -> {
                if (drmLicenseUrl == null) return null
                val mediaDrmCallback = HttpMediaDrmCallback(drmLicenseUrl, httpDataSourceFactory)
                DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .build(mediaDrmCallback)
            }
            "clearkey" -> {
                // Handle both licenseKeys list and single drmLicenseUrl for ClearKey
                val keys = when {
                    !licenseKeys.isNullOrEmpty() -> licenseKeys
                    !drmLicenseUrl.isNullOrEmpty() -> listOf(drmLicenseUrl)
                    else -> return null
                }
                val clearKeyLicenseJson = createClearKeyLicenseUri(keys)
                val localMediaDrmCallback = LocalMediaDrmCallback(clearKeyLicenseJson.toByteArray(Charsets.UTF_8))
                DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .build(localMediaDrmCallback)
            }
            else -> null
        }
    }

    private fun createClearKeyLicenseUri(licenseKeys: List<String>): String {
        return try {
            val key = licenseKeys.first()
            val keyParts = key.split(":")
            if (keyParts.size == 2) {
                val keyId = keyParts[0]
                val keyValue = keyParts[1]

                android.util.Log.d("DrmManager", "Processing ClearKey: $keyId -> $keyValue")

                // Convert hex strings to byte arrays
                val keyIdBytes = hexStringToByteArray(keyId)
                val keyValueBytes = hexStringToByteArray(keyValue)

                // Create base64url encoded strings (no padding)
                val keyIdBase64 = Base64.encodeToString(keyIdBytes, Base64.NO_WRAP or Base64.URL_SAFE).replace("=", "")
                val keyValueBase64 = Base64.encodeToString(keyValueBytes, Base64.NO_WRAP or Base64.URL_SAFE).replace("=", "")

                // Create the ClearKey JSON format
                val jsonObject = JSONObject()
                val keysArray = JSONArray()
                val keyObject = JSONObject()

                keyObject.put("kty", "oct")
                keyObject.put("k", keyValueBase64)
                keyObject.put("kid", keyIdBase64)
                keysArray.put(keyObject)

                jsonObject.put("keys", keysArray)
                jsonObject.put("type", "temporary")

                val jsonData = jsonObject.toString()
                android.util.Log.d("DrmManager", "ClearKey JSON: $jsonData")

                jsonData
            } else {
                android.util.Log.e("DrmManager", "Invalid ClearKey format: $key")
                ""
            }
        } catch (e: Exception) {
            android.util.Log.e("DrmManager", "Error creating ClearKey license", e)
            ""
        }
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
