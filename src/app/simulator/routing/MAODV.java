package app.simulator.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.gui.GraphEditor;
import app.simulator.Link;
import app.simulator.Simulator;
import app.simulator.packets.*;

import app.simulator.packets.AODVERR;
import app.simulator.packets.AODVRREP;
import app.simulator.packets.AODVRREQ;
import app.simulator.packets.MAODVRREQ;
import app.simulator.packets.Packet;
import app.simulator.packets.RoutingPacket;
import app.simulator.restrictions.Restrictions;

public class MAODV extends AODV {
	public Map<Router,Map<Router,Transit>> TransitTable = new HashMap<Router,Map<Router,Transit>>();
	public Map<Router,Route> OldRoutes = new HashMap<Router,Route>();
	MAODV(Router parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	MAODV() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	 void addTransit(Router From, Router To, int Hops){
		 if(!TransitTable.containsKey(From)){TransitTable.put(From, new HashMap<Router,Transit>());}
		 if(!TransitTable.containsKey(To)){TransitTable.put(To, new HashMap<Router,Transit>());}
			Transit t = new Transit(From,To,Hops);
			TransitTable.get(From).put(To,t);
			TransitTable.get(To).put(From,t);
			
		 
	 }
	 void addTransit(Transit t, int Hops){
		 if(!TransitTable.containsKey(t.From)){TransitTable.put(t.From, new HashMap<Router,Transit>());}
		 if(!TransitTable.containsKey(t.To)){TransitTable.put(t.To, new HashMap<Router,Transit>());}
			t.Hops+=Hops;
			TransitTable.get(t.From).put(t.To,t);
			TransitTable.get(t.To).put(t.From,t);
			
		 
	 }
	
	synchronized public void receive(RoutingPacket packet, Link link){
		//TODO analysis of packet
		if(packet instanceof MAODVRREQ){
			//System.out.println(""+Router+" "+RoutingTable);
			Router.send(new MAODVRREP(packet.getReceiver(), packet.getSender(),0,((MAODVRREQ)packet).transit),null);
			return;
		}
		if(packet instanceof MAODVERR){
			if(RoutingTable.containsKey(((AODVERR)packet).getDestination()))return;
			
		}
		if(packet instanceof AODVRREP){
			if(packet instanceof MAODVRREP){
				Transit t=null;
				if(TransitTable.containsKey(packet.getSender())){
					if(TransitTable.containsKey(packet.getReceiver())){
						t=TransitTable.get(packet.getSender()).get(packet.getReceiver());
					}
				}
				//for(Transit i:TransitTable.get(packet.getReceiver())){
					//if(i.From==packet.getReceiver()&&i.To==packet.getSender()){	t=i; break;}
					//if(i.To==packet.getReceiver()&&i.From==packet.getSender()){	t=i; break;}
				//}
				Transit t2= ((MAODVRREP)packet).transit;
				System.out.println(t);
				if(t==null){
					addTransit(((MAODVRREP)packet).transit,packet.getHops());
				}else{			
					if((t2.Hops-t.Hops<=packet.getHops())&&(t2.Hops-t.Hops>0)){
						t2.lastttl=2;
						addTransit(((MAODVRREP)packet).transit,packet.getHops()+t2.Hops);
						//addRoute(packet)
						
					}
					System.out.println(((MAODVRREP)packet).transit);
					while(WaitingRoute.containsKey(((MAODVRREP)packet).transit.From)){
						//if(packet instanceof AODVRREP)
						System.out.println("Hi");
						synchronized(GraphEditor.graph){
							FoundRoutesCount++;
						}
						for(Packet iPacket:WaitingRoute.get(packet.getSender())){
							route(iPacket);
						}
						WaitingRoute.remove(packet.getSender());
					}
				}
				return;
				
			}else{
				//System.out.println("OOO");
				addTransit(packet.getSender(),packet.getReceiver(),packet.getHops());
			}
			
		}
		if(packet instanceof AODVERR){
			RoutingTable.remove(((AODVERR)packet).getDestination());
			findRoute(Router,((AODVERR)packet).getDestination());
			//System.out.println(Router);
			//route(new AODVRREP(packet.getReceiver(), packet.getSender()));
		}
		
		
		
		super.receive(packet, link);
	}
	
	synchronized public boolean proceed(RoutingPacket packet, Link link){
		if((packet instanceof MAODVRREQ)&&RoutingTable.containsKey(packet.getReceiver())){
			//System.out.println(TransitTable);	
			//System.out.println(TransitTable);
			Transit pt=((MAODVRREQ)packet).transit;
			Transit t=null;
			if(TransitTable.containsKey(pt.From)){
				if(TransitTable.containsKey(pt.To)){//TODO proverka perevorota
					t=TransitTable.get(pt.From).get(pt.To);
				}
			}
			if(t!=null&&t.Hops<pt.Hops&&pt.Num<=t.Num){
				
				if(!checkHorizont((AODVRREQ)packet))return false;
				System.out.println("RREP"+Router+" t="+t.Hops+" pt="+pt.Hops);
					MAODVRREP p = new MAODVRREP(packet.getReceiver(), packet.getSender(),RoutingTable.get(packet.getReceiver()).getMetric(),((MAODVRREQ)packet).transit);
					//p.setHops(Hops,RoutingTable.get(packet.getReceiver()).getMetric());
					Router.send(p,null);
					//System.out.println(new AODVRREP(packet.getReceiver(), packet.getSender(),RoutingTable.get(packet.getReceiver()).getMetric()));
					packet.finaliz();
					return false;
			}
			
			return true;
		}
		if(packet instanceof AODVRREP){
			if(packet instanceof MAODVRREP){
				addTransit(((MAODVRREP)packet).transit,packet.getHops());
				while(WaitingRoute.containsKey(((MAODVRREP)packet).transit.From)){
					//if(packet instanceof AODVRREP)
					synchronized(GraphEditor.graph){
						FoundRoutesCount++;
					}
					for(Packet iPacket:WaitingRoute.get(packet.getSender())){
						route(iPacket);
					}
					WaitingRoute.remove(packet.getSender());
				}
				//return true;
			}else{

				addTransit(packet.getSender(),packet.getReceiver(),packet.getHops());
			}
			
		}
		
		
		return super.proceed(packet, link);
	}
	
	
	
	
	
	synchronized public void route(Packet packet){	
		
		if (!RoutingTable.containsKey(packet.getReceiver())&&(packet.getSender()!=Router)){
			//check transits
			if(TransitTable.containsKey(packet.getSender())){
				//MAODVRREQ p=new AODVRREQ(this.Router, packet.getReceiver(),getNewNumber(packet.getReceiver()));	
				
				
				
			
			Transit t=null;
			if(TransitTable.containsKey(packet.getSender())){
				if(TransitTable.containsKey(packet.getReceiver())){
					t=TransitTable.get(packet.getSender()).get(packet.getReceiver());
				}
			}
			if(t==null){
				super.route(packet);
				return;
			}
			final Transit tt=t;
			
			final MAODVRREQ p= new MAODVRREQ(this.Router, packet.getReceiver(),getNewNumber(packet.getReceiver()),t);
			p.setD(true);
			p.setTTL(2);
			/*new Thread(){
				@Override
				public void run(){
					int ttl=2;
					for(int i=0;i<Restrictions.MaxRouteFindAttempts;i++){
						try {
							int findperiod = Restrictions.minPacketTransmitTime*ttl;
							//Thread.sleep(findperiod*Restrictions.Scale);
							if(MAODV.this.Router.routing_protocol.RoutingTable.containsKey(tt.To))return;
							MAODVRREQ rreq=new MAODVRREQ(MAODV.this.Router, tt.To,getNewNumber(tt.To), tt);
							ttl*=2;
							rreq.setTTL(ttl);
							if(!Simulator.isPaused)broadcast(rreq,null);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					Router.send(new AODVERR(Router, tt.From,tt.To),null);
				}
				
			}.start();*/
			
			
				if(!WaitingRoute.containsKey(packet.getReceiver())){
					
					Router.send(p,null);
					WaitingRoute.put(packet.getReceiver(), new LinkedList<Packet>());
				}else{
					tt.lastttl*=2;
					p.setTTL(tt.lastttl);
					
					if(p.getTTL()>8){
						Router.send(new AODVERR(Router,tt.To,tt.From),null);
					}
					else{
						Router.send(p,null);
					}
						
				}
				WaitingRoute.get(packet.getReceiver()).add(packet);
				return;
			}
		}else{
			super.route(packet);
		}
		
	}
	private static final long serialVersionUID = 2479235735161227119L;

}
