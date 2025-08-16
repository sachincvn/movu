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
              Navigator.push(context, MaterialPageRoute(builder: (context) => VideoPlayerScreen(stream: stream)));
            },
          );
        },
      ),
    );
  }
}
