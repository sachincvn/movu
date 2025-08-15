package com.example.movu

import com.example.movu.player.view.VideoPlayerFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("movu/video_player", VideoPlayerFactory(flutterEngine.dartExecutor.binaryMessenger))
    }
}
