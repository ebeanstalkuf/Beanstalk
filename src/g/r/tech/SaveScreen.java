package g.r.tech;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.DAO.BoxFolder;
import com.box.androidlib.ResponseListeners.FileDownloadListener;
import com.box.androidlib.ResponseListeners.GetAccountTreeListener;
import com.box.androidlib.Utils.Cancelable;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class SaveScreen extends Activity {
	Button dropboxfiles;
	Button sdcardfiles;
	Button skydrivefiles;
	Button boxfiles;
	private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;
    ListView dbListView ;
    ListView sdListView;
    ListView skListView;
    ListView boxListView;
    Stack<File> prevSDFolders = new Stack<File>();
    Stack<Long> prevBoxFolders = new Stack<Long>();
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
	SDCardListAdapter sdAdapter;
	ArrayList<File> sdFiles;
	File sdpath;
	static int connectedWifi = 1;
	Boolean variable;
	
	public static final String CLOSE_A_ON_RESUME = "CLOSE_A_ON_RESUME";
    
    int i = 0;
    int flag = 0;
	
	final static private String APP_KEY = "dhgel7d3dcsen3d";
    final static private String APP_SECRET = "evnp2bxtokmy7yy";
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    
    //Box stuff
    private MyArrayAdapter adapter;
    private TreeListItem[] items;
    private String authToken;
    private long folderId;

    // Menu button options
    private static final int MENU_ID_UPLOAD = 1;
    private static final int MENU_ID_CREATE_FOLDER = 2;

    // Options shown when you click on a file/folder
    private static final String OPTION_FOLDER_DETAILS = "Folder details";
    private static final String OPTION_FOLDER_CONTENTS = "Folder contents";
    private static final String OPTION_FILE_DETAILS = "File details";
    private static final String OPTION_FILE_DOWNLOAD = "Download file";
    private static final String OPTION_SHARE = "Share";
    private static final String OPTION_DELETE = "Delete";
    private static final String OPTION_RENAME = "Rename";

    // Activity request codes
    private static final int REQUEST_CODE_FILE_PICKER = 1;
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen5_download);
        
      ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      //wifi
      State wifi = conMan.getNetworkInfo(1).getState();
      if (wifi == NetworkInfo.State.DISCONNECTED) 
      {
    	  //Not conncted to wifi
    	  connectedWifi = 0;
      }
    	    
    	welcomeText = (TextView) findViewById(R.id.initialText);
       
        sdcardfiles = (Button) findViewById(R.id.sdcard_fileb);
        sdcardfiles.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		    	//get rid of initial text
		        welcomeText.setVisibility(8);
				cloudService = "sdcard";
				updateSD(Environment.getExternalStorageDirectory());
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
				    	showToast("Shucks! I can't see anything over here. Try logging in again.");
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
        //Create listener for Skydrive file update button
        skydrivefiles = (Button) findViewById(R.id.skydrive_fileb);
        skydrivefiles.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
					welcomeText.setVisibility(8);
				    cloudService = "skydrive";
					skListView = (ListView) findViewById(android.R.id.list);
				    updatesk = new UpdateSkydrive(SaveScreen.this, skListView);
				    updatesk.run("me/skydrive");
				    }		
		});
        //Create listener for Box files update button
        boxfiles = (Button) findViewById(R.id.box_fileb);
        boxfiles.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				welcomeText.setVisibility(8);
				cloudService = "box";
				
		        // Check if we have an Auth Token stored.
		        final SharedPreferences prefs = getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
		        authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);
		        if (authToken == null) {
		            Toast.makeText(getApplicationContext(), "Shucks! I can't see anything over here. Try logging in again.", Toast.LENGTH_SHORT).show();
		            finish();
		            return;
		        }
		        else {
		        	//Toast.makeText(getApplicationContext(), "You are logged in to Box...", Toast.LENGTH_SHORT).show();
		        }

		        // View your root folder by default (folder_id = 0l), or this activity
		        // can also be launched to view subfolders
		        folderId = 0l;
		        Bundle extras = getIntent().getExtras();
		        if (extras != null && extras.containsKey("folder_id")) {
		            folderId = extras.getLong("folder_id");
		        }

		        // Initialize list items and set adapter
		        items = new TreeListItem[0];
		        boxListView = (ListView) findViewById(android.R.id.list);
		        
		        boxSetShit(boxListView);

			}
		});
    }
    public void onRestart()
    {
    	super.onRestart();
    	connectedWifi = 1;
    	this.recreate();
    }
    
    @Override
    public void onResume(){
      super.onResume();

      //Retrieve the message
      SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
      boolean IShouldClose=mPrefs.getBoolean(SaveScreen.CLOSE_A_ON_RESUME,false);

      if (IShouldClose){

         //remove the message (will always close here otherwise)
         mPrefs.edit().remove(SaveScreen.CLOSE_A_ON_RESUME).commit();

         //Terminate A
         finish();
      }
  }
    
    public void boxSetShit(ListView x)
    {
        adapter = new MyArrayAdapter(this, 0, items);
        x.setAdapter(adapter);
        x.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
				/**
		         * Demonstrates some of the actions you can perform on files and folders
		         */

		        if(items[position].type == TreeListItem.TYPE_FOLDER)
		        {
		        	prevBoxFolders.push(folderId);
		        	folderId = items[position].id;
		        	refresh();
		        	/*
		            Intent i = new Intent(SaveScreen.this, SaveScreen.class);
		            i.putExtra("folder_id", items[position].id);
		            startActivity(i);
		            */
		        }
		        else
		        {
		        	//Check SD Card status
		            String sdcardstatus = Environment.getExternalStorageState();
		            if(sdcardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
		            {
		            	mErrorMsg = "Error: Your SD card has been mounted as Read Only. Please re-mount with write access.";
		            	return;
		            }
		            else if(sdcardstatus.equals(Environment.MEDIA_REMOVED))
		            {
		            	mErrorMsg ="Error: Your device is not showing an SD Card. Beanstalk can only download a file to an SD card";
		            	return;
		            }
		            //Make sure SD Card is mounted
		            if(sdcardstatus.equals(Environment.MEDIA_MOUNTED))
		            {
		            	//if the card is mounted, then set up the path to the sd card.filename.xxx
		            	//Check if Beanstalk Downloads folder exists
		            	File bfolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Beanstalk Downloads");
		            	if(!bfolder.exists())
		            	{
		            		bfolder.mkdirs();
		            	}
		            	
		            	sdpath = new File(bfolder, items[position].name);
		            
		            }
		            else
		            {
		            	mErrorMsg = "Error: Your device's SD Card is not mounted. Beanstalk can only download a file to an SD card";
		            	return;
		            }
		        	/**
		             * Download a file and put it into the SD card. In your app, you can put the file wherever you have access to.
		             */
		            //Check size
		            if(items[position].file.getSize() > 52430000)
		            {
		            	//Greater than 50 megabytes
		            	if(!dataCap())
		            	{
		            		return;
		            	}
		            }
		            final Box box = Box.getInstance(Constants.API_KEY);
		            final java.io.File destinationFile = new java.io.File(Environment.getExternalStorageDirectory() + "/"
		                                                                  + URLEncoder.encode(items[position].name));

		            final ProgressDialog downloadDialog = new ProgressDialog(SaveScreen.this);
		            downloadDialog.setMessage("Preparing File...");
		            downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		            downloadDialog.setCancelable(true);
		            downloadDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) {
		                    // This will cancel the putFile operation
		                	downloadDialog.cancel();
		                }
		            });
		            downloadDialog.show();

		            //Toast.makeText(getApplicationContext(), "Click BACK to cancel the download.", Toast.LENGTH_SHORT).show();

		            final Cancelable cancelable = box.download(authToken, items[position].id , sdpath, null, new FileDownloadListener() {

		                @Override
		                public void onComplete(final String status) {
		                    downloadDialog.dismiss();
		                    if (status.equals(FileDownloadListener.STATUS_DOWNLOAD_OK)) {
		                        UploadScreen.sharefile = sdpath;
		            			Intent openUploadScreen = new Intent(SaveScreen.this.getApplicationContext(), UploadScreen.class);
		            			startActivity(openUploadScreen);
		                    	
		                        //Toast.makeText(getApplicationContext(), "File downloaded to " + destinationFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
		                    }
		                    else if (status.equals(FileDownloadListener.STATUS_DOWNLOAD_CANCELLED)) {
		                        Toast.makeText(getApplicationContext(), "Download Canceled.", Toast.LENGTH_LONG).show();
		                        if(sdpath.exists())
		                        {
		                        	sdpath.delete();
		                        }
		                    }
		                }

		                @Override
		                public void onIOException(final IOException e) {
		                    e.printStackTrace();
		                    downloadDialog.dismiss();
		                    Toast.makeText(getApplicationContext(), "Whoops! Looks like we lost our footing climbing up the stalk. Try getting that file again.", Toast.LENGTH_LONG).show();
		                }

		                @Override
		                public void onProgress(final long bytesDownloaded) {
		                    downloadDialog.setProgress((int) (((float)(items[position].file.getSize() - (items[position].file.getSize() - bytesDownloaded))) / items[position].file.getSize() * 100));
		                }
		            });
		            downloadDialog.setOnCancelListener(new OnCancelListener() {

		                @Override
		                public void onCancel(DialogInterface dialog) {
		                    cancelable.cancel();
		                }
		            });
		        }
				
			}
        });
        // Go get the account tree
        refresh();
    }
    
  //Moving back to previous folder using back key
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if prev folders is empty, send the back button to the TabView activity.
        	//If currently in skydrive
        	event.startTracking();
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
    	//What to do on short key press
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
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
            else if(cloudService.equals("sdcard"))
            {
            	if (prevSDFolders.isEmpty()) {
            		return super.onKeyUp(keyCode, event);
                }

                updateSD(prevSDFolders.pop());
                return true;
            }
            else if(cloudService.equals("box"))
            {
            	if (prevBoxFolders.isEmpty()) {
            		return super.onKeyUp(keyCode, event);
                }
                folderId = prevBoxFolders.pop();
                refresh();
                return true;
            }
    	}
            return super.onKeyUp(keyCode, event);
    } 
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event){
    	//what to do on long key press
    	if (keyCode == KeyEvent.KEYCODE_BACK)
    	{
    		finish();
    		cloudService = "nothing";
    		return true;
    	}
        return super.onKeyLongPress(keyCode, event);
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
    
    private class SDCardListAdapter extends BaseAdapter {
    
    	public ArrayList<File> files;
    	private View sdView;
    	private LayoutInflater sdInflater;
    	
    	public SDCardListAdapter(Context context){
    		sdInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		files = new ArrayList<File>();
    	}
    	
    	/*public void update(File[] newList){
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
        }*/
    	
    	public File getItem(int x){
    		return files.get(x);
    	}
    	
    	public ArrayList<File> getFiles(){
    		return files;
    	}
    	
    	public int getCount(){
    		return files.size();
    	}
    	
    	public long getItemId(int position){
    	
    		return position;
    	}
    	
    	private void setName(String name) {
            TextView tv = (TextView) sdView.findViewById(R.id.label);
            tv.setText(name);
        }
    	
    	private void setIcon(int iconResId) {
            ImageView img = (ImageView) sdView.findViewById(R.id.icon);
            img.setImageResource(iconResId);
        }
    	
    	 private View inflateSDCardListItem(ViewGroup parent) {
             return sdInflater.inflate(R.layout.screen5_rowlayout, parent, false);
         }
    	 
    	 /*public String getExt(String fileName){
    		 String filename_Without_Ext = "";
    		 String ext = "";

    		 int dotposition= fileName.lastIndexOf(".");
    		 filename_Without_Ext = fileName.substring(0,dotposition);
    		 ext = fileName.substring(dotposition + 1, fileName.length());

    		 return ext;
    		}*/
    	 
    	 private String shorten(String path){
    		 String x = path;
    		 if (path.contains("/mnt/sdcard/")){
     			x = path.replace("/mnt/sdcard/", "");
    		 }
    		 return x;
    	 }
    	
    	public View getView(int position, View convertView, final ViewGroup parent){
    		File marker = files.get(position);
    		sdView = convertView != null ? convertView : null;

            if(marker.isDirectory())
            {
            	if (sdView == null) {
                    sdView = inflateSDCardListItem(parent);
                }

                setIcon(R.drawable.folder);
                //String longPath = marker.getName();
                //String shortPath = shorten(longPath);
                setName(marker.getName());
            }
            else
            {
            	String extensionType = filename(marker.getPath());
                
                if( extensionType.equalsIgnoreCase("jpg") || extensionType.equalsIgnoreCase("png") || extensionType.equalsIgnoreCase("gif") 
                    	|| extensionType.equalsIgnoreCase("bmp") || extensionType.equalsIgnoreCase("psd") || extensionType.equalsIgnoreCase("tif") 
                    	|| extensionType.equalsIgnoreCase("tiff") || extensionType.equalsIgnoreCase("ai") || extensionType.equalsIgnoreCase("svg"))
                {
                	if (sdView == null) {
                        sdView = (View) inflateSDCardListItem(parent);
                    }

                    setIcon(R.drawable.photo);
                    //String longPath = marker.getPath();
                    //String shortPath = shorten(longPath);
                    setName(marker.getName());
                }
                else if( extensionType.equalsIgnoreCase("mp3") || extensionType.equalsIgnoreCase("m4a") || extensionType.equalsIgnoreCase("wav") 
                		|| extensionType.equalsIgnoreCase("flac") || extensionType.equalsIgnoreCase("aac") || extensionType.equalsIgnoreCase("m4p")
                		|| extensionType.equalsIgnoreCase("mmf") || extensionType.equalsIgnoreCase("ogg") || extensionType.equalsIgnoreCase("Opus")
                		|| extensionType.equalsIgnoreCase("raw") || extensionType.equalsIgnoreCase("vox") || extensionType.equalsIgnoreCase("wma") 
                		|| extensionType.equalsIgnoreCase("alac") || extensionType.equalsIgnoreCase("aiff"))
                {
                	if (sdView == null) {
                        sdView = inflateSDCardListItem(parent);
                    }

                    setIcon(R.drawable.music);
                    //String longPath = marker.getPath();
                    //String shortPath = shorten(longPath);
                    setName(marker.getName());
                }
                else if( extensionType.equalsIgnoreCase("mov") || extensionType.equalsIgnoreCase("divx") || extensionType.equalsIgnoreCase("xvid")
                		|| extensionType.equalsIgnoreCase("asf") || extensionType.equalsIgnoreCase("avi") || extensionType.equalsIgnoreCase("m1v")
                		|| extensionType.equalsIgnoreCase("m2v") || extensionType.equalsIgnoreCase("m4v") || extensionType.equalsIgnoreCase("fla")
                		|| extensionType.equalsIgnoreCase("flv") || extensionType.equalsIgnoreCase("sol") || extensionType.equalsIgnoreCase("mpeg")
                		|| extensionType.equalsIgnoreCase("mpe") || extensionType.equalsIgnoreCase("mpg") || extensionType.equalsIgnoreCase("MP4")
                		|| extensionType.equalsIgnoreCase("wmv") || extensionType.equalsIgnoreCase("swf") || extensionType.equalsIgnoreCase("fcp")
                		|| extensionType.equalsIgnoreCase("ppj") )
                {
                	if (sdView == null) {
                        sdView = inflateSDCardListItem(parent);
                    }

                    setIcon(R.drawable.video);
                    //String longPath = marker.getPath();
                    //String shortPath = shorten(longPath);
                    setName(marker.getName());
                }
                else
                {
                	if (sdView == null) {
                        sdView = inflateSDCardListItem(parent);
                    }

                    setIcon(R.drawable.document);
                    //String longPath = marker.getPath();
                    //String shortPath = shorten(longPath);
                    setName(marker.getName());
                }
            } 
            return sdView;
    	}
    }
    
    //Box stuff
    
    private class TreeListItem {

        public static final int TYPE_FILE = 1;
        public static final int TYPE_FOLDER = 2;
        public int type;
        public long id;
        public String name;
        public BoxFile file;
        @SuppressWarnings("unused")
        public BoxFolder folder;
        public long updated;
    }

    private class MyArrayAdapter extends ArrayAdapter<TreeListItem> {

        private final Context context;
        private final LayoutInflater mInflater;
        private View myView;

        public MyArrayAdapter(Context contextt, int textViewResourceId, TreeListItem[] objects) {
            super(contextt, textViewResourceId, objects);
            context = contextt;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	myView = convertView != null ? convertView : null;
            if (items[position].type == TreeListItem.TYPE_FOLDER) {
            	if (myView == null) {
                    myView = mInflater.inflate(R.layout.screen5_rowlayout, parent, false);
                }
            	TextView tv = (TextView) myView.findViewById(R.id.label);
                ImageView img = (ImageView) myView.findViewById(R.id.icon);
            	tv.setText(items[position].name);
            	img.setImageResource(R.drawable.folder);
            }
            else if (items[position].type == TreeListItem.TYPE_FILE) {
            	if (myView == null) {
                    myView = mInflater.inflate(R.layout.screen5_rowlayout, parent, false);
                }
            	TextView tv = (TextView) myView.findViewById(R.id.label);
                ImageView img = (ImageView) myView.findViewById(R.id.icon);
            	tv.setText(items[position].name);
            	
            	String extensionType = filename(items[position].name);
                
                if( extensionType.equalsIgnoreCase("jpg") || extensionType.equalsIgnoreCase("png") || extensionType.equalsIgnoreCase("gif") 
                    	|| extensionType.equalsIgnoreCase("bmp") || extensionType.equalsIgnoreCase("psd") || extensionType.equalsIgnoreCase("tif") 
                    	|| extensionType.equalsIgnoreCase("tiff") || extensionType.equalsIgnoreCase("ai") || extensionType.equalsIgnoreCase("svg"))
                {
                	img.setImageResource(R.drawable.photo);
                }
                else if( extensionType.equalsIgnoreCase("mp3") || extensionType.equalsIgnoreCase("m4a") || extensionType.equalsIgnoreCase("wav") 
                		|| extensionType.equalsIgnoreCase("flac") || extensionType.equalsIgnoreCase("aac") || extensionType.equalsIgnoreCase("m4p")
                		|| extensionType.equalsIgnoreCase("mmf") || extensionType.equalsIgnoreCase("ogg") || extensionType.equalsIgnoreCase("Opus")
                		|| extensionType.equalsIgnoreCase("raw") || extensionType.equalsIgnoreCase("vox") || extensionType.equalsIgnoreCase("wma") 
                		|| extensionType.equalsIgnoreCase("alac") || extensionType.equalsIgnoreCase("aiff"))
                {
                	img.setImageResource(R.drawable.music); 
                }
                else if( extensionType.equalsIgnoreCase("mov") || extensionType.equalsIgnoreCase("divx") || extensionType.equalsIgnoreCase("xvid")
                		|| extensionType.equalsIgnoreCase("asf") || extensionType.equalsIgnoreCase("avi") || extensionType.equalsIgnoreCase("m1v")
                		|| extensionType.equalsIgnoreCase("m2v") || extensionType.equalsIgnoreCase("m4v") || extensionType.equalsIgnoreCase("fla")
                		|| extensionType.equalsIgnoreCase("flv") || extensionType.equalsIgnoreCase("sol") || extensionType.equalsIgnoreCase("mpeg")
                		|| extensionType.equalsIgnoreCase("mpe") || extensionType.equalsIgnoreCase("mpg") || extensionType.equalsIgnoreCase("MP4")
                		|| extensionType.equalsIgnoreCase("wmv") || extensionType.equalsIgnoreCase("swf") || extensionType.equalsIgnoreCase("fcp")
                		|| extensionType.equalsIgnoreCase("ppj") )
                {
                	img.setImageResource(R.drawable.video);
                }
                else
                {
                	img.setImageResource(R.drawable.document);
                }
            }
            
            return myView;
        }

        @Override
        public int getCount() {
            return items.length;
        }
    }
    
    private void refresh() {
    	final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        final Box box = Box.getInstance(Constants.API_KEY);
        box.getAccountTree(authToken, folderId, new String[] {Box.PARAM_ONELEVEL}, new GetAccountTreeListener() {

            @Override
            public void onComplete(BoxFolder boxFolder, String status) {
                if (!status.equals(GetAccountTreeListener.STATUS_LISTING_OK)) {
                	progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Aww dang! There was an error logging into Box. Try logging in again.", Toast.LENGTH_SHORT).show();
                    //finish();
                    return;
                }
                else {
                	//Toast.makeText(getApplicationContext(), "There was no error getting the list.", Toast.LENGTH_SHORT).show();
                }

                /**
                 * Box.getAccountTree() was successful. boxFolder contains a list of subfolders and files. Shove those into an array so that our list adapter
                 * displays them.
                 */

                items = new TreeListItem[boxFolder.getFoldersInFolder().size() + boxFolder.getFilesInFolder().size()];

                int i = 0;

                Iterator<? extends BoxFolder> foldersIterator = boxFolder.getFoldersInFolder().iterator();
                while (foldersIterator.hasNext()) {
                    BoxFolder subfolder = foldersIterator.next();
                    TreeListItem item = new TreeListItem();
                    item.id = subfolder.getId();
                    item.name = subfolder.getFolderName();
                    item.type = TreeListItem.TYPE_FOLDER;
                    item.folder = subfolder;
                    item.updated = subfolder.getUpdated();
                    items[i] = item;
                    i++;
                }

                Iterator<? extends BoxFile> filesIterator = boxFolder.getFilesInFolder().iterator();
                while (filesIterator.hasNext()) {
                    BoxFile boxFile = filesIterator.next();
                    TreeListItem item = new TreeListItem();
                    item.id = boxFile.getId();
                    item.name = boxFile.getFileName();
                    item.type = TreeListItem.TYPE_FILE;
                    item.file = boxFile;
                    item.updated = boxFile.getUpdated();
                    items[i] = item;
                    i++;
                }

                adapter.notifyDataSetChanged();
               // ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                //progressBar.setVisibility(View.GONE);
                progressDialog.dismiss();
            }

            @Override
            public void onIOException(final IOException e) {
                //Toast.makeText(getApplicationContext(), "Failed to get tree - " + e.getMessage(), Toast.LENGTH_LONG).show();
            	progressDialog.dismiss();
            }
        });
    }
    /*
    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {

        /**
         * Demonstrates some of the actions you can perform on files and folders
         

        if(items[position].type == TreeListItem.TYPE_FOLDER)
        {
        	folderId = items[position].id;
        	refresh();
        	/*
            Intent i = new Intent(SaveScreen.this, SaveScreen.class);
            i.putExtra("folder_id", items[position].id);
            startActivity(i);
            
        }
        else
        {
        	/**
             * Download a file and put it into the SD card. In your app, you can put the file wherever you have access to.
             
            final Box box = Box.getInstance(Constants.API_KEY);
            final java.io.File destinationFile = new java.io.File(Environment.getExternalStorageDirectory() + "/"
                                                                  + URLEncoder.encode(items[position].name));

            final ProgressDialog downloadDialog = new ProgressDialog(SaveScreen.this);
            downloadDialog.setMessage("Downloading " + items[position].name);
            downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            downloadDialog.setMax((int) items[position].file.getSize());
            downloadDialog.setCancelable(true);
            downloadDialog.show();

            Toast.makeText(getApplicationContext(), "Click BACK to cancel the download.", Toast.LENGTH_SHORT).show();

            final Cancelable cancelable = box.download(authToken, items[position].id, destinationFile, null, new FileDownloadListener() {

                @Override
                public void onComplete(final String status) {
                    downloadDialog.dismiss();
                    if (status.equals(FileDownloadListener.STATUS_DOWNLOAD_OK)) {
                    	
                        File sdpath = new File(Environment.getExternalStorageDirectory() + "/"
                                + URLEncoder.encode(items[position].name));
                        UploadScreen.sharefile = destinationFile;
            			Intent openUploadScreen = new Intent(SaveScreen.this.getApplicationContext(), UploadScreen.class);
            			startActivity(openUploadScreen);
                    	
                        Toast.makeText(getApplicationContext(), "File downloaded to " + destinationFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                    else if (status.equals(FileDownloadListener.STATUS_DOWNLOAD_CANCELLED)) {
                        Toast.makeText(getApplicationContext(), "Download canceled.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onIOException(final IOException e) {
                    e.printStackTrace();
                    downloadDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Download failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgress(final long bytesDownloaded) {
                    downloadDialog.setProgress((int) bytesDownloaded);
                }
            });
            downloadDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    cancelable.cancel();
                }
            });
        }
    }
*/
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
    public void updateSD(final File folder)
    {
    	sdAdapter = new SDCardListAdapter(this);
    	sdListView = (ListView) findViewById(android.R.id.list);
        File file[] = folder.listFiles();
        //SDCardBrowser.update(file);
        
        sdFiles = sdAdapter.getFiles();
    	sdFiles.clear();
    	
    	for(int i=0; i < file.length; i++) {
    		sdFiles.add(file[i]);
    	}
    	//Folder is empty
    	if(sdFiles.size() < 1)
    	{
    		showToast("Whoops! Looks like this folder is empty.");
    	}
    	sdListView.setAdapter(sdAdapter);
    	dbListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				//Check if directory
				int index = position;
				if(sdFiles.get(index).isDirectory())
				{
					//Store folder
					prevSDFolders.push(folder);
					//Call UpdateSD with the clicked directory as the parameter
					updateSD(sdFiles.get(index));
				}
				else
				{
					UploadScreen.sharefile = sdFiles.get(index);
					UploadScreen.remove = 0;
		        	//Pass the file
					Intent openUploadScreen = new Intent(getApplicationContext(), UploadScreen.class);
					startActivity(openUploadScreen);
				}
			}
    	});
    }
    
    public boolean dataCap()
    {
    	AlertDialog dataCap = new AlertDialog.Builder(this).create();
    	dataCap.setTitle("Data Usage Warning");
    	dataCap.setMessage("The file you selected is over 50 MB and you are not connected to WiFi. This may incur data fees with your cellular provider. Do you want to continue?");
    	dataCap.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Just go back to what you were doing
            	variable = true;
            }
        });
    	dataCap.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Stop doing what your doing
            	variable = false;
            }
        });
    	dataCap.show();
    	return variable;
    }
}
/*The file you selected is over 50 MB and you are not connected to WiFi. This may incur data fees with your cellular provider. Do you want to continue?   */


