package phil.legoev3webservice.ai;

import java.awt.Point;
import java.util.List;

public interface PathListener {

	void onNewPath(List<Point> listener, int targetX, int targetY);

	void stateChanged();

}
