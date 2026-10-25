# Cobalt Opener

Tiny companion app: receives the link from the YouTube (Morphe/ReVanced) **Download** button and opens
`https://cobalt.canine.tools/#<link>`, which fills the link in and starts the download.

## Use
1. Run the **Build Cobalt Opener** workflow (Actions tab) and install the APK from the artifact.
2. In patched YouTube: Settings → ReVanced/Morphe → Downloads → set **External downloader package name** to `com.cobalt.opener` (enable external downloader).
3. Press Download on any video → Cobalt opens in your browser.

Change the instance in `app/src/main/res/values/strings.xml` (`cobalt_instance`).
