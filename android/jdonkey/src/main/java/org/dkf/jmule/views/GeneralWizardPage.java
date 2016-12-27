/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dkf.jmule.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.core.AndroidPlatform;
import org.dkf.jmule.core.ConfigurationManager;
import org.dkf.jmule.core.Constants;
import org.dkf.jmule.views.preference.StoragePreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gubatron
 * @author aldenml
 */
public class GeneralWizardPage extends RelativeLayout implements WizardPageView {
    private Logger log = LoggerFactory.getLogger(GeneralWizardPage.class);
    private OnCompleteListener listener;
    private TextView textStoragePath;
    private CheckBox checkSeedFinishedTorrents;
    private CheckBox checkSeedFinishedTorrentsWifiOnly;
    private CheckBox checkUpnp;
    private CheckBox checkDht;

    public GeneralWizardPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void load() {
        textStoragePath.setText(ConfigurationManager.instance().getStoragePath());
        checkUpnp.setChecked(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_FORWARD_PORTS));
        checkDht.setChecked(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_START_DHT));
        validate();
    }

    @Override
    public void finish() {
        ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_FORWARD_PORTS, checkUpnp.isChecked());
        ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_START_DHT, checkDht.isChecked());
        log.info("[wizard] upnp {}, dht {}", checkUpnp.isChecked(), checkDht.isChecked());
        Engine.instance().forwardPorts(checkUpnp.isChecked());
        Engine.instance().useDht(checkDht.isChecked());
    }

    @Override
    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    public void updateStoragePathTextView(String newLocation) {
        textStoragePath.setText(newLocation);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_general_wizard_page, this);

        TextView textStoragePathTitle = (TextView) findViewById(R.id.view_general_wizard_page_storage_path_title);
        textStoragePath = (TextView) findViewById(R.id.view_general_wizard_page_storage_path_textview);
        ImageView titleHorizontalBreak = (ImageView) findViewById(R.id.view_general_wizard_page_title_horizontal_break);

        if (AndroidPlatform.saf()) {
            textStoragePath.setOnClickListener(new StoragePathTextViewAdapter((Activity) getContext()));
        } else {
            titleHorizontalBreak.setVisibility(View.GONE);
            textStoragePathTitle.setVisibility(View.GONE);
            textStoragePath.setVisibility(View.GONE);
        }

        checkUpnp = (CheckBox) findViewById(R.id.view_general_wizard_page_check_upnp);
        checkUpnp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validate();
            }
        });

        checkDht = (CheckBox) findViewById(R.id.view_general_wizard_page_check_dht);
        checkDht.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validate();
            }
        });
    }

    protected void onComplete(boolean complete) {
        if (listener != null) {
            listener.onComplete(this, complete);
        }
    }

    /**
     * Put more complete/validation logic here.
     */
    private void validate() {
        onComplete(true);
    }

    private static class StoragePathTextViewAdapter extends ClickAdapter<Activity> {
        public StoragePathTextViewAdapter(Activity owner) {
            super(owner);
        }

        @Override
        public void onClick(Activity owner, View v) {
            StoragePreference.invokeStoragePreference(owner);
        }
    }
}
