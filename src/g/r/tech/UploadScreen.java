package g.r.tech;

//import android.os.Handler;			//needed for the upload all image used in ACTION_DRAG_ENDED
import java.io.File;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class UploadScreen extends Activity {
    /** Called when the activity is first created. */
	DropboxAPI<AndroidAuthSession> dropApi;
    final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    Context context;
    int flag;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.upload);
        
       findViewById(R.id.Upcloud).setOnTouchListener(new MyTouchListener());
    }
    
    public void showToast(String msg) {
	    Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
	    error.show();
	}
    
    public void uploadFile()
    {
    	
    }
    
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;
        flag = 0;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
            flag = 1;
        }

        return session;
    }
    
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
    
    
    private final class MyTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
          if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        	  //showToast("Dragging Something!!!");
            //ClipData data = ClipData.newPlainText("", "");
      		ClipData.Item item = new ClipData.Item((String) view.getTag());
    		ClipData data = new ClipData((CharSequence) view.getTag(),
    				new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }, item);
            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            
           // View uploadAll = findViewById(R.id.uploadall);
    		//uploadAll.setVisibility(View.VISIBLE);
    		//uploadAll.setOnDragListener(MainActivity.this);
    		
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.VISIBLE);
            return true;
          } else {
            return false;
          }
        }
      }
    
    class MyDragListener implements OnDragListener {
        
        Drawable movingCloud = getResources().getDrawable(R.drawable.upcloud);
        //this may be where we can substitute for the attached file preview; just make it invisible prior to drag
//        Drawable movingFile = getResources().getDrawable(R.drawable.default_file);
        //movingFile.setVisibility(0); 
       
    	public boolean onDrag(View v, DragEvent event) {
          int action = event.getAction();
          switch (event.getAction()) {
	          case DragEvent.ACTION_DRAG_STARTED:
	            // Do nothing
	            break;
	          case DragEvent.ACTION_DRAG_ENTERED:
	            v.setBackgroundDrawable(movingCloud);
	           // v.setBackgroundDrawable(movingFile);
	            break;
	          case DragEvent.ACTION_DRAG_EXITED:
	            v.setBackgroundDrawable(movingCloud);
	           //v.setBackgroundDrawable(movingFile);
	            break;
	          case DragEvent.ACTION_DROP:
	            // Dropped, reassign View to ViewGroup	        	  
	            /*View view = (View) event.getLocalState();
	            ViewGroup owner = (ViewGroup) view.getParent();
	            owner.removeView(view);
	            LinearLayout container = (LinearLayout) v;
	            container.addView(view);
	            view.setVisibility(View.VISIBLE);*/
	  			if (v.getId() == R.id.dropbox) {
		            showToast("Doing something!");
				}

			    AndroidAuthSession session = buildSession();
			    if(flag == 1)
			    {
			    	showToast("You have not logged into Dropbox!");
			    }
			    else
			    {
			    	dropApi = new DropboxAPI<AndroidAuthSession>(session);
			    	// Find the ListView resource.   
					//dbListView = (ListView) findViewById(android.R.id.list);
					//Find the Textview resource
					//t=(TextView)findViewById(R.id.filebrowserpath);
			    	String dropPath = Environment.getExternalStorageDirectory().getPath();
			    	File file = new File("beanstalk.jpg");
					//Find the Button resource
			    	UploadFile upload = new UploadFile(context, dropApi, dropPath, file);
			    	upload.execute();	
			    	return true;
			    }
	            break;
	          case DragEvent.ACTION_DRAG_ENDED:
	            v.setBackgroundDrawable(movingCloud);
				
	            //set the image of the circle with all services to disappear when stopped dragging
				/*new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						findViewById(R.id.uploadall).setVisibility(View.GONE);
					}
				}, 1000l);*/
				
	          default:
	            break;
          }
          return true;
        }
      }
    
}
