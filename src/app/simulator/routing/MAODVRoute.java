package app.simulator.routing;

import app.simulator.Link;

public class MAODVRoute extends AODVRoute {

	int FractureMetric;
	int FractureCount;
	int SumInfelicity;
	Router NearestFracture;
	MAODVRoute(Link link, int metric,int routernum,int FractureMetric,int FractureCount,int SumInfelicity,Router NearestFracture ) {
		super(link, metric,routernum);
		this.FractureMetric=FractureMetric;
		this.FractureCount=FractureCount;
		this.SumInfelicity=SumInfelicity;
		this.NearestFracture=NearestFracture;
		// TODO Auto-generated constructor stub
	}

}
