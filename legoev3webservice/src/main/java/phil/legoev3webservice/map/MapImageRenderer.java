package phil.legoev3webservice.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MapImageRenderer {

	private static final Color myWhite = new Color(255, 255, 255);
	private static final Color myGray = new Color(128, 128, 128);
	private static final Color myRed = new Color(255, 0, 0);
	private static final Color myBlack = new Color(0, 0, 0);
	private static final Color myBlue = new Color(0, 0, 255);

	private static final Color myGreen = new Color(0, 255, 0);

	public BufferedImage render(RobotState state, EnvironmentMap map, BufferedImage image) {
		if (image == null) {
			image = new BufferedImage(map.mapWidth, map.mapWidth, BufferedImage.TYPE_INT_RGB);
		}

		for (int i = 0; i < map.mapWidth; ++i) {
			for (int j = 0; j < map.mapWidth; ++j) {

				int rgb = myWhite.getRGB();
				switch (map.getAt(i, j)) {
				case EnvironmentMap.UNKNOWN:
					rgb = myWhite.getRGB();
					break;
				case EnvironmentMap.KNOWN_CLEAR:
					rgb = myGray.getRGB();
					break;
				case EnvironmentMap.OBSTRUCTION:
					rgb = myBlack.getRGB();
					break;
				case EnvironmentMap.HARD_OBSTRUCTION:
					rgb = myBlue.getRGB();
					break;

				case EnvironmentMap.DANGER:
					rgb = myRed.getRGB();
					break;

				}

				image.setRGB(i, j, rgb);

			}

		}

		Graphics graphics = image.getGraphics();
		double overallHeadingRad = state.heading_DEG * Math.PI / 180.0;
		double vX = Math.cos(overallHeadingRad) * 30;
		double vY = Math.sin(overallHeadingRad) * 30;
		graphics.setColor(myGreen);
		graphics.drawLine((int) state.x_CM, (int) state.y_CM, (int) (state.x_CM + vX), (int) (state.y_CM + vY));
		graphics.fillOval((int) state.x_CM - 4, (int) state.y_CM - 4, 8, 8);

		return image;
	}
}
