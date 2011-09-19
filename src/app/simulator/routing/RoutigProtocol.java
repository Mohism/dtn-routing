package app.simulator.routing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import app.simulator.Link;
import app.simulator.packets.AODVPacket;
import app.simulator.packets.BroadCastPacket;
import app.simulator.packets.Packet;
import app.simulator.packets.RoutingPacket;

abstract public class RoutigProtocol implements Serializable{
	
	public Map<Router,Route> RoutingTable = new HashMap<Router,Route>();
	public Map<Router,List<Packet>> WaitingRoute = new HashMap<Router,List<Packet>>();
	//TODO Need Multimap with Metrics and Flags
	

	synchronized public void addRoute(Router router,Link link,int metric){
		if(!RoutingTable.containsKey(router)){
			RoutingTable.put(router, new Route(link,metric));
			

		}else if(RoutingTable.get(router).getMetric()>=metric){
			RoutingTable.remove(router);
			RoutingTable.put(router, new Route(link,metric));
		}
		
	}
	synchronized public void removeRoute(Link link){
		
		Router router=null;
		do{
			router=null;
			for(Router iRouter:RoutingTable.keySet()){
				RoutingTable.get(iRouter).getLink();
				if(RoutingTable.get(iRouter).getLink()==link){
					router=iRouter;
					break;
				}
			}
			if(router!=null){
			
			
				//System.out.print(RoutingTable);
				//System.out.print("OK");
				RoutingTable.remove(router);
				//System.out.println(RoutingTable);
			}
		}while(router!=null);
	}
	synchronized public void removeRoute(Router router){RoutingTable.remove(router);}
	public Router Router;
	RoutigProtocol(Router parent){Router = parent;}
	
	public RoutigProtocol() {
		// TODO Auto-generated constructor stub
	}
	Map<Router,Map<Router,Integer>> CheckBuffer= new HashMap<Router,Map<Router,Integer>>();
	
	public boolean checkOld(AODVPacket packet){
		return true;
	}
	
	public boolean checkHorizont(BroadCastPacket packet){
		AODVPacket aodvpacket = (AODVPacket)packet; 
		if(!checkOld(aodvpacket))return false;
		if(!CheckBuffer.containsKey(aodvpacket.getSender())){
			Map<Router,Integer> receivers =  new HashMap<Router,Integer>();
			CheckBuffer.put(aodvpacket.getSender(), receivers);
			receivers.put(aodvpacket.getReceiver(), aodvpacket.getID());
			return true;			
		}else if (!CheckBuffer.get(aodvpacket.getSender()).containsKey(aodvpacket.getReceiver())) {
			CheckBuffer.get(aodvpacket.getSender()).put(aodvpacket.getReceiver(), aodvpacket.getID());
			return true;
		}else if(CheckBuffer.get(aodvpacket.getSender()).get(aodvpacket.getReceiver())<aodvpacket.getID()){
			CheckBuffer.get(aodvpacket.getSender()).remove(aodvpacket.getReceiver());
			CheckBuffer.get(aodvpacket.getSender()).put(aodvpacket.getReceiver(), aodvpacket.getID());
			return true;//T
		}else if(aodvpacket.getID()==0){
			System.out.println("MaxInteger");//TODO
			return false;
		}else{
			return false;
		}
	}
	
	abstract public void receive(RoutingPacket packet, Link link);
	abstract public boolean proceed(RoutingPacket packet, Link link);
	abstract public void findRoute(Router router1, Router router2);
	public void setRouter(Router r){this.Router=r;}
	
	synchronized public void broadcast(BroadCastPacket packet, Link link){
		if (!checkHorizont(packet)){
			//((Packet)packet).finaliz();
			return;
		}
		
		//((Packet)packet).decTTL();
		//if (((Packet)packet).getTTL()<=0){
				//((Packet)packet).drop();
				//return;
			//}
		
		for(Link iLink:Router.LinkTable.values()){
			if(iLink!=link){
				//Packet.count++;
				//Packet.activecount++;
				//System.out.println((Packet)packet);
				//System.out.println(((Packet)packet).clone());
				iLink.transmit(((Packet)packet).clone(),Router);//TODO Warning TypeCast		
			}
		}
		
	}
	synchronized public void route(Packet packet){	
		
		if (!RoutingTable.containsKey(packet.getReceiver())){
			
			if(!WaitingRoute.containsKey(packet.getReceiver())){
				findRoute(Router,packet.getReceiver());
				WaitingRoute.put(packet.getReceiver(), new LinkedList<Packet>());
			};
			WaitingRoute.get(packet.getReceiver()).add(packet);
			//packet.drop();//TODO remove string
			return;
		}
		RoutingTable.get(packet.getReceiver()).getLink().transmit(packet,Router);	
		//System.out.println(RoutingTable);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
