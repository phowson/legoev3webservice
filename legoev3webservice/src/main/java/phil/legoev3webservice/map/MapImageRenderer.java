package phil.legoev3webservice.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapImageRenderer {

	private static final Color myWhite = new Color(255, 255, 255);
	private static final Color myGray = new Color(128, 128, 128);
	private static final Color myRed = new Color(255, 0, 0);
	private static final Color myBlack = new Color(0, 0, 0);
	private static final Color myBlue = new Color(0, 0, 255);

	private static final Color myGreen = new Color(0, 255, 0);
	private static final Color myCyan = new Color(0, 102, 102);

	public BufferedImage render(RobotState state, EnvironmentMap map, BufferedImage image, List<Point> path,
			int targetX, int targetY) {
		if (image == null) {
			image = new BufferedImage(map.mapWidth, map.mapWidth, BufferedImage.TYPE_INT_RGB);
		}

		for (int i = 0; i < map.mapWidth; ++i) {
			for (int j = 0; j < map.mapWidth; ++j) {

				Color rgb = myWhite;
				switch (map.getAt(i, j)) {
				case EnvironmentMap.UNKNOWN:
					rgb = myWhite;
					break;
				case EnvironmentMap.KNOWN_CLEAR:
					rgb = myGray;
					break;
				case EnvironmentMap.OBSTRUCTION:
					rgb = myBlack;
					break;
				case EnvironmentMap.HARD_OBSTRUCTION:
					rgb = myBlue;
					break;

				case EnvironmentMap.DANGER:
					rgb = myRed;
					break;

				}

				if (map.isVisited(i, j)) {
					rgb = new Color(rgb.getRed()/2, rgb.getGreen()/2, rgb.getBlue()/2);
				}

				image.setRGB(i, j, rgb.getRGB());

			}

		}

		Graphics graphics = image.getGraphics();
		if (path != null) {
			for (Point p : path) {
				try {
					image.setRGB(p.x, p.y, myCyan.getRGB());
					image.setRGB(p.x - 1, p.y, myCyan.getRGB());
					image.setRGB(p.x + 1, p.y, myCyan.getRGB());

					image.setRGB(p.x, p.y - 1, myCyan.getRGB());
					image.setRGB(p.x, p.y + 1, myCyan.getRGB());
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}
		double overallHeadingRad = state.heading_DEG * Math.PI / 180.0;
		double vX = Math.cos(overallHeadingRad) * 30;
		double vY = Math.sin(overallHeadingRad) * 30;
		graphics.setColor(myGreen);
		graphics.drawLine((int) state.x_CM, (int) state.y_CM, (int) (state.x_CM + vX), (int) (state.y_CM + vY));
		graphics.fillOval((int) state.x_CM - 4, (int) state.y_CM - 4, 8, 8);

		if (targetX > 0) {
			graphics.setColor(myBlack);
			graphics.drawOval(targetX - 10, targetY - 10, 20, 20);
			graphics.drawString("Target", targetX - 50, targetY);
		}

		return image;
	}
}
