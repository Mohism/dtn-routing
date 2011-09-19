package app.simulator.routing;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import app.simulator.Link;
import app.simulator.packets.AODVPacket;
import app.simulator.packets.BroadCastPacket;
import app.simulator.packets.Packet;
import app.simulator.packets.RoutingPacket;
import app.simulator.restrictions.Restrictions;

public class Router extends Peer implements Serializable {
	public int a;
	public int router_num=1;
	public static int last_num=0;
	public Router(){
		//HELLO
		last_num++;
		a=last_num;
		try {
			routing_protocol=(RoutigProtocol) Restrictions.Algorythm.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		routing_protocol.setRouter(this);
	}
	
	public void removeRouter(){
		//for(Link link:LinkTable.values()){
			//link.removeLink();
		//}
	}
	public Link[] Orto= new Link[4];
	public String toString(){return ""+a;}//+" "+((MAODV)this.routing_protocol).TransitTable;}
	Map<Router,Link> LinkTable = new HashMap<Router,Link>();
	private static final long serialVersionUID = 1L;
	public RoutigProtocol routing_protocol= new AODV(this);
	synchronized public void registerLink(Link link,Router target){
		LinkTable.put(target,link);
		((AODV)routing_protocol).addRoute(target, link, 0,Integer.MAX_VALUE);
		router_num++;
	}
	synchronized public void unregisterLink(Router target){		
		routing_protocol.removeRoute(LinkTable.get(target));
		//System.out.println(LinkTable);
		LinkTable.remove(target);
	}
	synchronized public void receive(Packet packet,Link link){
		if(packet instanceof AODVPacket){
			
			((AODV)routing_protocol).addRoute(packet.getSender(), link, packet.getHops(),((AODVPacket)packet).router1num);
			
		}
		if (packet.getReceiver()==this){
			//if(!(packet instanceof RoutingPacket)) send(new Packet(packet.getReceiver(),packet.getSender()),link);
			packet.finaliz();
			
			if(packet instanceof RoutingPacket){
				routing_protocol.receive((RoutingPacket)packet, link);
			}
			return;
		}else{
			if(packet instanceof RoutingPacket&&!routing_protocol.proceed((RoutingPacket)packet,link)) return;
		}
		send(packet,link);
	}
	synchronized public void send(Packet packet,Link link){
		
		packet.decTTL();
		if (packet.getTTL()<=0){
			packet.drop();
			return;
		}
		
		if(packet instanceof BroadCastPacket){
			routing_protocol.broadcast((BroadCastPacket)packet,link);
		}else{
			routing_protocol.route(packet);
		}
	}
	

}
