
package info.lvcoffee.pppoew;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import com.lvcoffee.pppoew.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;



public class PppoeTabActivity extends TabActivity
{
	
	public static TabHost tabs;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pppoetab);

		Init();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	


	
	private void Init()
	{	
    	if (!hasRootPermission())
    	{
    		this.openNotRootDialog();
    	}
    	else
    	{
    		copyPPPOE();
    	}
    	
		tabs = getTabHost();
		TabHost.TabSpec spec = tabs.newTabSpec("help");
		spec.setIndicator(getString(R.string.tab1),getResources().getDrawable(R.drawable.helptab));
		spec.setContent(new Intent().setClass(this,helpWeb.class));
		tabs.addTab(spec);

		spec = tabs.newTabSpec("dial");
		spec.setIndicator(getString(R.string.tab2),getResources().getDrawable(R.drawable.pppoetab));
		spec.setContent(new Intent().setClass(this, pppoew.class));
		tabs.addTab(spec);

	}

	 
	
    @Override
    protected void onChildTitleChanged(Activity childActivity,
    		CharSequence title) {
    	  setTitle(title);
    	super.onChildTitleChanged(childActivity, title);
    }
	
  	private void openNotRootDialog() {
		new AlertDialog.Builder(PppoeTabActivity.this)
        .setTitle(getString(R.string.no_root))
        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	//disable dial tab
            		TabHost tabs = getTabHost();
            		View dialTab=tabs.getTabWidget().getChildAt(1);
            		dialTab.setEnabled(false);
                    LogUtil.i("No Root disable dial tab ");
                }
        })
        .show();
   	}	

	private boolean hasRootPermission() {
    	boolean rooted = true;
		try {
			File su = new File("/system/bin/su");
			if (su.exists() == false) {
				su = new File("/system/xbin/su");
				if (su.exists() == false) {
					su = new File("/su/bin/su");
					if (su.exists() == false) {
						rooted = false;
					}
				}
			}
		} catch (Exception e) {
			LogUtil.e("hasRootPermission: "+e.getMessage());
			rooted = false;
		}
		return rooted;
    }
    
	// Copy pppoew and pppdw to /system/bin
	private void copyPPPOE()
	{
    	new Thread(new Runnable(){
			public void run(){
			   try
			   {		
			    Process process = Runtime.getRuntime().exec("su");
			    DataOutputStream os = new DataOutputStream(process.getOutputStream());
				 //copy pppoew 
				File fpppoe = new File("/data/data/com.lvcoffee.pppoew/pppoew");
				if (!fpppoe.exists())
				{
					LogUtil.d("copy pppoew");
					InputStream pppoeStream = getResources().openRawResource(R.raw.pppoew);
					byte[] bytes = new byte[pppoeStream.available()];
					DataInputStream dis = new DataInputStream(pppoeStream);
					dis.readFully(bytes);
					FileOutputStream pppoeOutStream = new FileOutputStream("/data/data/com.lvcoffee.pppoew/pppoew");
					pppoeOutStream.write(bytes);
					pppoeOutStream.close();										
				}
				os.writeBytes("chmod 755 /data/data/com.lvcoffee.pppoew/pppoew\n");	
				
				//copy pppdw
				File fpppd = new File("/data/data/com.lvcoffee.pppoew/pppdw");
				if (!fpppd.exists())
				{
					LogUtil.d("copy pppdw");
					InputStream pppdStream = getResources().openRawResource(R.raw.pppdw);
		
					byte[] pppdbytes = new byte[pppdStream.available()];
					DataInputStream distream = new DataInputStream(pppdStream);
					distream.readFully(pppdbytes);
					FileOutputStream pppdOutStream = new FileOutputStream("/data/data/com.lvcoffee.pppoew/pppdw");
					pppdOutStream.write(pppdbytes);
					pppdOutStream.close();
				}
				os.writeBytes("chmod 755 /data/data/com.lvcoffee.pppoew/pppdw\n");
				
				//copy routew
				File froute = new File("/data/data/com.lvcoffee.pppoew/routew");
				if (!froute.exists())
				{
					LogUtil.d("copy routew");					
					InputStream routeStream = getResources().openRawResource(R.raw.routew);
		
					byte[] routebytes = new byte[routeStream.available()];
					DataInputStream rtstream = new DataInputStream(routeStream);
					rtstream.readFully(routebytes);
					FileOutputStream routeOutStream = new FileOutputStream("/data/data/com.lvcoffee.pppoew/routew");
					routeOutStream.write(routebytes);
					routeOutStream.close();						
				}
				os.writeBytes("chmod 755 /data/data/com.lvcoffee.pppoew/routew\n");
				os.writeBytes("exit\n");
				os.flush();					
			}
			catch (Exception e)
			{
				LogUtil.e("copy copyPPPOE error"+e.toString());		
				
				//java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
				
				//Toast toast = Toast.makeText(PppoeTabActivity.this, e.getMessage(), Toast.LENGTH_LONG);
				//toast.show();
			}
		  }
    	}).start();
   }

}