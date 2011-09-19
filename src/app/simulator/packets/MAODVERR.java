package app.simulator.packets;

import app.simulator.routing.Router;
import app.simulator.routing.Transit;

public class MAODVERR extends AODVERR {
	public Transit transit;
	public MAODVERR() {
		// TODO Auto-generated constructor stub
	}

	public MAODVERR(Router Router1, Router Router2, Router Destination) {
		super(Router1, Router2, Destination);
		// TODO Auto-generated constructor stub
	}

}
