package org.dkf.jdonkey.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import org.dkf.jdonkey.R;

/**
 * Created by mertsimsek on 04/11/15.
 */
public class MediaActivity extends Activity {

  String url = "https://api.soundcloud.com/tracks/230497727/stream?client_id=06a2d17b03d3ff6ae226b007edd5595d";
  String url2 = "https://api.soundcloud.com/tracks/227713501/stream?client_id=06a2d17b03d3ff6ae226b007edd5595d";

  SeekBar seekbar;
  Button button;
  Button buttonNext;
  TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_media);


    seekbar = (SeekBar) findViewById(R.id.seekbar);
    seekbar.setEnabled(false);
    button = (Button) findViewById(R.id.buttoncontrol);
    buttonNext = (Button) findViewById(R.id.buttonNext);
    textView = (TextView) findViewById(R.id.textstatus);
  }
}
