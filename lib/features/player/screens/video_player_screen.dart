import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:movu/core/models/video_stream.dart';
import 'package:movu/core/services/video_player_service.dart';
import 'package:movu/video_player/video_player_widget.dart';

class VideoPlayerScreen extends StatefulWidget {
  final VideoStream stream;

  const VideoPlayerScreen({super.key, required this.stream});

  @override
  State<VideoPlayerScreen> createState() => _VideoPlayerScreenState();
}

class _VideoPlayerScreenState extends State<VideoPlayerScreen> with WidgetsBindingObserver {
  VideoPlayerService? _videoPlayerService;
  bool _isInitialized = false;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    // Set preferred orientations for video playback
    SystemChrome.setPreferredOrientations([DeviceOrientation.landscapeLeft, DeviceOrientation.landscapeRight, DeviceOrientation.portraitUp]);

    // Initialize video player asynchronously to avoid blocking UI
    _initializeVideoPlayer();
  }

  Future<void> _initializeVideoPlayer() async {
    try {
      print('🎬 Starting video player initialization...');
      // Add a small delay to allow the UI to settle and navigation animation to complete
      await Future.delayed(const Duration(milliseconds: 150));

      _videoPlayerService = VideoPlayerService();
      print('🎬 VideoPlayerService created');

      _videoPlayerService!.initializeController(widget.stream);
      print('🎬 Controller initialized for stream: ${widget.stream.name}');

      if (mounted) {
        setState(() {
          _isInitialized = true;
          _isLoading = false;
        });
        print('🎬 Video player initialization completed successfully');
      }
    } catch (e) {
      print('❌ Video player initialization failed: $e');
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
        // Show error message
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to initialize video player: $e'), backgroundColor: Colors.red));
      }
    }
  }

  @override
  void deactivate() {
    // Just pause the video when deactivating, don't dispose yet
    print('🔄 Deactivate called - pausing video player');
    if (_videoPlayerService != null && _isInitialized) {
      _videoPlayerService!.controller.pause();
    }
    super.deactivate();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);

    // Dispose video player if not already disposed in deactivate
    if (_videoPlayerService != null) {
      _videoPlayerService!.dispose();
      _videoPlayerService = null;
    }

    // Reset orientation preferences
    SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp, DeviceOrientation.portraitDown, DeviceOrientation.landscapeLeft, DeviceOrientation.landscapeRight]);

    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if (_videoPlayerService != null) {
      switch (state) {
        case AppLifecycleState.paused:
          _videoPlayerService!.controller.pause();
          break;
        case AppLifecycleState.resumed:
          // Auto-resume can be added here if needed
          break;
        default:
          break;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(title: Text(widget.stream.name), backgroundColor: Colors.black, foregroundColor: Colors.white),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    print('🏗️ Building body - isLoading: $_isLoading, isInitialized: $_isInitialized, service: ${_videoPlayerService != null}');

    if (_isLoading) {
      print('🏗️ Showing loading screen');
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(color: Colors.white),
            SizedBox(height: 16),
            Text('Initializing Player...', style: TextStyle(color: Colors.white)),
          ],
        ),
      );
    }

    if (!_isInitialized || _videoPlayerService == null) {
      print('🏗️ Showing failed to initialize screen');
      return const Center(
        child: Text('Failed to initialize player', style: TextStyle(color: Colors.white)),
      );
    }

    print('🏗️ Showing VideoPlayer widget');
    return VideoPlayer(controller: _videoPlayerService!.controller);
  }
}
