package com.nimbus.app.beanstalk;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
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
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.nimbus.app.beanstalk.LiveSdkSampleApplication;
import com.nimbus.app.beanstalk.SaveScreen;

import com.nimbus.app.beanstalk.R;

public class UpdateSkydrive extends ListActivity {
	private static final String HOME_FOLDER = "me/skydrive";
	private LiveConnectClient mClient;
	private String mErrorMsg;
	private SkyDriveListAdapter skAdapter; 
	private String mCurrentFolderId;
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
	int running = 1;
    public UpdateSkydrive(Context context, ListView x)
    {
    	//Passable context
    	pContext = context;
    	//Usable context fror this activity
    	mContext = context.getApplicationContext();
    	//Authorization
    
    	//Listview assosciated with the listview on the file browser screen
    	skListView = x;
    	
    }

    public void run(String FolderId) {
    	auth();
        //If not connected
        if(connected == 0)
        {
        	skListView.setAdapter(null);
        	return;
        }
        mCurrentFolderId = FolderId;
    	skAdapter = new SkyDriveListAdapter(pContext);
    	final ProgressDialog progressDialog =
                ProgressDialog.show(pContext, "", "Loading. Please wait...", true);
        mClient.getAsync(FolderId + "/files", new LiveOperationListener() {
            @Override
            public void onComplete(LiveOperation operation) {
            	
                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    showToast(code + ": " + message);
                    return;
                }
                ArrayList<SkyDriveObject> fill = skAdapter.getSkyDriveObjs();
                fill.clear();
                JSONArray data = result.optJSONArray(JsonKeys.DATA);
                int uknobjs = 0;
                for (int i = 0; i < data.length(); i++) {
                	//Check for unknown object type
                	SkyDriveObject skyDriveObj;
                	try
                	{
                		skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));
                	}
                	catch (AssertionError e)
                	{
                		//Add that an additional file threw an error
                		uknobjs = uknobjs + 1;
                		continue;
                	}
                    fill.add(skyDriveObj);
                }
                progressDialog.dismiss();
                if(uknobjs == 1)
                {
                	showToast("Uh-oh! " + Integer.toString(uknobjs) + " file contains an Unknown SkyDriveObject type and could not be displayed.");
                }
                else if(uknobjs > 1)
                {
                	showToast("Uh-oh! " + Integer.toString(uknobjs) + " files contain an Unknown SkyDriveObject type and could not be displayed.");
                }
                
                if(fill.size() < 1)
                {
                	if(mCurrentFolderId.equals("me/skydrive"))
                	{
                		showToast("Whoops! Looks like the main folder is empty.");
                	}
                	else
                	{
                		showToast("Whoops! Looks like this folder is empty.");
                	}
                }
               skAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(LiveOperationException exception, LiveOperation operation) {
            	   progressDialog.dismiss();
                   showToast(exception.getMessage());
            }
        });
        skListView.setTextFilterEnabled(true);
        //If folder is empty
        skListView.setAdapter( skAdapter );
        skListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SkyDriveObject skyDriveObj = (SkyDriveObject) parent.getItemAtPosition(position);

                skyDriveObj.accept(new Visitor() {
                    @Override
                    public void visit(SkyDriveAlbum album) {
                        mPrevFolderIds.push(mCurrentFolderId);
                        run(album.getId());
                    }

                    @Override
                    public void visit(SkyDrivePhoto photo) {
                    	//Download File
                    	if(photo.getSize() > 52430000 && !connectedWifi())
    		            {
    		            	//Greater than 50 megabytes
    		            	dataCap(photo);
    		            }
    		            else
    		            {
    		            	DownloadSkydrive download = new DownloadSkydrive(pContext);
        					download.run(photo.getId(), photo.getName());
    		            }

                    }

                    @Override
                    public void visit(SkyDriveFolder folder) {
                        mPrevFolderIds.push(mCurrentFolderId);
                        run(folder.getId());
                    }

                    @Override
                    public void visit(SkyDriveFile file) {
                    	//Download File
                    	if(file.getSize() > 52430000 && !connectedWifi())
    		            {
    		            	//Greater than 50 megabytes
    		            	dataCap(file);
    		            }
    		            else
    		            {
    		            	DownloadSkydrive download = new DownloadSkydrive(pContext);
        					download.run(file.getId(), file.getName());
    		            }
                    }

                    @Override
                    public void visit(SkyDriveVideo video) {
                    	//Download File
                    	if(video.getSize() > 52430000 && !connectedWifi())
    		            {
    		            	//Greater than 50 megabytes
    		            	dataCap(video);
    		            }
    		            else
    		            {
    		            	DownloadSkydrive download = new DownloadSkydrive(pContext);
        					download.run(video.getId(), video.getName());
    		            }
                    }

                    @Override
                    public void visit(SkyDriveAudio audio) {
                    	//Download File
                    	if(audio.getSize() > 52430000 && !connectedWifi())
    		            {
    		            	//Greater than 50 megabytes
    		            	dataCap(audio);
    		            }
    		            else
    		            {
    		            	DownloadSkydrive download = new DownloadSkydrive(pContext);
        					download.run(audio.getId(), audio.getName());
    		            }
                    }
                });
            }
        });

        
	}
    public Boolean backKeyClicked()
    {
    	if (mPrevFolderIds.isEmpty()) {
            return false;
        }

        run(mPrevFolderIds.pop());
        return true;
    }
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
	//Custom Adapter
	private class SkyDriveListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<SkyDriveObject> mSkyDriveObjs;
        private View mView;

        public SkyDriveListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mSkyDriveObjs = new ArrayList<SkyDriveObject>();
        }

        /**
         * @return The underlying array of the class. If changes are made to this object and you
         * want them to be seen, call {@link #notifyDataSetChanged()}.
         */
        public ArrayList<SkyDriveObject> getSkyDriveObjs() {
            return mSkyDriveObjs;
        }

        @Override
        public int getCount() {
            return mSkyDriveObjs.size();
        }

        @Override
        public SkyDriveObject getItem(int position) {
            return mSkyDriveObjs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            SkyDriveObject skyDriveObj = getItem(position);
            mView = convertView != null ? convertView : null;

            skyDriveObj.accept(new Visitor() {
                @Override
                public void visit(SkyDriveVideo video) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.video);
                    setName(video);
                    //setDescription(video);
                }

                @Override
                public void visit(SkyDriveFile file) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.document);
                    setName(file);
                    //setDescription(file);
                }

                @Override
                public void visit(SkyDriveFolder folder) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.folder);
                    setName(folder);
                    //setDescription(folder);
                }

                @Override
                public void visit(SkyDrivePhoto photo) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.photo);
                    setName(photo);
                    //setDescription(photo);
                }

                @Override
                public void visit(SkyDriveAlbum album) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.folder);
                    setName(album);
                    //setDescription(album);
                }

                @Override
                public void visit(SkyDriveAudio audio) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.music);
                    setName(audio);
                    //setDescription(audio);
                }

                private void setName(SkyDriveObject skyDriveObj) {
                    TextView tv = (TextView) mView.findViewById(R.id.label);
                    tv.setText(skyDriveObj.getName());
                }
