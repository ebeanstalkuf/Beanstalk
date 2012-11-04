package g.r.tech;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class Home extends Activity {
    /** Called when the activity is first created. */
    
	static //Menu buttons screen1 HOME
	Button DropboxLog;

	//Menu buttons screen1 HOME
	Button GoogleLog;

	//Menu buttons screen1 HOME
	Button SkydriveLog; 
	
	Button MoveCloud;
	
	static ImageView greenLight1, greenLight2, greenLight3, redLight1, redLight2, redLight3;
	
    final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";

    // If you'd like to change the access type to the full Dropbox instead of
    // an app folder, change this value.
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    DropboxAPI<AndroidAuthSession> mApi;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        DropboxLog = (Button) findViewById(R.id.Dropboxbutton);
        GoogleLog = (Button) findViewById(R.id.Googlebutton);
        SkydriveLog = (Button) findViewById(R.id.Skydrivebutton);
        MoveCloud = (Button) findViewById(R.id.moveCloud); 
        greenLight1 = (ImageView) findViewById(R.id.dropboxGreenLight);
        greenLight2 = (ImageView) findViewById(R.id.googleGreenLight);
        greenLight3 = (ImageView) findViewById(R.id.skydriveGreenLight);
        redLight1 = (ImageView) findViewById(R.id.dropboxRedLight);
        redLight2 = (ImageView) findViewById(R.id.googleRedLight);
        redLight3 = (ImageView) findViewById(R.id.skydriveRedLight);
        
        //click listeners for buttons
        
 

        
        
        DropboxLog.setOnClickListener(new View.OnClickListener() {
        	
        	
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openDropbox = new Intent(v.getContext(), DropboxLog.class);
    			startActivityForResult(openDropbox,0);
    			

			}
			
						
		});
        
        
        
        
        GoogleLog.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openGoogle = new Intent(v.getContext(), GoogleLog.class);
				startActivityForResult(openGoogle, 0);
				
				
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
				Intent openUploadScreen = new Intent(v.getContext(), UploadScreen.class);
				startActivityForResult(openUploadScreen, 0);
			}
			
		});
        MoveCloud.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Intent openSaveScreen = new Intent(v.getContext(), SaveScreen.class);
				startActivityForResult(openSaveScreen, 0);
				return true;
			}
		});
        
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        checkAppKeySetup();

        // Display the proper UI state if logged in or not
        //setLoggedIn(mApi.getSession().isLinked());
        setDropboxLog(mApi.getSession().isLinked());
       
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
	
	public static void setGoogleLog(boolean mLoggedIn) {
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
            //showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
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
            /*showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);*/
            finish();
        }
    }

}
