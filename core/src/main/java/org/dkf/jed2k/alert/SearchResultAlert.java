package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.server.search.SearchResult;

/**
 * Created by inkpot on 24.07.2016.
 */
public class SearchResultAlert extends Alert {
    public SearchResult results;

    public SearchResultAlert(SearchResult r) {
        results = r;
    }

    @Override
    public Severity severity() {
        return Severity.Info;
    }

    @Override
    public int category() {
        return Category.ServerNotification.value;
    }

    @Override
    public String toString() {
        return "server search reslts count " + results.files.size() + " more " + ((results.moreResults!=0)?"yes":"no");
    }
}
