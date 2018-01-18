package phil.legoev3webservice.control;

import java.util.Arrays;

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

	@Override
	public String toString() {
		return "ScanData [irData=" + Arrays.toString(irData) + ", irData2=" + Arrays.toString(irData2) + ", colorData="
				+ Arrays.toString(colorData) + ", colorData2=" + Arrays.toString(colorData2) + "]";
	}

}
