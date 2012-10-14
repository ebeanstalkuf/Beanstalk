package g.r.tech;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
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
    public UpdateList(Context context, DropboxAPI <?> api, ListView x)
    {
    	mContext = context.getApplicationContext();
    	mApi = api;
    	dbListView = x;
    }

	@Override
    protected Boolean doInBackground(Void... params) {
		int i = 0;
		Entry dirent = null;
		try {
			dirent = mApi.metadata("/", 1000, null, true, null);
		} catch (DropboxException e) {
			// Unknown error
			mErrorMsg = "Unknown error.  Try again.";
			return false;
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
		ArrayList<String> nameslist = new ArrayList<String>();  
		nameslist.addAll( Arrays.asList(fnames)); 
		// Create Array Adapter using the planet list.  
		dblistAdapter = new ArrayAdapter<String>(mContext,R.layout.screen5_rowlayout, R.id.label, fnames);
		// Set the ArrayAdapter as the ListView's adapter.  
		dbListView.setAdapter( dblistAdapter );
		dbListView.setOnItemClickListener(new OnItemClickListener(){

        	@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				// TODO Auto-generated method stub
        		showToast("You clicked something!");
			}
        	});
    }
}