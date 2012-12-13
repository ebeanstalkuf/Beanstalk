package g.r.tech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.ResponseListeners.FileUploadListener;
import com.box.androidlib.Utils.Cancelable;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.microsoft.live.LiveConnectClient;

public class UploadBox extends ListActivity{
	private Context context;
	private Context pcontext;
	private String uploadPath;
	private File upFile;
	private long fileLength; 
	private UploadRequest request;
	private ProgressDialog dialog;
	private String authToken;
	private Cancelable cancelable;
	private long folderId;
	
	private boolean uploadAll = false;
	private Context upScreenContext;
	private LiveConnectClient mClient;
	
	public UploadBox(Context cntxt, long boxPath, File file)
	{
		context = cntxt.getApplicationContext();
		pcontext = cntxt;
		upFile = file;
		dialog = new ProgressDialog(cntxt);
		dialog.setMax(100);
		dialog.setMessage("Uploading " + file.getName());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		folderId = boxPath;
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
            	if(cancelable == null)
            	{
            		return;
            	}
            	cancelable.cancel();
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            	if(cancelable == null)
            	{
            		return;
            	}
            	cancelable.cancel();
            }
        });
        dialog.show();
	}
	
	public UploadBox(Context cntxt, long boxPath, File file, LiveConnectClient mClient, boolean uploadAll)
	{
		upScreenContext = cntxt;
		context = cntxt.getApplicationContext();
		pcontext = cntxt;
		upFile = file;
		this.uploadAll = uploadAll;
		this.mClient = mClient;
		
		dialog = new ProgressDialog(cntxt);
		dialog.setMax(100);
		if(!uploadAll)
			dialog.setMessage("Uploading " + file.getName());
		else
			dialog.setMessage("Uploading to Box...");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		folderId = boxPath;
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
            	cancelable.cancel();
            }
        });
        dialog.show();
	}
	
	public void run()
	{
		//Create Folder
		boolean skipBox = false;
		//Test Login
		final SharedPreferences prefs = pcontext.getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
        authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);
        if (authToken == null) {
        	if(uploadAll)
        	{
        		displayToast( "Oops! Looks like you aren't logged into Box. Moving on to SkyDrive...");
        		dialog.dismiss();
        		skipBox = true;
                if(skipBox)
                {
                	if(mClient != null){
	                	UploadSkyDrive upSky = new UploadSkyDrive(upScreenContext, upFile, mClient, UploadScreen.UPLOAD_ALL_ON);
	                	upSky.execute();
                	}
                }
                return;
        	}
        	else
        	{
                displayToast( "Shucks! I can't see anything over here. Try logging in again.");
                dialog.dismiss();
                return;
        	}
        }
        else
        {
        	//Create input stream
    		FileInputStream fis;
            try {
                fis = new FileInputStream(upFile);
                final Box boxServiceHandler = Box.getInstance(Constants.API_KEY);
                cancelable = boxServiceHandler.upload(authToken, Box.UPLOAD_ACTION_UPLOAD, fis, upFile.getName(), folderId,
                        new FileUploadListener() {

                            @Override
                            public void onComplete(BoxFile file, final String status) {
                                dialog.dismiss();
                                if (status.equals(FileUploadListener.STATUS_UPLOAD_OK)) {
                                	if(uploadAll)
                                	{
                                    	UploadSkyDrive upSky = new UploadSkyDrive(upScreenContext, upFile, mClient, UploadScreen.UPLOAD_ALL_ON);
                                    	upSky.execute();
                                	}
                                	else
                                	{
                                        displayToast( "Yahoo! Successfully uploaded " + upFile.getName());	
                                	}
                                    // I commented this out because I will refresh the tree when you go to the file browser
                                    //refresh();
                                }
                                else if (status.equals(FileUploadListener.STATUS_CANCELLED)) {
                                    displayToast("Upload cancelled.");
                                }
                                else {
                                    displayToast("Silly clouds...looks like we had a problem moving things around. Try again.");
                                }
                            }

                            @Override
                            public void onIOException(final IOException e) {
                                e.printStackTrace();
                                displayToast( "Silly clouds...looks like we had a problem moving things around. Try again.");
                                dialog.dismiss();
                            }

                            @Override
                            public void onMalformedURLException(final MalformedURLException e) {
                                e.printStackTrace();
                                displayToast( "Silly clouds...looks like we had a problem moving things around. Try again.");
                                dialog.dismiss();
                            }

                            @Override
                            public void onFileNotFoundException(final FileNotFoundException e) {
                                e.printStackTrace();
                                displayToast( "Silly clouds...looks like we had a problem moving things around. Try again.");
                                dialog.dismiss();
                            }

                            @Override
                            public void onProgress(final long bytesUploaded) {
                                dialog.setProgress((int)(((float)(upFile.length() - (upFile.length() - bytesUploaded))) / upFile.length() * 100));
                            }
                        });
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                displayToast("Silly clouds...looks like we had a problem moving things around. Try again.");
                return;
            }
        }
        
	}
	protected void displayToast(String message)
    {
    	Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	msg.show();
    }
}