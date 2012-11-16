package g.r.tech;

import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;

public class Home extends Activity {
    /** Called when the activity is first created. */
    
	static //Menu buttons screen1 HOME
	Button DropboxLog;

	//Menu buttons screen1 HOME
	Button BoxLog;

	//Menu buttons screen1 HOME
	Button SkydriveLog; 
	
	Button MoveCloud;
	
	static ImageView greenLight1, greenLight2, greenLight3, redLight1, redLight2, redLight3;
	
    final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";

    //Dropbox logged in state stuff
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    DropboxAPI<AndroidAuthSession> mApi;
    
    //Skydrive logged in state stuff
    private LiveSdkSampleApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        DropboxLog = (Button) findViewById(R.id.Dropboxbutton);
        BoxLog = (Button) findViewById(R.id.Boxbutton);
        SkydriveLog = (Button) findViewById(R.id.Skydrivebutton);
        MoveCloud = (Button) findViewById(R.id.moveCloud); 
        greenLight1 = (ImageView) findViewById(R.id.dropboxGreenLight);
        greenLight2 = (ImageView) findViewById(R.id.boxGreenLight);
        greenLight3 = (ImageView) findViewById(R.id.skydriveGreenLight);
        redLight1 = (ImageView) findViewById(R.id.dropboxRedLight);
        redLight2 = (ImageView) findViewById(R.id.boxRedLight);
        redLight3 = (ImageView) findViewById(R.id.skydriveRedLight);
        
        //click listeners for buttons
        
 

        
        
        DropboxLog.setOnClickListener(new View.OnClickListener() {
        	
        	
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openDropbox = new Intent(v.getContext(), DropboxLog.class);
    			startActivityForResult(openDropbox,0);
    			

			}
			
						
		});
        
        
        
        
        BoxLog.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openBox = new Intent(v.getContext(), BoxLog.class);
				startActivityForResult(openBox, 0);
				
				
			}
			
		});
        
        
        SkydriveLog.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openSkydrive = new Intent(v.getContext(), SkydriveLog.class);
				startActivityForResult(openSkydrive, 0);
				
			}
		});
        
        MoveCloud.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openSaveScreen = new Intent(v.getContext(), SaveScreen.class);
				startActivityForResult(openSaveScreen, 0);
			}
			
		});
        MoveCloud.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Intent openUploadScreen = new Intent(v.getContext(), UploadScreen.class);
				startActivityForResult(openUploadScreen, 0);
				return true;
			}
		});
        
        //Dropbox logged in stuff.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        checkAppKeySetup();
        setDropboxLog(mApi.getSession().isLinked());
        
        //Skydrive logged in stuff
        mApp = (LiveSdkSampleApplication) getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);

        mInitializeDialog = ProgressDialog.show(this, "", "Initializing. Please wait...", true);
        
        mAuthClient.initialize(Arrays.asList(Config.SCOPES), new LiveAuthListener() {
            @Override
            public void onAuthError(LiveAuthException exception, Object userState) {
                mInitializeDialog.dismiss();
            }

            @Override
            public void onAuthComplete(LiveStatus status,
                                       LiveConnectSession session,
                                       Object userState) {
                mInitializeDialog.dismiss();

                if (status == LiveStatus.CONNECTED) {
                    setSkydriveLog(true);
                    mApp.setConnectClient(new LiveConnectClient(session));
                    
                } else {
                    setSkydriveLog(false);
                }
            }
        });
       
    }

	public static void setDropboxLog(boolean mLoggedIn) {
		// TODO Auto-generated method stub
		if (mLoggedIn == true)
		{
			greenLight1.setVisibility(0);
			redLight1.setVisibility(8);
		}
		else{
			greenLight1.setVisibility(8);
			redLight1.setVisibility(0);
		}
	}
	
	public static void setBoxLog(boolean mLoggedIn) {
		// TODO Auto-generated method stub
		if (mLoggedIn == true)
		{
			greenLight2.setVisibility(0);
			redLight2.setVisibility(8);
		}
		else{
			greenLight2.setVisibility(8);
			redLight2.setVisibility(0);
		}
	}
	
	public static void setSkydriveLog(boolean mLoggedIn) {
		// TODO Auto-generated method stub
		if (mLoggedIn == true)
		{
			greenLight3.setVisibility(0);
			redLight3.setVisibility(8);
		}
		else{
			greenLight3.setVisibility(8);
			redLight3.setVisibility(0);
		}
	}
	
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
	
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
