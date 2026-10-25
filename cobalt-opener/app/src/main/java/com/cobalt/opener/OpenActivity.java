package com.cobalt.opener;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Receives a shared video link (e.g. from the YouTube ReVanced download button), asks Cobalt for a
 * 1080p file and saves it to Movies/Cobalt using the system DownloadManager (background, resumable,
 * the file stays on the phone for offline viewing). If the API can't be used, falls back to opening
 * the Cobalt web page in the browser.
 */
public class OpenActivity extends Activity {
    private static final Pattern URL_RE = Pattern.compile("https?://\\S+");
    private String link;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Intent in = getIntent();
        String text = in.getStringExtra(Intent.EXTRA_TEXT);
        if (text == null && in.getData() != null) text = in.getDataString();
        if (text != null) {
            Matcher m = URL_RE.matcher(text);
            if (m.find()) link = m.group();
        }
        if (link == null) {
            toast("No link found");
            finish();
            return;
        }
        toast("Cobalt: preparing " + getString(R.string.video_quality) + "p download...");
        new Thread(this::work).start();
    }

    private void work() {
        try {
            String err = download();
            if (err == null) {
                ui(() -> toast("Download started - see notifications. Saved in Movies/Cobalt"));
            } else {
                ui(() -> { toast("API failed (" + err + "), opening Cobalt in browser"); openBrowser(); });
            }
        } catch (Exception e) {
            ui(() -> { toast("Error: " + e.getMessage() + " - opening browser"); openBrowser(); });
        }
        ui(this::finish);
    }

    /** @return null on success, otherwise a short error description. */
    private String download() throws Exception {
        JSONObject body = new JSONObject();
        body.put("url", link);
        body.put("videoQuality", getString(R.string.video_quality));
        body.put("downloadMode", "auto");
        body.put("youtubeVideoCodec", "h264"); // h264 + aac mp4: plays on every phone
        body.put("filenameStyle", "basic");

        HttpURLConnection c = (HttpURLConnection) new URL(getString(R.string.cobalt_instance) + "/").openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(20000);
        c.setReadTimeout(60000);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("User-Agent", "CobaltOpener/2.0");
        String key = getString(R.string.cobalt_api_key).trim();
        if (!key.isEmpty()) c.setRequestProperty("Authorization", "Api-Key " + key);
        c.setDoOutput(true);
        try (OutputStream os = c.getOutputStream()) { os.write(body.toString().getBytes("UTF-8")); }

        int code = c.getResponseCode();
        InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
        if (is == null) return "HTTP " + code;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
        JSONObject r;
        try { r = new JSONObject(bo.toString("UTF-8")); } catch (Exception e) { return "HTTP " + code; }

        String status = r.optString("status");
        switch (status) {
            case "tunnel":
            case "redirect":
                enqueue(r.getString("url"), r.optString("filename", "video.mp4"));
                return null;
            case "picker": {
                JSONArray arr = r.getJSONArray("picker");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String u = o.getString("url");
                    enqueue(u, "cobalt_" + System.currentTimeMillis() + "_" + i + guessExt(o.optString("type")));
                }
                return null;
            }
            case "error": {
                JSONObject e = r.optJSONObject("error");
                return e != null ? e.optString("code", "error") : "error";
            }
            default:
                return status.isEmpty() ? "HTTP " + code : status;
        }
    }

    private static String guessExt(String type) {
        return "photo".equals(type) ? ".jpg" : ".mp4";
    }

    private void enqueue(String url, String filename) {
        filename = filename.replaceAll("[\\\\/:*?\"<>|]", "_");
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
        req.addRequestHeader("User-Agent", "CobaltOpener/2.0");
        req.setTitle(filename);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "Cobalt/" + filename);
        req.allowScanningByMediaScanner();
        ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(req);
    }

    private void openBrowser() {
        String target = getString(R.string.cobalt_instance) + "/#" + Uri.encode(link, ":/?=&");
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(target));
        open.addCategory(Intent.CATEGORY_BROWSABLE);
        startActivity(open);
    }

    private void ui(Runnable r) { runOnUiThread(r); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
