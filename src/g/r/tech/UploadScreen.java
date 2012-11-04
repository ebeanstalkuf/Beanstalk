package g.r.tech;

//import android.os.Handler;			//needed for the upload all image used in ACTION_DRAG_ENDED
import java.io.File;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class UploadScreen extends Activity implements OnDragListener,
OnItemLongClickListener {
    /** Called when the activity is first created. */
	DropboxAPI<AndroidAuthSession> dropApi;
    final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
   
    
    Context context;
    int flag;
    Button allServices;
    
    //variables for collision test
    ArrayList filesToshare;
	private BaseAdapter adapter;
	private int draggedIndex = -1;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.upload);
        allServices = (Button) findViewById(R.id.uploadall);
        
        //array list for collision
        filesToshare = new ArrayList();
        
        
        
        filesToshare.add(R.drawable.photo);
        GridView gridView = (GridView) findViewById(R.id.default_file);
        gridView.setOnItemLongClickListener(UploadScreen.this);
        
        gridView.setAdapter(adapter = new BaseAdapter() {

			@Override
			// Get a View that displays the data at the specified position in
			// the data set.
			public View getView(int position, View convertView,
					ViewGroup gridView) {
				// try to reuse the views.
				ImageView view = (ImageView) convertView;
				// if convert view is null then create a new instance else reuse
				// it
				if (view == null) {
					view = new ImageView(UploadScreen.this);
				}
				view.setImageResource((Integer) filesToshare.get(position));
				view.setTag(String.valueOf(position));
				return view;
			}

			@Override
			// Get the row id associated with the specified position in the
			// list.
			public long getItemId(int position) {
				return position;
			}

			@Override
			// Get the data item associated with the specified position in the
			// data set.
			public Object getItem(int position) {
				return filesToshare.get(position);
			}

			@Override
			// How many items are in the data set represented by this Adapter.
			public int getCount() {
				return filesToshare.size();
			}
		});
	}

	//@Override
	//public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.activity_main, menu);
		//return true;
	//}

	
	@Override
	public boolean onDrag(View view, DragEvent dragEvent) {
		switch (dragEvent.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			// Drag has started
			// If called for trash resize the view and return true
			allServices.setVisibility(0);
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.googledrive || view.getId() == R.id.otherservices ) {
				view.animate().scaleX(1.0f);
				view.animate().scaleY(1.0f);
				return true;
			} else // else check the mime type and set the view visibility
			if (dragEvent.getClipDescription().hasMimeType(
					ClipDescription.MIMETYPE_TEXT_PLAIN)) {
				view.setVisibility(View.GONE);
				return true;

			} else {
				return false;
			}
		case DragEvent.ACTION_DRAG_ENTERED:
			// Drag has entered view bounds
			// If called for trash can then scale it.
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.googledrive || view.getId() == R.id.otherservices) {
				view.animate().scaleX(1.5f);
				view.animate().scaleY(1.5f);
			}
			return true;
		case DragEvent.ACTION_DRAG_EXITED:
			// Drag exited view bounds
			// If called for trash can then reset it.
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.googledrive || view.getId() == R.id.otherservices) {
				view.animate().scaleX(1.0f);
				view.animate().scaleY(1.0f);
			}
			view.invalidate();
			return true;
		case DragEvent.ACTION_DRAG_LOCATION:
			// Ignore this event
			return true;
		case DragEvent.ACTION_DROP:
			// Dropped inside view bounds
			// If called for trash can then delete the item and reload the grid
			// view
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.googledrive || view.getId() == R.id.otherservices) {
				File file = new File("beanstalk.jpg");
				uploadDropbox(file);
				filesToshare.remove(draggedIndex);
				draggedIndex = -1;
			}
			adapter.notifyDataSetChanged();
		case DragEvent.ACTION_DRAG_ENDED:
			// Hide the trash can
			allServices.setVisibility(4);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					findViewById(R.id.dropbox).setVisibility(View.VISIBLE);
					findViewById(R.id.skydrive).setVisibility(View.VISIBLE);
					findViewById(R.id.googledrive).setVisibility(View.VISIBLE);
					findViewById(R.id.otherservices).setVisibility(View.VISIBLE);
				}
			}, 1000l);
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.googledrive || view.getId() == R.id.otherservices) {
				view.animate().scaleX(1.0f);
				view.animate().scaleY(1.0f);
			} else {
				view.setVisibility(View.VISIBLE);
			}
			// remove drag listeners
			view.setOnDragListener(null);
			return true;

		}
		return false;
	}

    protected void displayToast(String message)
    {
    	Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	msg.show();
    }
    
    public void uploadDropbox(File file)
    {
	    AndroidAuthSession session = buildSession();
	    if(flag == 1)
	    {
	    	displayToast("You have not logged into Dropbox!");
	    }
	    else
	    {
	    	dropApi = new DropboxAPI<AndroidAuthSession>(session);
		    if(flag == 1)
		    {
		    	displayToast("You have not logged into Dropbox!");
		    }
		    else
		    {
		    	//testing
			    boolean sdStatus = checkSDCardStatus();
			    String sdpath;
			    if(sdStatus)
			    {
			    	sdpath = Environment.getExternalStorageDirectory().getPath() + "/Beanstalk Downloads/";
			    	file = new File(sdpath + "beanstalk.jpg");
			    	
					displayToast("Uploading from: " + sdpath + file.getName());
					sdpath = "/Beanstalk/";
			    	//end testing
			    	UploadFile uploadDrop = new UploadFile(UploadScreen.this, dropApi, sdpath, file);
			    	uploadDrop.execute();	
			    }

		    }
	    }
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
    
    private boolean checkSDCardStatus()
    {
    	String mErrorMsg;
    	String sdcardstatus = Environment.getExternalStorageState();
        if(sdcardstatus.equals(Environment.MEDIA_REMOVED))
        {
        	mErrorMsg ="Error: Your device is not showing an SD Card. Beanstalk can only upload a file from an SD card";
        	displayToast(mErrorMsg);
        	return false;
        }
        //Make sure SD Card is mounted
        if(sdcardstatus.equals(Environment.MEDIA_MOUNTED))
        {
        	return true;
        }
        else
        {
        	mErrorMsg = "Error: Your device's SD Card is not mounted. Beanstalk can only upload a file from an SD card";
        	displayToast(mErrorMsg);
        	return false;
        }
    }
	
	@Override
	public boolean onItemLongClick(AdapterView gridView, View view,
			int position, long row) {
		ClipData.Item item = new ClipData.Item((String) view.getTag());
		ClipData clipData = new ClipData((CharSequence) view.getTag(),
				new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }, item);
		view.startDrag(clipData, new View.DragShadowBuilder(view), null, 0);
		
		View DropBox = findViewById(R.id.dropbox);
		DropBox.setVisibility(View.VISIBLE);
		DropBox.setOnDragListener(UploadScreen.this);
		DropBox.setOnDragListener(UploadScreen.this);
		
		View SkyDrive = findViewById(R.id.skydrive);
		SkyDrive.setVisibility(View.VISIBLE);
		SkyDrive.setOnDragListener(UploadScreen.this);
		SkyDrive.setOnDragListener(UploadScreen.this);
		
		View GoogleDrive = findViewById(R.id.googledrive);
		GoogleDrive.setVisibility(View.VISIBLE);
		GoogleDrive.setOnDragListener(UploadScreen.this);
		GoogleDrive.setOnDragListener(UploadScreen.this);
		
		View OtherServices = findViewById(R.id.otherservices);
		OtherServices.setVisibility(View.VISIBLE);
		OtherServices.setOnDragListener(UploadScreen.this);
		OtherServices.setOnDragListener(UploadScreen.this);


		draggedIndex = position;
		return true;
	
        
     /*   pre - version
       findViewById(R.id.Upcloud).setOnTouchListener(new MyTouchListener());
    }
    
    public void showToast(String msg) {
	    Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
	    error.show();
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
	            view.setVisibility(View.VISIBLE);
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
				
	         // default:
	         //   break;
         // }
        //  return true;
      //  }
        
        //
      }

			

	
    
}
