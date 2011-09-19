package app.gui;
/**
 * $Id: GraphEditor.java,v 1.5 2010-08-10 11:43:36 david Exp $
 * Copyright (c) 2006-2010, Gaudenz Alder, David Benson
 */



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.w3c.dom.Document;

import app.simulator.Link;
import app.simulator.Simulator;
import app.simulator.Link.LinkState;
import app.simulator.packets.Packet;
import app.simulator.restrictions.Restrictions;
import app.simulator.restrictions.Statistics;
import app.simulator.routing.Router;

import com.mxgraph.examples.swing.editor.BasicGraphEditor;
import com.mxgraph.examples.swing.editor.EditorMenuBar;
import com.mxgraph.examples.swing.editor.EditorPalette;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class GraphEditor extends BasicGraphEditor
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4601740824088314699L;

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Holds the URL for the icon to be used as a handle for creating new
	 * connections. This is currently unused.
	 */
	public static URL url = null;

	//GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/connector.gif");

	public GraphEditor()
	{
		this("mxGraph Editor", new CustomGraphComponent(new CustomGraph()));
	}

		
	public static  mxGraph graph;
	
	public static synchronized double getAngle(mxCell Center,mxCell Candidate){
		//Point2D Nord = new Point((int)Center.getGeometry().getX(),(int) Candidate.getGeometry().getY());
		//double b =  Center.getGeometry().getPoint().distance(Nord);
		//double c = Center.getGeometry().getPoint().distance((Point2D)Candidate.getGeometry().getPoint());
		//double a =  Candidate.getGeometry().getPoint().distance(Nord);
		
		double R=Center.getGeometry().getPoint().distance((Point2D)Candidate.getGeometry().getPoint());
		double sin=(Candidate.getGeometry().getCenterX()-Center.getGeometry().getCenterX())/R;
		double angle = Math.asin(sin);
		//if(!left){angle+=Math.PI/2; }
		//angle-=Math.PI;
		return angle;
	}
	
	static synchronized boolean isAngle(double Angle,mxCell Center,mxCell First, mxCell Candidate){
		if(Center==First)return false;
		
		
		double b =  Center.getGeometry().getPoint().distance(First.getGeometry().getPoint());
		double c = Center.getGeometry().getPoint().distance((Point2D)Candidate.getGeometry().getPoint());
		double a =  Candidate.getGeometry().getPoint().distance(First.getGeometry().getPoint());
		double cos=(b*b+c*c-a*a)/(2*b*c);
		
		double vec = (First.getGeometry().getX()-Center.getGeometry().getX())*(Candidate.getGeometry().getY()-Center.getGeometry().getY())- (First.getGeometry().getY()-Center.getGeometry().getY())*(Candidate.getGeometry().getX()-Center.getGeometry().getX());
		
		boolean left=vec>0;
		
		double angle = Math.acos(cos);
		if(left){angle+=Math.PI; }
		//System.out.println(left);
		
		
		if(angle>Angle&&angle<Angle+Math.PI/2){
			//graph.insertEdge(graph.getDefaultParent(), "Edge", null, cell1, cell2);
			return true;
		}else if(angle+2*Math.PI>Angle&&angle+2*Math.PI<Angle+Math.PI/2){
			return true;
		}else return false;
	}
	
	static boolean isOrto1(mxCell Center,mxCell First, mxCell Candidate){
		return isAngle(Math.PI/4,Center,First,Candidate);
	}
	static boolean isOrto2(mxCell Center,mxCell First, mxCell Candidate){
		return isAngle(3*Math.PI/4,Center,First,Candidate);
	}
	static boolean isOrto3(mxCell Center,mxCell First, mxCell Candidate){
		return isAngle(6*Math.PI/4,Center,First,Candidate);
	}
	
	
	static Link add(mxCell Cell,mxCell To,int i){
		Router CellR= (Router) Cell.getValue();
		Router ToR= (Router) To.getValue();
			graph.insertEdge(graph.getDefaultParent(), "Edge", null, Cell, To);
			Link link=new Link(CellR,ToR,(mxCell)graph.getEdgesBetween(Cell, To)[0]);
			ToR.Orto[i]=link;
			if(To.getEdgeCount()>Restrictions.maxLinks){
				for(int j=0;j<To.getEdgeCount();j++){
					mxCell edge = (mxCell) To.getEdgeAt(j);
					if(edge.getValue()!=ToR.Orto[0]&&edge.getValue()!=ToR.Orto[1]&&edge.getValue()!=ToR.Orto[2]&&edge.getValue()!=ToR.Orto[3]){
						edge.removeFromParent();
						edge.removeFromTerminal(true);
						edge.removeFromTerminal(false);
						//System.out.println("delete");
					}
				}
				
			
			}
			if(Cell.getEdgeCount()>Restrictions.maxLinks){
				for(int j=0;j<Cell.getEdgeCount();j++){
					mxCell edge = (mxCell) Cell.getEdgeAt(j);
					if(edge.getValue()!=CellR.Orto[0]&&edge.getValue()!=CellR.Orto[1]&&edge.getValue()!=CellR.Orto[2]&&edge.getValue()!=CellR.Orto[3]){
						edge.removeFromParent();
						edge.removeFromTerminal(true);
						edge.removeFromTerminal(false);
						//System.out.println("delete");
					}
				}
				
			
			}
			
			return link;
			
		
	}
	
	static boolean addOrto(mxCell Cell,mxCell To){
		Router CellR= (Router) Cell.getValue();
		Router ToR= (Router) To.getValue();
		
		mxCell MinCell=null;
		if(CellR.Orto[0]!=null){
			if(CellR.Orto[0].edge.getTerminal(true)==Cell){
				MinCell=(mxCell) CellR.Orto[0].edge.getTerminal(false);
			}else{
				MinCell=(mxCell) CellR.Orto[0].edge.getTerminal(true);
			}
		}
		if(ToR.Orto[0]==null){
			add(Cell,To,0);
			return true;
		}
		
		
		for(int i=1;i<4;i++){
			if(ToR.Orto[i]==null){
				if(MinCell==null){				
					CellR.Orto[0]=add(Cell,To,i);
					
					return true;
				}
				//System.out.println(Cell+" "+MinCell+" To:"+To);
				if(isOrto1(Cell,MinCell,To)){
					CellR.Orto[1]=add(Cell,To,i);
					
					return true;
				}
				if(isOrto2(Cell,MinCell,To)){
					CellR.Orto[2]=add(Cell,To,i);
					return true;
				}
				if(isOrto3(Cell,MinCell,To)){
					CellR.Orto[3]=add(Cell,To,i);
					return true;
				}
			}
		}
		
		
		
		
		
		
		
		
		
		return false;
		
	}
	private static int addEdgesForI(int i1,int i2,mxCell cell1, List<mxCell> cells, mxCell minCell, int CellsProceed){
		

			mxCell newCell=null;
			for(mxCell cell2:cells){
			
			}
			if(newCell!=null){
				cells.remove(newCell);
				CellsProceed++;
				
			}
			return CellsProceed;
			
		
		
	}
	
	
	public static mxCell[] getSortedEphire(final Object cellObj1){
		int n=Restrictions.maxLinks;
		java.util.ArrayList<mxCell> ephire = new java.util.ArrayList<mxCell>();
		final mxCell cell1=(mxCell)cellObj1;
		if(cell1.getValue()==null){
			cell1.setValue(new Router());
		}
	
		for(Object cellObj2:(Object[])graph.getChildVertices(graph.getDefaultParent())){
		
			mxCell cell2=(mxCell)cellObj2;
			if(cell1.getGeometry().getPoint().distance(cell2.getGeometry().getPoint())<Restrictions.defaultradius){
				if(cell1!=cell2)ephire.add(cell2);
			}
		
		}
		
		//Arrays.sort(ephire., c)
		mxCell[] SortedEphire = new mxCell[ephire.size()];
		ephire.toArray(SortedEphire);
		Arrays.sort(SortedEphire, new Comparator<mxCell>(){

			@Override
			public int compare(mxCell arg0, mxCell arg1) {
				if(cell1.getGeometry().getPoint().distance(arg0.getGeometry().getPoint())
						>cell1.getGeometry().getPoint().distance(arg1.getGeometry().getPoint())){
					return 1;
				}else{
					return -1;
				}
			}
			
		});
		return SortedEphire;
	}
	
	
	synchronized public static void linksUpd(final Object cellObj1){
		int n=Restrictions.maxLinks;
		
		//removeEdges(cellObj1);
	
		
		
		
		final mxCell cell1=(mxCell)cellObj1;
		if(cell1.getValue()==null){
			cell1.setValue(new Router());
		}
		
		for(Object edgeObj:graph.getEdges(cell1)){
			mxCell edge=(mxCell)edgeObj;
			edge.removeFromParent();
			edge.removeFromTerminal(true);
			edge.removeFromTerminal(false);
			mxCell cell = edge;
			if (cell.getValue() instanceof Link){
				((Link)cell.getValue()).removeLink();
			}else if(cell.getValue() instanceof Router){
				((Router)cell.getValue()).removeRouter();
			}
		}
		
		for(Object cellObj2:(Object[])graph.getChildVertices(graph.getDefaultParent())){
		
			mxCell cell2=(mxCell)cellObj2;
			if (cell1==cell2)continue;
			int i=0;
			//System.out.println(getSortedEphire(cell2));
			for(mxCell c:getSortedEphire(cell2)){
				if(i<n){
					if(graph.getEdgesBetween(c, cell2).length==0){
						if(c.getEdgeCount()<n){
							graph.insertEdge(graph.getDefaultParent(), "Edge", null, c, cell2);
							i++;
							System.out.println("added"+i+" "+cell2.getValue()+"-->"+c.getValue());
						}
					}else{
						i++;
					}
				}else{
					if(graph.getEdgesBetween(c, cell2).length!=0){
						removeEdgesBetween(c,cell2);
						System.out.println("removed"+c.getValue());
						//linksUpd(c);
					}
				}
				
				
			}
		
		}
		
		
	}
	
	
	
	synchronized public static void removeEdgesBetween(final mxCell cell1,final mxCell cell2){
		while(graph.getEdgesBetween(cell1, cell2).length!=0){
			mxCell edge = (mxCell) graph.getEdgesBetween(cell1, cell2)[0];
			
			edge.removeFromParent();
			edge.removeFromTerminal(true);
			edge.removeFromTerminal(false);
			mxCell cell = edge;
			if (cell.getValue() instanceof Link){
				((Link)cell.getValue()).removeLink();
			}else if(cell.getValue() instanceof Router){
				((Router)cell.getValue()).removeRouter();
			}
		}
		
	}
	
	
	
	synchronized public static void removeEdges(final Object cellObj1){
		//graph.get
		
		//new Thread(){
			//public void run(){
				//synchronized(graph){
	
		
		
		
		

			mxCell cell1=(mxCell)cellObj1;
			if(cell1.getValue()==null){
				cell1.setValue(new Router());
			}
			
		for(Object cellObj2:(Object[])graph.getChildVertices(graph.getDefaultParent())){
			
			mxCell cell2=(mxCell)cellObj2;
			if(cell1.getGeometry().getPoint().distance(cell2.getGeometry().getPoint())>Restrictions.defaultradius){
				if (((Object[])graph.getEdgesBetween(cell1, cell2)).length==0)continue;
				Object[] edges=graph.getEdgesBetween(cell1, cell2);
				
				mxCell edge = (mxCell)edges[0];
				
				mxCell lastedge =(mxCell)edges[edges.length-1];
				if(graph.getCellStyle(edge).get("shape").toString().equals("arrow"))continue;
				
				edge.removeFromParent();
				edge.removeFromTerminal(true);
				edge.removeFromTerminal(false);
				//linksUpd(cell2);
				mxCell cell = edge;
				if (cell.getValue() instanceof Link){
					((Link)cell.getValue()).removeLink();
				}else if(cell.getValue() instanceof Router){
					((Router)cell.getValue()).removeRouter();
				}
				
				
			}
		}
/*
		List<mxCell> cells = new ArrayList<mxCell>();
		for(Object cellObj2:(Object[])graph.getChildVertices(graph.getDefaultParent())){
			
			mxCell cell2=(mxCell)cellObj2;
		
			if(cell1==cell2)continue;
			if(cell1.getGeometry().getPoint().distance(cell2.getGeometry().getPoint())<Restrictions.defaultradius){
				
				if (((Object[])graph.getEdgesBetween(cell1, cell2)).length!=0)continue;
				
				if(cell2.getValue()==null){
					cell2.setValue(new Router());
				}
				
				//graph.insertEdge(graph.getDefaultParent(), "Edge", null, cell1, cell2);
					
					cells.add(cell2);
				
			}
				
		}
		int CellsProceed=0;
		//List<mxCell> cells2 = new ArrayList<mxCell>();
		//cells.
		
		
		
		//First
		List<mxCell> cells2 = new ArrayList<mxCell>();
		while(!cells.isEmpty()){
			double min=0;
			mxCell minCell=null;
			min=Double.MAX_VALUE;
			for(mxCell cell2:cells){
				if(cell1==cell2)continue;
				double dist = cell1.getGeometry().getPoint().distance((Point2D)cell2.getGeometry().getPoint());
				if(dist<min){minCell=cell2;min=dist;continue;}
				
			
			}
			if(minCell!=null){
				cells.remove(minCell);
				cells2.add(minCell);
			}
		}
			for (mxCell cell2:cells2){
				if(addOrto(cell1, cell2)){
					CellsProceed++;
				}
				
			}

		
		
	
		
		for(int i=0;i< Restrictions.maxLinks-CellsProceed;i++){
			
			if(i>=cells.size())return;
			//graph.insertEdge(graph.getDefaultParent(), "Edge", null, cell1, cells.get(i));
			
		}
		
		}	*/
	}	
		
	
	

	public GraphEditor(String appTitle, final mxGraphComponent component)
	{
		super("Wireless Modeling", component);
		
		graph = graphComponent.getGraph();
		Simulator modeler = new Simulator((mxGraphModel) graph.getModel());
		new Thread(modeler).start();
		graph.setCellsEditable(false);
		graph.setEdgeLabelsMovable(false);
		graph.setCellsBendable(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsDeletable(false);
		graph.setCellsResizable(false);
		component.setAntiAlias(false);
		graph.setCellsCloneable(false);
		//graph.setCellsSelectable(false);
		
		// mxResources.get("antialias")
		
		
		//MYCODE BEGIN--------------------------------
		component.setBackground(new Color(204,204,204));


		graph.addListener(mxEvent.MOVE_CELLS,new mxIEventListener(){

			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// TODO Auto-generated method stub
				//System.out.println(evt.getProperties());
				Point point=(Point)evt.getProperty("location");
				Object[] CellsArrayObj=	(Object[])evt.getProperty("cells");
				//System.out.println(CellsArrayObj[0]);
				for(Object cellObj1:CellsArrayObj ){
					if(((mxCell)cellObj1).isVertex())linksUpd(cellObj1);
				}
					
					
				
				  
			}
		});
	
		graph.addListener(mxEvent.CELLS_ADDED,new mxIEventListener(){

			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// TODO Auto-generated method stub
				//System.out.println(evt.getProperties());
				//System.out.println(component.getBackground());
				
				mxCell source = (mxCell)evt.getProperty("source");
				//System.out.println(evt.getProperties());
				if(source==null)return;				
				mxCell target = (mxCell)evt.getProperty("target");
				if(target==null)return;	
				
				
			source.setConnectable(false);
			target.setConnectable(false);
				//linksUpd(target);
				Object[] edges=graph.getEdgesBetween((mxCell)evt.getProperty("source"), (mxCell)evt.getProperty("target"));
				if(edges.length==0)return;	
				mxCell edge = (mxCell)edges[0];
				//if(edges.length==1)linksUpd(target);
				if(edge==null)return;
				int n= edges.length;
				mxCell lastedge =(mxCell)edges[n-1];
				
				if(graph.getCellStyle(lastedge).get("shape").toString().equals("arrow")){
					Packet p=new app.simulator.packets.Packet((Router)source.getValue(),(Router)target.getValue());					
					p.PacketCell=lastedge;
					((Router)source.getValue()).send(p, null);
					return;
				}

				
				if(source.getValue()==null||!(source.getValue()instanceof Router)){
					source.setValue(new Router());					
				}
				if(target.getValue()==null||!(target.getValue()instanceof Router)||target.getValue()==source.getValue()){
					target.setValue(new Router());					
				}
				
				if(edge.getValue()==null||!(edge.getValue()instanceof Link)){
					Link link = new Link((Router)source.getValue(),(Router)target.getValue(),edge);
					edge.setValue(link);
					
					//System.out.println(edge.getStyle());
				}
				
				graph.refresh();
			}
		});
		
		graph.addListener(mxEvent.REMOVE_CELLS,new mxIEventListener(){

			@Override
			public void invoke(Object sender, mxEventObject evt) {
				Object[] CellsArrayObj=	(Object[])evt.getProperty("cells");
				for(Object cellObj1:CellsArrayObj ){
					mxCell cell = (mxCell)cellObj1;
					if (cell.getValue() instanceof Link){
						((Link)cell.getValue()).removeLink();
					}else if(cell.getValue() instanceof Router){
						((Router)cell.getValue()).removeRouter();
					}
				}
				
			}
		});
	
		graph.addListener(mxEvent.REMOVE_CELLS_FROM_PARENT,new mxIEventListener(){

			@Override
			public void invoke(Object sender, mxEventObject evt) {
				Object[] CellsArrayObj=	(Object[])evt.getProperty("cells");
				for(Object cellObj1:CellsArrayObj ){
					mxCell cell = (mxCell)cellObj1;
					if (cell.getValue() instanceof Link){
						((Link)cell.getValue()).removeLink();
					}else if(cell.getValue() instanceof Router){
						((Router)cell.getValue()).removeRouter();
					}
				}
				//System.out.println(evt.getProperties());
			}
		});
		
		
		
		

	
			
			
		
		//MYCODE END---------------------------------------
		// Creates the shapes palette
		EditorPalette shapesPalette = insertPalette(mxResources.get("shapes"));


		// Sets the edge template to be used for creating new edges if an edge
		// is clicked in the shape palette
		shapesPalette.addListener(mxEvent.SELECT, new mxIEventListener()
		{
			public void invoke(Object sender, mxEventObject evt)
			{
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable)
				{
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];

					if (graph.getModel().isEdge(cell))
					{
						((CustomGraph) graph).setEdgeTemplate(cell);
					}
				}
			}

		});

		// Adds some template cells for dropping into the graph
		
		shapesPalette
				.addTemplate(
						"Router",
						new ImageIcon(
								GraphEditor.class
										.getResource("/com/mxgraph/examples/swing/images/ellipse.png")),
						"ellipse", 25, 25, "");
			


		shapesPalette
				.addEdgeTemplate(
						"Connect",
						new ImageIcon(
								GraphEditor.class
										.getResource("/com/mxgraph/examples/swing/images/straight.png")),
						"straight;endArrow=none", 120, 120, "");

		shapesPalette
				.addEdgeTemplate(
						"SendData",
						new ImageIcon(
								GraphEditor.class
										.getResource("/com/mxgraph/examples/swing/images/arrow.png")),
						"arrow;edgeStyle=none;opacity=30", 120, 120, "");

		

	}

	/**
	* 
	*/
	public static class CustomGraphComponent extends mxGraphComponent
	{

		Thread GraphThread = new Thread(new Runnable(){
			public void run(){
				
					try {
						while(true){	
							CustomGraphComponent.this.refresh();
							Thread.sleep(50);
						}
					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
		});
		/**
		 * 
		 */
		private static final long serialVersionUID = -6833603133512882012L;

		/**
		 * 
		 * @param graph
		 */
		public CustomGraphComponent(mxGraph graph)
		{
			super(graph);

			
			
			
			
			// Sets switches typically used in an editor
			
			
			
			
			setPageVisible(false);
			setGridVisible(false);
			setToolTips(true);
			getConnectionHandler().setCreateTarget(true);

			// Loads the defalt stylesheet from an external file
			mxCodec codec = new mxCodec();
			Document doc = mxUtils.loadDocument(GraphEditor.class.getResource(
					"/com/mxgraph/examples/swing/resources/basic-style.xml")
					.toString());
			codec.decode(doc.getDocumentElement(), graph.getStylesheet());

			// Sets the background to white
			getViewport().setOpaque(false);
			setBackground(Color.WHITE);
		}

		/**
		 * Overrides drop behaviour to set the cell style if the target
		 * is not a valid drop target and the cells are of the same
		 * type (eg. both vertices or both edges). 
		 */
		public Object[] importCells(Object[] cells, double dx, double dy,
				Object target, Point location)
		{
			if (target == null && cells.length == 1 && location != null)
			{
				target = getCellAt(location.x, location.y);

				if (target instanceof mxICell && cells[0] instanceof mxICell)
				{
					mxICell targetCell = (mxICell) target;
					mxICell dropCell = (mxICell) cells[0];

					if (targetCell.isVertex() == dropCell.isVertex()
							|| targetCell.isEdge() == dropCell.isEdge())
					{
						mxIGraphModel model = graph.getModel();
						model.setStyle(target, model.getStyle(cells[0]));
						graph.setSelectionCell(target);

						return null;
					}
				}
			}

			return super.importCells(cells, dx, dy, target, location);
		}

	}

	/**
	 * A graph that creates new edges from a given template edge.
	 */
	public static class CustomGraph extends mxGraph
	{
		/**
		 * Holds the edge to be used as a template for inserting new edges.
		 */
		protected Object edgeTemplate;

		/**
		 * Custom graph that defines the alternate edge style to be used when
		 * the middle control point of edges is double clicked (flipped).
		 */
		public CustomGraph()
		{
			setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		}

		/**
		 * Sets the edge template to be used to inserting edges.
		 */
		public void setEdgeTemplate(Object template)
		{
			edgeTemplate = template;
		}

		/**
		 * Prints out some useful information about the cell in the tooltip.
		 */
		public String getToolTipForCell(Object cell)
		{
			String tip = "<html>";
			mxGeometry geo = getModel().getGeometry(cell);
			mxCellState state = getView().getState(cell);

			if (getModel().isEdge(cell))
			{
				tip += "points={";

				if (geo != null)
				{
					List<mxPoint> points = geo.getPoints();

					if (points != null)
					{
						Iterator<mxPoint> it = points.iterator();

						while (it.hasNext())
						{
							mxPoint point = it.next();
							tip += "[x=" + numberFormat.format(point.getX())
									+ ",y=" + numberFormat.format(point.getY())
									+ "],";
						}

						tip = tip.substring(0, tip.length() - 1);
					}
				}

				tip += "}<br>";
				tip += "absPoints={";

				if (state != null)
				{

					for (int i = 0; i < state.getAbsolutePointCount(); i++)
					{
						mxPoint point = state.getAbsolutePoint(i);
						tip += "[x=" + numberFormat.format(point.getX())
								+ ",y=" + numberFormat.format(point.getY())
								+ "],";
					}

					tip = tip.substring(0, tip.length() - 1);
				}

				tip += "}";
			}
			else
			{
				tip += "geo=[";

				if (geo != null)
				{
					tip += "x=" + numberFormat.format(geo.getX()) + ",y="
							+ numberFormat.format(geo.getY()) + ",width="
							+ numberFormat.format(geo.getWidth()) + ",height="
							+ numberFormat.format(geo.getHeight());
				}

				tip += "]<br>";
				tip += "state=[";

				if (state != null)
				{
					tip += "x=" + numberFormat.format(state.getX()) + ",y="
							+ numberFormat.format(state.getY()) + ",width="
							+ numberFormat.format(state.getWidth())
							+ ",height="
							+ numberFormat.format(state.getHeight());
				}

				tip += "]";
			}

			mxPoint trans = getView().getTranslate();

			tip += "<br>scale=" + numberFormat.format(getView().getScale())
					+ ", translate=[x=" + numberFormat.format(trans.getX())
					+ ",y=" + numberFormat.format(trans.getY()) + "]";
			tip += "</html>";

			return tip;
		}

		/**
		 * Overrides the method to use the currently selected edge template for
		 * new edges.
		 * 
		 * @param graph
		 * @param parent
		 * @param id
		 * @param value
		 * @param source
		 * @param target
		 * @param style
		 * @return
		 */
		public Object createEdge(Object parent, String id, Object value,
				Object source, Object target, String style)
		{
			if (edgeTemplate != null)
			{
				mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
				edge.setId(id);

				return edge;
			}

			return super.createEdge(parent, id, value, source, target, style);
		}

	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		mxConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		GraphEditor editor = new GraphEditor();
		editor.createFrame(new EditorMenuBar(editor)).setVisible(true);
	}
}
