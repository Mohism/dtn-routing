package app.simulator.packets;

import java.io.Serializable;

import app.gui.GraphEditor;
import app.simulator.routing.Router;

public class RoutingPacket extends Packet implements Serializable{
	public static int count=0;
	public RoutingPacket() {
		super();
	}
	public RoutingPacket(Router Router1, Router Router2) {
		super(Router1, Router2);
		synchronized(GraphEditor.graph){
			count++;
		}
		// TODO Auto-generated constructor stub
	}

}
