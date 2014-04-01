/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher;

import org.openiot.gsndatapusher.gui.MainWindow;
import java.awt.Dimension;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import org.openiot.gsndatapusher.gui.AbstractSensorDummyPanel;
import org.openiot.gsndatapusher.singletontcplistener.SingletonTcpListenerSensorDummyPanel;
import org.openiot.gsndatapusher.tcplistener.TcpListenerSensorDummyPanel;


/**
 *
 * @author admin-jacoby
 */
public class Main {
       

    public static void main(String[] args) {

        setLogLevel(Level.INFO);
        // SensorManager sensor = new SensorManager(1, 1, 1, new SingletonTcpListenerAdapter(), new SingletonTcpListenerConfig());                 
        MainWindow window = new MainWindow();
        window.setMinimumSize(new Dimension(500, 300));
        window.setPreferredSize(new Dimension(900, 600));
        window.pack();
        window.setVisible(true);
    }

    private static void setLogLevel(Level level) {
        LogManager.getLogManager().getLogger("").setLevel(level);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        LogManager.getLogManager().getLogger("").addHandler(consoleHandler);
    }
}
