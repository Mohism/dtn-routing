package app.simulator.routing;

public class Transit {
	public Router From;
	public Router To;
	public int FromNum;
	public int ToNum;
	public int Num=1;
	public boolean Active=true;
	public int ToTreeNum=0;
	public int Timer;
	public int Hops;
	public int Angle=0;
	public Transit(){}
	public Transit(Router From, Router To,int Hops){
		this.Hops=Hops;
		this.From=From;
		this.To=To;
	}
	public String toString(){
		return ""+From+"-->"+To+"("+Hops+")";
	}
	public int lastttl=3;
}
