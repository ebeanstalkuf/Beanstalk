package g.r.tech;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class Home extends Activity {
    /** Called when the activity is first created. */
    
	//Menu buttons screen1 HOME
	Button DropboxLog, GoogleLog, SkydriveLog, UploadScreen, SaveScreen;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen2);
        
        DropboxLog = (Button) findViewById(R.id.Dropboxbutton);
        GoogleLog = (Button) findViewById(R.id.Googlebutton);
        SkydriveLog = (Button) findViewById(R.id.Skydrivebutton);
        UploadScreen = (Button) findViewById(R.id.Upcloud);
        SaveScreen = (Button) findViewById(R.id.Downcloud);
       
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
        
        UploadScreen.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openUploadScreen = new Intent(v.getContext(), UploadScreen.class);
				startActivityForResult(openUploadScreen, 0);
			}
		});

        SaveScreen.setOnClickListener(new View.OnClickListener() {
	
        	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent openSaveScreen = new Intent(v.getContext(), SaveScreen.class);
		startActivityForResult(openSaveScreen, 0);
        	}
        });
        
       
    }

	
	
	

}