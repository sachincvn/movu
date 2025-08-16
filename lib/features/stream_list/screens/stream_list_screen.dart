import 'package:flutter/material.dart';
import 'package:movu/core/utils/stream_data.dart';
import 'package:movu/features/player/screens/video_player_screen.dart';

class StreamListScreen extends StatelessWidget {
  const StreamListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Test Streams')),
      body: ListView.builder(
        itemCount: streams.length,
        itemBuilder: (context, index) {
          final stream = streams[index];
          return ListTile(
            title: Text(stream.name),
            onTap: () {
              // Use custom page route for better performance
              Navigator.push(
                context,
                PageRouteBuilder(
                  pageBuilder: (context, animation, secondaryAnimation) => VideoPlayerScreen(stream: stream),
                  transitionDuration: const Duration(milliseconds: 300),
                  reverseTransitionDuration: const Duration(milliseconds: 200),
                  transitionsBuilder: (context, animation, secondaryAnimation, child) {
                    // Fade transition for smoother navigation
                    return FadeTransition(opacity: animation, child: child);
                  },
                ),
              );
            },
          );
        },
      ),
    );
  }
}
