package info.lvcoffee.pppoew;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class getAddress
{
	  public List<String> getNetInterface()
	  {
		    List<String> lstUpNetInterface=new ArrayList<String>();;
	    	try
			{
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
							    (!strIfItem[0].equals("lo")))
					{
					
						lstUpNetInterface.add(strIfItem[0]);
						LogUtil.d("add interface "+strIfItem[0]);
						
					}
					strIs=is.readLine();				
				}
			}
			catch (Exception e)
			{
				LogUtil.e("getNetInterface "+e.getMessage());
			}			
		    return lstUpNetInterface;
	  }
	
	
	   public String getLocalIpAddress(String ifName) {   
	       try {   
	           for (Enumeration<NetworkInterface> en = NetworkInterface   
	                   .getNetworkInterfaces(); en.hasMoreElements();) {   
	               NetworkInterface intf = en.nextElement();   
	               
	               if(intf.getName().equals(ifName))
	               {
		               for (Enumeration<InetAddress> enumIpAddr = intf   
		                       .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
		                   InetAddress inetAddress = enumIpAddr.nextElement();   
		                   if (!inetAddress.isLoopbackAddress()) { 
		                       return inetAddress.getHostAddress().toString();   
		                   }   
		               }   
	               }
	           }   
	       } catch (SocketException ex) {   
	           LogUtil.d("getLocalIpAddress exception");   
	       }   
	       return "";   
	   }   

	   public String getLocalMacAddress(Context ctx) {   
	       WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);   
	       WifiInfo info = wifi.getConnectionInfo();  
	       return info.getMacAddress();   
	   }   
	 
	   public String getLocalMask(Context ctx) {   
	       WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);   
	       DhcpInfo info = wifi.getDhcpInfo();  
	       return intToIP(info.netmask); 

	   }   
	   
	   public String getLocalGateway(Context ctx) {   
	       WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);   
	       DhcpInfo info = wifi.getDhcpInfo();  
	       return intToIP(info.gateway); 

	   }   
	   
	   public String getLocalDNS(Context ctx) {   
	       WifiManager wifi = (WifiManager)  ctx.getSystemService(Context.WIFI_SERVICE);   
	       DhcpInfo info = wifi.getDhcpInfo();  
	       return intToIP(info.dns1)+"  ,  "+intToIP(info.dns2); 

	   }   
	 
	   private static String intToIP(int i) {
	       return ( (i& 0xFF) + "." +((i >>8)& 0xFF)+"."+((i>>16)&0xFF)+"."+((i >> 24 )&0xFF));
	   }
	   
}