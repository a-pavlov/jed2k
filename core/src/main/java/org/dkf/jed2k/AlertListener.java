package org.dkf.jed2k;

import org.dkf.jed2k.alert.SearchResultAlert;
import org.dkf.jed2k.alert.ServerMessageAlert;
import org.dkf.jed2k.alert.ServerStatusAlert;

/**
 * listener to handle events from session
 * Created by ap197_000 on 01.09.2016.
 */
public interface AlertListener {

    /**
     *
     * @param alert search result from session for all types of search
     */
    public void onSearchResult(final SearchResultAlert alert);

    /**
     *
     * @param alert server message
     */
    public void onServerMessage(final ServerMessageAlert alert);

    /**
     *
     * @param alert some server's information
     */
    public void onServerStatus(final ServerStatusAlert alert);
}
