import 'package:flutter/material.dart';
import 'package:movu/core/models/video_stream.dart';
import 'package:movu/core/services/video_player_service.dart';
import 'package:movu/video_player/video_player_widget.dart';

class VideoPlayerScreen extends StatefulWidget {
  final VideoStream stream;

  const VideoPlayerScreen({super.key, required this.stream});

  @override
  State<VideoPlayerScreen> createState() => _VideoPlayerScreenState();
}

class _VideoPlayerScreenState extends State<VideoPlayerScreen> {
  late final VideoPlayerService _videoPlayerService;

  @override
  void initState() {
    super.initState();
    _videoPlayerService = VideoPlayerService();
    _videoPlayerService.initializeController(widget.stream);
  }

  @override
  void dispose() {
    _videoPlayerService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.stream.name)),
      body: VideoPlayer(controller: _videoPlayerService.controller),
    );
  }
}
