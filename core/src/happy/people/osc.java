package game;

import oscP5.*;

public class MuseOscServer {

	static MuseOscServer museOscServer;
	float stress = 0.0f;
	static final float alpha = 0.001f;
	float stressVar = 0.0f;

	OscP5 museServer;
	static int recvPort = 5000;

	public static void main(String [] args) {
		museOscServer = new MuseOscServer();
		museOscServer.museServer = new OscP5(museOscServer, recvPort);
	}

	void oscEvent(OscMessage msg) {
		System.out.println("### got a message " + msg);
		if (msg.checkAddrPattern("/muse/eeg")==true) {  
			float tot = 0;
			for(int i = 0; i < 4; i++) {
				float cur = msg.get(i).floatValue();
				tot += cur;
			}
			tot /= (6731.26);
			stress = tot*alpha + stress * (1-alpha);
			stressVar = (tot - stress) * (tot - stress) * alpha + stressVar * (1 - alpha);
			System.out.println("Stress: " + stress);
			System.out.println("stress var: " + stressVar);
			
		} 
	}
}

