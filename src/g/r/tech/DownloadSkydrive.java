package g.r.tech;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import skydrive.SkyDriveAlbum;
import skydrive.SkyDriveAudio;
import skydrive.SkyDriveFile;
import skydrive.SkyDriveFolder;
import skydrive.SkyDriveObject;
import skydrive.SkyDriveObject.Visitor;
import skydrive.SkyDrivePhoto;
import skydrive.SkyDrivePhoto.Image;
import skydrive.SkyDriveVideo;
import skydrive.util.JsonKeys;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveDownloadOperation;
import com.microsoft.live.LiveDownloadOperationListener;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveStatus;

import g.r.tech.LiveSdkSampleApplication;
import g.r.tech.SaveScreen;

public class DownloadSkydrive {
	final ProgressDialog progressDialog;
	private static final String HOME_FOLDER = "me/skydrive";
	private LiveConnectClient mClient;
	private String mErrorMsg;
    String[] fnames = null;
    private Context mContext;
    ListView skListView ;
    Stack<String> mPrevFolderIds = new Stack<String>();
    ArrayList<SkyDriveObject> skyDriveObjs = new ArrayList<SkyDriveObject>();
    ArrayAdapter<String> sklistAdapter ; 
	ArrayList<String> dir = new ArrayList<String>();
	String path;
	Context pContext;
	TextView t;
	Button pathbutton;
	Activity mainAct;
	LiveSdkSampleApplication app;
	int connected = 0;
	ProgressDialog mInitializeDialog;
	File sdpath;
	String name;
	int hasrun = 0;
	
    public DownloadSkydrive(Context context)
    {
    	//Passable context
    	pContext = context;
    	//Usable context fror this activity
    	mContext = context.getApplicationContext();
    	progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Preparing File...");
        progressDialog.setCancelable(true);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
            	progressDialog.cancel();
            	showToast("Download Canceled");
            }
        });
        progressDialog.show();
    	
    }

    public void run(String fileId, String n) {
    	//Set name of file to passed name
    	name = n;
    	auth();
        //If not connected
        if(connected == 0)
        {
        	skListView.setAdapter(null);
        	return;
        }
        //Download Code
        
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
        	
        	sdpath = new File(bfolder, name);
        
        	//Check if it exists, if it does add a (2)
        	int i = 1;
        	while(sdpath.exists())
        	{
        		sdpath = new File(bfolder, duplicate(sdpath.getName(), i));
        		i++;
        	}
        }
        else
        {
        	mErrorMsg = "Error: Your device's SD Card is not mounted. Beanstalk can only download a file to an SD card";
        	return;
        }
        
        final LiveDownloadOperation operation =
                mClient.downloadAsync(fileId + "/content",
                                      sdpath,
                                      new LiveDownloadOperationListener() {
            @Override
            public void onDownloadProgress(int totalBytes,
                                           int bytesRemaining,
                                           LiveDownloadOperation operation) {
            	if(progressDialog.getProgress() > 0 && hasrun == 0)
            	{
            		hasrun = 1;
            		progressDialog.setCancelable(false);
            		showToast("You cannot cancel the download since the Skydrive server has begun sending your file. Please Wait until completed.");
            		progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            	}
                int percentCompleted = computePrecentCompleted(totalBytes, bytesRemaining);
                progressDialog.setProgress(percentCompleted);
            }

            @Override
            public void onDownloadFailed(LiveOperationException exception,
                                         LiveDownloadOperation operation) {
                progressDialog.dismiss();
                showToast("Whoops! Looks like we lost our footing climbing up the stalk. Try getting " + name + " again.");
            }

            @Override
            public void onDownloadCompleted(LiveDownloadOperation operation) {
            	UploadScreen.sharefile = sdpath;
            	Activity activity = (Activity) pContext;
    			Intent openUploadScreen = new Intent(mContext, UploadScreen.class);
                progressDialog.dismiss();
                activity.startActivity(openUploadScreen);
            }
        });

        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                operation.cancel();
            }
        });
        
	}
    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
	public void auth()
	{
		//Skydrive logged in stuff
		mainAct = (Activity) pContext;
		app = (LiveSdkSampleApplication) mainAct.getApplication();
        mClient = app.getConnectClient();
        if(mClient == null)
        {
        	showToast("Shucks! I can't see anything over here. Try logging in again.");
        	connected = 0;
        	return;
        }
        connected = 1;
	}
	private int computePrecentCompleted(int totalBytes, int bytesRemaining) {
        return (int) (((float)(totalBytes - bytesRemaining)) / totalBytes * 100);
    }
	public String duplicate(String fileName, int i){
		 String filename_Without_Ext = "";
		 String ext = "";

		 int dotposition= fileName.lastIndexOf(".");
		 if(dotposition <= 0)
		 {
			 filename_Without_Ext = fileName;
		 }
		 else
		 {
			 filename_Without_Ext = fileName.substring(0,dotposition);
			 ext = fileName.substring(dotposition, fileName.length());
		 }
		 
		 //Add (number) to filename
		 StringBuilder s = new StringBuilder();
		 if(i > 1)
		 {
			 int numberlength = filename_Without_Ext.lastIndexOf("(");
			 s.append(filename_Without_Ext.substring(0, numberlength));
			 s.append("(" + Integer.toString(i) + ")");
		 }
		 else
		 {
			 s.append(filename_Without_Ext);
			 s.append("(1)");
		 }
		 
		 if(ext.length() > 0)
		 {
			 s.append(ext);
		 }
		 return s.toString();
		}
}
