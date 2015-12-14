package myRLBot;

public class Action {
	public static final int RobotAhead = 0;
	public static final int RobotBack = 1;
	public static final int RobotTurnLeft = 2;
	public static final int RobotTurnRight = 3;
	public static final int RobotFire = 4;
  
	public static final int NumActions = 5;
    
	/* small moving distance is better; especially at wall */
	public static final double RobotMoveDistance = 300.0;
	public static final double RobotMoveAddDistance = 500.0;
	public static final double RobotTurnDegree = 45.0;
}
