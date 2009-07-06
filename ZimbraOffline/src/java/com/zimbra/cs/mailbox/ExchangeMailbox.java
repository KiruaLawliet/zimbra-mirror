package com.zimbra.cs.mailbox;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.offline.OfflineLC;
import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.OfflineSyncManager;
import com.zimbra.cs.offline.common.OfflineConstants;

public class ExchangeMailbox extends ChangeTrackingMailbox {




    public ExchangeMailbox(MailboxData data) throws ServiceException {
        super(data);
    }

    @Override
    void trackChangeNew(MailItem item) throws ServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    void trackChangeModified(MailItem item, int changeMask)
    throws ServiceException {
        // TODO Auto-generated method stub

    }

    OfflineDataSource getDataSource() throws ServiceException {
        return (OfflineDataSource)OfflineProvisioning.getOfflineInstance().getDataSource(getAccount());
    }

    private int sendPendingMessages(boolean isOnRequest) throws ServiceException {
        return 0;
    }

    private boolean isAutoSyncDisabled(DataSource ds) {
        return ds.getSyncFrequency() <= 0;
    }
    
    private boolean isTimeToSync(DataSource ds) throws ServiceException {
        OfflineSyncManager syncMan = OfflineSyncManager.getInstance();
        if (isAutoSyncDisabled(ds) || !syncMan.reauthOK(ds) || !syncMan.retryOK(ds))
            return false;
        long freqLimit = syncMan.getSyncFrequencyLimit();
        long frequency = ds.getSyncFrequency() < freqLimit ? freqLimit : ds.getSyncFrequency();
        return System.currentTimeMillis() - syncMan.getLastSyncTime(ds.getName()) >= frequency;
    }
    
    @Override
    public boolean isAutoSyncDisabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void syncOnTimer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sync(boolean isOnRequest, boolean isDebugTraceOn) throws ServiceException {
        if (lockMailboxToSync()) {
            synchronized (syncLock) {
                if (isOnRequest && isDebugTraceOn) {
                    OfflineLog.offline.debug("============================== SYNC DEBUG TRACE START ==============================");
                    getOfflineAccount().setRequestScopeDebugTraceOn(true);
                }

                try {
                    int count = sendPendingMessages(isOnRequest);
                    syncDataSource(count > 0, isOnRequest);
                } catch (Exception x) {
                    if (isDeleting())
                        OfflineLog.offline.info("Mailbox \"%s\" is being deleted", getAccountName());
                    else
                        OfflineLog.offline.error("exception encountered during sync", x);
                } finally {
                    if (isOnRequest && isDebugTraceOn) {
                        getOfflineAccount().setRequestScopeDebugTraceOn(false);
                        OfflineLog.offline.debug("============================== SYNC DEBUG TRACE END ================================");
                    }
                    unlockMailbox();
                }
            } //synchronized (syncLock)
        } else if (isOnRequest) {
            OfflineLog.offline.debug("sync already in progress");
        }
    }

    private void syncDataSource(boolean force, boolean isOnRequest) throws ServiceException {
        OfflineDataSource ds = getDataSource();
        if (!force && !isOnRequest && !isTimeToSync(ds))
            return;
        
        OfflineSyncManager syncMan = OfflineSyncManager.getInstance();
        try {
            OfflineLog.offline.info(">>>>>>>> name=%s;version=%s;build=%s;release=%s;os=%s;type=%s",
                    ds.getAccount().getName(), OfflineLC.zdesktop_version.value(), OfflineLC.zdesktop_buildid.value(), OfflineLC.zdesktop_relabel.value(),
                    System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"), ds.getType());

            syncMan.syncStart(ds.getName());
            DataSourceManager.importData(ds, null, true);
            syncMan.syncComplete(ds.getName());
            OfflineProvisioning.getOfflineInstance().setDataSourceAttribute(ds, OfflineConstants.A_zimbraDataSourceLastSync, Long.toString(System.currentTimeMillis()));
        } catch (Exception x) {
            if (isDeleting())
                OfflineLog.offline.info("Mailbox \"%s\" is being deleted", getAccountName());
            else
                syncMan.processSyncException(ds, x);
        }
    }
}
