package info.lvcoffee.pppoew;


import android.util.Log;


public final class LogUtil {
	/**
	 * 
	 */
	public enum LOGLEVEL
	{
		VERBOSE(0x0),
		DEBUG(0x1),
		INFO(0x2),
		WARNING(0x3),
		ERROR(0X4),
		NON(0x5);	
		
		
		int mnLevel = 0;
		LOGLEVEL(int nLevel)
		{
			mnLevel = nLevel;
		}
		
		public int GetValue()
		{
			return mnLevel;
		}
		
	};
	private static LOGLEVEL Log_Level = LOGLEVEL.NON;
	private final static String sTag = "PPPOEW";
	public LogUtil() {
	}

	public static void v(String sMsg)
	{
		if(Log_Level.GetValue() > LOGLEVEL.VERBOSE.GetValue())
			return;
		if(null != sMsg)
			Log.v(sTag, sMsg);
	}
	
	public static void d(String sMsg)
	{
		if(Log_Level.GetValue() > LOGLEVEL.DEBUG.GetValue())
			return;
		if(null != sMsg)
			Log.d(sTag, sMsg);
	}
	
	public static void i(String sMsg)
	{
		if(Log_Level.GetValue() > LOGLEVEL.INFO.GetValue())
			return;
		if(null != sMsg)
			Log.i(sTag, sMsg);
	}
	
	public static void w(String sMsg)
	{
		if(Log_Level.GetValue() > LOGLEVEL.WARNING.GetValue())
			return;
		if(null != sMsg)
			Log.w(sTag, sMsg);
	}
	
	public static void e(String sMsg)
	{
		if(Log_Level.GetValue() > LOGLEVEL.ERROR.GetValue())
			return;
		if(null != sMsg)
			Log.e(sTag, sMsg);
	}
	
	public static void e(String sMsg, Throwable tr)
	{
		if(Log_Level.GetValue() > LOGLEVEL.ERROR.GetValue())
			return;
		if(null != sMsg)
			Log.e(sTag, sMsg,tr);
	}
}


