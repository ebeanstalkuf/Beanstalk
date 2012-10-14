package g.r.tech;


import android.app.Activity;
import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class UploadScreen extends Activity {
    /** Called when the activity is first created. */

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        
       findViewById(R.id.Upcloud).setOnTouchListener(new MyTouchListener());
       findViewById(R.id.default_file).setOnTouchListener(new MyTouchListener());
    }
    
    private final class MyTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
          if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            ClipData data = ClipData.newPlainText("", "");
            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.VISIBLE);
            return true;
          } else {
            return false;
          }
        }
      }
    
    class MyDragListener implements OnDragListener {
        
        Drawable movingCloud = getResources().getDrawable(R.drawable.upcloud);
        //this may be where we can substitute for the attached file preview; just make it invisible prior to drag
        Drawable movingFile = getResources().getDrawable(R.drawable.default_file);
       
    	public boolean onDrag(View v, DragEvent event) {
          int action = event.getAction();
          switch (event.getAction()) {
          case DragEvent.ACTION_DRAG_STARTED:
            // Do nothing
            break;
          case DragEvent.ACTION_DRAG_ENTERED:
            v.setBackgroundDrawable(movingCloud);
            v.setBackgroundDrawable(movingFile);
            break;
          case DragEvent.ACTION_DRAG_EXITED:
            v.setBackgroundDrawable(movingCloud);
            v.setBackgroundDrawable(movingFile);
            break;
          case DragEvent.ACTION_DROP:
            // Dropped, reassign View to ViewGroup
            View view = (View) event.getLocalState();
            ViewGroup owner = (ViewGroup) view.getParent();
            owner.removeView(view);
            LinearLayout container = (LinearLayout) v;
            container.addView(view);
            view.setVisibility(View.VISIBLE);
            break;
          case DragEvent.ACTION_DRAG_ENDED:
            v.setBackgroundDrawable(movingCloud);
            v.setBackgroundDrawable(movingFile);
          default:
            break;
          }
          return true;
        }
      }
    
}
