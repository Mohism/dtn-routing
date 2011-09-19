package app.simulator;
import java.io.Serializable;

import com.mxgraph.model.mxGraphModel;


public class Simulator implements Runnable,Serializable{
	mxGraphModel model;
	public static boolean isPaused=true;
	public Simulator(mxGraphModel model){
		this.model=model;
	}
	
	@Override
	public void run() {
	while(true){	
		//System.out.println(model.getCells());
		      
        try{ 
           Thread.sleep((int)(1000));
        } catch( InterruptedException e ) {
        	
            System.out.println("Interrupted Exception caught");
        } 
	}
	}
}
