/*
 * ***** BEGIN LICENSE BLOCK *****
 *
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007 Zimbra, Inc.
 *
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 *
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.offline;

import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.offline.util.ymail.YMailClient;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.account.DataSource;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;

import javax.mail.internet.MimeMessage;
import java.io.IOException;

public class YMailSender extends MailSender {
    private final YMailClient ymc;
    private IOException error;

    public static YMailSender getInstance(OfflineDataSource ds)
        throws ServiceException {
        if (ds.isSaveToSent() || !ds.isYahoo()) {
            throw new IllegalArgumentException("Must be yahoo data source");
        }
        String user = ds.getAttr(OfflineProvisioning.A_zimbraDataSourceUsername);
        String pass = DataSource.decryptData(ds.getId(),
            ds.getAttr(OfflineProvisioning.A_zimbraDataSourcePassword));
        OfflineYAuth ya = OfflineYAuth.getInstance(ds.getMailbox());
        return new YMailSender(new YMailClient(ya.authenticate(user, pass)));
    }
    
    private YMailSender(YMailClient ymc) throws ServiceException {
        this.ymc = ymc;
    }

    @Override
    protected void sendMessage(MimeMessage mm, boolean ignoreFailedAddresses,
                               RollbackData[] rollback) throws IOException {
        try {
            ymc.sendMessage(mm);
        } catch (IOException e) {
            error = e;
            throw e;
        }
    }

    public boolean sendFailed() {
        return error != null;
    }

    public IOException getError() {
        return error;
    }
}
