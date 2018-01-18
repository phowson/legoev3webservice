package phil.legoev3webservice.control;

public class AdvanceResults {

	public final int clicksAdvanced;
	public final int startProximity;
	public final int endProximity;
	public final int reflectedLightIntensity;
	public final boolean pressed;

	public AdvanceResults(int clicksAdvanced, int startProximity, int endProximity, int reflectedLightIntensity,
			boolean pressed) {
		this.clicksAdvanced = clicksAdvanced;
		this.startProximity = startProximity;
		this.endProximity = endProximity;
		this.reflectedLightIntensity = reflectedLightIntensity;
		this.pressed = pressed;

	}

	@Override
	public String toString() {
		return "AdvanceResults [clicksAdvanced=" + clicksAdvanced + ", startProximity=" + startProximity
				+ ", endProximity=" + endProximity + ", reflectedLightIntensity=" + reflectedLightIntensity
				+ ", pressed=" + pressed + "]";
	}

}
