package org.dkf.jdonkey;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by mertsimsek on 04/11/15.
 */
public class RadioActivity extends Activity {

    private final String[] RADIO_URL = {"http://hayatmix.net/;yayin.mp3.m3u"};

    Button mButtonControlStart;
    TextView mTextViewControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        initializeUI();
    }

    public void initializeUI() {
        mButtonControlStart = (Button) findViewById(R.id.buttonControlStart);
        mTextViewControl = (TextView) findViewById(R.id.textviewControl);

    }
}
