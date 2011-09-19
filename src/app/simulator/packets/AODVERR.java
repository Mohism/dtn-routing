package app.simulator.packets;

import app.simulator.routing.Router;
import app.simulator.routing.Transit;

public class AODVERR extends AODVPacket {
	Router Destination;
	public int lasttl=0;
	Transit transit;
	public Router getDestination(){return Destination;}
	public AODVERR(){
		super();
	}
	public AODVERR(Router Router1, Router Router2,  Router Destination) {
		super(Router1, Router2);
		this.Destination=Destination;
		// TODO Auto-generated constructor stub
	}

}
