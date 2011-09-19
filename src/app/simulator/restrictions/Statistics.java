package app.simulator.restrictions;

import java.awt.Canvas;

import com.mxgraph.model.mxCell;

import app.gui.GraphEditor;
import app.simulator.Link;
import app.simulator.packets.Packet;
import app.simulator.packets.RoutingPacket;
import app.simulator.routing.AODV;
import app.simulator.routing.Router;

public class Statistics {
	public static String getStat(){
		String S;
		S= "Statisics:\n" +
				"Packets Sum count: "+Packet.count+"\n" +
				"Packets Current count: "+Packet.activecount+"\n"+
				"Packets Finalized: "+Packet.finalizedcount+"\n" +
				"Packets Dropped: "+Packet.droppededcount+"\n" +
				"Service packet count: "+RoutingPacket.count+"\n"+
				"Transmits count: "+Link.packetcount+"\n" +
				"Service transmits count: "+Link.routingpacketcount+"\n"+
				"Need routes: "+AODV.NeedRoutesCount+"\n" +
				"Found routes: "+AODV.FoundRoutesCount+"\n" +
				
				"Average Packets/Distance: \n"+
				"Average MovingSpeed/Distance: \n"+
				"Average Packet Transmit Latency: \n";
		
		mxCell parent = (mxCell) GraphEditor.graph.getDefaultParent();
		for(Object V:GraphEditor.graph.getChildVertices(parent)){
			Router R = (Router) ((mxCell)V).getValue();
			if (R==null) return S;
			if(!R.routing_protocol.WaitingRoute.isEmpty()){
				S+=R.routing_protocol.WaitingRoute+"\n";
			}
		}
	
		return S;
	}
	
	public void addPacketsFromDistance(int PacketsCount,int Distance){
		
	}
	public void drawPacketsFromDistance(Canvas canvas){
		
	}
	public void addMovingSpeedFromPackets(int PacketsCount,int MovingSpeed){
		
	}
	
	public void drawMovingSpeedFromPackets(Canvas canvas){
		
	}
	public void addAverageLatency(int Latency, int Time){
		
	}			
	public void drawAverageLatency(Canvas canvas){
		
	}
}
