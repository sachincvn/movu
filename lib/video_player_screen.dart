import 'package:flutter/material.dart';
import 'package:movu/video_player/native_video_player_controller.dart';
import 'package:movu/video_player/video_player_config.dart';
import 'package:movu/video_player/video_player_widget.dart';

class VideoPlayerScreen extends StatefulWidget {
  const VideoPlayerScreen({super.key});

  @override
  State<VideoPlayerScreen> createState() => _VideoPlayerScreenState();
}

class _VideoPlayerScreenState extends State<VideoPlayerScreen> {
  late final NativeVideoPlayerController _controller;

  @override
  void initState() {
    super.initState();
    _controller = NativeVideoPlayerController();
    final config = VideoPlayerConfig(
      url: 'https://z5ak-cmaflive.zee5.com/cmaf/live/2105503/AAJTAKHDNEWSELE/index-connected.m3u8?hdntl=exp=1755310285~acl=%2f*~data=hdntl~hmac=fd17e707d7b12558a67b6d8b0ec9bacde5f2da103ff379a9089a3f4afaea89b2',
    );
    _controller.initialize(config);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Video Player'),
      ),
      body: VideoPlayer(controller: _controller),
    );
  }
}
