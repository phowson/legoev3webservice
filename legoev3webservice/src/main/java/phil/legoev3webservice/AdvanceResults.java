package phil.legoev3webservice;

public class AdvanceResults {

	public final int clicksAdvanced;
	public final int startProximity;
	public final int endProximity;
	public final int reflectedLightIntensity;
	public final boolean pressed;

	public AdvanceResults(int clicksAdvanced, int startProximity, int endProximity, int reflectedLightIntensity, boolean pressed) {
		this.clicksAdvanced = clicksAdvanced;
		this.startProximity = startProximity;
		this.endProximity = endProximity;
		this.reflectedLightIntensity = reflectedLightIntensity;
		this.pressed = pressed;
		
	}

}
