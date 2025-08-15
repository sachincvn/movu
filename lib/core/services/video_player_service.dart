import 'package:movu/core/models/video_stream.dart';
import 'package:movu/video_player/native_video_player_controller.dart';
import 'package:movu/video_player/video_player_config.dart';

class VideoPlayerService {
  late final NativeVideoPlayerController controller;

  VideoPlayerService() {
    controller = NativeVideoPlayerController();
  }

  void initializeController(VideoStream stream) {
    final Map<String, String> headers = {};
    if (stream.userAgent != null) {
      headers['User-Agent'] = stream.userAgent!;
    }
    if (stream.cookie != null) {
      headers['Cookie'] = stream.cookie!;
    }
    if (stream.referer != null) {
      headers['Referer'] = stream.referer!;
    }
    if (stream.origin != null) {
      headers['Origin'] = stream.origin!;
    }

    DrmConfig? drmConfig;
    if (stream.drmScheme != null) {
      if (stream.drmScheme == 'clearkey' && stream.drmLicense != null) {
        drmConfig = DrmConfig(
          scheme: 'clearkey',
          licenseKeys: [stream.drmLicense!],
        );
      } else if (stream.drmScheme == 'widevine' && stream.drmLicense != null) {
        drmConfig = DrmConfig(
          scheme: 'widevine',
          licenseUrl: stream.drmLicense!,
        );
      }
    }

    final config = VideoPlayerConfig(
      url: stream.link,
      headers: headers.isNotEmpty ? headers : null,
      drmConfig: drmConfig,
    );
    controller.initialize(config);
  }

  void dispose() {
    controller.dispose();
  }
}
