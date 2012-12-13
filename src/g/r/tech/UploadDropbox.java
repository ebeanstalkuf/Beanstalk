package g.r.tech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.microsoft.live.LiveConnectClient;

public class UploadDropbox extends AsyncTask<Void, Long, Boolean> {
	
	private DropboxAPI<AndroidAuthSession> apiObj;
	private String uploadPath;
	private File upFile;
	private long fileLength; 
	private UploadRequest request;
	private Context context;
	private final ProgressDialog dialog;
	Boolean mCanceled = false;
	private FileInputStream fis;
	private String error;
	
	private boolean uploadAll = false;
	private Context upScreenContext;
	private LiveConnectClient mClient; //used for UploadAll
	
	public UploadDropbox(Context cntxt, DropboxAPI<AndroidAuthSession> api, String dropboxPath, File file)
	{
		context = cntxt.getApplicationContext();
		
		fileLength = file.length();
		apiObj = api;
		uploadPath = dropboxPath;
		upFile = file;
		
		dialog = new ProgressDialog(cntxt);
		dialog.setMax(100);
		dialog.setMessage("Uploading " + file.getName());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
            	mCanceled = true;
            	
                // This will cancel the getThumbnail operation by closing
                // its stream
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            	// This will cancel the putFile operation
            	mCanceled = true;
            	
                // This will cancel the getThumbnail operation by closing
                // its stream
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        dialog.show();
	}
	
	public UploadDropbox(Context cntxt, DropboxAPI<AndroidAuthSession> api, String dropboxPath, File file, LiveConnectClient mClient, boolean uploadAll)
	{
		upScreenContext = cntxt;
		context = cntxt.getApplicationContext();
		
		fileLength = file.length();
		apiObj = api;
		uploadPath = dropboxPath;
		upFile = file;
		this.uploadAll = uploadAll;
		this.mClient = mClient;
		
		dialog = new ProgressDialog(cntxt);
		dialog.setMax(100);
		dialog.setMessage("Uploading " + file.getName());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
            	mCanceled = true;
            	
                // This will cancel the getThumbnail operation by closing
                // its stream
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            	// This will cancel the putFile operation
            	mCanceled = true;
            	
                // This will cancel the getThumbnail operation by closing
                // its stream
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        dialog.show();
	}
	
	
	/**
	 * TODO
	 */
    @Override
    protected Boolean doInBackground(Void... params) {
    	if (mCanceled) {
            return false;
        }
    	//return true;
    	fis = null;
    	try{
    		//displayToast("Uploading: " + upFile);
			fis = new FileInputStream(upFile);
			String writePath = uploadPath + upFile.getName();
			//displayToast("Second: " + writePath);
			request = apiObj.putFileOverwriteRequest(writePath,fis, upFile.length(), new ProgressListener() {
               @Override
                public long progressInterval() {
                    // Update the progress bar every half-second or so
                    return 500;
                }
				
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
    	} catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            error = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            error = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            error = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // uploadPath not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
        
            }
            // This gets the Dropbox error, translated into the user's language
            error = e.body.userError;
            if (error == null) {
                error = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            error = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            error = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            error = "Unknown error.  Try again.";
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
    	if(uploadAll)
    	{
    		if(!result)
    		{
    			resultMsg = "Oops, something went wrong with Dropbox, moving on to Box...";
    			displayToast(resultMsg);
    		}
    		UploadBox uploadBox = new UploadBox(upScreenContext, 0l, upFile, mClient, UploadScreen.UPLOAD_ALL_ON);
    		uploadBox.run();
    	}
    	else
    	{
        	
        	if(result)
        	{
        		resultMsg = "Yahoo! Successfully uploaded " + upFile.getName();
        	}
        	else
        	{
        		if(mCanceled)
        		{
        			resultMsg = "Upload Canceled";
        		}
        		else
        		{
        			resultMsg = "Silly clouds...looks like we had a problem moving things around. Try again.";
        		}
        	}
        	displayToast(resultMsg);
    	}


    }
    
    protected void displayToast(String message)
    {
    	Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	msg.show();
    }
}
