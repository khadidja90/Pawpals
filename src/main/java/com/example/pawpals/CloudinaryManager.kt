// CloudinaryManager.kt
package com.example.pawpals

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {

    fun init(context: Context) {
        val config: HashMap<String, Any> = HashMap()
        config["cloud_name"] = "dmiizt4qx" // remplace par le tien
        config["api_key"] = "591781556162275"       // remplace par le tien
        config["api_secret"] = "N1AO3YOPoabrxjsQCneVC49Pewk" // remplace par le tien
        config["secure"] = "true"
        config["use_scheduled_upload"] = false
        MediaManager.init(context, config)
    }
}
