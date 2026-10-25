package com.cobalt.opener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Receives a shared video link (e.g. from YouTube ReVanced's download button) and opens it in Cobalt. */
public class OpenActivity extends Activity {
    private static final Pattern URL = Pattern.compile("https?://\\S+");

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Intent in = getIntent();
        String text = in.getStringExtra(Intent.EXTRA_TEXT);
        if (text == null && in.getData() != null) text = in.getDataString();
        String link = null;
        if (text != null) {
            Matcher m = URL.matcher(text);
            if (m.find()) link = m.group();
        }
        if (link == null) {
            Toast.makeText(this, "No link found", Toast.LENGTH_SHORT).show();
        } else {
            // cobalt auto-fills and starts the download from "#<link>"
            String target = getString(R.string.cobalt_instance) + "/#" + Uri.encode(link, ":/?=&");
            Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(target));
            open.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(open);
        }
        finish();
    }
}
