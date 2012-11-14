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
	private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;
    ListView dbListView ;
    ListView sdListView;
    private FileOutputStream mFos;
    TextView t;
    Button b;
    TextView welcomeText;
    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;
    
    
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
				sdListView.setAdapter(arrayAdapter);
		        
			}
		});
                        
        //Create listener for Dropbox file update button
        dropboxfiles = (Button) findViewById(R.id.dropbox_fileb);
        dropboxfiles.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				    AndroidAuthSession session = buildSession();
				    if(flag == 1)
				    {
				    	showToast("You have not logged into Dropbox!");
				    }
				    else
				    {
				    	mApi = new DropboxAPI<AndroidAuthSession>(session);
				    	
				    	//get rid of initial text
				        welcomeText.setVisibility(8);
				        
				     // Find the ListView resource.
						dbListView = (ListView) findViewById(android.R.id.list);
						//Find the Textview resource
						t=(TextView)findViewById(R.id.filebrowserpath);
						//Find the Button resource
						b=(Button)findViewById(R.id.top_divider);
				    	UpdateList update = new UpdateList(SaveScreen.this, mApi, dbListView, "/", t, b);
				    	update.execute();	
				    }
			}
			
		
		});
        
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

