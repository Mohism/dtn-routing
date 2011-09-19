package app.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.Random;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mxgraph.model.mxCell;

import app.simulator.Simulator;
import app.simulator.packets.Packet;
import app.simulator.restrictions.Restrictions;
import app.simulator.routing.AODV;
import app.simulator.routing.MAODV;
import app.simulator.routing.Router;

public class SettingsPanel extends JPanel {
	
	
	public void add2(Component field, String Name){
		JLabel label = new JLabel(Name);
		add(label);
		add(field);
		
	}
	JTextField Algorythm =new JTextField("MAODV");
	JTextField Topology =new JTextField("Regular Mesh");
	JTextField Standart =new JTextField("802.15.4");
	JTextField Consistence   = new JTextField("0.9");	
	JTextField Fluctuations   = new JTextField("0.5");
	JTextField Nodes =new JTextField("50");
	JTextField SendersCount = new JTextField("1");
	JTextField PacketsFreq = new JTextField("1");
	//JTextField SendingFreq = new JTextField("1");
	JTextField MinSendingDistance = new JTextField("3");
	JTextField MaxSendingDistance = new JTextField("30");
	
	//JTextField PacketsFreq = new JTextField("0");
	JTextField MovingNodes= new JTextField("0");
	JTextField MovingSpeedPercent = new JTextField("0.5");
	//JTextField MaxMovingDistancePercent= new JTextField("1.0");
	JTextField MaxNodeConnections= new JTextField("6");
	JTextField TimeScale= new JTextField("5");
	JTextField From= new JTextField("1");
	JTextField To= new JTextField("29");
	public SettingsPanel(){
		super();
		this.setLayout(new GridLayout(this.getClass().getDeclaredFields().length+1,2));
		for(Field f:this.getClass().getDeclaredFields()){
			try {
				this.add2((Component)(f.get(this)),f.getName());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		final JButton StartPause = new JButton("Start");
		final JButton Stop= new JButton("Stop");
		
		
		TimeScale.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Restrictions.Scale=Integer.parseInt(TimeScale.getText());
				
			}
			
		});
		
