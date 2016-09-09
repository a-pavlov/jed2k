package org.dkf.jdonkey.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.dkf.jdonkey.R;

/**
 * Created by inkpot on 09.09.2016.
 */
public class SearchParametersView extends LinearLayout {

    private EditText editMinSize;
    private EditText editMaxSize;
    private EditText editSources;

    public SearchParametersView(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(getContext(), R.layout.view_search_parameters, this);
        editMinSize = (EditText)findViewById(R.id.view_search_parameter_min_size);
        editMaxSize = (EditText)findViewById(R.id.view_search_parameter_max_size);
        editSources = (EditText)findViewById(R.id.view_search_parameter_sources);
        editMinSize.setText(R.string.search_paramter_zero);
        editMaxSize.setText(R.string.search_paramter_zero);
        editSources.setText(R.string.search_paramter_zero);
    }
}
