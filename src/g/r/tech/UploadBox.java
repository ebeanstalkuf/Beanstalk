package g.r.tech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.ResponseListeners.FileUploadListener;
import com.box.androidlib.Utils.Cancelable;
import com.dropbox.client2.DropboxAPI.UploadRequest;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.DAO.BoxFolder;
import com.box.androidlib.ResponseListeners.CreateFolderListener;
import com.box.androidlib.ResponseListeners.DeleteListener;
import com.box.androidlib.ResponseListeners.FileDownloadListener;
import com.box.androidlib.ResponseListeners.FileUploadListener;
import com.box.androidlib.ResponseListeners.GetAccountTreeListener;
import com.box.androidlib.ResponseListeners.RenameListener;
import com.box.androidlib.Utils.Cancelable;

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
            	cancelable.cancel();
            }
        });
        dialog.show();
	}
	public void run()
	{
		//Create Folder
		
		//Test Login
		final SharedPreferences prefs = pcontext.getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
        authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);
        if (authToken == null) {
            displayToast( "Shucks! I can't see anything over here. Try logging in again.");
            return;
        }
        //Create input stream
		FileInputStream fis;
        try {
            fis = new FileInputStream(upFile);
            final Box boxServiceHandler = Box.getInstance(Constants.API_KEY);
            cancelable = boxServiceHandler.upload(authToken, Box.UPLOAD_ACTION_UPLOAD, fis, upFile.getName(), folderId,
                    new FileUploadListener() {

                        @Override
                        public void onComplete(BoxFile file, final String status) {
                            if (status.equals(FileUploadListener.STATUS_UPLOAD_OK)) {
                                displayToast( "Yahoo! Successfully uploaded " + upFile.getName());
                                // I commented this out because I will refresh the tree when you go to the file browser
                                //refresh();
                            }
                            else if (status.equals(FileUploadListener.STATUS_CANCELLED)) {
                                displayToast("Upload cancelled.");
                            }
                            else {
                                displayToast("Silly clouds...looks like we had a problem moving things around. Try again.");
                            }
                            dialog.dismiss();
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
	protected void displayToast(String message)
    {
    	Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	msg.show();
    }
}