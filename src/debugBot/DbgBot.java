package debugBot;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import java.awt.Color;
import java.awt.Graphics2D;
import robocode.WinEvent;
import robocode.util.*;

public class DbgBot extends AdvancedRobot {

	// The coordinates of the last scanned robot
    public int scannedX = Integer.MIN_VALUE;
    public int scannedY = Integer.MIN_VALUE;
	public double angle = 0.0;
	public double enemy = 0.0;
	public double enemy2 = 0.0;
	
	public void run() {
	     // Rotate the radar for ever in order to detect robots
		turnRadarRight(360);
	     while (true) {
	    	 setBodyColor(Color.BLUE);
	 		setGunColor(Color.RED);
	 		setRadarColor(Color.YELLOW);
	 		setBulletColor(Color.CYAN);
	 		scan();
	         //turnRadarRight(360);
	 		 //setTurnRadarRight(360);
	         setTurnRight((enemy-getHeading()));
	         //setTurnRadarRight(1);
	         
	         //setFire(0.5);
	     	if (getGunHeat() == 0)
	  			setFire(2);
	         execute();
	     }
	 }
	/*
	public void onPaint(Graphics2D g) {
	     // Set the paint color to red
	     g.setColor(java.awt.Color.RED);
	     // Paint a filled rectangle at (50,50) at size 100x150 pixels
	     g.fillRect(50, 50, 100, 150);
	 }
	*/
	
	// Paint a transparent square on top of the last scanned robot

	/**
	 * onWin:  Do a victory dance
	 */
	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

	
	 // Called when we have scanned a robot
	 public void onScannedRobot(ScannedRobotEvent e) {
	     
		 enemy = getHeading() + e.getBearing();
		 enemy2 = getHeadingRadians() + e.getBearingRadians() 
				 - getRadarHeadingRadians();
		 // Calculate the angle to the scanned robot
	     angle = Math.toRadians((getHeading() + e.getBearing()) % 360);
	 
	     // Calculate the coordinates of the robot
	     scannedX = (int)(getX() + Math.sin(angle) * e.getDistance());
	     scannedY = (int)(getY() + Math.cos(angle) * e.getDistance());
	     setTurnRadarRightRadians(Utils.normalRelativeAngle(enemy2));
	     //setFire(0.5);
	     setDebugProperty("heading", " "+e.getHeading()+" "+e.getHeadingRadians());
	     setDebugProperty("bearing", " "+e.getBearing()+" "+e.getBearingRadians());
	     setDebugProperty("enemy", " "+enemy);
	     setDebugProperty("enemy2", " "+enemy2);
	 }
	

public void onPaint(Graphics2D g) {
    // Set the paint color to a red half transparent color
    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));

    // Draw a line from our robot to the scanned robot
    g.drawLine(scannedX, scannedY, (int)getX(), (int)getY());

    // Draw a filled square on top of the scanned robot that covers it
    g.fillRect(scannedX - 20, scannedY - 20, 40, 40);
}

}