package app.simulator.packets;

import app.simulator.routing.Router;
import app.simulator.routing.Transit;

public class MAODVRREP extends AODVRREP {
	public Transit transit;
	public MAODVRREP(){
		super();
	};
	public MAODVRREP(Router Router1, Router Router2, int metric, Transit transit){
		super(Router1,Router2,metric);
		this.transit=transit;
	}
}
