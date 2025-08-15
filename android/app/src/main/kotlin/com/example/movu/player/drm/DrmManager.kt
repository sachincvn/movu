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

                if (drmLicenseUrl != null) {
                    mediaItemDrmConfiguration.setLicenseUri(drmLicenseUrl)
                } else if (drmScheme == "clearkey" && licenseKeys != null) {
                    val licenseUri = createClearKeyLicenseUri(licenseKeys)
                    mediaItemDrmConfiguration.setLicenseUri(licenseUri)
                }

                mediaItemBuilder.setDrmConfiguration(mediaItemDrmConfiguration.build())
            }
        }
    }

    private fun createClearKeyLicenseUri(licenseKeys: List<String>): String {
        val keysList = mutableListOf<ByteArray>()
        for (key in licenseKeys) {
            val keyParts = key.split(":")
            if (keyParts.size == 2) {
                val keyId = Util.getUtf8Bytes(keyParts[0])
                val keyValue = Util.getUtf8Bytes(keyParts[1])
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
}
