package g.r.tech;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//Sean Farrell changes test

public class DropboxLog extends Activity {
    /** Called when the activity is first created. */
	
	Button login;
	Button logout;
	TextView header1;
	TextView header2;
	TextView logo;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen1);
        login = (Button) findViewById(R.id.bLogin);
        logout = (Button) findViewById(R.id.bLogout);
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
    }
}
