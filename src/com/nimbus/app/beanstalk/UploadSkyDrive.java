

package com.nimbus.app.beanstalk;


import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import skydrive.SkyDriveObject;
import skydrive.util.JsonKeys;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveUploadOperationListener;
import com.microsoft.live.OverwriteOption;

public class UploadSkyDrive {

    public static final String EXTRA_PATH = "path";

    private static final int DIALOG_DOWNLOAD_ID = 0;
    public static final String SKYDRIVE_HOME = "me/skydrive";
    
    Context context;
    Context cntxt;
    String uploadPath;
    File skyFile;
	static String skyFolderID;
	static boolean skyFolderFound;

    private LiveConnectClient mClient;
    private String mCurrentFolderId;
    
    //Skydrive logged in state stuff
    private LiveSdkSampleApplication mApp;
    private LiveAuthClient mAuthClient;
    
    private ProgressDialog uploadAllProgressDialog;
    
    private boolean uploadAll = false;
    
    TextView resultTextView;

	protected boolean uploadVar;
    
    public UploadSkyDrive(Context context, File file, LiveConnectClient client)
    {
    	this.context = context.getApplicationContext();
    	cntxt = context;
    	mClient = client;
    	//this.uploadPath = uploadPath;
		skyFile = file;
		uploadVar = false;
		//String filePath = "";//data.getStringExtra(FilePicker.EXTRA_FILE_PATH);
		/*if(!skyFolderFound && skyFolderID == null)
		{
			showToast("Finding Beanstalk folder...");
			filterSkyDrive(SKYDRIVE_HOME);
		}*/

    }
    
    public UploadSkyDrive(Context context, LiveConnectClient client)
    {
    	this.context = context.getApplicationContext();
    	cntxt = context;
    	mClient = client;
    	uploadVar = false;
    }
    
