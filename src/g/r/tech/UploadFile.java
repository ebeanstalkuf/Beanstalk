package g.r.tech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class UploadFile extends AsyncTask<Void, Long, Boolean> {
	
	private DropboxAPI<AndroidAuthSession> apiObj;
	private String path;
	private File upFile;
	private long fileLength; 
	private UploadRequest request;
	private Context context;
	private final ProgressDialog dialog;
	
	private String error;
	
	
	public UploadFile(Context cntxt, DropboxAPI<AndroidAuthSession> api, String dropboxPath, File file)
	{
		context = cntxt.getApplicationContext();
		
		fileLength = file.length();
		apiObj = api;
		path = dropboxPath;
		upFile = file;
		
		dialog = new ProgressDialog(cntxt);
		dialog.setMax(100);
		dialog.setMessage("Uploading " + file.getName());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
                request.abort();
            }
        });
        dialog.show();
	}
	
	
	/**
	 * TODO
	 */
    @Override
    protected Boolean doInBackground(Void... params) {
    	//return true;
    	try{
			FileInputStream fis = new FileInputStream(upFile);
			String writePath = path + upFile.getName();
			request = apiObj.putFileOverwriteRequest(writePath,fis, upFile.length(), new ProgressListener() {
				@Override
				public void onProgress(long bytes, long total) {
					publishProgress(bytes);
				}
			}
			);
			
			if(request != null)
			{
				request.upload();
				return true;
			}
    	} catch(DropboxFileSizeException e){
    		error = "The file size to upload is too large.";
    	} catch(DropboxUnlinkedException e) {
    		error = "The authentication session was not correct.";
    	} catch(DropboxPartialFileException e) {
    		error = "The file upload was canceled.";
    	} catch (DropboxIOException e) {
    		error = "The network is currently unavailable. Please try again.";
    	} catch (DropboxException e) {
    		error = "An unexpected error has occurred. Please try again.";
    	} catch (FileNotFoundException e) {
    		error = "An error has occurred with the file. Check the file path and try again.";
    	}
		return false;
    }
    
    @Override
    protected void onProgressUpdate(Long... progress)
    {
    	int percentDone = (int) (100.0*(double) progress[0]/fileLength + 0.5);
    	dialog.setProgress(percentDone);
    }
    
    @Override
    protected void onPostExecute(Boolean result)
    {
    	dialog.dismiss();
    	String resultMsg;
    	if(result)
    	{
    		resultMsg = "Successfully uploaded " + upFile.getName();
    	}
    	else
    	{
    		resultMsg = error;
    	}
    	displayToast(resultMsg);
    }
    
    protected void displayToast(String message)
    {
    	Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	msg.show();
    }
}
