package g.r.tech;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;
import android.widget.AdapterView.OnItemClickListener;

public class SaveScreen extends Activity {
	Button dropboxfiles;
	Button sdcardfiles;
	Button skydrivefiles;
	private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;
    ListView dbListView ;
    ListView sdListView;
    ListView skListView;
    private FileOutputStream mFos;
    TextView t;
    Button b;
    TextView welcomeText;
    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;
	String cloudService = "empty";
	UpdateSkydrive updatesk;
	UpdateList updatedb;
    
    int i = 0;
    int flag = 0;
	
	final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    
    

    
    ArrayAdapter arrayAdapter;
    
    
    public void update(File[] newList){
    	String[] paths=new String[newList.length];
    	for (int i =0; i<newList.length; i++){
    		String path=newList[i].getPath();
    		
    		if (path.contains("/mnt/sdcard/")){
    			paths[i]=path.replace("/mnt/sdcard/", "");
    		}
    	else
    			paths[i] = path;	
    	}
    	arrayAdapter = new ArrayAdapter(this,R.layout.screen5_rowlayout, R.id.label, paths);
    }

    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen5_download);
        
        welcomeText = (TextView) findViewById(R.id.initialText);
        
        sdListView = (ListView) findViewById(android.R.id.list);
        File file[] = Environment.getExternalStorageDirectory().listFiles(); 
        update(file);

       
        sdcardfiles = (Button) findViewById(R.id.sdcard_fileb);
        sdcardfiles.setOnClickListener(new View.OnClickListener() {
			
        	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		    	//get rid of initial text
		        welcomeText.setVisibility(8);
				cloudService = "dropbox";
				sdListView.setAdapter(arrayAdapter);
		        
			}
		});
                        
        //Create listener for Dropbox file update button
        dropboxfiles = (Button) findViewById(R.id.dropbox_fileb);
        // Find the ListView resource.
		dbListView = (ListView) findViewById(android.R.id.list);
        dropboxfiles.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
					welcomeText.setVisibility(8);
					cloudService = "dropbox";
				    AndroidAuthSession session = buildSession();
				    if(flag == 1)
				    {
				    	showToast("You have not logged into Dropbox!");
				    	//Clear list
				    	dbListView.setAdapter(null);
				    	cloudService = "nothing";
				    }
				    else
				    {
				    	mApi = new DropboxAPI<AndroidAuthSession>(session);
				    	
				    	//get rid of initial text
				        welcomeText.setVisibility(8);
				        
				     
				    	updatedb = new UpdateList(SaveScreen.this, mApi, dbListView, "/");
				    	updatedb.execute();	
				    }
			}
			
		
		});
        //Create listener for Dropbox file update button
        skydrivefiles = (Button) findViewById(R.id.skydrive_fileb);
        skydrivefiles.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
					welcomeText.setVisibility(8);
				    cloudService = "skydrive";
					skListView = (ListView) findViewById(android.R.id.list);
				    updatesk = new UpdateSkydrive(SaveScreen.this, skListView);
				    updatesk.run("me/skydrive");
				    cloudService = "nothing";
				    }		
		});
    }
    //Moving back to previous folder using back key
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if prev folders is empty, send the back button to the TabView activity.
        	//If currently in skydrive
            if(cloudService.equals("skydrive"))
            {
            	//If updateSkydrive uses backkey for listview
			    if(updatesk.backKeyClicked())
			    {
			    	return true;
			    }
            }
            else if(cloudService.equals("dropbox"))
            {
            	//If updateList uses backkey for listview
			    if(updatedb.backKeyClicked())
			    {
			    	return true;
			    }
            }
            return super.onKeyDown(keyCode, event);
        } 
        else 
        	{
            	return super.onKeyDown(keyCode, event);
        	}
    }
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    private AndroidAuthSession buildSession() {
    	String[] stored = null;
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        String key = prefs.getString("ACCESS_KEY", null);
        String secret = prefs.getString("ACCESS_SECRET", null);
        if (key != null && secret != null) {
        	stored = new String[2];
        	stored[0] = key;
        	stored[1] = secret;
        } 
        
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
        	session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        	flag = 1;
        }

        return session;
    }
}

