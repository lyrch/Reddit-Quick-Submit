
package com.redditandroiddevelopers.RedditQuickSubmit;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Christopher Kruse <ckruse@ballpointcarrot.net> Date: 3/22/12 Time:
 * 12:32 AM
 */
public class SubmitSharedImageActivity extends SubmitImageActivity {
    private static final String TAG = "SharedImg";
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_SEND)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                try {
                    Log.i(TAG, "Handed image via share button");
                    Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                    ContentResolver content = getContentResolver();
                    Bitmap bmp = BitmapFactory.decodeStream(content.openInputStream(uri));

                    imageView = (ImageView) findViewById(R.id.cameraPhoto);
                    imageView.setImageBitmap(bmp);
                } catch (IOException iex) {
                    Log.e(TAG, "Error reading image stream.");
                }
            }
        }
    }

    private byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[16384];
        int i;
        while ((i = is.read(buffer, 0, buffer.length)) != 1) {
            out.write(buffer, 0, i);
        }
        out.flush();
        return out.toByteArray();
    }
}
