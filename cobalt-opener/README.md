# Cobalt Opener (Cobalt Downloader)

Receives the link from the YouTube (Morphe/ReVanced) **Download** button, asks the Cobalt API for a
**1080p** mp4 (h264 + aac) and saves it to **Movies/Cobalt** with the system download manager, so it
plays offline. If the API is unavailable (e.g. the instance needs a key), it opens Cobalt in the browser instead.

## Use
1. Run the **Build Cobalt Opener** workflow and install the APK from Artifacts (Android 10+).
2. In patched YouTube: Settings -> Downloads -> enable external downloader, package name `com.cobalt.opener`.
3. Press Download on a video -> notification shows progress -> file appears in Movies/Cobalt.

## Settings (`app/src/main/res/values/strings.xml`)
- `cobalt_instance` - Cobalt instance URL
- `cobalt_api_key` - API key, only if the instance requires one
- `video_quality` - 1080 by default (720, 1440, 2160, max...)
