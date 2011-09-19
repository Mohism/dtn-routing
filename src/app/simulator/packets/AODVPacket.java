package app.simulator.packets;

import java.io.Serializable;

import app.simulator.routing.AODVRoute;
import app.simulator.routing.Route;
import app.simulator.routing.Router;

public class AODVPacket extends RoutingPacket implements Serializable {
	int ID=0;
	public int router1num;
	public int router2num;
	public int getID(){return ID;}
	public void setID(int id){ID=id;}
	public void incID(){ID++;}
	public AODVPacket() {
		super();
	}
	public AODVPacket(Router Router1, Router Router2) {
		super(Router1, Router2);
		router1num=	Router1.router_num;
		Route route = Router1.routing_protocol.RoutingTable.get(Router2);
		if (route == null || !(route instanceof AODVRoute)){
			router2num = 0;
		}else{
			if(this instanceof AODVRREQ){
				router2num = ((AODVRoute)route).routernum+1;
			}else{
				router2num = ((AODVRoute)route).routernum;
			}
		}
		
		
		// TODO Auto-generated constructor stub
	}
	
	

}
