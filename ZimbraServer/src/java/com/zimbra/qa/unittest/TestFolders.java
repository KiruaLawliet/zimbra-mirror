package com.zimbra.qa.unittest;

import junit.framework.TestCase;

import com.liquidsys.coco.account.Account;
import com.liquidsys.coco.db.DbMailItem;
import com.liquidsys.coco.db.DbPool;
import com.liquidsys.coco.db.DbResults;
import com.liquidsys.coco.db.DbUtil;
import com.liquidsys.coco.db.DbPool.Connection;
import com.liquidsys.coco.mailbox.Folder;
import com.liquidsys.coco.mailbox.MailItem;
import com.liquidsys.coco.mailbox.Mailbox;
import com.liquidsys.coco.util.LiquidLog;

/**
 * @author bburtin
 */
public class TestFolders extends TestCase
{
    private Mailbox mMbox;
    private Account mAccount;
    private Connection mConn;
    
    private static String FOLDER_PREFIX = "TestFolders";
    
    /**
     * Creates the message used for tag tests 
     */
    protected void setUp()
    throws Exception {
        LiquidLog.test.debug("TestFolders.setUp()");
        super.setUp();

        mAccount = TestUtil.getAccount("user1");
        mMbox = Mailbox.getMailboxByAccount(mAccount);
        mConn = DbPool.getConnection();
        
        // Wipe out folders for this test, in case the last test didn't
        // exit cleanly
        deleteFolders();
    }

    public void testManySubfolders()
    throws Exception {
        LiquidLog.test.debug("testManySubfolders");
        final int NUM_LEVELS = 20;
        int parentId = Mailbox.ID_FOLDER_INBOX;
        Folder top = null;
        
        for (int i = 1; i <= NUM_LEVELS; i++) {
            Folder folder = mMbox.createFolder(null, FOLDER_PREFIX + i, parentId);
            if (i == 1) {
                top = folder;
            }
            parentId = folder.getId();
        }
        
        mMbox.delete(null, top.getId(), top.getType());
    }
    
    protected void tearDown() throws Exception {
        LiquidLog.test.debug("TestFolders.tearDown()");

        deleteFolders();
        
        DbPool.quietClose(mConn);
        super.tearDown();
    }

    private void deleteFolders()
    throws Exception {
        // Delete folders bottom-up to avoid orphaned folders
        String sql =
            "SELECT id " +
            "FROM " + DbMailItem.getMailItemTableName(mMbox) +
            " WHERE subject LIKE '" + FOLDER_PREFIX + "%' " +
            "ORDER BY id DESC";
        DbResults results = DbUtil.executeQuery(sql);
        while (results.next()) {
            int id = results.getInt(1);
            mMbox.delete(null, id, MailItem.TYPE_FOLDER);
        }
    }
}
