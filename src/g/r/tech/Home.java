package g.r.tech;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.view.accessibility.AccessibilityNodeInfo;

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
	
	

}
