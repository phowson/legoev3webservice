package phil.legoev3webservice.control;

import java.util.Arrays;

public class ScanData {

	public final int[] ultrasoundData;
	public final int[] ultrasoundData2;
	public final int[] colorData;
	public final int[] colorData2;

	public ScanData(int[] irData, int[] colorData, int[] irData2, int[] colorData2) {
		this.ultrasoundData = irData;
		this.ultrasoundData2 = irData2;

		this.colorData = colorData;
		this.colorData2 = colorData2;
	}

	@Override
	public String toString() {
		return "ScanData [ultrasoundData=" + Arrays.toString(ultrasoundData) + ", ultrasoundData2="
				+ Arrays.toString(ultrasoundData2) + ", colorData=" + Arrays.toString(colorData) + ", colorData2="
				+ Arrays.toString(colorData2) + "]";
	}

}
