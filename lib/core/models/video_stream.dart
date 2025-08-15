class VideoStream {
  final String name;
  final String link;
  final String? drmScheme;
  final String? drmLicense;
  final String? userAgent;
  final String? cookie;
  final String? referer;
  final String? origin;

  VideoStream({
    required this.name,
    required this.link,
    this.drmScheme,
    this.drmLicense,
    this.userAgent,
    this.cookie,
    this.referer,
    this.origin,
  });
}
