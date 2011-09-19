package app.simulator.routing;

import app.simulator.Link;

public class AODVRoute extends Route{

	public int routernum;
	AODVRoute(Link link, int metric, int routernum) {
		super(link, metric);
		this.routernum=routernum;
		// TODO Auto-generated constructor stub
	}

}
