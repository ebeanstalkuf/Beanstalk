package com.nimbus.app.beanstalk;

//import android.os.Handler;			//needed for the upload all image used in ACTION_DRAG_ENDED
import com.nimbus.app.beanstalk.R;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.DragEvent;
import android.view.Menu;
import android.widget.ShareActionProvider;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveConnectClient;

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
    final static public boolean UPLOAD_ALL_ON = true;
    final static public boolean UPLOAD_ALL_OFF = false;
   
    //SkyDrive variables
    private LiveConnectClient mClient;
    private static final String SKYDRIVE_HOME = "me/skydrive";
    private LiveSdkSampleApplication mApp;
    private ProgressDialog mInitializeDialog;
    private boolean loggedIn;
    File skyFile;
    LiveAuthClient mAuth;
	static String skyFolderID;
	static boolean skyFolderFound;
    
    Context context;
    int flag;
    //Button allServices;
    Button homeGear;
    //cloud and container on upload screen that holds the files and disappears when dragged
    GridView uploadcloud, cloudcontainer;
    static File sharefile = null;
    static int remove = 1;
    
    //variables for collision test
    ArrayList filesToshare;
	private BaseAdapter adapter;
	private int draggedIndex = -1;
    //Animation animScale;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.upload);        
        uploadcloud = (GridView) findViewById(R.id.Upcloud);
        cloudcontainer = (GridView) findViewById(R.id.default_file);
        //allServices = (Button) findViewById(R.id.uploadall);
        homeGear = (Button) findViewById(R.id.settings);
        
       //animScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        
        
        //array list for collision
        filesToshare = new ArrayList();
        if(sharefile == null)
        {
        	displayToast("Well this is embarassing...I don't know what to upload! Please go back to the file browser and select your file again.");
        	return;
        }    
        
		String fileName = sharefile.getName();
        String extensionType = filename(fileName);
        
        if( extensionType.equalsIgnoreCase("jpg") || extensionType.equalsIgnoreCase("png") || extensionType.equalsIgnoreCase("gif") 
        	|| extensionType.equalsIgnoreCase("bmp") || extensionType.equalsIgnoreCase("psd") || extensionType.equalsIgnoreCase("tif") 
        	|| extensionType.equalsIgnoreCase("tiff") || extensionType.equalsIgnoreCase("ai") || extensionType.equalsIgnoreCase("svg"))
        {
        	filesToshare.add(R.drawable.photo_upload);
        }
        else if( extensionType.equalsIgnoreCase("mp3") || extensionType.equalsIgnoreCase("m4a") || extensionType.equalsIgnoreCase("wav") 
        		|| extensionType.equalsIgnoreCase("flac") || extensionType.equalsIgnoreCase("aac") || extensionType.equalsIgnoreCase("m4p")
        		|| extensionType.equalsIgnoreCase("mmf") || extensionType.equalsIgnoreCase("ogg") || extensionType.equalsIgnoreCase("Opus")
        		|| extensionType.equalsIgnoreCase("raw") || extensionType.equalsIgnoreCase("vox") || extensionType.equalsIgnoreCase("wma") 
        		|| extensionType.equalsIgnoreCase("alac") || extensionType.equalsIgnoreCase("aiff"))
        {
        	filesToshare.add(R.drawable.music_upload);
        }
        else if( extensionType.equalsIgnoreCase("mov") || extensionType.equalsIgnoreCase("divx") || extensionType.equalsIgnoreCase("xvid")
        		|| extensionType.equalsIgnoreCase("asf") || extensionType.equalsIgnoreCase("avi") || extensionType.equalsIgnoreCase("m1v")
        		|| extensionType.equalsIgnoreCase("m2v") || extensionType.equalsIgnoreCase("m4v") || extensionType.equalsIgnoreCase("fla")
        		|| extensionType.equalsIgnoreCase("flv") || extensionType.equalsIgnoreCase("sol") || extensionType.equalsIgnoreCase("mpeg")
        		|| extensionType.equalsIgnoreCase("mpe") || extensionType.equalsIgnoreCase("mpg") || extensionType.equalsIgnoreCase("MP4")
        		|| extensionType.equalsIgnoreCase("wmv") || extensionType.equalsIgnoreCase("swf") || extensionType.equalsIgnoreCase("fcp")
        		|| extensionType.equalsIgnoreCase("ppj") )
        {
        	filesToshare.add(R.drawable.video_upload);
        }
        else if( extensionType.equalsIgnoreCase("zip") || extensionType.equalsIgnoreCase("tar") || extensionType.equalsIgnoreCase("iso") 
        		|| extensionType.equalsIgnoreCase("bz2") || extensionType.equalsIgnoreCase("gz") || extensionType.equalsIgnoreCase("lz")
        		|| extensionType.equalsIgnoreCase("lzma") || extensionType.equalsIgnoreCase("lzo") || extensionType.equalsIgnoreCase("xz")
        		|| extensionType.equalsIgnoreCase("z") || extensionType.equalsIgnoreCase("7z") || extensionType.equalsIgnoreCase("s7z") 
        		|| extensionType.equalsIgnoreCase("apk") || extensionType.equalsIgnoreCase("zz") || extensionType.equalsIgnoreCase("zipx") || extensionType.equalsIgnoreCase("tbz2") 
        		|| extensionType.equalsIgnoreCase("tgz") || extensionType.equalsIgnoreCase(".tar.gz") || extensionType.equalsIgnoreCase("rar") || extensionType.equalsIgnoreCase("sit"))
        {
        	filesToshare.add(R.drawable.zip_upload);
        }
        else
        {
        	filesToshare.add(R.drawable.document_upload);
        }
        GridView gridView = (GridView) findViewById(R.id.default_file);
        gridView.setOnItemLongClickListener(UploadScreen.this);
        gridView.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				 if(event.getAction() == MotionEvent.ACTION_MOVE){
	                    return true;
	                }
	                return false;
			}

        });
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
    
        homeGear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDatShit();
			}
		});

        
	}
    
    public void closeDatShit()
    {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    mPrefs.edit().putBoolean(SaveScreen.CLOSE_A_ON_RESUME,true).commit();
	    finish();
    }
	public void onPause()
    {
    	super.onPause();
    	remove();
    	sharefile = null;
    }
    public void onDestroy()
    {
    	super.onDestroy();
    	remove();
    	sharefile = null;
    }
    public void onStop()
    {
    	super.onStop();
    	remove();
    	sharefile = null;
    }
    public void onRestart()
    {
    	super.onRestart();
    	remove();
    	sharefile = null;
    }
	
    //Share Option in Action Bar
    private ShareActionProvider myShareActionProvider;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.share);
        myShareActionProvider = (ShareActionProvider)item.getActionProvider();
        myShareActionProvider.setShareHistoryFileName(
        ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        myShareActionProvider.setShareIntent(createShareIntent());
        return true;
    }
    
    private Intent createShareIntent() {
    	  Intent shareIntent = new Intent(Intent.ACTION_SEND); 
    	  
    	  shareIntent.putExtra(Intent.EXTRA_EMAIL, "");  
    	  shareIntent.putExtra(Intent.EXTRA_SUBJECT, sharefile.getName()); 
    	  shareIntent.putExtra(Intent.EXTRA_TEXT, "File shared by Beanstalk"); 
    	  
    	  
    	  //For some reason right now it is sending the file name and text but does not attach the file.
    	  if(sharefile != null){	  
    	   shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharefile));
    	   //Toast.makeText(this, "share intent with Attachment is made", Toast.LENGTH_SHORT).show();
    	   shareIntent.setType( "*/*" ); //allows us to share any file type that sharefile may be
    	  }
    	  	 //Toast.makeText(this, "share intent created", Toast.LENGTH_SHORT).show();
    	     return shareIntent; 
    }
	
    private void setShareIntent(Intent shareIntent) {
    	  if (myShareActionProvider != null) {
    	   myShareActionProvider.setShareIntent(shareIntent); 
    	  } 
    }
    
