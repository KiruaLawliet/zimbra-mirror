package projects.zcs.tests.mail.messages.newwindow;

import java.lang.reflect.Method;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import framework.core.SelNGBase;
import framework.util.RetryFailedTests;
import framework.util.SleepUtil;
import projects.zcs.clients.ProvZCS;
import projects.zcs.tests.CommonTest;

/**
 * @author Jitesh Sojitra
 */
@SuppressWarnings("static-access")
public class CloseMail extends CommonTest {
	//--------------------------------------------------------------------------
	// SECTION 1: DATA-PROVIDERS
	//--------------------------------------------------------------------------
	@DataProvider(name = "dataProvider")
	public Object[][] createData(Method method) throws Exception {
		String test = method.getName();
		if (test.equals("closeNewWindow")) {
			return new Object[][] { { SelNGBase.selfAccountName.get(),
					SelNGBase.selfAccountName.get(), "ccuser@testdomain.com",
					"bccuser@testdomain.com",
					getOnlyEnglishAlphabetCharAndNumber(),
					getOnlyEnglishAlphabetCharAndNumber(), "" } };
		} else {
			return new Object[][] { { "" } };
		}
	}

	//--------------------------------------------------------------------------
	// SECTION 2: SETUP
	//--------------------------------------------------------------------------
	@BeforeClass(groups = { "always" })
	public void zLogin() throws Exception {
		zLoginIfRequired();
		zGoToApplication("Mail");
	}

	@BeforeMethod(groups = { "always" })
	public void zResetIfRequired() throws Exception {
		if (SelNGBase.needReset.get() && !SelNGBase.isExecutionARetry.get()) {
			zLogin();
		}
		SelNGBase.needReset.set(true);
	}

	//--------------------------------------------------------------------------
	// SECTION 3: TEST-METHODS
	//--------------------------------------------------------------------------
	@Test(dataProvider = "dataProvider", groups = { "smoke", "full" }, retryAnalyzer = RetryFailedTests.class)
	public void closeNewWindow(String from, String to, String cc, String bcc,
			String subject, String body, String attachments) throws Exception {
		if (SelNGBase.isExecutionARetry.get())
			handleRetry();

		checkForSkipException(
				"na",
				"SF",
				"39446",
				"New window goes blank while typing SHIFT C suddenly after login to web client (SF only)");

		to = SelNGBase.selfAccountName.get();
		String recipients[] = { to };
		ProvZCS.injectMessage(from, recipients, cc, subject, body);
		page.zMailApp.ClickCheckMailUntilMailShowsUp(subject);
		obj.zMessageItem.zClick(subject);
		obj.zButton.zClick(page.zMailApp.zDetachIconBtn2);
		SleepUtil.sleep(2000);
		SelNGBase.selenium.get().selectWindow("_blank");
		obj.zButton.zClick(page.zMailApp.zCloseIconBtn_newWindow);
		SelNGBase.selenium.get().selectWindow(null);

		SelNGBase.needReset.set(false);
	}

	//--------------------------------------------------------------------------
	// SECTION 4: RETRY-METHODS
	//--------------------------------------------------------------------------
	// since all the tests are independent, retry is simply kill and re-login
	private void handleRetry() throws Exception {
		SelNGBase.isExecutionARetry.set(false);
		zLogin();
	}
}