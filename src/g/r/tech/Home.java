package g.r.tech;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.view.accessibility.AccessibilityNodeInfo;

public class Home extends Activity {
    /** Called when the activity is first created. */
    
	static //Menu buttons screen1 HOME
	RadioButton DropboxLog;

	//Menu buttons screen1 HOME
	RadioButton GoogleLog;

	//Menu buttons screen1 HOME
	RadioButton SkydriveLog; 
	
	Button UploadScreen, SaveScreen;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        DropboxLog = (RadioButton) findViewById(R.id.Dropboxbutton);
        GoogleLog = (RadioButton) findViewById(R.id.Googlebutton);
        SkydriveLog = (RadioButton) findViewById(R.id.Skydrivebutton);
        UploadScreen = (Button) findViewById(R.id.Upcloud);
        SaveScreen = (Button) findViewById(R.id.Downcloud); 
        
        //click listeners for buttons
        
 

        
        
        DropboxLog.setOnClickListener(new View.OnClickListener() {
        	
        	
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openDropbox = new Intent(v.getContext(), DropboxLog.class);
    			startActivityForResult(openDropbox,0);
    			DropboxLog.setChecked(false);

			}
			
						
		});
        
        
        
        
        GoogleLog.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openGoogle = new Intent(v.getContext(), GoogleLog.class);
				startActivityForResult(openGoogle, 0);
				GoogleLog.setChecked(false);
				
			}
			
		});
        
        
        SkydriveLog.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openSkydrive = new Intent(v.getContext(), SkydriveLog.class);
				startActivityForResult(openSkydrive, 0);
				SkydriveLog.setChecked(false);
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

	public static  void setDropboxLog(boolean mLoggedIn) {
		// TODO Auto-generated method stub
		DropboxLog.setChecked(mLoggedIn);
	}
	
	

}