    public UploadSkyDrive(Context context, File file, LiveConnectClient client, boolean uploadAll)
    {
    	this.context = context.getApplicationContext();
    	cntxt = context;
    	mClient = client;
    	//this.uploadPath = uploadPath;
		skyFile = file;
		uploadVar = false;
		this.uploadAll = uploadAll;

    }
    
    
    public Boolean execute() {
        
    	uploadVar = true;
    	if(uploadAll)
    	{
            uploadAllProgressDialog = new ProgressDialog(cntxt);
            uploadAllProgressDialog.setMax(100);
            uploadAllProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            uploadAllProgressDialog.setMessage("Uploading to SkyDrive...");
            uploadAllProgressDialog.setProgress(0);
            uploadAllProgressDialog.setCancelable(false);
            uploadAllProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // This will cancel the putFile operation
                    uploadAllProgressDialog.cancel();
                }
            });
           
            uploadAllProgressDialog.show();
            uploadAllProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    	}
        filterSkyDrive(SKYDRIVE_HOME);        
        //showToast("Folder id is: " + skyFolderID);
        //createFolderSkyDrive();
		return true;
    }
    
    private void upload()
    {
        if(skyFolderID != null)
        {
        	
            final ProgressDialog uploadProgressDialog = 
            		new ProgressDialog(cntxt);
            uploadProgressDialog.setMax(100);
            uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            uploadProgressDialog.setMessage("Uploading " + skyFile.getName());
            uploadProgressDialog.setProgress(0);
            uploadProgressDialog.setCancelable(false);
            uploadProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // This will cancel the putFile operation
                    uploadProgressDialog.cancel();
                }
            });
           
            if(!uploadAll){
            	uploadProgressDialog.show();
                uploadProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
            

            //showToast("Folder id: "+ skyFolderID);
            //showToast("File name: "+ skyFile.getName());
            
            OverwriteOption overwrite = OverwriteOption.Overwrite;
            
            final LiveOperation operation =
                    mClient.uploadAsync(skyFolderID,
                                        skyFile.getName(),
                                        skyFile,
                                        overwrite,//will overwrite the file if existing
                                        new LiveUploadOperationListener() {
                @Override
                public void onUploadProgress(int totalBytes,
                                             int bytesRemaining,
                                             LiveOperation operation) {
                    int percentCompleted = computePercentCompleted(totalBytes, bytesRemaining);

                    if(!uploadAll)
                    	uploadProgressDialog.setProgress(percentCompleted);
                    else
                    	uploadAllProgressDialog.setProgress(percentCompleted);
                }

                @Override
                public void onUploadFailed(LiveOperationException exception,
                                           LiveOperation operation) {
                	if(!uploadAll) {
                		uploadProgressDialog.dismiss();
                	}                		
                	else
                		uploadAllProgressDialog.dismiss();
                    showToast("Uh oh...something went wrong when uploading to SkyDrive.");
                }

                @Override
                public void onUploadCompleted(LiveOperation operation) {
                	if(!uploadAll){
                		uploadProgressDialog.dismiss();
                        showToast("Yahoo! Upload of " + skyFile.getName() +" complete!");
                	}
                	else {
                		uploadAllProgressDialog.dismiss();
                	}

                    JSONObject result = operation.getResult();
                    if (result.has(JsonKeys.ERROR)) {
                        JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                        String message = error.optString(JsonKeys.MESSAGE);
                        String code = error.optString(JsonKeys.CODE);
                        //showToast(code + ": " + message);
                        return;
                    }

                    //loadFolder(mCurrentFolderId);
                }
            }, null);
            if(!uploadAll) {
	            uploadProgressDialog.setOnCancelListener(new OnCancelListener() {
	                @Override
	                public void onCancel(DialogInterface dialog) {
	                    operation.cancel();
	                }
	            });
            }
            else
            {
	            uploadAllProgressDialog.setOnCancelListener(new OnCancelListener() {
	                @Override
	                public void onCancel(DialogInterface dialog) {
	                    operation.cancel();
	                }
	            });
            }
            
        }
        else
        {
        	showToast("Whoops! Looks like there was a problem. Check and make sure you have a Beanstalk folder on Skydrive");
        }
    }
    
    public void createFolderSkyDrive(){
    	
        final ProgressDialog createDialog = 
        		new ProgressDialog(cntxt);
        //createDialog.setMax(100);
        createDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        createDialog.setMessage("Creating Beanstalk Folder...");
        //createDialog.setProgress(0);
        createDialog.setCancelable(false);
        
        if(!uploadAll)
        	createDialog.show();
    	
        final LiveOperationListener opListener = new LiveOperationListener() {
            public void onError(LiveOperationException exception, LiveOperation operation) {
            		createDialog.dismiss();
                   showToast("Shoot! There was a problem making a Beanstalk folder.");
               }
            public void onComplete(LiveOperation operation) {
            		createDialog.dismiss();
	                JSONObject result = operation.getResult();
	                /*String text = "Folder created:\n" +
	                    "\nID = " + result.optString("id") +
	                    "\nName = " + result.optString("name");
	                showToast(text);*/
	                skyFolderID = result.optString("id");
	                skyFolderFound = true;
	                if(uploadVar)
	                {
	                	upload();
	                }
	                	
               }
           };
   			try {
                JSONObject body = new JSONObject();
                body.put("name", "Beanstalk");
                body.put("description", "Folder used by the Android app Beanstalk");
                mClient.postAsync(SKYDRIVE_HOME, body, opListener); 
            }
            catch(JSONException ex) {
            	createDialog.dismiss();
                showToast("Shoot! There was a problem making a Beanstalk folder.");
            }
    }
    
    public void filterSkyDrive(String folderID)
    {
		 final ProgressDialog searchDialog = 
	        		new ProgressDialog(cntxt);
		 searchDialog.setMessage("Checking Beanstalk Folder...");
		 searchDialog.setCancelable(false);
    	if(!uploadAll)
    	{
	        searchDialog.show(); 
    	}

    	
    	mClient.getAsync(folderID + "/files", new LiveOperationListener() {
            @Override
            public void onComplete(LiveOperation operation) {
            	searchDialog.dismiss();
                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    //showToast(code + ": " + message);
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
                    	break;
                    }
                    //fill.add(skyDriveObj);
                }
            	if(!skyFolderFound && skyFolderID == null )
            	{
            		createFolderSkyDrive();
            	}
            	else if(uploadVar && (skyFolderFound && skyFolderID != null))
            	{
            		upload();
            	}
            }
            @Override
            public void onError(LiveOperationException exception, LiveOperation operation) {
            	   searchDialog.dismiss();
                   //showToast(exception.getMessage());
            }
        });
    	

    }
    
    protected void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }
    
    private int computePercentCompleted(int totalBytes, int bytesRemaining) {
        return (int) (((float)(totalBytes - bytesRemaining)) / totalBytes * 100);
    }

    private ProgressDialog showProgressDialog(String title, String message, boolean indeterminate) {
        return ProgressDialog.show(context, title, message, indeterminate);
    }
}
