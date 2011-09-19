package app.simulator.packets;

import java.io.Serializable;

import app.simulator.routing.Router;

public class AODVRREQ extends AODVPacket  implements BroadCastPacket, Serializable {
	boolean D = false;
	public boolean getD(){return D;}
	public void setD(boolean d){D=d;}
	public AODVRREQ(){
		super();
	}
 
	public AODVRREQ(Router Router1, Router Router2, int ID) {
		super(Router1, Router2);
		this.setID(ID);
		// TODO Auto-generated constructor stub
	}

}
