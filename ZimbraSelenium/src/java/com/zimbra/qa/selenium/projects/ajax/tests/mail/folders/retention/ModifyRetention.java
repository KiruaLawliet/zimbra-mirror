package com.zimbra.qa.selenium.projects.ajax.tests.mail.folders.retention;

import org.testng.annotations.*;

import com.zimbra.qa.selenium.framework.items.FolderItem;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.DialogEditFolder;

public class ModifyRetention extends PrefGroupMailByMessageTest {

	public ModifyRetention() {
		logger.info("New " + ModifyRetention.class.getCanonicalName());

	}

	@Test(
			description = "Modify a basic retention (Context menu -> Edit -> Retention)", 
			groups = { "functional" }
			)
	public void ModifyRetention_01() throws HarnessException {

		//-- Data
		
		// Create the subfolder
		String foldername = "folder" + ZimbraSeleniumProperties.getUniqueString();

		app.zGetActiveAccount().soapSend(
					"<CreateFolderRequest xmlns='urn:zimbraMail'>"
				+		"<folder name='" + foldername + "' l='" +  FolderItem.importFromSOAP(app.zGetActiveAccount(), FolderItem.SystemFolder.Inbox).getId() + "'/>"
				+	"</CreateFolderRequest>");

		FolderItem folder = FolderItem.importFromSOAP(app.zGetActiveAccount(), foldername);
		ZAssert.assertNotNull(folder, "Verify the subfolder is available");

		// Add a retention policy
		app.zGetActiveAccount().soapSend(
				"<FolderActionRequest xmlns='urn:zimbraMail'>"
			+		"<action id='" + folder.getId() + "' op='retentionpolicy'>"
			+			"<retentionPolicy>"
			+				"<keep>"
			+					"<policy lifetime='5d' type='user'/>"
			+				"</keep>"
			+			"</retentionPolicy>"
			+		"</action>"
			+	"</FolderActionRequest>");

		
		
		
		//-- GUI
		
		// Click on Get Mail to refresh the folder list
		app.zPageMail.zToolbarPressButton(Button.B_GETMAIL);

		// Rename the folder using context menu
		DialogEditFolder dialog = (DialogEditFolder) app.zTreeMail.zTreeItem(Action.A_RIGHTCLICK, Button.B_TREE_EDIT, folder);

		// Set to 4 years
		dialog.zNavigateToTab(DialogEditFolder.DialogTab.Retention);
		dialog.zRetentionSetRangeValue(6);

		// Save
		dialog.zClickButton(Button.B_OK);
		
		
		//-- Verification
		
		// Verify the retention policy on the folder
		app.zGetActiveAccount().soapSend(
				"<GetFolderRequest xmlns='urn:zimbraMail'>"
			+		"<folder l='" + folder.getId() + "'/>"
			+	"</GetFolderRequest>");
		String lifetime = app.zGetActiveAccount().soapSelectValue("//mail:policy", "lifetime");
		
		ZAssert.assertEquals(lifetime, "6d", "Verify the policy lifetime is set to 6 days");
		
	}


}
