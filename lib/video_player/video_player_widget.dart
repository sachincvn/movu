import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'native_video_player_controller.dart';
import 'video_player_controller.dart';

class VideoPlayer extends StatefulWidget {
  final VideoPlayerController controller;

  const VideoPlayer({super.key, required this.controller});

  @override
  State<VideoPlayer> createState() => _VideoPlayerState();
}

class _VideoPlayerState extends State<VideoPlayer> {
  @override
  void initState() {
    super.initState();
    widget.controller.addListener(_onStateChanged);
  }

  @override
  void didUpdateWidget(covariant VideoPlayer oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.controller != oldWidget.controller) {
      oldWidget.controller.removeListener(_onStateChanged);
      widget.controller.addListener(_onStateChanged);
    }
  }

  @override
  void dispose() {
    widget.controller.removeListener(_onStateChanged);
    super.dispose();
  }

  void _onStateChanged() {
    setState(() {
      // The UI will rebuild whenever the controller notifies its listeners.
    });
  }

  @override
  Widget build(BuildContext context) {
    final nativeController = widget.controller as NativeVideoPlayerController;
    return Stack(
      alignment: Alignment.center,
      children: [
        AndroidView(
          viewType: 'movu/video_player',
          layoutDirection: TextDirection.ltr,
          creationParams: nativeController.config.toMap(),
          creationParamsCodec: const StandardMessageCodec(),
          onPlatformViewCreated: nativeController.onPlatformViewCreated,
        ),
        if (widget.controller.isBuffering)
          const CircularProgressIndicator(),
        if (widget.controller.errorMessage != null)
          Container(
            color: Colors.black.withOpacity(0.7),
            padding: const EdgeInsets.all(16),
            child: Text(
              widget.controller.errorMessage!,
              style: const TextStyle(color: Colors.white),
            ),
          ),
        Positioned(
          bottom: 0,
          left: 0,
          right: 0,
          child: _buildControls(),
        ),
      ],
    );
  }

  Widget _buildControls() {
    return Container(
      padding: const EdgeInsets.all(8.0),
      color: Colors.black.withOpacity(0.5),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Text(widget.controller.position.toString().split('.').first.padLeft(8, '0')),
              Expanded(
                child: Slider(
                  value: widget.controller.position.inSeconds.toDouble().clamp(0.0, widget.controller.duration.inSeconds.toDouble()),
                  min: 0.0,
                  max: widget.controller.duration.inSeconds.toDouble() > 0 ? widget.controller.duration.inSeconds.toDouble() : 1.0,
                  onChanged: (value) {
                    widget.controller.seekTo(Duration(seconds: value.toInt()));
                  },
                ),
              ),
              Text(widget.controller.duration.toString().split('.').first.padLeft(8, '0')),
            ],
          ),
          Row(
            children: [
              Expanded(
                child: IconButton(
                  icon: Icon(widget.controller.isPlaying ? Icons.pause : Icons.play_arrow,color: Colors.white),
                  onPressed: () => widget.controller.isPlaying
                      ? widget.controller.pause()
                      : widget.controller.play(),
                ),
              ),
              Expanded(
                child: PopupMenuButton<int>(
                  onSelected: (int index) => widget.controller.setTrack(index),
                  itemBuilder: (BuildContext context) {
                    return widget.controller.videoTracks.asMap().entries.map((entry) {
                      final index = entry.key;
                      final track = entry.value;
                      return PopupMenuItem<int>(
                        value: index,
                        child: Text('${track['height']}p'),
                      );
                    }).toList();
                  },
                  child: const Icon(Icons.settings, color: Colors.white),
                ),
              ),
              Expanded(
                child: PopupMenuButton<double>(
                  onSelected: (double speed) => widget.controller.setSpeed(speed),
                  itemBuilder: (BuildContext context) {
                    return [0.5, 1.0, 1.5, 2.0].map((speed) {
                      return PopupMenuItem<double>(
                        value: speed,
                        child: Text('${speed}x'),
                      );
                    }).toList();
                  },
                  child: const Icon(Icons.speed,color: Colors.white),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
