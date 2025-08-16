import 'package:flutter/services.dart';
import 'video_player_config.dart';
import 'video_player_controller.dart';

class NativeVideoPlayerController extends VideoPlayerController {
  MethodChannel? _channel;
  final String _viewType = 'movu/video_player';

  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  bool _isPlaying = false;
  bool _isBuffering = false;
  String? _errorMessage;
  List<Map<String, dynamic>> _videoTracks = [];
  late VideoPlayerConfig _config;

  @override
  Future<void> initialize(VideoPlayerConfig config) async {
    _config = config;
  }

  void onPlatformViewCreated(int id) {
    _channel = MethodChannel('movu/video_player_$id');
    _channel!.setMethodCallHandler(_onMethodCall);
    _channel!.invokeMethod('initialize', _config.toMap());
  }

  Future<void> _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onDuration':
        _duration = Duration(milliseconds: call.arguments as int);
        break;
      case 'onPosition':
        _position = Duration(milliseconds: call.arguments as int);
        break;
      case 'onIsPlayingChanged':
        _isPlaying = call.arguments as bool;
        break;
      case 'onIsBufferingChanged':
        _isBuffering = call.arguments as bool;
        break;
      case 'onPlayerError':
        final error = call.arguments as Map<dynamic, dynamic>;
        _errorMessage = error['errorMessage'] as String?;
        break;
      case 'onTracks':
        _videoTracks = (call.arguments as List).map((track) => Map<String, dynamic>.from(track as Map)).toList();
        break;
      default:
        // Handle unknown method
        return;
    }
    notifyListeners();
  }

  @override
  Future<void> play() => _channel?.invokeMethod('play') ?? Future.value();

  @override
  Future<void> pause() => _channel?.invokeMethod('pause') ?? Future.value();

  Future<void> stop() => _channel?.invokeMethod('stop') ?? Future.value();

  @override
  Future<void> seekTo(Duration position) => _channel?.invokeMethod('seekTo', {'position': position.inMilliseconds}) ?? Future.value();

  @override
  Future<void> setSpeed(double speed) => _channel?.invokeMethod('setPlaybackSpeed', {'speed': speed}) ?? Future.value();

  @override
  Future<void> setTrack(int trackIndex) => _channel?.invokeMethod('setTrack', {'trackIndex': trackIndex}) ?? Future.value();

  @override
  Duration get duration => _duration;

  @override
  Duration get position => _position;

  @override
  bool get isPlaying => _isPlaying;

  @override
  bool get isBuffering => _isBuffering;

  @override
  String? get errorMessage => _errorMessage;

  @override
  List<Map<String, dynamic>> get videoTracks => _videoTracks;

  String get viewType => _viewType;

  VideoPlayerConfig get config => _config;
}
