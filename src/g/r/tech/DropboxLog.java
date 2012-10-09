package g.r.tech;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//HEY THERE
//testing testing
//testing
//testing3
//testing4
//testing5
//this is the last test before start making changes
//dude where is my change?
public class DropboxLog extends Activity {
    /** Called when the activity is first created. */
	
	Button login;
	Button logout;
	TextView header1;
	TextView header2;
	TextView logo;
	EditText email;
	EditText password;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        login = (Button) findViewById(R.id.bLogin);
        logout = (Button) findViewById(R.id.bLogout);
        header1 = (TextView) findViewById(R.id.tvHeader1);
        logo = (TextView) findViewById(R.id.dropboxHeader);
        email = (EditText) findViewById(R.id.dropboxEmail);
        password = (EditText) findViewById(R.id.dropboxPassword);
        
        
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
