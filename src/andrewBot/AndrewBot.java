package andrewBot;

//import robocode.Robot;
import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.ScannedRobotEvent;
//import robocode.FileStreamOutput;

public class AndrewBot extends AdvancedRobot {

    //Enemy keeps track of absolute enemy direction
    double enemy = 0;
    boolean lock=false;
    long lastlock=0;
    double distance=0;
    double confidence = 1.0;
    public void run() {
    	setBodyColor(Color.BLUE);
 		setGunColor(Color.RED);
 		setRadarColor(Color.YELLOW);
 		setBulletColor(Color.CYAN);
            while(true) {
                    if(lock) {
                            setAhead(distance-100);
                            if (getGunHeat() == 0)
                            		setFire(distance*confidence/30);
                            setTurnRight(enemy-getHeading());
                            //setTurnRadarRight(getHeading()-getRadarHeading());
                            setTurnRadarRight(enemy-getRadarHeading());
                            
                            execute();
                            if(getTime()-lastlock > 10) lock=false;
                    } else {
                            setTurnRadarRight(enemy-getHeading()>0?360:-360);
                            execute();
                    }
            }
    }
    public void onScannedRobot(ScannedRobotEvent e) {
            enemy = getHeading()+e.getBearing();
            distance = e.getDistance();
            lock = true;
            lastlock = getTime();
           
    }
    public void onBulletHit(BulletHitEvent e) {
            confidence=1.0;
    }
    public void onBulletMissed(BulletMissedEvent e) {
            confidence*=0.9;
    }
	
}
