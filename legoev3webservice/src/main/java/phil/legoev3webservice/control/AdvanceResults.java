package phil.legoev3webservice.control;

public class AdvanceResults {

	public final int clicksAdvancedLeft;
	public final int clicksAdvancedRight;
	public final int startProximity;
	public final int endProximity;
	public final int reflectedLightIntensity;
	public final boolean pressed;

	public AdvanceResults(int clicksAdvancedLeft, int clicksAdvancedRight, int startProximity, int endProximity,
			int reflectedLightIntensity, boolean pressed) {
		this.clicksAdvancedLeft = clicksAdvancedLeft;
		this.clicksAdvancedRight = clicksAdvancedRight;
		this.startProximity = startProximity;
		this.endProximity = endProximity;
		this.reflectedLightIntensity = reflectedLightIntensity;
		this.pressed = pressed;
	}

	public int getDistance() {
		return Math.min(this.clicksAdvancedLeft, this.clicksAdvancedRight);
	}

	public int getRotation() {
		return (this.clicksAdvancedLeft - this.clicksAdvancedRight) / 2;
	}

	@Override
	public String toString() {
		return "AdvanceResults [clicksAdvancedLeft=" + clicksAdvancedLeft + ", clicksAdvancedRight="
				+ clicksAdvancedRight + ", startProximity=" + startProximity + ", endProximity=" + endProximity
				+ ", reflectedLightIntensity=" + reflectedLightIntensity + ", pressed=" + pressed + "]";
	}

}
