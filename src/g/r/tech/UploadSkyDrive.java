package g.r.tech;

import java.util.Stack;

import com.microsoft.live.LiveConnectClient;

public class UploadSkyDrive {
    public static final String EXTRA_PATH = "path";

    private static final int DIALOG_DOWNLOAD_ID = 0;
    private static final String HOME_FOLDER = "me/skydrive";

    private LiveConnectClient mClient;
    private String mCurrentFolderId;
    private Stack<String> mPrevFolderIds;
    
    public UploadSkyDrive()
    {
    	
    }
}
