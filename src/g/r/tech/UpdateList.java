package g.r.tech;

import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class UpdateList extends AsyncTask<Void, Long, Boolean> {
    private String mErrorMsg;
    String[] fnames = null;
    private DropboxAPI<?> mApi;
    private Context mContext;
    ListView dbListView ;
    ArrayAdapter<String> dblistAdapter ; 
    ArrayList<Entry> files = new ArrayList<Entry>();
	ArrayList<String> dir=new ArrayList<String>();
	String path;
	Context pContext;
	TextView t;
	Button pathbutton;
	Entry dirent = null;
	Activity activity;
    public UpdateList(Context context, DropboxAPI <?> api, ListView x, String dropboxPath, TextView y, Button b)
    {
    	//Passable context
    	pContext = context;
    	//Usable context fror this activity
    	mContext = context.getApplicationContext();
    	//Authorization
    	mApi = api;
    	//Listview assosciated with the listview on the file browser screen
    	dbListView = x;
    	//Initial folder path
    	path = dropboxPath;
    	// Text view assosciated with the path
    	t = y;
    	//Button assosciated with expand path
    	pathbutton = b;
    }

	@Override
    protected Boolean doInBackground(Void... params) {
		int i = 0;
			//Get entries
			try {
				dirent = mApi.metadata(path, 1000, null, true, null);
			} catch (DropboxException e) {
				// Unknown error
					mErrorMsg = "Unknown error.  Try again.";
					return false;
			}
		//If the path is not the main folder, add the back option
		if(!path.equals("/"))
		{
			dir.add("..");
		}
		for (Entry ent: dirent.contents) 
		{
			files.add(ent);// Add it to the list of thumbs we can choose from                       
			//dir = new ArrayList<String>();
			dir.add(new String(files.get(i++).fileName()));
		}
		i=0;
		fnames=dir.toArray(new String[dir.size()]);
		return true;
	}
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
	
	@Override
    protected void onPostExecute(Boolean result) {
		//Listen to path expand button
		 pathbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showToast("Path: " + path);	    
				}
				
			
			});
		//Folder is empty
		if(files.size() < 1)
		{
			showToast("That folder is empty");
			//Remove last slash
			path = path.substring(0, path.length()-1);
			//Remove characters upto slash
			while(path.charAt(path.length()-1) != '/')
			{
				path = path.substring(0, path.length()-1);
			}
			//Go back to previous folder
			UpdateList back = new UpdateList(pContext, mApi, dbListView, path,t, pathbutton);
			back.execute();
		}
		else
		{
			// Create Array Adapter using the fnames array
			dblistAdapter = new ArrayAdapter<String>(mContext,R.layout.screen5_rowlayout, R.id.label, fnames);
			// Set the ArrayAdapter as the ListView's adapter.  
			dbListView.setAdapter( dblistAdapter );
			// Set Path text 
			t.setText(path);
			//showToast(Environment.getExternalStoragePublicDirectory("test.jpg").getPath());
			dbListView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position,
						long id) {
					int index = position;
					int backclicked = 0;
					//If not in the main folder, subtract one from position to get the correct indes of files array
					if(!path.equals("/"))
					{
						//Clicked on back button
						if(position == 0)
						{
							//Set Path to parent path of current directory
							path = dirent.parentPath();
							//Update List
							UpdateList back = new UpdateList(pContext, mApi, dbListView, path,t, pathbutton);
							back.execute();
							//Set the flag that back has been clicked so download file is not called
							backclicked = 1;
						}
						else
						{
							//If your not in the main path you but subtract 1 from position because you add the back button in this case
							index = position - 1;
						}
					}
					//Clicked on Directory
					if((files.get(index).isDir) && (backclicked == 0))
					{
						path = path + files.get(index).fileName() + "/";
						UpdateList directory = new UpdateList(pContext, mApi, dbListView, path,t, pathbutton);
						directory.execute();
					}
					//Clicked on a file
					else if(backclicked == 0)
					{
						DownloadFile download = new DownloadFile(pContext, mApi, files.get(index));
						download.execute();
					}	
        		
				}
			});
		}
	}
}