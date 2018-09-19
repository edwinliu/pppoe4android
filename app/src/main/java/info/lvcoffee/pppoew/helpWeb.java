package info.lvcoffee.pppoew;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.lvcoffee.pppoew.R;

public class helpWeb extends Activity { 
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        setContentView(R.layout.help);
        
        WebView wv;
        
        wv = (WebView) findViewById(R.id.htmlView);
        wv.loadUrl("file:///android_asset/help.htm");
    }
    
	public void onResume() {
	    super.onResume();
	}
	
	public void onPause() {
	    super.onPause();
	}
}
  