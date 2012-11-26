

package g.r.tech;


import java.io.File;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveStatus;
import com.microsoft.live.LiveUploadOperationListener;

public class UploadSkyDrive {

    public static final String EXTRA_PATH = "path";

    private static final int DIALOG_DOWNLOAD_ID = 0;
    private static final String HOME_FOLDER = "me/skydrive";
    
    Context context;
    String uploadPath;
    File upFile;

    private LiveConnectClient mClient;
    private String mCurrentFolderId;
    
    //Skydrive logged in state stuff
    private LiveSdkSampleApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
    final ProgressDialog uploadProgressDialog;
    
    TextView resultTextView;
    
    public UploadSkyDrive(Context context, String uploadPath, File file)
    {
    	this.context = context;
		//String filePath = "";//data.getStringExtra(FilePicker.EXTRA_FILE_PATH);

		File uploadFile = file;

		uploadProgressDialog = new ProgressDialog(this.context);
		uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		uploadProgressDialog.setMessage("Uploading...");
		uploadProgressDialog.setCancelable(true);
		uploadProgressDialog.show();
    }
    
    
    protected Boolean execute() {

		final LiveOperation operation =
				mClient.uploadAsync(uploadPath,
									upFile.getName(),
									upFile,
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
				showToast(exception.getMessage());
			}

			@Override
			public void onUploadCompleted(LiveOperation operation) {
				uploadProgressDialog.dismiss();

				/*JSONObject result = operation.getResult();
				if (result.has(JsonKeys.ERROR)) {
					JSONObject error = result.optJSONObject(JsonKeys.ERROR);
					String message = error.optString(JsonKeys.MESSAGE);
					String code = error.optString(JsonKeys.CODE);
					showToast(code + ": " + message);
					return;
				}

				loadFolder(mCurrentFolderId);*/
			}
		});

		uploadProgressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				operation.cancel();
			}
		});
		return false;
    }

    /*
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
    
    
    private boolean checkSkydrive()
    {
    	final boolean loggedIn = false;
    	//Skydrive logged in stuff
        mApp = (LiveSdkSampleApplication) getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);

        mInitializeDialog = ProgressDialog.show(context, "", "Checking logged-in status. Please wait...", true);
        
        mAuthClient.initialize(Arrays.asList(Config.SCOPES), new LiveAuthListener() {
            @Override
            public void onAuthError(LiveAuthException exception, Object userState) {
                mInitializeDialog.dismiss();
            }

            @Override
            public void onAuthComplete(LiveStatus status,
                                       LiveConnectSession session,
                                       Object userState) {
                mInitializeDialog.dismiss();

                if (status == LiveStatus.CONNECTED) {
                    //setSkydriveLog(true);
                	loggedIn = true;
                    
                } else {
                    //setSkydriveLog(false);
                	loggedIn = false;
                }
            }
        });
        return loggedIn;
=======
    /*@Override
    protected void onProgressUpdate(Long... progress)
    {
    	int percentDone = (int) (100.0*(double) progress[0]/fileLength + 0.5);
    	uploadProgressDialog.setProgress(percentDone);
>>>>>>> Upload for SkyDrive update
    }*/
 
    /*public void createFolder() {
        final LiveOperationListener opListener = new LiveOperationListener() {
            public void onError(LiveOperationException exception, LiveOperation operation) {
                   resultTextView.setText("Error creating folder: " + exception.getMessage());
               }
            public void onComplete(LiveOperation operation) {
                JSONObject result = operation.getResult();
                String text = "Folder created:\n" +
                    "\nID = " + result.optString("id") +
                    "\nName = " + result.optString("name");
                   resultTextView.setText(text);
               }
           };
        mAuthClient.login(context.getApplicationContext(), Arrays.asList(new String[] { "wl.skydrive_update" }), 
            new LiveAuthListener() {
                   public void onAuthError(LiveAuthException exception, Object userState) {
                       resultTextView.setText("Error signing in: " + exception.getMessage());
                   }
                public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
                    try {
                        JSONObject body = new JSONObject();
                        body.put("name", "MyBrandNewFolder");
                        body.put("description", "My brand new folder");
                           mClient.postAsync("me/skydrive", body, opListener);    
                    }
                    catch(JSONException ex) {
                        resultTextView.setText("Error building folder: " + ex.getMessage());
                    }
                   }
            }
        ); 
    }
	*/
    
    private void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }
    
    private int computePercentCompleted(int totalBytes, int bytesRemaining) {
        return (int) (((float)(totalBytes - bytesRemaining)) / totalBytes * 100);
    }

    private ProgressDialog showProgressDialog(String title, String message, boolean indeterminate) {
        return ProgressDialog.show(context, title, message, indeterminate);
    }
}
