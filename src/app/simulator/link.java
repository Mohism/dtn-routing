package app.simulator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxCellState;

import app.gui.GraphEditor;
import app.simulator.packets.Packet;
import app.simulator.packets.RoutingPacket;
import app.simulator.restrictions.Restrictions;
import app.simulator.routing.Router;

public class Link implements Serializable{
	private static final long serialVersionUID = 1L;
	public mxCell edge;
	public static int packetcount=0;
	public static int routingpacketcount=0;
	List<Packet> PacketQueue = new LinkedList<Packet>();
	boolean removed= false;
	boolean Active = false;
	boolean Orto = false;
	Link left;
	Link right;
	Link top;
	
	
	public enum LinkState implements Serializable {
	    OFF, SR,S ,R, FREE
	}
	BlockingQueue<Packet> SendingQueue = new LinkedBlockingQueue<Packet>();
	BlockingQueue<Packet> ReceivingQueue = new LinkedBlockingQueue<Packet>();
	BlockingQueue<Packet> DataQueue = new LinkedBlockingQueue<Packet>();
	
	Packet p1,p2;
	
	static boolean GraphSynchro=false;
	Map<String,Object>stylepassive;
	Map<String,Object>stylered;
	Map<String,Object>styleblack;
	Map<String,Object>styleblue;
	
	public double getAngle(Router r){
		if(edge.getSource()==null){
			return 0;
		}
		if((Router)edge.getSource().getValue()==r){
			return GraphEditor.getAngle((mxCell)edge.getSource(), (mxCell)edge.getTarget());
		}else{
			return GraphEditor.getAngle((mxCell)edge.getTarget(), (mxCell)edge.getSource());
		}
	}
	

	private void ThreadTiming(Packet p) throws InterruptedException{
		
		//new HashMap<String,Object>();
		//System.out.print("*");
		
		if(GraphEditor.graph.getView()==null)System.out.println("f..ck!");
		if(edge==null)System.out.println("f..ck!");
		mxCellState edgestate=GraphEditor.graph.getView().getState(edge);
		if(GraphEditor.graph.getView().getState(edge)!=null){
		if(p instanceof RoutingPacket){
			//edgestate.setStyle(stylered);
		}else{
			//edgestate.setStyle(styleblue);
		}
		}
		while(Simulator.isPaused){
			Thread.sleep(200);
		}
		
		//System.out.println();
		boolean isUpdater=false;
		synchronized(GraphEditor.graph){
			if(GraphSynchro==false){
				GraphSynchro=true;
				isUpdater=true;
				
			}
		}	
		
		if(p instanceof RoutingPacket){};
			Thread.sleep(Restrictions.minPacketTransmitTime*Restrictions.Scale);
		synchronized(GraphEditor.graph){
			if(isUpdater){
				//GraphEditor.graph.refresh();
				//GraphEditor.graph.getView().
				GraphSynchro=false;
				isUpdater=false;
				GraphEditor.graph.refresh();
			}
		}
		
		
		
		synchronized(State){
			if(State==LinkState.SR)State=LinkState.R;
			if(State==LinkState.S)State=LinkState.FREE;
		}
		
		if(GraphEditor.graph.getView()==null)System.out.println("f..ck!");
		//if(styleblack==null)System.out.println("fuck!");
		
		edgestate=GraphEditor.graph.getView().getState(edge);
		if(edgestate!=null){
			if(SendingQueue.isEmpty()&&ReceivingQueue.isEmpty()){
				//edgestate.setStyle(styleblack);
			}
		}
	}
	
	
	
