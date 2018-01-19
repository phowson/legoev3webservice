package phil.legoev3webservice;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import phil.legoev3webservice.ai.AStarAlgorithm;
import phil.legoev3webservice.ai.LinearisePath;
import phil.legoev3webservice.ai.RobotMoveCommand;
import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.MapImageRenderer;
import phil.legoev3webservice.map.RobotState;

public class TestAStar {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {
		RobotState state = new RobotState();
		state.x_CM = 500;
		state.y_CM = 500;

		EnvironmentMap map = EnvironmentMap.load("testmap1");
		AStarAlgorithm alg = new AStarAlgorithm(state, map, 500, 510);


		for (int i = 0; i < 10; ++i) {
			List<Point> path = alg.getAStarPath();
			LinearisePath lp = new LinearisePath(state, map, 20, 30);
			RobotMoveCommand lc = lp.getNextLinearCommand(path);
			state.rotate(lc.heading - state.heading_DEG );

			MapImageRenderer renderer = new MapImageRenderer();
			BufferedImage image = renderer.render(state, map, null, path);
			File outputfile = new File("saved" + i + ".png");
			ImageIO.write(image, "png", outputfile);
			state.advance(lc.distance);
		}
	}

}
