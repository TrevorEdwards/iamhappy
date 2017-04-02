package happy.people.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import happy.people.Basic3DTest;
import happy.people.IAmHappy;
import java.util.concurrent.TimeUnit;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 768;
		config.width = 1024;
		config.foregroundFPS = 60;
		config.fullscreen = true;
		Basic3DTest box = new Basic3DTest();
		new LwjglApplication(box, config);
	}
}
