/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkf.jdonkey.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import org.apache.commons.io.IOUtils;
import org.dkf.jdonkey.BuildConfig;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.util.UIUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class AboutFragment extends Fragment implements MainFragment {

    public AboutFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        final TextView title = (TextView) view.findViewById(R.id.fragment_about_title);
        title.setText("JDonkey " + " v" + Constants.JED2K_VERSION_STRING);

        final TextView buildNumber = (TextView) view.findViewById(R.id.fragment_about_build_number);
        buildNumber.setText("\nbuild " + BuildConfig.VERSION_CODE + " - sdk level " + Build.VERSION.SDK_INT);

        final TextView content = (TextView) view.findViewById(R.id.fragment_about_content);
        content.setText(Html.fromHtml(getAboutText()));
        content.setMovementMethod(LinkMovementMethod.getInstance());

        final ImageButton githubButton = (ImageButton) view.findViewById(R.id.fragment_github_button);

        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.openURL(v.getContext(), "http://github.com/a-pavlov/jed2k");
            }
        });

        return view;
    }

    @Override
    public View getHeader(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        TextView header = (TextView) inflater.inflate(R.layout.view_main_fragment_simple_header, null);
        header.setText(R.string.about);

        return header;
    }

    @Override
    public void onShow() {
    }

    private String getAboutText() {
        try {
            InputStream raw = getResources().openRawResource(R.raw.about);
            return IOUtils.toString(raw, "UTF-8");
        } catch (IOException e) {
            return "";
        }
    }
}
