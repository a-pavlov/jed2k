package org.dkf.jmule;

import org.dkf.jed2k.alert.*;

/**
 * listener to handle events from session
 * Created by ap197_000 on 01.09.2016.
 */
public interface AlertListener {

    public void onListen(final ListenAlert alert);

    /**
     *
     * @param alert search result from session for all types of search
     */
    void onSearchResult(final SearchResultAlert alert);

    void onServerConnectionAlert(final ServerConnectionAlert alert);

    /**
     *
     * @param alert server message
     */
    void onServerMessage(final ServerMessageAlert alert);

    /**
     *
     * @param alert some server's information
     */
    void onServerStatus(final ServerStatusAlert alert);

    /**
     *
     * @param alert server id received
     */
    void onServerIdAlert(final ServerIdAlert alert);

    /**
     *
     * @param alert server connection closed with reason in code field
     */
    void onServerConnectionClosed(final ServerConectionClosed alert);

    /**
     * handle add
     * @param alert
     */
    void onTransferAdded(final TransferAddedAlert alert);
    void onTransferRemoved(final TransferRemovedAlert alert);
    /**
     *  handle actual pause/resume on transfer
     * @param alert pause/resume alert for transfer
     */
    void onTransferPaused(final TransferPausedAlert alert);
    void onTransferResumed(final TransferResumedAlert alert);
    void onTransferIOError(final TransferDiskIOErrorAlert alert);

    void onPortMapAlert(final PortMapAlert alert);
}
