class VideoPlayerConfig {
  final String url;
  final Map<String, String>? headers;
  final DrmConfig? drmConfig;

  VideoPlayerConfig({
    required this.url,
    this.headers,
    this.drmConfig,
  });

  Map<String, dynamic> toMap() {
    return {
      'url': url,
      if (headers != null) 'headers': headers,
      if (drmConfig != null) 'drm_scheme': drmConfig!.scheme,
      if (drmConfig != null) 'drm_license_url': drmConfig!.licenseUrl,
      if (drmConfig != null && drmConfig!.licenseHeaders != null) 'drm_license_headers': drmConfig!.licenseHeaders,
      if (drmConfig != null && drmConfig!.licenseKeys != null) 'drm_license_keys': drmConfig!.licenseKeys,
    };
  }
}

class DrmConfig {
  final String scheme;
  final String? licenseUrl;
  final List<String>? licenseKeys;
  final Map<String, String>? licenseHeaders;

  DrmConfig({
    required this.scheme,
    this.licenseUrl,
    this.licenseKeys,
    this.licenseHeaders,
  });

  Map<String, dynamic> toMap() {
    return {
      'scheme': scheme,
      'license_url': licenseUrl,
      'license_keys': licenseKeys,
      'license_headers': licenseHeaders,
    };
  }
}
