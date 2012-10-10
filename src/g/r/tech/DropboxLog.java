package g.r.tech;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.client2.session.Session.AccessType;

//Sean Farrell changes test

public class DropboxLog extends Activity {
    /** Called when the activity is first created. */
	
	Button login;
	Button logout;
	Button back;
	TextView header1;
	TextView header2;
	TextView logo;
	
    private static final String TAG = "DBRoulette";

    ///////////////////////////////////////////////////////////////////////////
    //                      Your app-specific settings.                      //
    ///////////////////////////////////////////////////////////////////////////

    // Replace this with your app key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";

    // If you'd like to change the access type to the full Dropbox instead of
    // an app folder, change this value.
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen1);
        login = (Button) findViewById(R.id.bLogin);
        logout = (Button) findViewById(R.id.bLogout);
        back = (Button) findViewById(R.id.bBack);
        header1 = (TextView) findViewById(R.id.tvHeader1);
        logo = (TextView) findViewById(R.id.dropboxHeader);
        
        
        login.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        logout.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        back.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DropboxLog.this.finish();
				
			}
		});
    }
}
