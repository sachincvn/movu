import 'package:flutter/services.dart';
import 'video_player_config.dart';
import 'video_player_controller.dart';

class NativeVideoPlayerController extends VideoPlayerController {
  late final MethodChannel _channel;
  final String _viewType = 'video_player';

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
    _channel = MethodChannel('video_player_$id');
    _channel.setMethodCallHandler(_onMethodCall);
  }

  Future<void> _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onDuration':
        _duration = Duration(milliseconds: call.arguments as int);
        break;
      case 'onPosition':
        _position = Duration(milliseconds: call.arguments as int);
        break;
      case 'onIsPlaying':
        _isPlaying = call.arguments as bool;
        break;
      case 'onBuffering':
        _isBuffering = call.arguments as bool;
        break;
      case 'onError':
        _errorMessage = call.arguments as String?;
        break;
      case 'onTracks':
        _videoTracks = (call.arguments as List)
            .map((track) => Map<String, dynamic>.from(track))
            .toList();
        break;
      default:
        throw MissingPluginException();
    }
    notifyListeners();
  }

  @override
  Future<void> play() => _channel.invokeMethod('play');

  @override
  Future<void> pause() => _channel.invokeMethod('pause');

  @override
  Future<void> seekTo(Duration position) =>
      _channel.invokeMethod('seekTo', {'position': position.inMilliseconds});

  @override
  Future<void> setSpeed(double speed) =>
      _channel.invokeMethod('setSpeed', {'speed': speed});

  @override
  Future<void> setTrack(int bitrate) =>
      _channel.invokeMethod('setTrack', {'bitrate': bitrate});

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
