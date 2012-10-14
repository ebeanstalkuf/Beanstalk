package g.r.tech;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.ListActivity;
import android.widget.AdapterView.OnItemClickListener;

public class SaveScreen extends ListActivity {
	Button dropboxfiles;
	private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;
    ListView dbListView ;
    private FileOutputStream mFos;

    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;
   
    int i = 0;
    int flag = 0;
	
	final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen5_download);
        
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
				    	// Find the ListView resource.   
						dbListView = (ListView) findViewById(android.R.id.list);  
				    	UpdateList update = new UpdateList(SaveScreen.this, mApi, dbListView);
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

