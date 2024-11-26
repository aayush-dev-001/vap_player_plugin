import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class VapPlayerView extends StatefulWidget {
  final String videoPath;
  final double width;
  final double height;
  final int loop;
  final bool enableMask;
  final VoidCallback? onReady;
  final VoidCallback? onStart;
  final VoidCallback? onComplete;
  final Function(String)? onError;

  const VapPlayerView({
    super.key,
    required this.videoPath,
    required this.width,
    required this.height,
    required this.loop,
    this.enableMask = false,
    this.onReady,
    this.onStart,
    this.onComplete,
    this.onError,
  });

  @override
  State<VapPlayerView> createState() => _VapPlayerViewState();
}

class _VapPlayerViewState extends State<VapPlayerView> {
  String? _localPath;
  bool _isLoading = true;
  String? _error;
  int _viewId = -1;
  MethodChannel? _channel;

  @override
  void initState() {
    super.initState();
    _prepareVideo();
  }

  Future<void> _prepareVideo() async {
    try {
      _localPath = widget.videoPath;
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
        widget.onReady?.call();
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _error = e.toString();
        });
        widget.onError?.call(e.toString());
      }
    }
  }

  void _setupMethodChannel(int id) {
    _viewId = id;
    _channel = MethodChannel('vap_player_plugin_$id');
    _channel?.setMethodCallHandler(_handleMethodCall);
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onVideoStart':
        widget.onStart?.call();
        break;
      case 'onVideoComplete':
        widget.onComplete?.call();
        break;
      case 'onVideoError':
        final Map<dynamic, dynamic> error = call.arguments;
        widget.onError?.call(error['message'] as String);
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return SizedBox(
        width: widget.width,
        height: widget.height,
        child: const Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    if (_error != null || _localPath == null) {
      return SizedBox(
        width: widget.width,
        height: widget.height,
        child: Center(
          child: Text(_error ?? 'Failed to load video'),
        ),
      );
    }

    final Map<String, dynamic> creationParams = <String, dynamic>{
      'videoPath': _localPath,
      'enableMask': widget.enableMask,
      'loop': widget.loop,
    };

    return SizedBox(
      width: widget.width,
      height: widget.height,
      child: AndroidView(
        viewType: 'vap_player_view',
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
        onPlatformViewCreated: _setupMethodChannel,
      ),
    );
  }
}
