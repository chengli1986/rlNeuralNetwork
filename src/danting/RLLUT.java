package danting;

import robocode.RobocodeFileOutputStream;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitWallEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.DeathEvent;
import robocode.WinEvent;
import robocode.RobotDeathEvent;

import java.awt.geom.*;
import java.io.PrintStream;
import java.io.IOException;


public class RLLUT extends AdvancedRobot {

	private LUT table;
	public double ImReward = 0.0; 
	public static int NumWin; 
	public static int NumLose; 
	public double firePower; 
	public int direction = 1;
	public int isHitWall = 0; 
	public int isHitByBullet = 0;

	public void run() {
		table = new LUT();
		table.distance = 100000; 
		LUT.makeState();
		table.initialiseLUT(); 
		System.out.println("makestate complete");
		while (true) { 
			robotMovement();
			firePower = 1000/table.distance; 
			if (firePower > 3)
				firePower = 3; 
			radarMovement();
			gunMovement();
			if (getGunHeat() == 0) {
				setFire(firePower); }
				execute(); 
			}
		}
	
	private void robotMovement() {
		int state = getState();
		int action = table.selectAction(state); 
		table.QLearning(state, action, ImReward);
		ImReward = 0.0; 
		isHitWall = 0; 
		isHitByBullet = 0;
		switch (action) {
			case LUT.Ahead: 
				setAhead(LUT.RobotMoveDistance); 
				break;
			case LUT.Back: 
				setBack(LUT.RobotMoveDistance); 
				break;
			case LUT.TurnLeft: 
				setTurnLeft(LUT.RobotTurnDegree); 
				break;
			case LUT.TurnRight: 
				setTurnRight(LUT.RobotTurnDegree); 
				break;
		} 
	}
	
	private int getState() {
		int heading = LUT.GetHeading(getHeading());
		int targetDistance = LUT.GetTargetDistance(table.distance);
		int targetBearing = LUT.GetTargetBearing(table.bearing);
		int state = LUT.State[heading][targetBearing][targetDistance][isHitByBullet][isHitWall]; 
		return state;
	}
	
	private void radarMovement() {
		double radarOffset;
		if (getTime() - table.ctime > 4) { //if we haven't seen anybody for a bit....
			radarOffset = 4*Math.PI; //rotate the radar to find a target }
		} else {
			radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(table.y - getY(),table.x - getX()));
			radarOffset = NormaliseBearing(radarOffset); 
			if (radarOffset < 0)
				radarOffset -= Math.PI/10; 
			else
				radarOffset += Math.PI/10; 
		}
		setTurnRadarLeftRadians(radarOffset); 
	}

	private void gunMovement() {
		long time;
		long nextTime;
		Point2D.Double p;
		p = new Point2D.Double(table.x, table.y); 
		for (int i = 0; i < 20; i++) {
			nextTime = (int)Math.round((getrange(getX(),getY(),p.x,p.y)/(20-(3*firePower)))); 
			time = getTime() + nextTime - 10;
			p = table.guessPosition(time);
		}
		double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(),p.x - getX()));
		setTurnGunLeftRadians(NormaliseBearing(gunOffset)); 
	}
	
	double NormaliseBearing(double angle) { 
		if (angle > Math.PI)
			angle -= 2*Math.PI; 
		if (angle < -Math.PI) 
			angle += 2*Math.PI; 
		return angle;
	}
	
	public double getrange( double x1,double y1, double x2,double y2 ) {
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo ); 
		return h;
	}
	
	public void onBulletHit(BulletHitEvent e) {
		if (table.name == e.getName()) {
			ImReward = e.getBullet().getPower() * 3; 
			out.println("Bullet Hit: " + ImReward);
		} 
	}
	
	public void onBulletMissed(BulletMissedEvent e) {
		ImReward = -e.getBullet().getPower(); 
		out.println("Bullet Missed: " + ImReward);
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		if (table.name == e.getName()) {
			double power = e.getBullet().getPower(); 
			ImReward = -(4 * power + 2 * (power - 1)); 
			out.println("Hit By Bullet: " + ImReward);
		}
		isHitByBullet = 1; 
	}

	public void onHitRobot(HitRobotEvent e) {
		if (table.name == e.getName()) {
			ImReward = -0.6;
			out.println("Hit Robot: " + ImReward);
		} 
	}
	
	public void onHitWall(HitWallEvent e) {
		ImReward = (Math.abs(getVelocity()) * 0.5 - 1); 
		out.println("Hit Wall: " + ImReward);
		isHitWall = 1;
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		if ((e.getDistance() < table.distance)||(table.name == e.getName())) {
			//Gets the absolute bearing to the position of enemy
			double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*Math.PI);
			//Sets all the information of enemy
			table.name = e.getName();
			double h = NormaliseBearing(e.getHeadingRadians() - table.head);
			h = h/(getTime() - table.ctime);
			table.changehead = h;
			table.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is
			table.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is
			table.bearing = e.getBearingRadians();
			table.head = e.getHeadingRadians(); 
			table.ctime = getTime();
			table.speed = e.getVelocity(); 
			table.distance = e.getDistance(); 
			table.energy = e.getEnergy();
		} 
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName() == table.name) 
			table.distance = 10000;
	}
	
	public void onWin(WinEvent event) {
		NumWin++;
		ImReward = 10;
		PrintStream saveFile = null; 
		try {
			saveFile = new PrintStream(new RobocodeFileOutputStream(getDataFile("WinLose.txt")));
		} catch (IOException e) {
			System.out.println( "*** Could not create output stream for save file."); 
		}
		saveFile.println(NumWin + " "+ NumLose+" " + table.getTotalValue());
		saveFile.println("\n");
		saveFile.close();
	}
	
	public void onDeath(DeathEvent event) {
		NumLose++;
		PrintStream saveFile = null;
		try {
			saveFile = new PrintStream(new RobocodeFileOutputStream(getDataFile("WinLose.txt")));
		} catch (IOException e) {
			System.out.println( "*** Could not create output stream for save file."); 
		}
		saveFile.println(NumWin + " " + NumLose + " " + table.getTotalValue());
		saveFile.println("\n"); 
		saveFile.close();
	}
}
