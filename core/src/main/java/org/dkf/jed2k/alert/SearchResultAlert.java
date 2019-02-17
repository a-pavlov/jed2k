package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.SearchEntry;

import java.util.List;

/**
 * Created by inkpot on 24.07.2016.
 */
public class SearchResultAlert extends Alert {
    private List<SearchEntry> results;
    private boolean hasMoreResults;

    public SearchResultAlert(final List<SearchEntry> results, boolean hasMoreResults) {
        this.results = results;
        this.hasMoreResults = hasMoreResults;
    }

    @Override
    public Severity severity() {
        return Severity.Info;
    }

    @Override
    public int category() {
        return Category.ServerNotification.value;
    }

    public List<SearchEntry> getResults() {
        return this.results;
    }

    public boolean isHasMoreResults() {
        return this.hasMoreResults;
    }

    public String toString() {
        return "SearchResultAlert(results=" + this.getResults() + ", hasMoreResults=" + this.isHasMoreResults() + ")";
    }
}
