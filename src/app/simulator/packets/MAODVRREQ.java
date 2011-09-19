package app.simulator.packets;

import app.simulator.routing.Router;
import app.simulator.routing.Transit;

public class MAODVRREQ extends AODVRREQ {
	public int angle;
	public Transit transit;
	public MAODVRREQ(){
		super();
	};
	public MAODVRREQ(Router Router1, Router Router2, int metric, Transit transit){
		super(Router1,Router2,metric);
		this.transit=transit;
	}
}
