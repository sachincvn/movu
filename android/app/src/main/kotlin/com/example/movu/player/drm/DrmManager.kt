package com.example.movu.player.drm

import android.util.Base64
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import org.json.JSONArray
import org.json.JSONObject

class DrmManager {
    fun configureDrm(mediaItemBuilder: MediaItem.Builder, drmScheme: String?, drmLicenseUrl: String?, licenseKeys: List<String>?) {
        if (drmScheme != null) {
            val drmUuid = when (drmScheme) {
                "widevine" -> C.WIDEVINE_UUID
                "clearkey" -> C.CLEARKEY_UUID
                else -> null
            }

            if (drmUuid != null) {
                val mediaItemDrmConfiguration = MediaItem.DrmConfiguration.Builder(drmUuid)

                when (drmScheme) {
                    "widevine" -> {
                        if (drmLicenseUrl != null) {
                            mediaItemDrmConfiguration.setLicenseUri(drmLicenseUrl)
                            // Add required headers for Widevine license requests
                            val requestHeaders = mapOf(
                                "Content-Type" to "application/octet-stream",
                                "User-Agent" to "ExoPlayer/1.3.1"
                            )
                            mediaItemDrmConfiguration.setLicenseRequestHeaders(requestHeaders)
                        }
                    }
                    "clearkey" -> {
                        if (drmLicenseUrl != null) {
                            mediaItemDrmConfiguration.setLicenseUri(drmLicenseUrl)
                        } else if (licenseKeys != null) {
                            val licenseUri = createClearKeyLicenseUri(licenseKeys)
                            mediaItemDrmConfiguration.setLicenseUri(licenseUri)
                        }
                    }
                }

                // Enable multi-session for better DRM handling
                mediaItemDrmConfiguration.setMultiSession(true)

                mediaItemBuilder.setDrmConfiguration(mediaItemDrmConfiguration.build())
            }
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
