package info.lvcoffee.pppoew;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lvcoffee.pppoew.R;


public class connectState extends Activity 
{
	String TAG="PPPOEW";
//	AdView adV;
	TextView tvStates;
	public static final int MESSAGE_NET_CONNECT = 1;
	public static final int MESSAGE_NET_DISCONNECT = 2;
    int notificationID = 1;
    NotificationManager ntManager;
	private pppoewApplication application = null;
	public static connectState currentInstance = null;
	private static int ID_WAIT_PROCESS = 0;
	private ProgressDialog progressDialog;
	
	private static void setCurrent(connectState current){
		   connectState.currentInstance = current;
	    }
	   
  //  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectstate);
//        adV = (AdView) findViewById(R.id.adView);
//        adV.setAdViewListener(new youmiAdListener());
	//	tvStates=(TextView)findViewById(R.id.netstate);		
        // Init Application
        this.application = (pppoewApplication)this.getApplication();
        connectState.setCurrent(this);

		startListening();
		
	//	Intent in=this.getIntent();

	 //   ntManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);  
     //   if(in.getBooleanExtra("showProgress", false))
		if(application.appState==application.APP_STATE_DIALLING)
        {
	        showWaitProgressBar();
	    	
	        /*
		    Notification nt = new Notification(R.drawable.icon, "PPPOEW",
		    										System.currentTimeMillis());

		    nt.flags=Notification.FLAG_NO_CLEAR;
		    Intent intent = new Intent(this, connectState.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		 
		    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,        
		    								intent, PendingIntent.FLAG_UPDATE_CURRENT);
		    nt.setLatestEventInfo(this, "PPPOEW", "", contentIntent);
		    ntManager.notify(notificationID, nt);
		    */
        }
		

        
    }
	
