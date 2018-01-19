package phil.legoev3webservice;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import phil.legoev3webservice.ai.AStarAlgorithm;
import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.MapImageRenderer;
import phil.legoev3webservice.map.RobotState;

public class TestAStar {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {
		RobotState state = new RobotState();
		state.x_CM = 500;
		state.y_CM = 500;

		EnvironmentMap map = EnvironmentMap.load("testmap1");
		AStarAlgorithm alg = new AStarAlgorithm(state, map, 999, 700);
		List<Point> path = alg.getAStarPath();
		System.out.println(path);
		MapImageRenderer renderer = new MapImageRenderer();
		BufferedImage image = renderer.render(state, map, null, path);
		File outputfile = new File("saved.png");
		ImageIO.write(image, "png", outputfile);
	}

}
