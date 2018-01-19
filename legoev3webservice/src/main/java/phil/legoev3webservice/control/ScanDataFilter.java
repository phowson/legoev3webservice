package phil.legoev3webservice.control;

import java.awt.Robot;
import java.util.Arrays;

import phil.legoev3webservice.robot.RobotCalibration;

public class ScanDataFilter {

	public FilteredSensorData filter(ScanData data) {
		double[] d = new double[6];
		double[] d2 = new double[4];

		FilteredSensorData out = new FilteredSensorData(new double[data.irData.length], new double[data.irData.length]);
		double heading = 90;
		for (int i = 0; i < data.irData.length; ++i) {
			double dst;
			if (i == 0) {
				d2[0] = data.irData[i];
				d2[1] = data.irData2[i];
				d2[2] = data.irData[i + 1];
				d2[3] = data.irData2[i + 1];

				Arrays.sort(d2);

				dst = interpolate(0.5 * (d2[1] + d2[2]));

			} else if (i == data.irData.length - 1) {
				d2[0] = data.irData[i];
				d2[1] = data.irData2[i];
				d2[2] = data.irData[i - 1];
				d2[3] = data.irData2[i - 1];
				Arrays.sort(d2);
				out.headings[i] = heading;
				dst = interpolate(0.5 * (d2[1] + d2[2]));
			} else {
				d[0] = data.irData[i];
				d[1] = data.irData2[i];
				d[2] = data.irData[i - 1];
				d[3] = data.irData2[i - 1];
				d[4] = data.irData[i + 1];
				d[5] = data.irData2[i + 1];

				Arrays.sort(d);

				out.headings[i] = heading;
				dst = interpolate(0.25 * (d[1] + d[2] + d[3] + d[4])) ;
			}

			if (dst >= RobotCalibration.SENSOR_INFINITY_POINT_CM) {
				dst = Double.POSITIVE_INFINITY;
			}
			out.headings[i] = heading;
			out.distance_CM[i] = dst;

			heading -= RobotCalibration.SCAN_DEGREES_PER_VALUE;
		}

		return out;

	}

	private double interpolate(double pc) {
		for (int i = 1; i < RobotCalibration.SENSOR_CALIBRATION_CM.length; ++i) {
			if (RobotCalibration.SENSOR_CALIBRATION_PC[i] >= pc) {
				double prev = RobotCalibration.SENSOR_CALIBRATION_PC[i - 1];
				double z = pc - prev;
				double d = RobotCalibration.SENSOR_CALIBRATION_PC[i] - prev;
				double dC = RobotCalibration.SENSOR_CALIBRATION_CM[i] - RobotCalibration.SENSOR_CALIBRATION_CM[i - 1];
				return dC * (z / d) + RobotCalibration.SENSOR_CALIBRATION_CM[i];
			}

		}
		return Double.POSITIVE_INFINITY;
	}

}
