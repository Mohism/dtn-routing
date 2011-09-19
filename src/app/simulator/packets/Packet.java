package app.simulator.packets;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import com.mxgraph.model.mxCell;

import app.gui.GraphEditor;
import app.simulator.restrictions.Restrictions;
import app.simulator.routing.AODV;
import app.simulator.routing.Router;



public class Packet implements Serializable{
	public static int count=0;
	public static int activecount=0;
	public static int finalizedcount=0;
	public static int droppededcount=0;
	public int tree=1;
	Router From;
	Router To;
	public static final int BaseTTL=Restrictions.maxPathLegth;
	int TTL=BaseTTL;
	int Hops=0;
	
	public mxCell PacketCell;
	
	public Router getSender(){return From;}
	public Router getReceiver(){return To;}
	public void setTTL(int TTL){this.TTL=TTL;}
	public int getTTL(){return TTL;}
	public void setHops(int Hops){this.Hops=Hops;}
	public int getHops(){return Hops;}
	public void decTTL(){TTL--;Hops++;}
	public void drop() {
		if (!(this instanceof RoutingPacket)){
			synchronized(GraphEditor.graph){
				activecount--;
				droppededcount++;
				System.out.println("Dropped!"+this);
			}
		}
		
		
		
		
		
	};
	public void finaliz() {
		//TODO Remove
		if(PacketCell!=null){
			PacketCell.removeFromParent();
			//GraphEditor.graph.refresh();
		}
		if (!(this instanceof RoutingPacket)){
			synchronized(GraphEditor.graph){
				activecount--;
				finalizedcount++;
			}
		}
		
		//System.out.println("Finalized!"+this);
		
	};
	
	public Packet(){
		if (!(this instanceof RoutingPacket)){
			synchronized(GraphEditor.graph){
				count++;
				activecount++;
			}
		}
		
	}
	public Packet(Router Router1,Router Router2){
		this.From=Router1;
		this.To=Router2;
		if (!(this instanceof RoutingPacket)){
			synchronized(GraphEditor.graph){
				count++;
				activecount++;
			}
		}
	}
	
	public Packet clone(){
		Packet p=null;
		try {
			p = this.getClass().newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class cl=this.getClass();
		while(cl!=Object.class){
		for(Field F:cl.getDeclaredFields()){
			try {
				if((F.getModifiers()&Modifier.FINAL)!=0)continue;
				if((F.getModifiers()&Modifier.FINAL)!=0)continue;
				//System.out.println(F);
				F.set(p, F.get(this));
					} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		cl=cl.getSuperclass();
		}
		return p;
	}

	public String toString(){return this.getClass().getSimpleName()+"("+From+"-->"+To+")TTL="+TTL;}
}
