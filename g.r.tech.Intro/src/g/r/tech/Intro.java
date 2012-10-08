package g.r.tech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


//lets try a test here ;)
public class Intro extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen1);
        
        //5 secons presentation
        Thread timer = new Thread()
        {
        	public void run()
        	{
        		try
        		{
        			//5 seconds in mili
        			sleep(1000);
        		}
        		catch(InterruptedException e)
        		{
        			e.printStackTrace();
        			
        		}
        		finally
        		{
        			//define new intent
        			Intent openHome = new Intent("g.r.tech.HOME");
        			startActivity(openHome);
        		}
        	}
        };
        
        timer.start();
    }

	@Override
	//kil intro
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish(); //kill intro
	}
    
    
}
