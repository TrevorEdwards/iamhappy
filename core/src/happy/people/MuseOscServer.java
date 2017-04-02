package happy.people;

import oscP5.*;

public class MuseOscServer extends Thread {

	public static void main(String[] args) {
		MuseOscServer mos = new MuseOscServer();
		mos.start();
	}

	MuseOscServer museOscServer;
	public float stress = 0.0f;
	static final float alpha = 0.009f;
	public float stressVar = 0.0f;

	public float getNormalizedStressVar() {
		if (museOscServer != null)
		return museOscServer.normalizedStressVar  ;
		return 0;
	}

	public float normalizedStressVar;

	OscP5 museServer;
	static int recvPort = 5000;

	@Override
	public void run() {
		System.out.println("startsss");
		museOscServer = new MuseOscServer();
		museOscServer.museServer = new OscP5(museOscServer, recvPort);
//		museOscServer.museServer.addListener(new Listener());
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
			normalizedStressVar = stressVar * 90f;
//			System.out.println("Stress: " + stress);
			System.out.println("oh");
			System.out.println(normalizedStressVar);
//			System.out.println("stress var: " + stressVar);
			
		} 
	}

//	class Listener implements OscEventListener {
//
//		@Override
//		public void oscEvent(OscMessage msg) {
//			System.out.println("### got a message " + msg);
//			if (msg.checkAddrPattern("/muse/eeg") == true) {
//				float tot = 0;
//				for (int i = 0; i < 4; i++) {
//					float cur = msg.get(i).floatValue();
//					tot += cur;
//				}
//				tot /= (6731.26);
//				stress = tot * alpha + stress * (1 - alpha);
//				stressVar = (tot - stress) * (tot - stress) * alpha + stressVar * (1 - alpha);
//				normalizedStressVar = stressVar / 0.1f;
////			System.out.println("Stress: " + stress);
//				System.out.println("oh");
////			System.out.println("stress var: " + stressVar);
//			}
//		}
//
//		@Override
//		public void oscStatus(OscStatus oscStatus) {
//
//		}
//	}
}

