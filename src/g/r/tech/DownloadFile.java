package g.r.tech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

/**
 * Here we show getting metadata for the files and folders in dropbox. Creation of the "file browser" and the process of downloading a file.
 */
public class DownloadFile extends AsyncTask<Void, Long, Boolean> {
    /** Called when the activity is first created. */
	
	private Context mContext;
    private final ProgressDialog mDialog;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;

    private FileOutputStream mFos;

    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;
    Entry filename;
    File sdpath;
    String tt;

    // Note that, since we use a single file name here for simplicity, you
    // won't be able to use this code for two simultaneous downloads.
        
    @SuppressWarnings("deprecation")
	public DownloadFile(Context context, DropboxAPI<?> api, Entry file) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        mApi = api;
        filename = file;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Downloading Image");
        mDialog.setButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCanceled = true;
                mErrorMsg = "Canceled";

                // This will cancel the getThumbnail operation by closing
                // its stream
                if (mFos != null) {
                    try {
                        mFos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

       mDialog.show();
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
            if (mCanceled) {
                return false;
            }

            // Set Path and file length variables according to passed in entry
            String path = filename.path;
            mFileLen = filename.bytes;
            
            //Check SD Card status
            String sdcardstatus = Environment.getExternalStorageState();
            if(sdcardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            {
            	mErrorMsg = "Error: Your SD card has been mounted as Read Only. Please re-mount with write access.";
            	return false;
            }
            else if(sdcardstatus.equals(Environment.MEDIA_REMOVED))
            {
            	mErrorMsg ="Error: Your device is not showing an SD Card. Beanstalk can only download a file to an SD card";
            	return false;
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
            	//Set SD path
            	//sdpath = Environment.getExternalStorageDirectory().getPath() + "/Beanstalk Downloads/" + filename.fileName();
            	//Create File called filename in Beanstalk Downloads
            	sdpath = new File(bfolder, filename.fileName());
            
            }
            else
            {
            	mErrorMsg = "Error: Your device's SD Card is not mounted. Beanstalk can only download a file to an SD card";
            	return false;
            }
            // Try to create a file in the sdpath
            try {
                mFos = new FileOutputStream(sdpath);
            } catch (FileNotFoundException e) {
                mErrorMsg = "Error: Couldn't create a local file to store the file. This error could be caused if your device's SD Card is mounted on your computer. If this is the case, please disconnect your device and try again.";
                return false;
            }

            // This downloads the file
            try {
                DropboxFileInfo info = mApi.getFile(path, null, mFos, null);
                Log.i("File download:", "The file's rev is: " + info.getMetadata().rev);
                // /path/to/new/file.txt now has stuff in it.
            } catch (DropboxException e) {
                Log.e("DbExampleLog", "Something went wrong while downloading.");
            } finally {
                if (mFos != null) {
                    try {
                        mFos.close();
                    } catch (IOException e) {}
                }
            }
            if (mCanceled) {
                return false;
            }

            return true;

    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Successfully downloaded " + filename.fileName() + " to: " + sdpath);
            
        } else {
            // Couldn't download it, so show an error
            showToast("Error: Problem Downloading " + filename.fileName() + " to: " + sdpath + ". Please try again.");
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }

    
    
}
