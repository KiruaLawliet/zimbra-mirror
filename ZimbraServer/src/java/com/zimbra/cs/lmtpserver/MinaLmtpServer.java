/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2009 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.2 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.lmtpserver;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mina.MinaHandler;
import com.zimbra.cs.mina.MinaServer;
import com.zimbra.cs.mina.MinaCodecFactory;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class MinaLmtpServer extends MinaServer {
    public static boolean isEnabled() {
        return Boolean.getBoolean("ZimbraNioLmtpEnabled") || LC.nio_lmtp_enabled.booleanValue();
    }

    public MinaLmtpServer(LmtpConfig config, ExecutorService pool)
            throws IOException, ServiceException {
        super(config, pool);
        registerMinaStatsMBean("MinaLmtpServer");
    }

    @Override
    public MinaHandler createHandler(IoSession session) {
        return new MinaLmtpHandler(this, session);
    }

    @Override
    protected ProtocolCodecFactory getProtocolCodecFactory() {
        return new MinaCodecFactory() {
            public ProtocolDecoder getDecoder() {
                return new MinaLmtpDecoder(getStats());
            }
        };
    }

    @Override
    public Log getLog() { return ZimbraLog.lmtp; }
}
