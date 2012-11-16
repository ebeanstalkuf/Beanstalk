package g.r.tech;

import android.os.AsyncTask;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    DropBoxListAdapter dbAdapter;
    ArrayList<Entry> files = new ArrayList<Entry>();
	ArrayList<String> dir=new ArrayList<String>();
	static String path;
	Context pContext;
	TextView t;
	Button pathbutton;
	static Entry dirent = null;
	Activity activity;
	ProgressDialog progressDialog;
    public UpdateList(Context context, DropboxAPI <?> api, ListView x, String dropboxPath)
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
		progressDialog = ProgressDialog.show(pContext, "", "Loading. Please wait...", true);
    }

	@Override
    protected Boolean doInBackground(Void... params) {
		int i = 0;
		//Create Adapter
    	dbAdapter = new DropBoxListAdapter(pContext);
    	//Set ArrayAdapter
    	files = dbAdapter.getDropBoxObjs();
    	files.clear();
			//Get entries
			try {
				dirent = mApi.metadata(path, 1000, null, true, null);
			} catch (DropboxException e) {
				// Unknown error
					mErrorMsg = "Unknown error.  Try again.";
					return false;
			}
		for (Entry ent: dirent.contents) 
		{
			files.add(ent);// Add it to the list of thumbs we can choose from                       
		}
		i=0;
		progressDialog.dismiss();
		return true;
	}
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
	
	@Override
    protected void onPostExecute(Boolean result) {
		//Folder is empty
		if(files.size() < 1)
		{
			showToast("This folder is empty");
		}
		
		dbListView.setTextFilterEnabled(true);
		// Set the ArrayAdapter as the ListView's adapter.  
		dbListView.setAdapter( dbAdapter );
		//showToast(Environment.getExternalStoragePublicDirectory("test.jpg").getPath());
		dbListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				int index = position;
				//Clicked on Directory
				if((files.get(index).isDir))
				{
					path = path + files.get(index).fileName() + "/";
					UpdateList directory = new UpdateList(pContext, mApi, dbListView, path);
					directory.execute();
				}
				//Clicked on a file
				else
				{
					DownloadFile download = new DownloadFile(pContext, mApi, files.get(index));
					download.execute();
				}	
        	
			}
		});
	}
	public Boolean backKeyClicked()
    {
		if(!path.equals("/"))
		{
			if(dirent == null)
			{
				//Remove last slash
				path = path.substring(0, path.length()-1);
				//Remove characters upto slash
				while(path.charAt(path.length()-1) != '/')
				{
					path = path.substring(0, path.length()-1);
				}
			}
			else
			{
				//Set Path to parent path of current directory
				path = dirent.parentPath();
			}
			//Update List
			UpdateList back = new UpdateList(pContext, mApi, dbListView, path);
			back.execute();
			return true;
		}
		return false;
    }
	private class DropBoxListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<Entry> mDropBoxObjs;
        private View mView;

        public DropBoxListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDropBoxObjs = new ArrayList<Entry>();
        }

        /**
         * @return The underlying array of the class. If changes are made to this object and you
         * want them to be seen, call {@link #notifyDataSetChanged()}.
         */
        public ArrayList<Entry> getDropBoxObjs() {
            return mDropBoxObjs;
        }

        @Override
        public int getCount() {
            return mDropBoxObjs.size();
        }

        @Override
        public Entry getItem(int position) {
            return mDropBoxObjs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            Entry dropBoxObj = getItem(position);
            mView = convertView != null ? convertView : null;

            if(dropBoxObj.isDir)
            {
            	if (mView == null) {
                    mView = inflateNewDropBoxListItem(parent);
                }

                setIcon(R.drawable.folder);
                setName(dropBoxObj.fileName());
            }
            else
            {
            	String extensionType = filename(dropBoxObj.fileName());
                
                if( extensionType.equals("jpg") || extensionType.equals("png") || extensionType.equals("gif") 
                	|| extensionType.equals("bmp") || extensionType.equals("psd") || extensionType.equals("tif") 
                	|| extensionType.equals("tiff") || extensionType.equals("ai") || extensionType.equals("svg"))
                {
                	if (mView == null) {
                        mView = inflateNewDropBoxListItem(parent);
                    }

                    setIcon(R.drawable.photo);
                    setName(dropBoxObj.fileName());
                }
                else if( extensionType.equals("mp3") || extensionType.equals("m4a") || extensionType.equals("wav") 
                		|| extensionType.equals("flac") || extensionType.equals("aac") || extensionType.equals("m4p")
                		|| extensionType.equals("mmf") || extensionType.equals("ogg") || extensionType.equals("Opus")
                		|| extensionType.equals("raw") || extensionType.equals("vox") || extensionType.equals("wma") 
                		|| extensionType.equals("alac") || extensionType.equals("aiff"))
                {
                	if (mView == null) {
                        mView = inflateNewDropBoxListItem(parent);
                    }

                    setIcon(R.drawable.music);
                    setName(dropBoxObj.fileName());
                }
                else if( extensionType.equals("mov") || extensionType.equals("divx") || extensionType.equals("xvid")
                		|| extensionType.equals("asf") || extensionType.equals("avi") || extensionType.equals("m1v")
                		|| extensionType.equals("m2v") || extensionType.equals("m4v") || extensionType.equals("fla")
                		|| extensionType.equals("flv") || extensionType.equals("sol") || extensionType.equals("mpeg")
                		|| extensionType.equals("mpe") || extensionType.equals("mpg") || extensionType.equals("MP4")
                		|| extensionType.equals("wmv") || extensionType.equals("swf") || extensionType.equals("fcp")
                		|| extensionType.equals("ppj") )
                {
                	if (mView == null) {
                        mView = inflateNewDropBoxListItem(parent);
                    }

                    setIcon(R.drawable.video);
                    setName(dropBoxObj.fileName());
                }
                else
                {
                	if (mView == null) {
                        mView = inflateNewDropBoxListItem(parent);
                    }

                    setIcon(R.drawable.document);
                    setName(dropBoxObj.fileName());
                }
            } 
            return mView;
            
        }
        private void setName(String name) {
            TextView tv = (TextView) mView.findViewById(R.id.label);
            tv.setText(name);
        }
        
        private View inflateNewDropBoxListItem(ViewGroup parent) {
            return mInflater.inflate(R.layout.screen5_rowlayout, parent, false);
        }

        private void setIcon(int iconResId) {
            ImageView img = (ImageView) mView.findViewById(R.id.icon);
            img.setImageResource(iconResId);
        }
    }
	 public String filename(String fileName){
		 String filename_Without_Ext = "";
		 String ext = "";

		 int dotposition= fileName.lastIndexOf(".");
		 filename_Without_Ext = fileName.substring(0,dotposition);
		 ext = fileName.substring(dotposition + 1, fileName.length());

		 return ext;
		}
}