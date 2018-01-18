package phil.legoev3webservice.control;

import java.util.Arrays;

public class FilteredSensorData {
	public final double[] headings;
	public final double[] distance_CM;

	public FilteredSensorData(double[] headings, double[] distance_CM) {
		super();
		this.headings = headings;
		this.distance_CM = distance_CM;
	}

	@Override
	public String toString() {
		return "FilteredSensorData [headings=" + Arrays.toString(headings) + ", distance_CM="
				+ Arrays.toString(distance_CM) + "]";
	}

}
