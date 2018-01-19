package phil.legoev3webservice.ai;

public class SearchPoint implements Comparable<SearchPoint> {
	public final int x;
	public final int y;
	public final double cost;
	public final SearchPoint pred;
	public final double pathLength;

	public SearchPoint(int x, int y, double cost, SearchPoint pred, double pathLength) {
		super();
		this.x = x;
		this.y = y;
		this.cost = cost;
		this.pred = pred;
		this.pathLength = pathLength;
	}

	@Override
	public int compareTo(SearchPoint o) {
		if (cost < o.cost) {
			return -1;
		}

		if (cost > o.cost) {
			return 1;
		}
		return 0;
	}
}