		add(StartPause);
		add(Stop);
		final ActionListener StartPauseListener = new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(StartPause.getText().matches("Start")){
					StartPause.setText("Pause");
					Simulator.isPaused=false;
					Restrictions.Scale=Integer.parseInt(TimeScale.getText());
					Restrictions.maxLinks=Integer.parseInt(MaxNodeConnections.getText());
					if(Standart.getText().matches("802.11"))Restrictions.isWiFi=true;else Restrictions.isWiFi=false;
					if(Algorythm.getText().matches("MAODV"))Restrictions.Algorythm=MAODV.class;else Restrictions.Algorythm=AODV.class;
					final Random random = new Random();
					
					//GraphEditor.graph.insertVertex(GraphEditor.graph.getDefaultParent(), "random", null, 100, 100, 25, 25, "ellipse");
					final int maxCoord=(int)(Double.parseDouble(Consistence.getText())*Math.sqrt(Integer.parseInt(Nodes.getText()))*Restrictions.defaultradius);
					
					
					//Generate Nodes
					if(Topology.getText().matches("Random Mesh")){
						for(int i=0;i<Integer.parseInt(Nodes.getText());i++){
							int x = Math.abs(random.nextInt())%maxCoord;
							int y = Math.abs(random.nextInt())%maxCoord;
							GraphEditor.graph.insertVertex(GraphEditor.graph.getDefaultParent(), "random point", null, x, y, 25, 25, "ellipse");
						}
					}
					
					final int CoordStep=(int) (Restrictions.defaultradius*Double.parseDouble(Consistence.getText()));
					if(Topology.getText().matches("Random Regular Mesh")){
						for(int x=(int) (Restrictions.defaultradius*Double.parseDouble(Consistence.getText()));x<maxCoord;x+=Restrictions.defaultradius*Double.parseDouble(Consistence.getText())){
							for(int y=(int) (Restrictions.defaultradius*Double.parseDouble(Consistence.getText()));y<maxCoord;y+=Restrictions.defaultradius*Double.parseDouble(Consistence.getText())){
								int dx = (int) (random.nextGaussian()*Restrictions.defaultradius*Double.parseDouble(Consistence.getText())*Double.parseDouble(Fluctuations.getText()));
								int dy = (int) (random.nextGaussian()*Restrictions.defaultradius*Double.parseDouble(Consistence.getText())*Double.parseDouble(Fluctuations.getText()));
								//System.out.println(""+x+","+y);
								GraphEditor.graph.insertVertex(GraphEditor.graph.getDefaultParent(), "random point", null, x+dx, y+dy, 25, 25, "ellipse");
								
							}
						}
					}
					
					if(Topology.getText().matches("Regular Mesh")){
						for(int x=(int) (Restrictions.defaultradius*Double.parseDouble(Consistence.getText()));x<maxCoord;x+=Restrictions.defaultradius*Double.parseDouble(Consistence.getText())){
							for(int y=(int) (Restrictions.defaultradius*Double.parseDouble(Consistence.getText()));y<maxCoord;y+=Restrictions.defaultradius*Double.parseDouble(Consistence.getText())){
								
								//System.out.println(""+x+","+y);
								GraphEditor.graph.insertVertex(GraphEditor.graph.getDefaultParent(), "random point", null, x, y, 25, 25, "ellipse");
								
							}
						}
					}
					mxCell v = (mxCell) GraphEditor.graph.getChildVertices(GraphEditor.graph.getDefaultParent())[0];
					GraphEditor.linksUpd(v);
					GraphEditor.graph.refresh();
					
					
					
					
					Object[] VerticesObj = GraphEditor.graph.getChildVertices(GraphEditor.graph.getDefaultParent());
					//Generate Packets
					int iMax = (int) (Integer.parseInt(SendersCount.getText()));
					if( iMax == 1){
						int numSender = Integer.parseInt(From.getText())-1;
						int numReceiver = Integer.parseInt(To.getText())-1;
						Router Sender = (Router) ((mxCell)VerticesObj[numSender]).getValue();
						Router Receiver = (Router) ((mxCell)VerticesObj[numReceiver]).getValue();
						new PacketThread(Sender,Receiver,(int) (Restrictions.Scale*1000/Double.parseDouble(PacketsFreq.getText())));
					}else{
						int i=0;
						while(i++<iMax){
						
							int numSender = Math.abs(random.nextInt())%VerticesObj.length;
							int numReceiver = Math.abs(random.nextInt())%VerticesObj.length;
							if(numSender==numReceiver){
								i--;
								continue;
							}
						
						//TODO Sending Distance
							Router Sender = (Router) ((mxCell)VerticesObj[numSender]).getValue();
							Router Receiver = (Router) ((mxCell)VerticesObj[numReceiver]).getValue();
						//if(PacketsFreq.getText().matches("0")){
							Sender.send(new Packet(Sender, Receiver), null);
						//}else{
							//new PacketThread(Sender,Receiver,(int) (Restrictions.Scale*1000/Double.parseDouble(PacketsFreq.getText())));
						//}
						
						
						}
					}
					//if(true) return;
					
					//Move Nodes
					for(int i=0;i<Integer.parseInt(MovingNodes.getText());i++){
						final int num = Math.abs(random.nextInt())%VerticesObj.length;
						final mxCell cell = (mxCell)VerticesObj[num];
						final int sleeptime=(int) (1000*Restrictions.Scale/Double.parseDouble(MovingSpeedPercent.getText()));
						//final int MaxStep = (int) (Restrictions.defaultradius*Double.parseDouble(MovingSpeedPercent.getText()));
						
						new Thread(){
							public void run(){
							while (cell!=null&&cell.getParent()==GraphEditor.graph.getDefaultParent()){
								//GraphEditor.graph.refresh();
								if(Simulator.isPaused){
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									continue;
								}
								
								int oldx = (int) cell.getGeometry().getCenterX();
								int oldy = (int) cell.getGeometry().getCenterY();
								int x =Math.abs(random.nextInt())% (maxCoord);
								int y = Math.abs(random.nextInt())% (maxCoord);
								int dx =(x-oldx)/Restrictions.SoftMoving;
								int dy = (y-oldy)/Restrictions.SoftMoving;
								
								
								
								for(int i = 0;i<Restrictions.SoftMoving;i++){
									
									//System.out.println("moving"+cell+"  "+dx+" "+dy);
									synchronized(cell){
									cell.getGeometry().setX(cell.getGeometry().getCenterX()+dx);
									cell.getGeometry().setY(cell.getGeometry().getCenterY()+dy);
									//GraphEditor.linksUpd(cell);
									}
									
									try {
										Thread.sleep(sleeptime/Restrictions.SoftMoving);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									}
								}
							
							}
						}.start();
						
					}
				
								
					
					
					
					
				}else if(StartPause.getText().matches("Pause")){
					StartPause.setText("Resume");
					//isStart=true;
					Simulator.isPaused=true;
				}else if(StartPause.getText().matches("Resume")){
					StartPause.setText("Pause");
					Simulator.isPaused=false;
				}
				// TODO Auto-generated method stub
				
			}
			
		};
		
		StartPause.addActionListener(StartPauseListener);
		Stop.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//System.out.println(GraphEditor.graph.getChildCells(GraphEditor.graph.getDefaultParent()));
				//GraphEditor.graph.removeCells(GraphEditor.graph.getChildCells(GraphEditor.graph.getDefaultParent()), true);
				//GraphEditor.graph.removeCellsFromParent(GraphEditor.graph.getChildCells(GraphEditor.graph.getDefaultParent()));
				mxCell parent = (mxCell) GraphEditor.graph.getDefaultParent();
				for(Object CellObj:GraphEditor.graph.getChildCells(parent)){
					((mxCell)CellObj).removeFromParent();
				}
				Router.last_num=0;
				Packet.count=0;
				Packet.finalizedcount=0;
				Packet.droppededcount=0;
				Packet.activecount=0;
				AODV.FoundRoutesCount=0;
				AODV.NeedRoutesCount=0;
				if(StartPause.getText().matches("Pause")){
					StartPauseListener.actionPerformed(null);
				}
				StartPause.setText("Start");
				//Object[] ChildsObj = GraphEditor.graph.getChildCells(GraphEditor.graph.getDefaultParent());
				
				//System.out.println("Objects="+(mxCell)ChildsObj[0]);
				GraphEditor.graph.refresh();
			}
			
		});
		
			
	}

}

class PacketThread{
	
	public PacketThread(final Router Sender, final Router Receiver,final int sleeptime){
		new Thread(){
			public void run(){
				while(true){
					if(!Simulator.isPaused)Sender.send(new Packet(Sender, Receiver), null);	
					try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			}
			
		}.start();
	}
}
