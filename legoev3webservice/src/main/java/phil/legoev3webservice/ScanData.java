package phil.legoev3webservice;

public class ScanData {

	public final int[] irData;
	public final int[] irData2;
	public final int[] colorData;
	public final int[] colorData2;

	public ScanData(int[] irData, int[] colorData, int[] irData2, int[] colorData2) {
		this.irData = irData;
		this.irData2 = irData2;

		this.colorData = colorData;
		this.colorData2 = colorData2;
	}

}
