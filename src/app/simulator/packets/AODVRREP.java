package app.simulator.packets;

import java.io.Serializable;

import app.simulator.routing.Router;

public class AODVRREP extends AODVPacket implements Serializable {

	public AODVRREP(){
		super();
	}
	
	public AODVRREP(Router Router1, Router Router2) {
		super(Router1, Router2);
		// TODO Auto-generated constructor stub
	}

	public AODVRREP(Router Router1, Router Router2, int metric) {
		super(Router1, Router2);
		this.Hops=metric;
		// TODO Auto-generated constructor stub
	}

}
