package app.simulator.restrictions;

import app.simulator.routing.AODV;
import app.simulator.routing.RoutigProtocol;

public  class Restrictions {
	public static double defaultradius=150;
	public static boolean isWiFi=false;//TODO realize
	public static int maxLinks=8;//TODO realize
	public static int maxPathLegth=30;
	public static int packetSize=25*1024;
	public static int maxSpeed=250*1024;
	public static int minPacketTransmitTime=1000*packetSize/maxSpeed;
	public static int maxRealRadius=70;//metr
	public static int ConnectionTime=30;//ms
	public static int Scale=100;//ms
	public static int MaxRouteFindAttempts=100;
	public static int RouteFindPeriod=10000;
	public static boolean autoremovelink=false;//TODO realize
	public static Class Algorythm = AODV.class;
	public static int SoftMoving=10;
	public static boolean Conus=false;
}