//    private final class youmiAdListener implements net.youmi.android.AdViewListener { 
//		 public void onAdViewSwitchedAd(AdView adv) 
//		 {
//			 application.appState=application.APP_STATE_OK ;
//     		 Log.d(TAG, "onAdViewSwitchedAd.");
//		 }
//	
//		 public void onConnectFailed(AdView adv) 
//		 {
//			 application.appState=application.APP_STATE_FAIL;
//			 Log.d(TAG, "onConnectFailed.");
//		 }
//    }
    
    public Handler viewUpdateHandler = new Handler(){
        public void handleMessage(Message msg) {
        	switch(msg.what) {
        	case MESSAGE_NET_CONNECT :
        		Log.d(TAG, "CONNECT.");
        		tvStates.setText(R.string.net_connect);        	
            	break;
            	
        	case MESSAGE_NET_DISCONNECT :
        		Log.d(TAG, "DISCONNECT");
        		tvStates.setText(R.string.net_disconnect);
            	break;
        	}
        }
    };
    
	private static final int MENU_LOG = 0;
	private static final int MENU_EXIT = 1;
	private static final int MENU_ABOUT = 2;
	
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean supRetVal = super.onCreateOptionsMenu(menu);
    	SubMenu logSubmenu = menu.addSubMenu(0, MENU_LOG, 0, getString(R.string.save_log));
    	logSubmenu.setIcon(drawable.ic_menu_save);
   
    	SubMenu exitSubmenu = menu.addSubMenu(0, MENU_EXIT, 0, getString(R.string.exit_app));
    	exitSubmenu.setIcon(drawable.ic_menu_close_clear_cancel);

    	SubMenu aboutSubmenu = menu.addSubMenu(0, MENU_ABOUT, 0, getString(R.string.menu_about));
    	aboutSubmenu.setIcon(drawable.ic_menu_help);
	
    	return supRetVal;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	boolean supRetVal = super.onOptionsItemSelected(menuItem);
    	Log.d(TAG, "Menuitem:getId  -  "+menuItem.getItemId()); 
    	switch (menuItem.getItemId()) {
	    	case MENU_LOG :
	    		saveLog();
		        break;
		        
	    	case MENU_EXIT :
	    		//ntManager.cancel(notificationID);
	    	//	ntManager.cancelAll();
	    		try
	    		{
			    	File pppoepid = new File("/data/data/com.lvcoffee.pppoew/pid.pppoe");
					
					if (pppoepid.exists())
					{			
						Log.d(TAG,"kill pppoe");
	
						FileReader pidRd = new FileReader(pppoepid);
						BufferedReader brd = new BufferedReader(pidRd);
			
						String pidStr=brd.readLine();
						int pid= Integer.parseInt(pidStr);
				   
			
						Process process = Runtime.getRuntime().exec("su");
						DataOutputStream os = new DataOutputStream(process.getOutputStream());
						os.writeBytes("kill -9 " + pid);
						os.writeBytes("exit\n");
						os.flush();	
			
				
						brd.close();
						pidRd.close();
	
					}
	    		}
				catch (Exception e)
				{
					Log.d(TAG,e.toString());
				}
	    	    onDestroy();	  
	    		System.exit(0);
		        break;
		        
	    	case MENU_ABOUT :
	    		this.openAboutDialog();
	    		break;  		
    	}
    	return supRetVal;
    }  
  
    private void  showWaitProgressBar()
    {
    
		showDialog(ID_WAIT_PROCESS);
    	new Thread(new Runnable(){  

    		public void run() {  
 
    			for(int i=1;i<=20;i++){  
                         try{  
                               Thread.sleep(1000);  
  		        			
  		        			 if(application.appState==application.APP_STATE_OK)
  		        			 {
  		        				dismissDialog(ID_WAIT_PROCESS);
  		        				 break;
  		        			 }
  		        			 else if (i==20)
  		        			 {
   		        				dismissDialog(ID_WAIT_PROCESS);        	  		   
 		        				 break;		        				 
  		        			 }
                          }
                         catch(Exception ex)
                          {  
                             ex.printStackTrace();  
                         }  
                      } 
                   }  
                     
               }).start();  	
    }
    


    
    protected Dialog onCreateDialog(int id) {
    	if (id == ID_WAIT_PROCESS) {
	    	progressDialog = new ProgressDialog(this);
	    	progressDialog.setTitle(getString(R.string.dial_up));
	    	progressDialog.setMessage(getString(R.string.dialup_summary));
	    	progressDialog.setIndeterminate(false);
	    	progressDialog.setCancelable(true);
	        return progressDialog;
    	}
    	return null;
    }
    
	public void saveLog()
    {
	    String state = Environment.getExternalStorageState();
	    if (!Environment.MEDIA_MOUNTED.equals(state)) {
    		new AlertDialog.Builder(connectState.this)
            .setTitle(getString(R.string.no_sdcard))
            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {                      
                    	connectState.this.finish();
                    }
            })
            .show();
    		return;	        
	    } 
    
    	try
		{
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("logcat -df /sdcard/logcat.log\n");
			os.writeBytes("netcfg >>/sdcard/logcat.log\n");
			os.writeBytes("/data/data/com.lvcoffee.pppoew/routew >>/sdcard/logcat.log\n");
			os.writeBytes("exit\n");
			os.flush();	
			Toast toast = Toast.makeText(this, R.string.log_saved, Toast.LENGTH_LONG);
			toast.show();			
		}
		catch (Exception e)
		{
			Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			toast.show();
		}
    }
  
	
   	private void openAboutDialog() {
		LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.aboutview, null); 
        TextView versionName = (TextView)view.findViewById(R.id.versionName);
        versionName.setText(getVersionName());     
        TextView about= (TextView)view.findViewById(R.id.about);
        about.setText(R.string.about);    
		new AlertDialog.Builder(connectState.this)
        .setTitle("About")
        .setView(view)

        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "Close pressed");
                }
        })
        .show();  		
   	}
  
    public String getVersionName() {
    	String version = "?";
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (Exception e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {  

         Log.d(TAG,"KeyEvent.onKeyDown");
       
          if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
          {  
        	    Log.d(TAG,"KeyEvent.KEYCODE_BACK");
        	     KeyEvent ke=new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_HOME);

        	   return super.dispatchKeyEvent(ke); 

          }  
          return super.onKeyDown(keyCode, event);
      }   
   
    public boolean onKeyUp(int keyCode, KeyEvent event) {  

        Log.d(TAG,"KeyEvent.onKeyUp");

         if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
         {  
        	  Log.d(TAG,"KeyEvent.KEYCODE_BACK");
    	     KeyEvent ke=new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_HOME);
    	     
    	     return super.dispatchKeyEvent(ke); 

         }  
         return super.onKeyUp(keyCode, event);
     }  
  
    
	  private void startListening() {
		
	    	Thread watcher = new Thread( new Runnable() {
	    	      Message msg = Message.obtain();
	            public void run() {
	               	try{
		        		Process process = Runtime.getRuntime().exec("su");
		    			DataOutputStream os = new DataOutputStream(process.getOutputStream());
	
		    			while(true) {
		    				Thread.sleep(2000); 
				      
			            	if(application.appState==application.APP_STATE_OK)
			            	{
			 	            	 msg = Message.obtain();

					             msg.what = connectState.MESSAGE_NET_CONNECT;
				            	 connectState.currentInstance.viewUpdateHandler.sendMessage(msg);
			 	         
			            		 Log.d(TAG,"DATA_CONNECTED");
			            	}
			            	else
			            	{	   
				    			 os.writeBytes("/data/data/com.lvcoffee.pppoew/routew add -net 0.0.0.0 netmask 0.0.0.0 dev ppp0\n");

				 	             msg = Message.obtain();
					             msg.what = connectState.MESSAGE_NET_DISCONNECT;
				            	 viewUpdateHandler.sendMessage(msg);
			 	         
			            		 Log.d(TAG,"DATA_DISCONNECTED");
			            		 
			            	}
		    			}
	               	}
	            	catch (Exception e)
	        		{
	        			Log.d(TAG,e.toString());	
	        		}
	            	}
	        });
	        
	        watcher.setName("pppoe Listening");
	        watcher.start();
	 	
	    }
}