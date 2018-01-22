package phil.legoev3webservice;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import phil.legoev3webservice.control.ScanData;
import phil.legoev3webservice.control.ScanDataFilter;
import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.MapImageRenderer;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class EnvironmentMapPlay {

	public static void main(String[] args) throws IOException {

		int[] irData = new int[] { 92, 93, 92, 92, 92, 92, 91, 90, 88, 90, 88, 86, 84, 84, 82, 81, 79, 78, 76, 76, 74,
				73, 71, 70, 69, 68, 66, 66, 64, 64, 63, 62, 61, 59, 58, 56, 54, 53, 52, 51, 50, 49, 48, 47, 46, 44, 43,
				42, 41, 40, 39, 38, 37, 36, 36, 34, 34, 33, 32, 31, 31, 31, 30, 29, 29, 28, 28, 28, 28, 27, 27, 27, 26,
				26, 25, 25, 24, 24, 24, 24, 24, 24, 24, 24, 24, 23, 23, 23, 23, 24, 13, 23, 23, 23, 23, 23, 23, 24, 23,
				24, 24, 24, 24, 24, 24, 25, 25, 26, 26, 26, 27, 27, 28, 28, 28, 28, 29, 29, 30, 30, 30, 31, 31, 32, 32,
				33, 34, 34, 35, 36, 36, 37, 38, 39, 39, 40, 42, 42, 43, 45, 46, 47, 47, 49, 50, 51, 52, 53, 54, 55, 56,
				57, 58, 59, 59, 59, 60, 61, 61, 61, 61, 61, 61, 60, 60, 59, 58, 57, 57, 57, 56, 57, 57, 56, 57, 57, 57,
				57, 58, 58 };
		int[] irData2 = new int[] { 90, 90, 91, 88, 87, 85, 84, 82, 81, 79, 79, 77, 75, 74, 73, 72, 70, 69, 68, 67, 65,
				64, 62, 61, 60, 59, 57, 55, 54, 53, 52, 50, 49, 49, 47, 46, 45, 44, 43, 41, 40, 40, 39, 38, 37, 36, 35,
				36, 34, 33, 33, 31, 31, 31, 30, 30, 29, 29, 29, 28, 28, 28, 28, 27, 27, 26, 26, 26, 26, 25, 25, 25, 25,
				24, 24, 24, 24, 25, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 25,
				25, 25, 25, 25, 26, 28, 28, 28, 28, 29, 29, 29, 30, 30, 30, 31, 31, 32, 32, 33, 33, 34, 35, 36, 36, 37,
				38, 39, 39, 40, 41, 42, 43, 44, 45, 46, 48, 48, 49, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 60, 61, 61,
				61, 62, 62, 61, 61, 60, 59, 59, 58, 57, 57, 57, 57, 56, 56, 57, 57, 57, 57, 58, 58, 58, 58, 59, 58, 58,
				57, 57, 59 };
		int[] colorData = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] colorData2 = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		ScanData sd = new ScanData(irData, colorData, irData2, colorData2);
		RobotState state = new RobotState();
		state.x_CM = 500;
		state.y_CM = 500;
		state.heading_DEG = 0;
		EnvironmentMap map = new EnvironmentMap(1000);

		map.hitHardObsticle(state, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		map.apply(state, new ScanDataFilter().filter(sd), true);
		BufferedImage image = new MapImageRenderer().render(state, map, null, null, -1,-1);
		File outputfile = new File("saved.png");
		ImageIO.write(image, "png", outputfile);

	}

}
