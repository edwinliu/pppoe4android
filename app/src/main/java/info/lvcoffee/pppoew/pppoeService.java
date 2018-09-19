package info.lvcoffee.pppoew;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lvcoffee.pppoew.R;


public class pppoeService extends Service{
		DataInputStream inStream;
		DataOutputStream outStream;
		String strRead;	
		Process proc =null;
		boolean bThreadRunning=false;
		boolean bExit=false;
		//这里定义吧一个Binder类，用在onBind()有方法里，这样Activity那边可以获取到
		private MyBinder mBinder = new MyBinder();

		@Override
		public IBinder onBind(Intent intent) {
			LogUtil.d("start IBinder~~~");
			return mBinder;
		}
		@Override
		public void onCreate() {
			LogUtil.d("start onCreate~~~");
			super.onCreate();
		}
		
		@Override
		public void onStart(Intent intent, int startId) {
			LogUtil.d("start onStart~~~");
			super.onStart(intent, startId);	
		}
		
		@Override
		public void onDestroy() {
			LogUtil.d("start onDestroy~~~");
			super.onDestroy();
		}
		
		
		@Override
		public boolean onUnbind(Intent intent) {
			LogUtil.d("start onUnbind~~~");
			return super.onUnbind(intent);
		}
		
		//开始拨号连接
		public void startDial(final String user, final String password ,final String netInterface)
		{
			LogUtil.d("start Dial");
			if(!bThreadRunning)
			{
				startMonitorThread();
			}
			
			try
			{
				Process procPppoe = Runtime.getRuntime().exec("su");	    				
				String pppoeStr=getString(R.string.str_pppoe1)+" "+netInterface+" "+getString(R.string.str_pppoe2)
										+" "+user+" password "+password+" "+getString(R.string.str_pppoe3)+" &\n";
				LogUtil.i(pppoeStr);
				DataOutputStream osPppoe = new DataOutputStream(procPppoe.getOutputStream());
				osPppoe.writeBytes(pppoeStr);
				osPppoe.flush();
				//在i699i上发现有2个default路由，会导致PPPOE的default路由加不上，保险起见，删5次
				for(int i=0;i<5;i++)
				{
					osPppoe.writeBytes("/data/data/com.lvcoffee.pppoew/routew delete default\n");
					osPppoe.flush();
				}
				osPppoe.close();	
				bExit=false;
			}
			catch (Exception e)
			{
				LogUtil.d("startDial Exception");
			}			
		}
		
		void startMonitorThread()
		{
		    new Thread(new Runnable(){  

		    		public void run() {  	

    		            Intent it = new Intent();  
    		            it.setAction("android.intent.action.PPPOE_PROGRESS");  
    		            it.putExtra("step", Constant.PPPOE_STEP_START);  	 		    		            
 
    		            sendBroadcast(it);  
    		            
		    			try
		    			{
		    				if(proc==null)
		    				{
		    					proc = Runtime.getRuntime().exec("su");
		    				
			    				outStream = new DataOutputStream(proc.getOutputStream());
			    				outStream.writeBytes("logcat -c\n");
			    				outStream.flush();	
			    				
			    				outStream.writeBytes("logcat -s pppd\n");
			    				outStream.flush();	
			    				
			    				inStream= new DataInputStream(proc.getInputStream());
		    				}
		    
		    			}
		    			catch (Exception e)
		    			{
		    				LogUtil.d("pppoew Exception");
		    				return;
		    			}	
		    			
		    			bThreadRunning=true;
		    			while(bThreadRunning)
		    			{
			    			try
			    			{
	                            Thread.sleep(100);  

			    				strRead=inStream.readLine();
			    			
			    				if(bExit==true)
			    					continue;
			    					
			    				if(strRead.length()>0)
			    				{
			    				//	Log.e("pppoeService",strRead);
			    				
			    		            //发送特定action的广播  
			    		            Intent intent = new Intent();  
			    		            intent.setAction("android.intent.action.PPPOE_PROGRESS");  
			    		            if(strRead.contains("Timeout waiting for"))
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_TIMEOUT);  	
				    		            bExit=true;
				    				} 
			    		            else if(strRead.contains("Network is down"))
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_NETWORK_DOWN);  	
				    		            bExit=true;		    		            	
			    		            }
			    		            else if(strRead.contains("PAP authentication failed"))
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_PWD_ERROR);  	
				    		            bExit=true;		    		            	
			    		            }
			    		            else if(strRead.contains("Couldn't set tty to PPP discipline: Invalid argument"))
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_NOT_SUPPORT_PPPOE);  	
				    		            bExit=true;				    		            	
			    		            }
			    		            else if(strRead.contains("Connection terminated"))
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_CONNECT_TERMINATE);  	
				    		            bExit=true;				    		            	
			    		            }
			    		            else if(strRead.contains("authentication succeeded"))
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_SUCCESS);  	
				    		            bExit=true;		    		            	
			    		            }
			    		            else 
			    		            {
				    		            intent.putExtra("step", Constant.PPPOE_STEP_NULL);  		
			    		            }
			    		            sendBroadcast(intent);  		    		            
			    				}
			    			}
			    			catch (Exception e)
			    			{
			    				bThreadRunning=false;	
			    				LogUtil.d("pppoew Exception");
			    			}	 
		    			}
		    		}
		    	},"pppoew" ).start();  	
		}   
	    
		public class MyBinder extends Binder{
			pppoeService getService()
			{
				return pppoeService.this;
			}
		}	
	
}