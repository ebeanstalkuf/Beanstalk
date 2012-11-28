package g.r.tech;

//import android.os.Handler;			//needed for the upload all image used in ACTION_DRAG_ENDED
import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import skydrive.SkyDriveObject;
import skydrive.util.JsonKeys;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveUploadOperationListener;
import com.microsoft.live.OverwriteOption;

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
   
    //SkyDrive variables
    private LiveConnectClient mClient;
    private static final String SKYDRIVE_HOME = "me/skydrive";
    private LiveSdkSampleApplication mApp;
    private ProgressDialog mInitializeDialog;
    private boolean loggedIn;
    File skyFile;
    LiveAuthClient mAuth;
	protected String skyFolderID;
	protected boolean skyFolderFound;
    
    Context context;
    int flag;
    Button allServices;
    //cloud and container on upload screen that holds the files and disappears when dragged
    GridView uploadcloud, cloudcontainer;
    static File sharefile = null;
    
    //variables for collision test
    ArrayList filesToshare;
	private BaseAdapter adapter;
	private int draggedIndex = -1;



    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.upload);
        uploadcloud = (GridView) findViewById(R.id.Upcloud);
        cloudcontainer = (GridView) findViewById(R.id.default_file);
        allServices = (Button) findViewById(R.id.uploadall);
        
        //array list for collision
        filesToshare = new ArrayList();
        
        String extensionType = filename(sharefile);
        
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
        else
        {
        	filesToshare.add(R.drawable.document_upload);
        }
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
			uploadcloud.setVisibility(4);
			cloudcontainer.setVisibility(4);
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices ) {
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
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices) {
				view.animate().scaleX(1.5f);
				view.animate().scaleY(1.5f);
			}
			return true;
		case DragEvent.ACTION_DRAG_EXITED:
			// Drag exited view bounds
			// If called for trash can then reset it.
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices) {
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
			if (view.getId() == R.id.dropbox ) {
				File file = null;//new File("beanstalk.jpg");
				uploadDropbox(file);
				filesToshare.remove(draggedIndex);
				draggedIndex = -1;
			}
			else if(view.getId() == R.id.skydrive)
			{
				//upload to skydrive
				File file = sharefile;
				if(authSkyDrive())
				{
					displayToast("Starting upload to SkyDrive!");
					uploadSkyDrive(file, SKYDRIVE_HOME);
				}
				else
				{
					displayToast("Couldn't authenticate SkyDrive...");
				}
			}
			else if(view.getId() == R.id.box)
			{
				//Box upload code
				UploadBox upload = new UploadBox(this, 0l, sharefile);
				upload.run();
				filesToshare.remove(draggedIndex);
				draggedIndex = -1;
			}
			adapter.notifyDataSetChanged();
		case DragEvent.ACTION_DRAG_ENDED:
			// Hide the trash can
			allServices.setVisibility(4);
			uploadcloud.setVisibility(0);
			cloudcontainer.setVisibility(0);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					findViewById(R.id.dropbox).setVisibility(View.VISIBLE);
					findViewById(R.id.skydrive).setVisibility(View.VISIBLE);
					findViewById(R.id.box).setVisibility(View.VISIBLE);
					findViewById(R.id.otherservices).setVisibility(View.VISIBLE);
				}
			}, 1000l);
			if (view.getId() == R.id.dropbox || view.getId() == R.id.skydrive || view.getId() == R.id.box || view.getId() == R.id.otherservices) {
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
    	skyFile = upFile;

        final ProgressDialog uploadProgressDialog = 
        		new ProgressDialog(this);
        uploadProgressDialog.setMax(100);
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.setMessage("Uploading...");
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setCancelable(true);
        uploadProgressDialog.show();
        
        
        
        OverwriteOption overwrite = OverwriteOption.Overwrite;
        
        filterSkyDrive(SKYDRIVE_HOME);
        if(!skyFolderFound)
        {
        	createFolderSkyDrive();
        	if(skyFolderFound)
        		displayToast("Created folder Beanstalk.");
        	else
        		displayToast("Error creating folder.");
        }
        
        displayToast("Folder id is: " + skyFolderID);
        //createFolderSkyDrive();

        final LiveOperation operation =
                mClient.uploadAsync(uploadPath,
                                    skyFile.getName(),
                                    skyFile,
                                    overwrite,//will overwrite the file if existing
                                    new LiveUploadOperationListener() {
            @Override
            public void onUploadProgress(int totalBytes,
                                         int bytesRemaining,
                                         LiveOperation operation) {
                int percentCompleted = computePercentCompleted(totalBytes, bytesRemaining);

                uploadProgressDialog.setProgress(percentCompleted);
            }

            @Override
            public void onUploadFailed(LiveOperationException exception,
                                       LiveOperation operation) {
                uploadProgressDialog.dismiss();
                displayToast(exception.getMessage());
            }

            @Override
            public void onUploadCompleted(LiveOperation operation) {
                uploadProgressDialog.dismiss();
                displayToast("Upload of " + skyFile.getName() +" Complete!");
/*                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    showToast(code + ": " + message);
                    return;
                }*/

                //loadFolder(mCurrentFolderId);
            }
        }, null);

        uploadProgressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                operation.cancel();
            }
        });
    }
    
    public void createFolderSkyDrive(){
    	
        final LiveOperationListener opListener = new LiveOperationListener() {
            public void onError(LiveOperationException exception, LiveOperation operation) {
                   displayToast("Error creating folder: " + exception.getMessage());
               }
            public void onComplete(LiveOperation operation) {
                JSONObject result = operation.getResult();
                String text = "Folder created:\n" +
                    "\nID = " + result.optString("id") +
                    "\nName = " + result.optString("name");
                   displayToast(text);
               }
           };
   			try {
                JSONObject body = new JSONObject();
                body.put("name", "Beanstalk");
                body.put("description", "Folder used by the Android app Beanstalk");
                mClient.postAsync(SKYDRIVE_HOME, body, opListener);    
            }
            catch(JSONException ex) {
                displayToast("Error building folder: " + ex.getMessage());
            }
    }
    
    private void filterSkyDrive(String folderID)
    {
    	mClient.getAsync(folderID + "/files", new LiveOperationListener() {
            @Override
            public void onComplete(LiveOperation operation) {
            	
                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    displayToast(code + ": " + message);
                    return;
                }
                //ArrayList<SkyDriveObject> fill;// = skAdapter.getSkyDriveObjs();
                //fill.clear();
                
                JSONArray data = result.optJSONArray(JsonKeys.DATA);
                for (int i = 0; i < data.length(); i++) {
                    SkyDriveObject skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));
                    if(skyDriveObj.getName().equalsIgnoreCase("Beanstalk") &&
                    		skyDriveObj.getType().equalsIgnoreCase("folder"))
                    {
                    	skyFolderFound = true;
                    	skyFolderID = skyDriveObj.getId(); 
                    }
                    //fill.add(skyDriveObj);
                }
            }
            @Override
            public void onError(LiveOperationException exception, LiveOperation operation) {
            	   //progressDialog.dismiss();
                   displayToast(exception.getMessage());
            }
        });
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
			    	if(sharefile == null)
					{
						displayToast("You have not chosen a file to upload. Please choose a file from the file browser.");
					}
					else
					{
						//Store Path the Beanstalk Downloads
						sdpath = Environment.getExternalStorageDirectory().getPath() + "/Beanstalk Downloads/";
				    					    	
						displayToast("Uploading from: " + sdpath + sharefile.getName());
						String uploadPath = "/Beanstalk/";
				    	//end testing
						//sharefile is the static variable
						//If files is null, don't allow upload
						UploadDropbox uploadDrop = new UploadDropbox(UploadScreen.this, dropApi, uploadPath, sharefile);
						uploadDrop.execute();	
						sharefile = null;
					}
			    }

		    }
	    }
    }
    
    private boolean authSkyDrive()
    {
    	//Skydrive logged in stuff    	
		mApp = (LiveSdkSampleApplication) getApplication();
        mClient = mApp.getConnectClient();
        if(mClient == null)
        {
        	displayToast("You have not logged into Skydrive!");
        	loggedIn = false;
        	return loggedIn;
        }
        loggedIn = true;
        return loggedIn;
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
		
		View GoogleDrive = findViewById(R.id.box);
		GoogleDrive.setVisibility(View.VISIBLE);
		GoogleDrive.setOnDragListener(UploadScreen.this);
		GoogleDrive.setOnDragListener(UploadScreen.this);
		
		View OtherServices = findViewById(R.id.otherservices);
		OtherServices.setVisibility(View.VISIBLE);
		OtherServices.setOnDragListener(UploadScreen.this);
		OtherServices.setOnDragListener(UploadScreen.this);


		draggedIndex = position;
		return true;
	
      }
		
	 public String filename(File sharefile){
		
		 String fileName = sharefile.getName();
		 String filename_Without_Ext = "";
		 String ext = "";

		 int dotposition= fileName.lastIndexOf(".");
		 filename_Without_Ext = fileName.substring(0,dotposition);
		 ext = fileName.substring(dotposition + 1, fileName.length());

		 return ext;
		}

	
    
}
