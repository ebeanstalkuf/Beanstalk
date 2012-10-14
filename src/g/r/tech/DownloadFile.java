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
// TESTING THIS SHIT!!!
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

    // Note that, since we use a single file name here for simplicity, you
    // won't be able to use this code for two simultaneous downloads.
        
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

            
            String sdpath = Environment.getExternalStorageState() + "/" + filename.fileName();
            try {
                mFos = new FileOutputStream(sdpath);
            } catch (FileNotFoundException e) {
                mErrorMsg = "Couldn't create a local file to store the file";
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
            showToast("Successfully donwloaded " + filename.fileName());
            
        } else {
            // Couldn't download it, so show an error
            showToast(mErrorMsg);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }

    
    
}