	Thread SendingThread = new Thread(new Runnable(){
		public void run(){
			while(!removed){
				try {
					
					p1 = SendingQueue.take();
					if (p1==null) continue;
					ThreadTiming(p1);
					Router1.receive(p1,Link.this);
					p1=null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
	Thread ReceivingThread = new Thread(new Runnable(){
		public void run(){
			while(!removed){
				try {
					p2 = ReceivingQueue.take();
					if (p2==null) continue;
					ThreadTiming(p2);
					Router2.receive(p2,Link.this);
					p2=null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
	
	Router Router1;
	Router Router2;
	LinkState State;

	public Router getLinkedRouter(Router Router){
		if(Router==Router1)return Router2;
		if(Router==Router2)return Router1;
		return null;
	}
	
	public Link(Router Router1,Router Router2,mxCell edge){
		this.Router1=Router1;
		this.Router2=Router2;
		Router1.registerLink(this,Router2);
		Router2.registerLink(this,Router1);
		State=LinkState.FREE;
		
		this.edge=edge;
		Map<String,Object> style = GraphEditor.graph.getCellStyle(edge);
		style.put("strokeWidth", 1);
		//System.out.println(style);
		stylered= new HashMap<String,Object>(style);
		styleblack= new HashMap<String,Object>(style);
		styleblue= new HashMap<String,Object>(style);
			
		
			stylered.put("strokeColor", "red");
			styleblue.put("strokeColor", "blue");
			styleblack.put("strokeColor", "black");  
			
			//free.setStyle(styleblack);
			//busyR.setStyle(stylered);
			//busyP.setStyle(styleblue);
			
			
			mxCellState edgestate=GraphEditor.graph.getView().getState(edge);
			if(edgestate!=null){
				if(SendingQueue.isEmpty()&&ReceivingQueue.isEmpty()){
					edgestate.setStyle(styleblack);
				}
			}
			
		SendingThread.start();
		ReceivingThread.start();
		
		
		
		
		
	}
	
	public void setActive(){
		stylered.put("strokeWidth", 3);
		Active = true;
	}
	
	public void setUnActive(){
		stylered.put("strokeWidth", 1);
		Active = false;
	}
	public boolean isActive(){return Active;};
	
	public void removeLink(){
		Router1.unregisterLink(Router2);
		Router2.unregisterLink(Router1);
		for(Packet p:SendingQueue){
			p.drop();
		}
		for(Packet p:ReceivingQueue){
			p.drop();
		}
		SendingQueue.clear();
		ReceivingQueue.clear();
		//System.out.println("Remove");
		//System.out.println(Router2.routing_protocol.RoutingTable);
		//System.out.println(Router1.routing_protocol.RoutingTable);
		removed=true;
	}
	
	
	public void transmit(Packet packet,Router From){
		/*synchronized(State){
			if(State==LinkState.FREE)State=LinkState.S;
			if(State==LinkState.R)State=LinkState.SR;
		}
		try {
			DataQueue.put(packet);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		*/
		if(removed){
			System.out.println("link!");
			packet.drop();
			return;
		}
		if (packet instanceof RoutingPacket){
			synchronized(GraphEditor.graph){
				routingpacketcount++;
			}
		}else{
			synchronized(GraphEditor.graph){
				packetcount++;
			}
		}
		//System.out.println("Link:"+this.Router1+"<-->"+this.Router2+" Packet:"+packet);
		if(From==Router1)receive(packet);else
		if(From==Router2)send(packet);
		
	}
	
	private void send(Packet packet){
	//TODO	
		try {
			synchronized(State){
				if(State==LinkState.FREE)State=LinkState.S;
				if(State==LinkState.R)State=LinkState.SR;
			}
			
			SendingQueue.put(packet);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Router1.receive(packet,this);
	}
	private void receive(Packet packet){
	//TODO
		
		synchronized(State){
			if(State==LinkState.FREE)State=LinkState.R;
			if(State==LinkState.S)State=LinkState.SR;
		}
		
		try {

			ReceivingQueue.put(packet);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Router2.receive(packet,this);
	}
	public String toString(){
		String S="";//this.Router1+"<-->"+this.Router2;
		
		if(p1!=null)S+="-->:"+p1+"\n";
		//if(!SendingQueue.isEmpty())S+=SendingQueue+"\n";
		if(p2!=null)S+="<--:"+p2+"\n";
		//if(!ReceivingQueue.isEmpty())S+=ReceivingQueue+"\n";
		return S;
	}
}
