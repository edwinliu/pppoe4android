package info.lvcoffee.pppoew;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lvcoffee.pppoew.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class pppoew extends Activity 
	{
	private pppoeService  mPppoeService;  
	private Context mContext;  
	private String TAG="PPPOEW";
	private Button btnDial;
	private Button btnDisDial;
	private Button btnConState;	
	private EditText etUsername;
	private EditText etPassword;
	private EditText etInterface;	
    private ImageButton  ibSeluser;
    private ImageButton  ibSelinterface;
	LinearLayout llPppoe;
	LinearLayout llState;    
    
    
	public ArrayList<String> listUser;
	public ArrayList<String> listPwd;
	public List<String> lstInterface;    
	private static int ID_WAIT_PROCESS = 0;
	private ProgressDialog progressDialog;		
	private PppoeProgressReceiver receiver;
    
	/** Called when the activity is first created. */
  //  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mContext=getApplicationContext();

        receiver = new PppoeProgressReceiver();  
        IntentFilter filter = new IntentFilter();  
        filter.addAction("android.intent.action.PPPOE_PROGRESS");  
        //注册  
        registerReceiver(receiver, filter);  
        
    }

   //  @Override
   public void onResume()
   {
	    super.onResume();
        setCurrentView();
		Intent it  = new Intent();
		it.setClass(pppoew.this, pppoeService.class);
	//	mContext.startService(it);
		mContext.bindService(it, mServiceConnection, BIND_AUTO_CREATE);

   }
    

	public void onPause() {
	    super.onPause();

	}
	
   @Override
   public void onStop()
   {
//		Intent it  = new Intent();
//		it.setClass(pppoew.this, pppoeService.class);
//	    mContext.stopService(it);
	    super.onStop();
   }
    
   
   @Override  
   protected void onDestroy() {  
       unregisterReceiver(receiver);  
       super.onDestroy();  
   }  
 

   private void setCurrentView()
   {
	   llPppoe=(LinearLayout)findViewById(R.id.pppoeView);
	   llState=(LinearLayout)findViewById(R.id.stateView);
	   
	   //手机未拨号，显示拨号；已经拨号，显示IP地址等信息
	   if(checkState())
	   {
		   	llPppoe.setVisibility(View.VISIBLE);
		   	llState.setVisibility(View.GONE);
		   	initPppoeView();
	   }
	   else
	   {
		   	llPppoe.setVisibility(View.GONE);
		   	llState.setVisibility(View.VISIBLE);   
		   	initConnectStateView();
	   }
	}
   
   
    //检查是否已经拨号成功
   private boolean checkState()
   {
	   	getAddress getAddr=new getAddress();
		List<String> curInterface=new ArrayList<String>();;    
		curInterface=getAddr.getNetInterface();
    	int iIfNum=curInterface.size();  
    	int i=0;
    	String strInterface ;
    	  	
    	while(i<iIfNum)
    	{
    		strInterface=curInterface.get(i);
    		if(strInterface.equals("ppp0"))
    			return false;;   		
    		i++;
    	}	
    	return true;
   }
    
   private void initPppoeView()
   {
       btnDial=(Button)findViewById(R.id.dialing);
       btnDial.setOnClickListener(mDialListener);
       btnConState=(Button)findViewById(R.id.cleanUsers);
       btnConState.setOnClickListener(mDelUserPasswordListener);
       etUsername=(EditText)findViewById(R.id.username);
       etPassword=(EditText)findViewById(R.id.password);
       etInterface=(EditText)findViewById(R.id.iface);
       ibSeluser=(ImageButton)findViewById(R.id.Seluser);
       ibSelinterface=(ImageButton)findViewById(R.id.SelInterface);
       listUser=new ArrayList<String>();
       listUser.clear();
       listPwd=new ArrayList<String>();
       listUser.clear();   
       lstInterface=new ArrayList<String>();
       lstInterface.clear();
       ibSeluser.setOnClickListener(mSelUserListener);       
       ibSelinterface.setOnClickListener(mSelInterfaceListener);       
       getUsernamePassword();
	   SharedPreferences sharePref=this.getSharedPreferences("pppoe",Context.MODE_PRIVATE);
	   String strInterface=sharePref.getString("interface", "");
       etInterface.setText(strInterface);    
       
       TextView tvVersion=(TextView)findViewById(R.id.pppoeVersion);
       tvVersion.setText("Version:  "+getVersionName());
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
   
   private void initConnectStateView()
   {
	   TextView mac=(TextView)findViewById(R.id.mac);
	   TextView ip=(TextView)findViewById(R.id.ip);
	   TextView mask=(TextView)findViewById(R.id.mask);
	   TextView gateway=(TextView)findViewById(R.id.gateway);
	   TextView dns=(TextView)findViewById(R.id.DNS);
	   //TODO:按网络接口读取
	   getAddress getAdd=new getAddress();
	   mac.setText(getAdd.getLocalMacAddress(mContext));
	   ip.setText(getAdd.getLocalIpAddress("ppp0"));
	   mask.setText(getAdd.getLocalMask(mContext));
	   gateway.setText(getAdd.getLocalGateway(mContext));
	   dns.setText(getAdd.getLocalDNS(mContext));	     
       btnDisDial=(Button)findViewById(R.id.disConnect);
       btnDisDial.setOnClickListener(mTerminalPppoe);	   

   }
  
   
   /** 
    * 广播接收器 
    * @author user 
    * 
    */  
   private class PppoeProgressReceiver extends BroadcastReceiver {  
 
       @Override  
       public void onReceive(Context context, Intent intent) {  
           Bundle bundle = intent.getExtras();  
           int iStep = bundle.getInt("step");  

           switch(iStep)
           {
              case Constant.PPPOE_STEP_START:
            	  progressDialog.setMessage(getString(R.string.dialup_summary));
            	  break;
            	  
              case Constant.PPPOE_STEP_TIMEOUT:
            	  terminatePppoe();
            	  dismissDialog(ID_WAIT_PROCESS);
            	  showDialog(getString(R.string.stepFail),getString(R.string.stepTimeout));
            	  break; 
            	  
              case Constant.PPPOE_STEP_PWD_ERROR:
            	  terminatePppoe();
            	  dismissDialog(ID_WAIT_PROCESS);
            	  showDialog(getString(R.string.stepFail),getString(R.string.stepPwdError));
            	  break;
            	  
              case Constant.PPPOE_STEP_NETWORK_DOWN:   
            	  terminatePppoe();
            	  dismissDialog(ID_WAIT_PROCESS);
            	  showDialog(getString(R.string.stepFail),getString(R.string.stepNetworkDown));
            	  break;       
   
              case Constant.PPPOE_STEP_NOT_SUPPORT_PPPOE:    
            	  terminatePppoe();
            	  dismissDialog(ID_WAIT_PROCESS);
            	  showDialog(getString(R.string.stepFail),getString(R.string.stepNotSupportPppoe));
            	  break;  
   
              case Constant.PPPOE_STEP_CONNECT_TERMINATE:  
            	  terminatePppoe();
            	  dismissDialog(ID_WAIT_PROCESS);
            	  showDialog(getString(R.string.stepFail),getString(R.string.stepConnectTerminate));
            	  break;  
            	  
              case Constant.PPPOE_STEP_SUCCESS:
            	  progressDialog.setMessage(getString(R.string.stepSuccess));
            	  dismissDialog(ID_WAIT_PROCESS);
	  			  LogUtil.d("dial success");						
	  			  Toast toast = Toast.makeText(mContext,"dial success", Toast.LENGTH_LONG);
	  			  toast.show();
	  			 
	  			//  PointsManager.getInstance(mContext).spendPoints(1);
		  		  llPppoe.setVisibility(View.GONE);
				  llState.setVisibility(View.VISIBLE);   
				  initConnectStateView();

            	  break;
            	  
               default:
             	  break;
           }
          
       }  
   }  
   

   
   private void showDialog(String title,String msg)
   {
		new AlertDialog.Builder(pppoew.this)
			.setTitle(title)
			.setIcon(android.R.drawable.ic_dialog_info)
		    .setMessage(msg)
		    .setPositiveButton("OK", null)
		    .show();
   }
	
    private View.OnClickListener mSelUserListener= new View.OnClickListener() { 	
	
        public void onClick(View v) {
        	int iUserNum=listUser.size();
        	int i=0;
        	int index=0;
        	String[] strArray=new String[iUserNum] ;
        	String strUser=etUsername.getText().toString();    	
        	while(i<iUserNum)
        	{
        		strArray[i]=listUser.get(i);
        		if(strArray[i].equals(strUser))
        			index=i;
        		
        		i++;
        	}
        	
    		new AlertDialog.Builder(pppoew.this)
            .setNegativeButton("Cancel",null)
            .setSingleChoiceItems(strArray, index, 
			 	  new DialogInterface.OnClickListener() {			 	                              
			 	     public void onClick(DialogInterface dialog, int which) {
			             etUsername.setText(listUser.get(which));
			             etPassword.setText(listPwd.get(which));   
			             LogUtil.d("user:"+etUsername.getText().toString()
			             		                    +",password:"+etPassword.getText().toString());
			 	        dialog.dismiss();
			 	     }
			 	    }
			 	  )
            .show();    		    	
        }
    };
    
    private View.OnClickListener mSelInterfaceListener= new View.OnClickListener() { 	
    	    	
        public void onClick(View v) {
        	if(false==checkWifiConnect())
        	{
        		return;       		
        	}
        	
        	getAddress getAddr=new getAddress();
        	lstInterface.clear();
        	lstInterface=getAddr.getNetInterface();
        	int iIfNum=lstInterface.size();  
       	
        	if(iIfNum==0)
        	{
        		new AlertDialog.Builder(pppoew.this)
        		.setTitle(R.string.netInterface)
        		.setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(getString(R.string.no_wifi))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {                      
                            //    pppoew.this.finish();
                        	LogUtil.d("Dont open WIFI");
                        }
                })
                .show();
        		return;             		       		
        	}
        		
        		
        	int i=0;
        	int index=0;
        	String[] strArray=new String[iIfNum] ;
        	String strIf=etInterface.getText().toString();   	
        	while(i<iIfNum)
        	{
        		strArray[i]=lstInterface.get(i);
        		if(strArray[i].equals(strIf))
        			index=i;   		
        		i++;
        	}
        	
    		new AlertDialog.Builder(pppoew.this)
            .setNegativeButton("Cancel",null)
            .setSingleChoiceItems(strArray,index, 
			 	  new DialogInterface.OnClickListener() {
			 	                              
			 	     public void onClick(DialogInterface dialog, int which) {
			 	    	etInterface.setText(lstInterface.get(which));
			 	        dialog.dismiss();
			 	     }
			 	    }
			 	  )
            .show();    		    	
        }
    };
    
  
    private void savePppoeInfo()
    {
		SharedPreferences sharePref=this.getSharedPreferences("pppoe",Context.MODE_PRIVATE);
		Editor edPref=sharePref.edit();
		edPref.putString("interface", etInterface.getText().toString());
		edPref.commit();		
				
    	String strUser=etUsername.getText().toString();
    	String strPwd=etPassword.getText().toString();

    	try
		{   	
    		String strPap=strUser+" * "+strPwd+" *\n";
        	
			int userIndex=9999;
			int i=0;
			while(i<listUser.size())
			{
				//存在的用户名，则更新密码
			    if(strUser.equals(listUser.get(i)))
			    {
			    	listPwd.set(i, strPwd);
			    	userIndex=i;
			    	break;
			    }
			    i++;
			}

			int j=0;
			while(j<listUser.size())
			{
			    if(j!=userIndex)
			    {
		    		 strPap=strPap+listUser.get(j)+" * "+listPwd.get(j)+" *\n";					 					 
			    }
				j++;
			}

        	File fpap = new File("/data/data/com.lvcoffee.pppoew/pap-secrets");
        	FileWriter fw=new FileWriter(fpap);
        	fw.write(strPap);	
        	fw.close();
		}
		catch (Exception e)
		{
			Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			toast.show();			
		}			
    		
    		
    }
    
    private void getUsernamePassword()
    {
		try
		{
	    	File fpap = new File("/data/data/com.lvcoffee.pppoew/pap-secrets");
			
			if (fpap.exists())
			{			
				Log.d(TAG,"get user name and pwd from file");

				FileReader fpapRd = new FileReader(fpap);
				BufferedReader brd = new BufferedReader(fpapRd);
	
				String papStr=brd.readLine();
				String[] strPap= papStr.split(" ");
				
				if(strPap.length==4)
				{
					listUser.add(strPap[0]);
					listPwd.add(strPap[2]);
					etUsername.setText(strPap[0]);
					etPassword.setText(strPap[2]);
				}
		
				papStr=brd.readLine();
				strPap= papStr.split(" ");
				while(strPap.length==4)
				{
					listUser.add(strPap[0]);
					listPwd.add(strPap[2]);	
					papStr=brd.readLine();
					strPap= papStr.split(" ");
				}
				
				brd.close();
				fpapRd.close();
			}

		}
		catch (Exception e)
		{
			LogUtil.d(e.toString());
			//Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			//toast.show();
		}
    }
    
    
  	
    private View.OnClickListener mDialListener= new View.OnClickListener() {
        public void onClick(View v) {    	      	
        	if(false==checkWifiConnect())
        	{
        		return;    		
        	}
        	  /*
        	int curPoint=pppoewApplication.g_coin;
        	if(0==curPoint)
        	{
    			Toast toast = Toast.makeText(mContext,"Please to get point", Toast.LENGTH_LONG);
    			toast.show();  		
        		return;
        	}
        	*/
        	if(false==checkUserInput())
        	{
		    	new AlertDialog.Builder(pppoew.this)
		    			.setTitle(R.string.inputError)
			    		.setIcon(android.R.drawable.ic_dialog_info)
			    	    .setMessage(getString(R.string.input_error))
			    	    .setPositiveButton("OK", null)
			    	    .show();
		    	return;
        	}	

        	savePppoeInfo();
        	showWaitProgressBar();
        	mPppoeService.startDial(etUsername.getText().toString(),
        				etPassword.getText().toString(),	etInterface.getText().toString());
        	
        }
    };
   
    
    private void  showWaitProgressBar()
    {   
		showDialog(ID_WAIT_PROCESS);
    	new Thread(new Runnable(){  

    		public void run() {  
 
    			for(int i=1;i<=60;i++){  
                         try{  
                             Thread.sleep(1000);  	        			
  		        			 if (i==60)
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
	    	progressDialog.setCancelable(false);
	        return progressDialog;
    	}
    	return null;
    }
    
    private View.OnClickListener mDelUserPasswordListener= new View.OnClickListener() {
    	
        public void onClick(View v) {
    		new AlertDialog.Builder(pppoew.this)
			.setTitle(R.string.user_name)
    		.setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(getString(R.string.cleanHint))
            .setNegativeButton("No",null)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {               	   
		                    LogUtil.d("Clean all user and pwd");
		        	    	File fpap = new File("/data/data/com.lvcoffee.pppoew/pap-secrets");
		        	    	fpap.delete();
		        	    	etUsername.setText("");
		        	    	etPassword.setText("");
		        	    	listUser.clear();
		        	    	listPwd.clear();
                		}
			    	} )
		    	
            .show();    		    	
        }
    };
    
    
    /*检查用网络接口，户名和密码输入，有人说有密码是空的情况*/
    private boolean checkUserInput()
    {
    	//Log.d(TAG,etUsername.getText().toString()+","+etPassword.getText().toString());
    	if((etUsername.getText().toString().trim().length()==0)||(etInterface.getText().toString().trim().length()==0))
    		return false;
    	return true;
    }

 
    
    /*检查WIFI是否已经连上了无线AP*/
    private boolean checkWifiConnect()
    {

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (State.CONNECTED == state) {			
			return true;
		}

		new AlertDialog.Builder(pppoew.this)
		.setTitle(R.string.inputError)
		.setIcon(android.R.drawable.ic_dialog_info)
        .setMessage(getString(R.string.no_wifi))
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {                      
                    //    pppoew.this.finish();
                	LogUtil.d("Dont open WIFI");
                }
        })
        .show();
		
		return false;
		
    }
  
    
    private View.OnClickListener mTerminalPppoe= new View.OnClickListener() {
    	
        public void onClick(View v) {
    		new AlertDialog.Builder(pppoew.this)
			.setTitle(R.string.disConnect)
    		.setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(getString(R.string.disConnectInfo))
            .setNegativeButton("No",null)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {               	   
            			    terminatePppoe();
        				    onDestroy();	  
        					System.exit(0);
        		        }
                	
			    	} )
		    	
            .show();    		    	
        }
        
      
    };
    
    
    private void terminatePppoe()
    {
		try
		{
			String pidPathName="/data/data/com.lvcoffee.pppoew/pid.pppoe";
	    	File pppoepid = new File(pidPathName);
			
			if (pppoepid.exists())
			{				
				Process process = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(process.getOutputStream());
				os.writeBytes("chmod 644 "+pidPathName+" \n");		
				FileReader pidRd = new FileReader(pppoepid);
				BufferedReader brd = new BufferedReader(pidRd);		
				String pidStr=brd.readLine();
				int pid= Integer.parseInt(pidStr);
				
				os.writeBytes("kill -9 " + pid+" \n");
				os.writeBytes("exit\n");
				os.flush();	
				os.close();
				brd.close();
				pidRd.close();
	
			}
		}
		catch (Exception e)
		{
			Log.d(TAG,e.toString());
		}
    }
    
	//这里需要用到ServiceConnection在Context.bindService和context.unBindService()里用到
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		//当我bindService时，让TextView显示MyService里getSystemTime()方法的返回值	
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mPppoeService = ((pppoeService.MyBinder)service).getService();
			LogUtil.d("onServiceConnected");
		
		}
		
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
}