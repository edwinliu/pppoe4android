package info.lvcoffee.pppoew;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lvcoffee.pppoew.R;

public class pppoewApplication extends Application {
	private String TAG="PPPOEW";

	public String strUsername=" ";
	public String strPassword=" ";

	public ArrayList<String> listUser;
	public ArrayList<String> listPwd;
    
	public  final int APP_STATE_INIT = 0;
	public  final int APP_STATE_DIALLING = 1;
	public  final int APP_STATE_OK = 2;
	public  final int APP_STATE_FAIL = 3;
	public  int  appState=APP_STATE_INIT;

	Context tabContext=null;
		
	@Override
	public void onCreate() {
		super.onCreate();
	}


    public byte PppoeDialUp()
	    {
	    	String ifName="nowifi";
	    	try
			{
	    		String strPap="\""+strUsername+" * "+strPassword+" *\"";
				Process process = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(process.getOutputStream());
				DataInputStream is = new DataInputStream(process.getInputStream());
				os.writeBytes("netcfg\n");
				os.writeBytes("exit\n");
				os.flush();	
				String strIs=is.readLine();
				while(strIs!=null)
				{
					String[] strIfItem= strIs.split("\\s+");
					if((strIfItem.length==5)&&(strIfItem[1].equals("UP"))&&
							    (!strIfItem[0].equals("lo")&&(!strIfItem[0].equals("usb0"))))
					{
						ifName=strIfItem[0];
						Log.d(TAG,"use interface "+ifName);
						break;
					}
					strIs=is.readLine();				
				}
				
				Process proc = Runtime.getRuntime().exec("su");
				DataOutputStream oStream = new DataOutputStream(proc.getOutputStream());
				oStream.writeBytes("echo "+strPap+" > /data/data/com.lvcoffee.pppoew/pap-secrets\n");
				int userIndex=9999;
				int i=0;
				while(i<listUser.size())
				{
					//存在的用户名，则更新密码
				    if(strUsername.equals(listUser.get(i)))
				    {
				    	listPwd.set(i, strPassword);
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
			    		 strPap="\""+listUser.get(j)+" * "+listPwd.get(j)+" *\"";
						 oStream.writeBytes("echo "+strPap+" >> /data/data/com.lvcoffee.pppoew/pap-secrets\n");		    		 
				    }
					j++;
				}
				
				oStream.writeBytes("chmod 666 /data/data/com.lvcoffee.pppoew/pap-secrets\n");	
				
				if(ifName.equals("nowifi"))
					return 1;
				String pppoeStr=getString(R.string.str_pppoe1)+" "+ifName+" "+getString(R.string.str_pppoe2)
										+" "+strUsername+" password "+strPassword+" "+getString(R.string.str_pppoe3)+" &\n";
				oStream.writeBytes(pppoeStr);
				oStream.writeBytes("exit\n");
				oStream.flush();	
				appState=APP_STATE_DIALLING;
			}
			catch (Exception e)
			{
				Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
				toast.show();
				return 2;
			}
			return 0;
	    }
	  
}