package g.r.tech;

import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;

public class SkydriveLog extends Activity {
    /** Called when the activity is first created. */
	
    private LiveSdkSampleApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
    private Button mSignInButton;
    private TextView mBeginTextView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skydrive_login);
        
        mApp = (LiveSdkSampleApplication) getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);

        mInitializeDialog = ProgressDialog.show(this, "", "Initializing. Please wait...", true);

        mBeginTextView = (TextView) findViewById(R.id.beginTextView);
        mSignInButton = (Button) findViewById(R.id.signInButton);
        
        // Check to see if the CLIENT_ID has been changed.
        if (Config.CLIENT_ID.equals("00000000480DC92A")) {
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToast("In order to use the sample, you must first place your client id " + 
                              "in com.microsoft.live.sample.Config.CLIENT_ID. For more " +
                              "information see http://go.microsoft.com/fwlink/?LinkId=220871");
                }
            });
        } else {
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuthClient.login(SkydriveLog.this,
                                      Arrays.asList(Config.SCOPES),
                                      new LiveAuthListener() {
                        @Override
                        public void onAuthComplete(LiveStatus status,
                                                   LiveConnectSession session,
                                                   Object userState) {
                            if (status == LiveStatus.CONNECTED) {
                                launchMainActivity(session);
                            } else {
                                showToast("Login did not connect. Status is " + status + ".");
                            }
                        }
    
                        @Override
                        public void onAuthError(LiveAuthException exception, Object userState) {
                            showToast(exception.getMessage());
                        }
                    });
                }
            });
        }
    }
    
    private void launchMainActivity(LiveConnectSession session) {
        assert session != null;
        mApp.setSession(session);
        mApp.setConnectClient(new LiveConnectClient(session));
        //startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSignIn() {
        mSignInButton.setVisibility(View.VISIBLE);
        mBeginTextView.setVisibility(View.VISIBLE);
    }
}
