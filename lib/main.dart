import 'package:flutter/material.dart';
import 'package:movu/features/stream_list/screens/stream_list_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(title: 'Video Player Demo', home: const StreamListScreen());
  }
}
