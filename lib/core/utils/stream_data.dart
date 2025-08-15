import 'package:movu/core/models/video_stream.dart';

final List<VideoStream> streams = [
  VideoStream(name: 'Hls', link: 'https://dai.google.com/linear/hls/event/CdbR3iRfRdCTk3lm7jer8A/master.m3u8'),
  VideoStream(name: 'Hls Fancode', link: 'https://in-mc-fdlive.fancode.com/mumbai/132793_english_hls_f1a7d2432210814adfreeta-di_h264/index.m3u8'),
  VideoStream(name: 'Mp4', link: 'https://www.sample-videos.com/video321/mp4/720/big_buck_bunny_720p_5mb.mp4'),
  VideoStream(name: 'Flv', link: 'https://www.sample-videos.com/video321/flv/720/big_buck_bunny_720p_5mb.flv'),
  VideoStream(name: 'Mkv 2', link: 'https://www.sample-videos.com/video321/mkv/720/big_buck_bunny_720p_5mb.mkv'),
  VideoStream(name: '3GP', link: 'https://www.sample-videos.com/video321/3gp/240/big_buck_bunny_240p_5mb.3gp'),
  // Google Test Streams for DRM Testing
  VideoStream(name: 'Google Widevine Test', link: 'https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd', drmScheme: 'widevine', drmLicense: 'https://proxy.uat.widevine.com/proxy?provider=widevine_test'),
  VideoStream(name: 'Google ClearKey Test', link: 'https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd', drmScheme: 'clearkey', drmLicense: '1ab45440532c439994dc5c5ad9584bac:1cb55dda3110417d906c1e2c339e9717'),
  VideoStream(name: 'Mpd Clearkey', link: 'https://d1g8wgjurz8via.cloudfront.net/bpk-tv/Zeetv/default/manifest.mpd', drmScheme: 'clearkey', drmLicense: 'ed068cf84f0640ccbc7c0e395c0a272e:bb722190f2bb446391020411a7d0828b'),
  VideoStream(name: 'Mpd + UserAgent + Cookies', link: 'https://livetv-push.hotstar.com/dash/live/2002466/sshindiwv/master.mpd', userAgent: 'Hotstar;in.startv.hotstar/25.01.27.5.3788 (Android/13)', cookie: 'hdntl=exp=1751009201~acl=*sshindi*~id=3ffb365236d5ee1f8a0ef76ed62968bc~data=hdntl~hmac=7cea17c2ea6bd71441bb1913c6c5d6fda8785bbb09cf85348b1a7c5e9a6a05ff', referer: 'https://www.hotstar.com/', origin: 'https://www.hotstar.com', drmScheme: 'clearkey', drmLicense: 'fe7718fbb3fb4ba78c07cc0f578744e6624e24b1843b459fab0a949609416f0d:'),
  VideoStream(name: 'Mpd Widevine', link: 'https://dangaplay.akamaized.net/transcoded_videos/684d259f8530b876fed0a462/dash/adaptive_HD_file_dash.mpd', drmScheme: 'widevine', drmLicense: 'https://drm.apisaranyu.in/wv/getlicense/eyJwcm92aWRlciI6InNhcmFueXVlbnRlcjEwIiwiY29udGVudCI6IjY4NGQyNWEwNjg0ZDI1YTA4NTMwYjg3NmZlZDBhNDYzIiwibGljZW5zZV9leHBpcnlfdGltZXN0YW1wIjoxNzU3ODQ4OTg1LCJkYXNoVHJhY2tSdWxlcyI6W3sidHlwZSI6IkFVRElPIiwibGV2ZWwiOjF9LHsidHlwZSI6IlNEIiwibGV2ZWwiOjF9LHsidHlwZSI6IkhEIiwibGV2ZWwiOjF9LHsidHlwZSI6IlVIRDEiLCJsZXZlbCI6M30seyJ0eXBlIjoiVUhEMiIsImxldmVsIjozLCJoZGNwIjoiSERDUF9OT19ESUdJVEFMX09VVFBVVCJ9XSwibGljZW5zZVJ1bGVzIjp7ImNhblBlcnNpc3QiOnRydWUsImV4cGlyZXMiOjAsInJlbnRhbER1cmF0aW9uIjo4NjQwMH19?us=e36d84162bf664ac957165bbac9daf98'),
  VideoStream(name: 'Mkv', link: 'https://file-stremx-ce482e76047f.herokuapp.com/1049497/Coolie+%282025%29+South+Hindi+Dubbed+Movie+HQCam+720p+ESub.mkv?hash=AgADbR'),
];
