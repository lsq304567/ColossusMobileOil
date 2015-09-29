package com.swiftsoft.colossus.mobileoil;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

/**
 * Created by Alan on 28/09/2015.
 */
public class LogonActivityTest extends ActivityUnitTestCase<Logon>
{
    private Intent mLaunchIntent;

    public LogonActivityTest()
    {
        super(Logon.class);
    }

    protected void setup() throws Exception
    {
        super.setUp();

        mLaunchIntent = new Intent(getInstrumentation().getTargetContext(), Logon.class);

        startActivity(mLaunchIntent, null, null);

        final Button launchNextButton =
                (Button)getActivity().findViewById(R.id.logon1_button);
    }

    @MediumTest
    public void textNextActivityWasLaunchedWithIntent()
    {
        startActivity(mLaunchIntent, null, null);

        final Button launchNextButton = (Button)getActivity().findViewById(R.id.logon1_button);

        launchNextButton.performClick();

        final Intent launchIntent = getStartedActivityIntent();
        assertNotNull("Intent was null", launchIntent);
        assertTrue(isFinishCalled());

//        final String payload = launchIntent.getStringExtra(Checklist.)
    }
}