/*	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.email:
	      Toast.makeText(this, "email selected", Toast.LENGTH_SHORT)
	          .show();
	      break;
	    case R.id.mms:
	      Toast.makeText(this, "mms selected", Toast.LENGTH_SHORT)
	          .show();
	      break;

	    default:
	      break;
	    }
	    return true;
	  }*/
	
	@Override
	public boolean onDrag(View view, DragEvent dragEvent) {
		switch (dragEvent.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			// Drag has started
			// If called for trash resize the view and return true

			//allServices.setVisibility(0);
			uploadcloud.setVisibility(4);
			cloudcontainer.setVisibility(4);
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices || view.getId() == R.id.uploadall) {
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
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices || view.getId() == R.id.uploadall) {
				//view.bringToFront();
				view.animate().scaleX(1.2f);
				view.animate().scaleY(1.2f);
			}
			return true;
		case DragEvent.ACTION_DRAG_EXITED:
			// Drag exited view bounds
			// If called for trash can then reset it.
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices || view.getId() == R.id.uploadall) {

				view.animate().scaleX(1.0f);
				view.animate().scaleY(1.0f);
				//view.invalidate();
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
			if (view.getId() == R.id.dropbox ) {
				//upload to dropbox 
				File file = null;//new File("beanstalk.jpg");
				uploadDropbox(file);
				//filesToshare.remove(draggedIndex);
				//draggedIndex = -1;
			}
			else if(view.getId() == R.id.skydrive)
			{
				//upload to skydrive
				if(sharefile == null)
				{
					displayToast("Well this is embarassing...I don't know what to upload! Please go back to the file browser and select your file again.");
				}
				else
				{
					File file = sharefile;
					//displayToast("Starting upload to SkyDrive!");
					uploadSkyDrive(file, SKYDRIVE_HOME);
					//filesToshare.remove(draggedIndex);
					//draggedIndex = -1;
				}
			}
			else if(view.getId() == R.id.box)
			{
				if(sharefile == null)
				{
					displayToast("Well this is embarassing...I don't know what to upload! Please go back to the file browser and select your file again.");
				}
				else
				{
					//Box upload code
					UploadBox upload = new UploadBox(this, 0l, sharefile);
					upload.run();
					//filesToshare.remove(draggedIndex);
					//draggedIndex = -1;
				}
			}
			else if(view.getId() == R.id.uploadall)
			{
				uploadAll();
			}
			else if(view.getId() == R.id.otherservices)
			{
				if(sharefile == null)
				{
					displayToast("Well this is embarassing...I don't know what to upload! Please go back to the file browser and select your file again.");
				}
				else
				{
					remove = 0;
					displayToast("Yahoo! Successfully saved " + sharefile.getName() + " to your SD card!");
				}
			}
			adapter.notifyDataSetChanged();
		case DragEvent.ACTION_DRAG_ENDED:
			// Hide the trash can
			//allServices.setVisibility(4);
			uploadcloud.setVisibility(0);
			cloudcontainer.setVisibility(0);
			//view.invalidate();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					findViewById(R.id.dropbox).setVisibility(View.VISIBLE);
					findViewById(R.id.skydrive).setVisibility(View.VISIBLE);
					findViewById(R.id.box).setVisibility(View.VISIBLE);
					findViewById(R.id.otherservices).setVisibility(View.VISIBLE);
				}
			}, 1000l);
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices || view.getId() == R.id.uploadall) {
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
	
    private void uploadSkyDrive(File upFile, String uploadPath)
    {
    	mClient = authSkyDrive();
    	if(mClient == null)
    	{
    		return;
    	}
    	skyFile = upFile;
    	
    	UploadSkyDrive upSky = new UploadSkyDrive(this, upFile, mClient);
    	upSky.execute();
    }
    
    private LiveConnectClient authSkyDrive()
    {
    	//Skydrive logged in stuff    	
		mApp = (LiveSdkSampleApplication) getApplication();
        mClient = mApp.getConnectClient();
        if(mClient == null)
        {
        	displayToast("Shucks! I can't see anything over here. Try logging into Skydrive.");
        	loggedIn = false;
        	return mClient;
        }
        loggedIn = true;
        //filterSkyDrive(SKYDRIVE_HOME);
        return mClient;
    }
    
    public void uploadDropbox(File file)
    {
	    AndroidAuthSession session = buildSession();
	    if(flag == 1)
	    {
	    	displayToast("Shucks! I can't see anything over here. Try logging into Dropbox.");
	    }
	    else
	    {
	    	dropApi = new DropboxAPI<AndroidAuthSession>(session);
	    	if(sharefile == null)
			{
	    		displayToast("Well this is embarassing...I don't know what to save! Please go back to the file browser and select your file again.");
			}
			else
			{			    					    	
				String uploadPath = "/Beanstalk/";
				//sharefile is the static variable
				//If files is null, don't allow upload
				UploadDropbox uploadDrop = new UploadDropbox(UploadScreen.this, dropApi, uploadPath, sharefile);
				uploadDrop.execute();	
			}
		    
	    }
    }
    
    private void uploadAll()
    {
    	if(sharefile == null)
		{
    		displayToast("Well this is embarassing...I don't know what to save! Please go back to the file browser and select your file again.");
		}
		else
		{
			displayToast("Starting upload of " + sharefile.getName() + " to all services!");
			//Set it so the file will not be removed
			remove = 0;
			mClient = authSkyDrive();
			AndroidAuthSession session = buildSession();
			dropApi = new DropboxAPI<AndroidAuthSession>(session);
    				    					    	
			String uploadPath = "/Beanstalk/";
			//sharefile is the static variable
			//If files is null, don't allow upload
			if(flag != 1){
				UploadDropbox uploadDrop = new UploadDropbox(UploadScreen.this, dropApi, uploadPath, sharefile, mClient, UPLOAD_ALL_ON);
				uploadDrop.execute();	
			}
			else
			{
				displayToast("Shucks! I can't see anything over here. Try logging into Dropbox.");
	    		UploadBox uploadBox = new UploadBox(UploadScreen.this, 0l, sharefile, mClient, UploadScreen.UPLOAD_ALL_ON);
	    		uploadBox.run();
			}
		}
    	//displayToast("Uploading Complete!");
    }
    
    private int computePercentCompleted(int totalBytes, int bytesRemaining) {
        return (int) (((float)(totalBytes - bytesRemaining)) / totalBytes * 100);
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
        	mErrorMsg ="Your device is not showing an SD Card. Beanstalk needs an SD card to work properly.";
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
        	mErrorMsg = "Your device is not showing an SD Card. Beanstalk needs an SD card to work properly.";
        	displayToast(mErrorMsg);
        	return false;
        }
    }
    
    protected void displayToast(String message)
    {
    	Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	msg.show();
    }
	
    /*WORK IN PROGRESS FOR CUSTOM DRAG SHADOW VIEW
    //Building custom ShadowBuilder so icon size when dragged is larger
    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
    	 private static Drawable shadow;

    	 //overrides the original shadow builder and pulls in the various components
    	 public MyDragShadowBuilder(View v) {
    	  super(v);
    	  shadow = new ColorDrawable(Color.LTGRAY);
    	    }

    	 //gathers the views existing size measurements and doubles it
    	 @Override
    	    public void onProvideShadowMetrics (Point size, Point touch){
    	        int width = 2 * getView().getWidth();
    	        int height = 2 * getView().getHeight();

    	        shadow.setBounds(0, 0, width, height);
    	        size.set(width, height);
    	        touch.set(width / 2, height / 2);
    	    }

    	 	//creates the canvas that draws the new view
    	    @Override
    	    public void onDrawShadow(Canvas canvas) {
    	        shadow.draw(canvas);
    	    }

    	}*/
    
    
    
	@Override
	public boolean onItemLongClick(AdapterView gridView, View view,
			int position, long row) {
		ClipData.Item item = new ClipData.Item((String) view.getTag());
		ClipData clipData = new ClipData((CharSequence) view.getTag(),
				new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }, item);
		
		//Custom dragshadow builder
		//DragShadowBuilder myShadow = new MyDragShadowBuilder(view);
		
		view.startDrag(clipData, new View.DragShadowBuilder(view), null, 0);
		
		View DropBox = findViewById(R.id.dropbox);
		DropBox.setVisibility(View.VISIBLE);
		DropBox.setOnDragListener(UploadScreen.this);
		DropBox.setOnDragListener(UploadScreen.this);
		
		View SkyDrive = findViewById(R.id.skydrive);
		SkyDrive.setVisibility(View.VISIBLE);
		SkyDrive.setOnDragListener(UploadScreen.this);
		SkyDrive.setOnDragListener(UploadScreen.this);
		
		View GoogleDrive = findViewById(R.id.box);
		GoogleDrive.setVisibility(View.VISIBLE);
		GoogleDrive.setOnDragListener(UploadScreen.this);
		GoogleDrive.setOnDragListener(UploadScreen.this);
		
		View OtherServices = findViewById(R.id.otherservices);
		OtherServices.setVisibility(View.VISIBLE);
		OtherServices.setOnDragListener(UploadScreen.this);
		OtherServices.setOnDragListener(UploadScreen.this);
		
		View UploadAll = findViewById(R.id.uploadall);
		UploadAll.setVisibility(View.VISIBLE);
		UploadAll.setOnDragListener(UploadScreen.this);
		UploadAll.setOnDragListener(UploadScreen.this);


		draggedIndex = position;
		return true;
	
      }
		
	    public String filename(String fileName){
	    	
			 String filename_Without_Ext = "";
			 String ext = "";

			 int dotposition= fileName.lastIndexOf(".");
			 if(dotposition <= 0)
			 {
				 ext = "nothing";
				 return ext;
			 }
			 filename_Without_Ext = fileName.substring(0,dotposition);
			 ext = fileName.substring(dotposition + 1, fileName.length());

			 return ext;
			}
	private void remove()
	 {
		 if(sharefile != null && remove == 1)
		 {
			 sharefile.delete();
		 }
		 remove = 1;
	 }
	
    
}