/* This is not used in out row layout
                private void setDescription(SkyDriveObject skyDriveObj) {
                    String description = skyDriveObj.getDescription();
                    if (description == null) {
                        description = "No description.";
                    }

                    TextView tv = (TextView) mView.findViewById(R.id.descriptionTextView);
                    tv.setText(description);
                }
*/
                private View inflateNewSkyDriveListItem() {
                    return mInflater.inflate(R.layout.screen5_rowlayout, parent, false);
                }

                private void setIcon(int iconResId) {
                    ImageView img = (ImageView) mView.findViewById(R.id.icon);
                    img.setImageResource(iconResId);
                }
            });


            return mView;
        }
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
	public void dataCap(final SkyDrivePhoto photo)
    {
    	AlertDialog dataCap = new AlertDialog.Builder(pContext).create();
    	dataCap.setTitle("Data Usage Warning");
    	dataCap.setMessage("The file you selected is over 50 MB and you are not connected to WiFi. This may incur data fees with your cellular provider. Do you want to continue?");
    	dataCap.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Just go back to what you were doing
            	DownloadSkydrive download = new DownloadSkydrive(pContext);
				download.run(photo.getId(), photo.getName());
            }
        });
    	dataCap.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Stop doing what your doing
            }
        });
    	dataCap.show();
    }
	public void dataCap(final SkyDriveFile file)
    {
    	AlertDialog dataCap = new AlertDialog.Builder(pContext).create();
    	dataCap.setTitle("Data Usage Warning");
    	dataCap.setMessage("The file you selected is over 50 MB and you are not connected to WiFi. This may incur data fees with your cellular provider. Do you want to continue?");
    	dataCap.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Just go back to what you were doing
            	DownloadSkydrive download = new DownloadSkydrive(pContext);
				download.run(file.getId(), file.getName());
            }
        });
    	dataCap.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Stop doing what your doing
            }
        });
    	dataCap.show();
    }
	public void dataCap(final SkyDriveVideo video)
    {
    	AlertDialog dataCap = new AlertDialog.Builder(pContext).create();
    	dataCap.setTitle("Data Usage Warning");
    	dataCap.setMessage("The file you selected is over 50 MB and you are not connected to WiFi. This may incur data fees with your cellular provider. Do you want to continue?");
    	dataCap.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Just go back to what you were doing
            	DownloadSkydrive download = new DownloadSkydrive(pContext);
				download.run(video.getId(), video.getName());
            }
        });
    	dataCap.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Stop doing what your doing
            }
        });
    	dataCap.show();
    }
	public void dataCap(final SkyDriveAudio audio)
    {
    	AlertDialog dataCap = new AlertDialog.Builder(pContext).create();
    	dataCap.setTitle("Data Usage Warning");
    	dataCap.setMessage("The file you selected is over 50 MB and you are not connected to WiFi. This may incur data fees with your cellular provider. Do you want to continue?");
    	dataCap.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Just go back to what you were doing
            	DownloadSkydrive download = new DownloadSkydrive(pContext);
				download.run(audio.getId(), audio.getName());
            }
        });
    	dataCap.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Stop doing what your doing
            }
        });
    	dataCap.show();
    }
	public boolean connectedWifi()
    {
    	ConnectivityManager conMan = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        //wifi
        State wifi = conMan.getNetworkInfo(1).getState();
        if (wifi == NetworkInfo.State.DISCONNECTED) 
        {
      	  return false;
        }
        return true;
    }
}
