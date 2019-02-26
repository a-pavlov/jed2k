package org.dkf.jmule.adapters.menu;

import android.content.Context;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jmule.R;
import org.dkf.jmule.adapters.SearchResultListAdapter;
import org.dkf.jmule.fragments.SearchFragment;
import org.dkf.jmule.views.MenuAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockSearchAction extends MenuAction {
    private final Logger log = LoggerFactory.getLogger(BlockSearchAction.class);
    final SearchFragment fragment;
    final SearchEntry searchEntry;

    public BlockSearchAction(Context context
            , final SearchFragment fragment
            , final SearchEntry searchEntry) {
        super(context, R.drawable.ic_delete_forever_black_24dp, R.string.search_block_action);
        this.fragment = fragment;
        this.searchEntry = searchEntry;
    }

    @Override
    protected void onClick(Context context) {
        log.info("block {}", searchEntry.getFileName());
        fragment.removeEntry(searchEntry);
    }
}
