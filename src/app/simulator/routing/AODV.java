package app.simulator.routing;



import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import app.gui.GraphEditor;
import app.simulator.Link;
import app.simulator.Simulator;
import app.simulator.packets.*;
import app.simulator.restrictions.Restrictions;
import app.simulator.routing.RoutigProtocol;


public class AODV extends RoutigProtocol implements Serializable {
	public static int NeedRoutesCount=0;
	public static int FoundRoutesCount=0;
	
	AODV(Router parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	AODV() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	//int AODVRREQcounter=0;
	
	Map<Router,Integer> AODVRREQcounter = new HashMap<Router,Integer>();
	
	
	public int getNewNumber(Router router){
		if(AODVRREQcounter.containsKey(router)){
			int n=AODVRREQcounter.remove(router)+1;
			AODVRREQcounter.put(router, n);
			return n;
			
		}else{
			AODVRREQcounter.put(router, 1);
			return 1;
		}
	}
	
	synchronized public void findRoute(final Router router1, final Router router2){
		synchronized(GraphEditor.graph){
			NeedRoutesCount++;
		}
			broadcast(new AODVRREQ(router1, router2,getNewNumber(router2)),null);
			new Thread(){
				@Override
				public void run(){
					for(int i=0;i<Restrictions.MaxRouteFindAttempts;i++){
						try {
							Thread.sleep(Restrictions.RouteFindPeriod*Restrictions.Scale);
							if(router1.routing_protocol.RoutingTable.containsKey(router2))return;
							if(!Simulator.isPaused)broadcast(new AODVRREQ(router1, router2,getNewNumber(router2)),null);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}.start();

	}
	
	Map<Router,Integer> CheckOld= new HashMap<Router,Integer>();
	public boolean checkOld(AODVPacket packet){
		
		return true;
	}
	
	
	synchronized public void receive(RoutingPacket packet, Link link){
		//TODO analysis of packet
		if(packet instanceof AODVRREQ){
			//System.out.println(""+Router+" "+RoutingTable);
			Router.send(new AODVRREP(packet.getReceiver(), packet.getSender()),null);
		}
		if(packet instanceof AODVRREP){
			//if (!RoutingTable.containsKey(packet.getSender()))
				
			//System.out.println(Router);
			//route(new AODVRREP(packet.getReceiver(), packet.getSender()));
		}
		if(packet instanceof AODVERR){
			RoutingTable.remove(((AODVERR)packet).getDestination());
			findRoute(Router,((AODVERR)packet).getDestination());
			//System.out.println(Router);
			//route(new AODVRREP(packet.getReceiver(), packet.getSender()));
		}
		
		
		while(WaitingRoute.containsKey(packet.getSender())){
			//if(packet instanceof AODVRREP)
			synchronized(GraphEditor.graph){
				FoundRoutesCount++;
			}
			for(Packet iPacket:WaitingRoute.get(packet.getSender())){
				route(iPacket);
			}
			WaitingRoute.remove(packet.getSender());
		}
	}
	synchronized public boolean proceed(RoutingPacket packet, Link link){
		if((packet instanceof AODVRREQ)&&((AODVRREQ)packet).getD()&&RoutingTable.containsKey(packet.getReceiver())){
			if(!checkHorizont((AODVRREQ)packet))return false;
			AODVRREP p = new AODVRREP(packet.getReceiver(), packet.getSender(),RoutingTable.get(packet.getReceiver()).getMetric());
			//p.setHops(Hops,RoutingTable.get(packet.getReceiver()).getMetric());
			Router.send(p,null);
			//System.out.println(new AODVRREP(packet.getReceiver(), packet.getSender(),RoutingTable.get(packet.getReceiver()).getMetric()));
			packet.finaliz();
			return false;
		}
		return true;
	}
	synchronized public void addRoute(Router router,Link link,int metric, int router_num){
		//super.addRoute(router, link, metric);
		//return;
		
		if(!RoutingTable.containsKey(router)){
			RoutingTable.put(router, new AODVRoute(link,metric,router_num));

		}else if((((AODVRoute)RoutingTable.get(router)).routernum<router_num)||(RoutingTable.get(router).getMetric()>=metric)){
			if((((AODVRoute)RoutingTable.get(router)).routernum<router_num))System.out.println("RT:"+(((AODVRoute)RoutingTable.get(router)).routernum+"NEW:"+router_num));
			RoutingTable.remove(router);
			RoutingTable.put(router, new AODVRoute(link,metric,router_num));
		
			while(WaitingRoute.containsKey(router)){
				//if(packet instanceof AODVRREP)
				System.out.println("Hi");
				synchronized(GraphEditor.graph){
					FoundRoutesCount++;
				}
				for(Packet iPacket:WaitingRoute.get(router)){
					route(iPacket);
				}
				WaitingRoute.remove(router);
			}
			//System.out.println(""+((AODVRoute)RoutingTable.get(router)).routernum+" "+router_num+"("+router.router_num+")");
		}
	}
	
	
synchronized public void route(Packet packet){	
		
		if (!RoutingTable.containsKey(packet.getReceiver())&&(packet.getSender()!=Router)){
			Router.send(new AODVERR(Router, packet.getSender(),packet.getReceiver()),null);
			//packet.drop();
		}else{
			super.route(packet);
		}
		
	}
	private static final long serialVersionUID = 2479235735161227119L;
}
