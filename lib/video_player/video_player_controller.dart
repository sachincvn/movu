import 'package:flutter/material.dart';
import 'package:movu/video_player/video_player_config.dart';

abstract class VideoPlayerController extends ChangeNotifier {
  Future<void> initialize(VideoPlayerConfig config);
  Future<void> play();
  Future<void> pause();
  Future<void> seekTo(Duration position);
  Future<void> setSpeed(double speed);
  Future<void> setTrack(int bitrate);

  Duration get duration;
  Duration get position;
  bool get isPlaying;
  bool get isBuffering;
  String? get errorMessage;
  List<Map<String, dynamic>> get videoTracks;
}
