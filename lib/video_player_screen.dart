import 'package:flutter/material.dart';
import 'package:flutter/material.dart';
import 'package:movu/video_player/native_video_player_controller.dart';
import 'package:movu/video_player/video_player_config.dart';
import 'package:movu/video_player/video_player_widget.dart';
import 'package:movu/video_stream.dart';

class VideoPlayerScreen extends StatefulWidget {
  final VideoStream stream;

  const VideoPlayerScreen({super.key, required this.stream});

  @override
  State<VideoPlayerScreen> createState() => _VideoPlayerScreenState();
}

class _VideoPlayerScreenState extends State<VideoPlayerScreen> {
  late final NativeVideoPlayerController _controller;

  @override
  void initState() {
    super.initState();
    _controller = NativeVideoPlayerController();

    final Map<String, String> headers = {};
    if (widget.stream.userAgent != null) {
      headers['User-Agent'] = widget.stream.userAgent!;
    }
    if (widget.stream.cookie != null) {
      headers['Cookie'] = widget.stream.cookie!;
    }
    if (widget.stream.referer != null) {
      headers['Referer'] = widget.stream.referer!;
    }
    if (widget.stream.origin != null) {
      headers['Origin'] = widget.stream.origin!;
    }

    DrmConfig? drmConfig;
    if (widget.stream.drmScheme != null) {
      if (widget.stream.drmScheme == 'clearkey' && widget.stream.drmLicense != null) {
        drmConfig = DrmConfig(
          scheme: 'clearkey',
          licenseKeys: [widget.stream.drmLicense!],
        );
      } else if (widget.stream.drmScheme == 'widevine' && widget.stream.drmLicense != null) {
        drmConfig = DrmConfig(
          scheme: 'widevine',
          licenseUrl: widget.stream.drmLicense!,
        );
      }
    }

    final config = VideoPlayerConfig(
      url: widget.stream.link,
      headers: headers.isNotEmpty ? headers : null,
      drmConfig: drmConfig,
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
        title: Text(widget.stream.name),
      ),
      body: VideoPlayer(controller: _controller),
    );
  }
}
