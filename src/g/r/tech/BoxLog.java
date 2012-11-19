package g.r.tech;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.User;
import com.box.androidlib.ResponseListeners.GetAccountInfoListener;
import com.box.androidlib.ResponseListeners.LogoutListener;

//Sean Farrell changes test

public class BoxLog extends Activity {
    /** Called when the activity is first created. */
	
	
	Button login;
	Button logout;
	Button register;
	TextView header1;
	TextView logState;
	ImageView logo;
	
	private String authToken;

    private static boolean mLoggedIn;
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_login);
        login = (Button) findViewById(R.id.bLogin);
        logout = (Button) findViewById(R.id.bLogout);
        register = (Button) findViewById(R.id.bRegister);
        header1 = (TextView) findViewById(R.id.tvHeader1);
        logo = (ImageView) findViewById(R.id.boxHeader);
        logState = (TextView) findViewById(R.id.logState);
        
        if(mLoggedIn) {
        	logState.setText(R.string.signedIn);
        } else {
        	logState.setText(R.string.signedOut);
        }
        
        login.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//if(!mLoggedIn)
				{
	                Intent intent = new Intent(BoxLog.this, Authentication.class);
	                startActivity(intent);
                    
                }
			}
		});
        
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Box.getInstance(Constants.API_KEY).logout(authToken, new LogoutListener() {

                    @Override
                    public void onIOException(IOException e) {
                        Toast.makeText(getApplicationContext(),
                            "Logout failed - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete(String status) {
                        if (status.equals(LogoutListener.STATUS_LOGOUT_OK)) {
                            // Delete stored auth token and send user back to
                            // splash page
                            final SharedPreferences prefs = getSharedPreferences(
                                Constants.PREFS_FILE_NAME, 0);
                            final SharedPreferences.Editor editor = prefs.edit();
                            editor.remove(Constants.PREFS_KEY_AUTH_TOKEN);
                            editor.commit();
                            Toast
                                .makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG)
                                .show();
                            logOut();
                            //Intent i = new Intent(Dashboard.this, Splash.class);
                            //startActivity(i);
                            //finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Logout failed - " + status,
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        
        register.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( "https://www.box.com/pricing/" ) );
			    startActivity( browse );
			}
		});

        

        // Display the proper UI state if logged in or not
        //setLoggedIn();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        

        logState.setText(getResources().getString(R.string.checking_login_status));
        //homeButton.setVisibility(View.GONE);
        //authenticateButton.setVisibility(View.GONE);

        // Check if we have an auth token stored as shared_prefs
        final SharedPreferences prefs = getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
        authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);
        if (authToken == null) {
            logOut();
        } else {
            // We have an auth token. Let's execute getAccountInfo() and put the
            // user's e-mail address up on the screen.
            // This request will also serve as a way for us to verify that the
            // auth token is actually still valid.
            final Box box = Box.getInstance(Constants.API_KEY);
            box.getAccountInfo(authToken, new GetAccountInfoListener() {
                @Override
                public void onComplete(final User boxUser, final String status) {
                    // see http://developers.box.net/w/page/12923928/ApiFunction_get_account_info for possible status codes
                    if (status.equals(GetAccountInfoListener.STATUS_GET_ACCOUNT_INFO_OK) && boxUser != null) {
                        logState.setText(R.string.signedIn);
                    } else {
                        // Could not get user info. It's possible the auth token
                        // was no longer valid. Check the status code that was
                        // returned.
                        logOut();
                    }
                }

                @Override
                public void onIOException(IOException e) {
                    // No network connection?
                    e.printStackTrace();
                    logOut();
                }
            });
        }
    }
    
    
    private void logOut() {
        // Remove credentials from the session
 
        // Change UI state to display logged out version
        setLoggedIn(false);
        Home.setBoxLog(false);
        logState.setText(R.string.signedOut);
    }

    /**
     * Convenience function to change UI state based on being logged in
     */
    private void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;
    	if (loggedIn==true) {
    		logState.setText(R.string.signedIn);
    		Home.setDropboxLog(loggedIn);
    	} else {
    	}
    } //SUCCESS
}
