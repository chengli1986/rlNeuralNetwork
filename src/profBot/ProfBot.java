package profBot;

//import robocode.Robot;
//import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.AdvancedRobot;

public class ProfBot extends AdvancedRobot {

	private int moveDirection = 1;

	public void run() {
		
		setAdjustGunForRobotTurn(true);
		
		while (true) {
			setTurnRadarRight(90);
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double enemyBearing = e.getBearing();
		
		if (getVelocity() == 0)
			moveDirection *= -1;

		setTurnRight(normalizeBearing(enemyBearing + 90));
		setAhead(1000 * moveDirection);
	}

	//----------------------------------------------------------
	// Helper method
	//----------------------------------------------------------

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
}
