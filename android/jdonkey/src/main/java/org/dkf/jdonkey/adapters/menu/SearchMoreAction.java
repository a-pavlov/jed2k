package org.dkf.jdonkey.adapters.menu;

import android.content.Context;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.fragments.SearchFragment;
import org.dkf.jdonkey.views.MenuAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 13.09.2016.
 */

public class SearchMoreAction extends MenuAction {
    private final Logger log = LoggerFactory.getLogger(SearchMoreAction.class);
    final SearchFragment fragment;

    public SearchMoreAction(Context context, final SearchFragment fragment) {
        super(context, R.drawable.contextmenu_icon_play, R.string.search_more_action);
        this.fragment = fragment;
    }

    @Override
    protected void onClick(Context context) {
        log.info("search more request");
        fragment.performSearchMore();
    }
}
