package org.openiot.gsndatapusher;

import java.awt.Dimension;
import org.openiot.gsndatapusher.gui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin-jacoby
 */
public class Main {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		LOGGER.info("Starting.");
		// SensorManager sensor = new SensorManager(1, 1, 1, new SingletonTcpListenerAdapter(), new SingletonTcpListenerConfig());
		MainWindow window = new MainWindow();
		window.setTitle("X-GSN Data Pusher");
		window.setMinimumSize(new Dimension(500, 300));
		window.setPreferredSize(new Dimension(900, 600));
		window.pack();
		window.setVisible(true);
	}
}
