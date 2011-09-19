package app.simulator.routing;

import java.io.Serializable;

import app.simulator.Link;

public class Route implements Serializable{
	Link link;
	int Metric=0;
	boolean Active=true;
	public void setMetric(int Metric){this.Metric=Metric;}
	public int getMetric(){return Metric;}
	Route(Link link,int metric){this.link=link;this.Metric=metric;}
	Link getLink(){return link;}
	public String toString(){return ""+link+"("+Metric+")";}
}
