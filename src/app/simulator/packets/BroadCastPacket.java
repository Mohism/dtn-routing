package app.simulator.packets;

import java.io.Serializable;

import app.simulator.routing.Router;

public interface BroadCastPacket {
	Router getSender();
	int getID();

}
