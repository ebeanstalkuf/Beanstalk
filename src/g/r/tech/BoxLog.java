package g.r.tech;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//Sean Farrell changes test

public class BoxLog extends Activity {
    /** Called when the activity is first created. */
	
	
	Button login;
	Button logout;
	Button register;
	TextView header1;
	TextView logState;
	ImageView logo;

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
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
                if (mLoggedIn) {
                    logOut();
                }
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
    protected void onResume() {
        super.onResume();

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
