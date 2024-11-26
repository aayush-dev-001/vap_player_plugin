import 'dart:io';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:vap_player_plugin/vap_player_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      debugShowCheckedModeBanner: false,
      home: const MyVapPlayerWidget(),
    );
  }
}

class MyVapPlayerWidget extends StatefulWidget {
  const MyVapPlayerWidget({super.key});

  @override
  State<MyVapPlayerWidget> createState() => _MyVapPlayerWidgetState();
}

class _MyVapPlayerWidgetState extends State<MyVapPlayerWidget> {
  String? localFilePath;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _downloadVapFile();
  }

  Future<void> _downloadVapFile() async {
    const String fileUrl =
        'https://bunnylive.in/entry/entryFiles/67095dfe1e607.mp4'; // Replace with your file URL
    try {
      // Get the directory for storing the file
      final directory = await getApplicationDocumentsDirectory();
      final filePath = '${directory.path}/animation.vap';

      // Download the file
      final response = await http.get(Uri.parse(fileUrl));
      if (response.statusCode == 200) {
        final file = File(filePath);
        await file.writeAsBytes(response.bodyBytes);
        setState(() {
          localFilePath = filePath;
          isLoading = false;
        });
      } else {
        throw Exception('Failed to download file');
      }
    } catch (e) {
      print('Error downloading file: $e');
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: isLoading
          ? const CircularProgressIndicator()
          : localFilePath != null
              ? VapPlayerView(
                  videoPath: localFilePath!,
                  loop: 2,
                  width: MediaQuery.of(context).size.width,
                  height: MediaQuery.of(context).size.height,
                  onReady: () => print("Player ready"),
                  onStart: () => print("Playback started"),
                  onComplete: () => print("Playback complete"),
                  onError: (error) => print("Error: $error"),
                )
              : const Text('Failed to load animation.'),
    );
  }
}
