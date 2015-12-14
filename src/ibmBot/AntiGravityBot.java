package ibmBot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * An AdvancedRobot illustrating Anti-Gravity movement. Adapted from:
 * http://www.ibm.com/developerworks/library/j-antigrav/
 * 
 * Changes include: updated to Java 6, documentation, formatting.
 * 
 * @author Alisdair Owens (original)
 * @author Philip Johnson
 */
public class AntiGravityBot extends AdvancedRobot {

  /** Holds all Robots found during game. Maps their name to info about them. */
  Map<String, Enemy> enemies = new HashMap<String, Enemy>(); 
  /** The robot we will target for firing upon. */
  Enemy targetEnemy;
  /** The strength of the gravity point in the middle of the field. */
  double midpointstrength = 0;
  /** Number of turns since that strength was changed. */
  int midpointcount = 0;

  /**
   * Implements AntiGravityMovement, while also scanning the field for robots and targeting one to
   * attack.
   */
  public void run() {
    setColors(Color.red, Color.blue, Color.green);
    // Make robot, gun, and radar turns independent of each other.
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    // Turn the radar completely around in order to scan entire field and find all robot enemies.
    turnRadarRightRadians(2 * Math.PI);
    // Now that we know the field, start moving, targeting, and firing. 
    while (true) {
      // Specify where we will move on next execute().
      setupAntiGravityMove(); 
      double firePower = getFirePower();
      // Specify where the gun should point on next execute(). 
      setupMoveGun(firePower); 
      // Specify to fire on next execute().
      setFire(firePower);
      // Specify a complete radar scan on next execute(). 
      setupRadarScan(); 
      // Now execute. 
      execute(); 
    }
  }

  /**
   * Determines bullet firepower as inversely proportional to the distance from the 
   * currently targetted enemy. We don't want to waste energy on targets we are unlikely
   * to hit since they are far away and our bullet is likely to miss. 
   * 
   * @return The firepower to use, between almost 0 and 3.
   */
  private double getFirePower() {
    double firePower = 1;
    if (targetEnemy != null) { 
      firePower = 400 / targetEnemy.distance;
    }
    if (firePower > 3) {
      firePower = 3;
    }
    return firePower;
  }

  /**
   * Implements an anti-gravity movement strategy with three components.
   * <ol>
   * <li>Each enemy repulses with a force inversely proportional to distance from us.
   * <li>A variable midpoint force is intended to improve overall movement.
   * <li>Walls are repulsive when we are close to them.
   * </ol>
   * Calls AdvancedRobot.setTurnLeft() and AdvancedRobot.setAhead() to implement.
   */
  void setupAntiGravityMove() {
    double xforce = 0;
    double yforce = 0;
    double force;
    double ang;
    GravPoint p;

    for (Enemy en : this.enemies.values()) {
      if (en.live) {
        p = new GravPoint(en.x, en.y, -1000);
        force = p.power / Math.pow(getRange(getX(), getY(), p.x, p.y), 2);
        // Find the bearing from the point to us
        ang = normaliseBearing(Math.PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
        // Add the components of this force to the total force in their respective directions
        xforce += Math.sin(ang) * force;
        yforce += Math.cos(ang) * force;
      }
    }

    /*
     * The next section adds a middle point with a random (positive or negative) strength. The
     * strength changes every 5 turns, and goes between -1000 and 1000. This gives a better overall
     * movement.
     */
    midpointcount++;
    if (midpointcount > 5) {
      midpointcount = 0;
      midpointstrength = (Math.random() * 2000) - 1000;
    }
    p = new GravPoint(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2, midpointstrength);
    force = p.power / Math.pow(getRange(getX(), getY(), p.x, p.y), 1.5);
    ang = normaliseBearing(Math.PI / 2 - Math.atan2(getY() - p.y, getX() - p.x));
    xforce += Math.sin(ang) * force;
    yforce += Math.cos(ang) * force;

    /*
     * The following four lines add wall avoidance. They will only affect us if the bot is close to
     * the walls due to the force from the walls decreasing at a power 3.
     */
    xforce += 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
    xforce -= 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
    yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
    yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);

    // Move in the direction of our resolved force.
    goTo(getX() - xforce, getY() - yforce);
  }

  /**
   * Move toward (x, y) on next execute().
   * @param x The X coordinate.
   * @param y The Y coordinate. 
   */
  void goTo(double x, double y) {
    double dist = 40;
    double angle = Math.toDegrees(absbearing(getX(), getY(), x, y));
    double r = turnTo(angle);
    out.println("r is: " + r);
    setAhead(dist * r);
  }

  /**
   * Turns the shortest angle possible to come to a heading, then returns the direction 
   * (1 or -1) that the bot needs to move in.
   * @param angle The desired new heading. 
   * @return Our new direction, represented as a 1 or -1. 
   **/
  int turnTo(double angle) {
    double ang;
    int dir;
    ang = normaliseBearing(getHeading() - angle);
    if (ang > 90) {
      ang -= 180;
      dir = -1;
    }
    else if (ang < -90) {
      ang += 180;
      dir = -1;
    }
    else {
      dir = 1;
    }
    setTurnLeft(ang);
    return dir;
  }

  /** Make the radar turn in a complete circle. **/
  void setupRadarScan() {
    setTurnRadarLeftRadians(2 * Math.PI);
  }

  /** 
   * Move the gun to the predicted next bearing of the enemy.
   * @param firePower The firepower associated with the targetEnemy.  
   */
  void setupMoveGun(double firePower) {
    if (targetEnemy != null) {
    long time = getTime()
        + (int) Math.round((getRange(getX(), getY(), targetEnemy.x, targetEnemy.y) / 
            (20 - (3 * firePower))));
    Point2D.Double p = targetEnemy.guessPosition(time);

    // offsets the gun by the angle to the next shot based on linear targeting 
    double gunOffset = getGunHeadingRadians()
        - (Math.PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));
    setTurnGunLeftRadians(normaliseBearing(gunOffset));
    }
  }

  /**
   * If a bearing is not within the -pi to pi range, alters it to provide the shortest angle.
   * @param angle The original angle.
   * @return The shortest angle.
   */
  double normaliseBearing(double angle) {
    double newAngle = angle;
    if (angle > Math.PI) {
      newAngle -= 2 * Math.PI;
    }
    else if (angle < -Math.PI) {
      newAngle += 2 * Math.PI;
    }
    return newAngle;
  }

  /**
   * If a heading is not within the 0 to 2pi range, alters it to provide the shortest angle.
   * @param angle The original angle.
   * @return The shortest angle.
   */
  double normaliseHeading(double angle) {
    double newAngle = angle;
    if (angle > 2 * Math.PI) {
      newAngle -= 2 * Math.PI;
    }
    if (angle < 0) {
      newAngle += 2 * Math.PI;
    }
    return newAngle;
  }

  /**
   * Returns the distance between two x,y coordinates.
   * @param x1 First x.
   * @param y1 First y.
   * @param x2 Second x.
   * @param y2 Second y.
   * @return The distance between (x1, y1) and (x2, y2).
   */
  public double getRange(double x1, double y1, double x2, double y2) {
    double xo = x2 - x1;
    double yo = y2 - y1;
    return Math.sqrt(xo * xo + yo * yo);
  }

  /**
   * Gets the absolute bearing between to x,y coordinates.
   * @param x1 First x.
   * @param y1 First y.
   * @param x2 Second x.
   * @param y2 Second y.
   * @return The absolute bearing from (x1, y1) to (x2, y2).
   */
  public double absbearing(double x1, double y1, double x2, double y2) {
    double xo = x2 - x1;
    double yo = y2 - y1;
    double h = getRange(x1, y1, x2, y2);
    if (xo > 0 && yo > 0) {
      return Math.asin(xo / h);
    }
    if (xo > 0 && yo < 0) {
      return Math.PI - Math.asin(xo / h);
    }
    if (xo < 0 && yo < 0) {
      return Math.PI + Math.asin(-xo / h);
    }
    if (xo < 0 && yo > 0) {
      return 2.0 * Math.PI - Math.asin(-xo / h);
    }
    return 0;
  }

  /**
   * When a robot is detected, add it to our enemies list. 
   * @param e The ScannedRobotEvent. 
   */
  public void onScannedRobot(ScannedRobotEvent e) {
    Enemy en;
    if (enemies.containsKey(e.getName())) {
      en = enemies.get(e.getName());
    }
    else {
      en = new Enemy();
      enemies.put(e.getName(), en);
    }
    // Get the absolute bearing to the point where the bot is
    double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2 * Math.PI);
    // Initialize info about this enemy robot.
    //double h = normaliseBearing(e.getHeadingRadians() - en.heading);
    //h = h / (getTime() - en.ctime);
    en.x = getX() + Math.sin(absbearing_rad) * e.getDistance(); // works out the x coordinate of
    // where the target is
    en.y = getY() + Math.cos(absbearing_rad) * e.getDistance(); // works out the y coordinate of
    // where the target is
    en.heading = e.getHeadingRadians();
    en.ctime = getTime(); // game time at which this scan was produced
    en.speed = e.getVelocity();
    en.distance = e.getDistance();
    en.live = true;
    
    // Now decide if the scanned enemy robot will be the robot that we target. 
    // We target the closest enemy that is alive. 
    if (targetEnemy == null) {
      targetEnemy = en;
    }
    else if ((en.distance < targetEnemy.distance) || (!targetEnemy.live)) {
      targetEnemy = en;
    }
  }

  /**
   * Get rid of dead robots from our list of enemies. 
   * @param e The RobotDeathEvent.
   */
  public void onRobotDeath(RobotDeathEvent e) {
    Enemy en = enemies.get(e.getName());
    en.live = false;
  }

  /**
   * Provides state information about all robots in this match.
   * @author Philip Johnson
   */
  private static class Enemy {
    public double heading, speed, x, y, distance;
    public long ctime; // game time that the scan was produced
    public boolean live; // is the enemy alive?

    /**
     * Returns a point indicating where we believe this enemy will be in the future. 
     * @param when The future time.
     * @return The future location. 
     */
    public Point2D.Double guessPosition(long when) {
      double diff = when - ctime;
      double newY = y + Math.cos(heading) * speed * diff;
      double newX = x + Math.sin(heading) * speed * diff;

      return new Point2D.Double(newX, newY);
    }
  }

  /** 
   * Holds the x, y, and strength info of a gravity point. 
   */
  private static class GravPoint {
    public double x, y, power;

    /**
     * Creates a new GravPoint class indicating a source of repulsive force for our robot.
     * @param pX The x coordinate.
     * @param pY The y coordinate.
     * @param pPower The repulsive force to be associated with this object. 
     */
    public GravPoint(double pX, double pY, double pPower) {
      x = pX;
      y = pY;
      power = pPower;
    }
  }
}
