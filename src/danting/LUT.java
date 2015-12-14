package danting;

import java.awt.geom.*;

public class LUT{
	public int NumInputs;
	public int VariableFloor[]=new int[10]; 
	public int VariableCeiling[]=new int[10]; 
	public double LUTable[][];
	public static int NumStates;
	public int NumActions = 4;
	// State
	public static int Heading = 4;
	public static int TargetBearing = 4; 
	public static int TargetDistance = 10; 
	public static int HitByBullet = 2; 
	public static int HitWall = 2;
	public static int State[][][][][];

	// Q-learning
	public double LearningRate = 0.1; 
	public double DiscountRate = 0.9; 
	public double ExploitationRate = 0.1; 
	public boolean firstTime;
	public int OldState;
	public int OldAction;
	public double oldQ;
	public double newQ;

	// Action
	public static final int Ahead = 0; 
	public static final int Back = 1; 
	public static final int TurnLeft = 2; 
	public static final int TurnRight = 3;
	
	public static double RobotMoveDistance = 300.0;
	public static double RobotTurnDegree = 20.0;
	// Target
	String name;
	public double bearing; 
	public double head;
	public long ctime;
	public double speed; 
	public double x, y;
	public double distance; 
	public double changehead; 
	public double energy;
	
	public LUT() {
	
	}
	
	public void initialiseLUT(){
		LUTable=new double[NumStates][NumActions]; 
		for (int i=0;i<NumStates;i++) {
			 for (int j=0;j<NumActions;j++) { 
				LUTable[i][j]=0;
			}
		}
	}
	
	public double getMaxQValue(int state) {
		double MaxQ = 0.0;
		for (int i = 0; i < LUTable[state].length; i++) {
			if (LUTable[state][i] > MaxQ)
				MaxQ = LUTable[state][i];
		}
		return MaxQ;
	}
	
	public int getBestAction(int state) {
		double MaxQ = 0.0 ;
		
		int BestAction = 0;
		for (int i = 0; i < LUTable[state].length; i++) {
			if (LUTable[state][i] > MaxQ)
			{
				MaxQ = LUTable[state][i];
				BestAction = i;
			}
		}
		return BestAction; 
	}
	
	public double getQValue(int state, int action) {
		return LUTable[state][action]; 
	}
	
	public void setQValue(int state, int action, double value) {
		LUTable[state][action] = value; 
	}
	
	public double getTotalValue() {
		double sum = 0.0;
		for (int i = 0; i < NumStates; i++) {
			for (int j = 0; j < NumActions; j++) {
				sum = sum + LUTable[i][j]; 
			}
		}
		return sum; 
	}
	
	// Q-learning
	public void QLearning(int state, int action, double ImReward) {
		if (firstTime) 
			firstTime = false; 
		else {
			oldQ = getQValue(OldState, OldAction);
			newQ = (1 - LearningRate) * oldQ + LearningRate * (ImReward + DiscountRate * getMaxQValue(state)); // Q-Learning
			//newQ = (1 - LearningRate) * oldQ + LearningRate * (ImReward + DiscountRate *getQValue(state, action)); //SARSA
		
			System.out.println("State - " + OldState + 
							   ", Old Q-Value: " + oldQ + 
							   ", New Q-Value: " + newQ + 
							   ", Different: " + (newQ - oldQ)); 
			setQValue(OldState, OldAction, newQ);
		}
		OldState = state; 
		OldAction = action;
	}
	
	public int selectAction(int state) {
		double randomNum = Math.random();
		if ( randomNum < ExploitationRate ) {
			return (int) (Math.random()*(NumActions-1)); 
		} else {
			return getBestAction(state);
		}
	}
	
	//State
	public static void makeState() { 
		NumStates=0;
		State = new int[Heading][TargetBearing][TargetDistance][HitByBullet][HitWall];
		for (int i1 = 0; i1 < Heading; i1++)
			for (int i2 = 0; i2 < TargetBearing; i2++)
				for (int i3 = 0; i3 < TargetDistance; i3++) 
					for (int i4 = 0; i4 < HitByBullet; i4++)
						for (int i5 = 0; i5 < HitWall; i5++){ 
							NumStates++;
							State[i1][i2][i3][i4][i5] = NumStates;
						}
	}
	
	public static int GetHeading(double heading) {
		double angle = 360 / Heading;
		double newHeading = heading + angle / 2; 
		if (newHeading > 360.0)
			newHeading -= 360.0;
		return (int)(newHeading / angle);
	}
	
	public static int GetTargetDistance(double value) {
		int distance = (int)(value / 100.0); 
		if (distance > TargetDistance - 1) 
			distance = TargetDistance - 1;
		return distance;
		}
	
	// Target
	public static int GetTargetBearing(double Bearing) {
		if (Bearing < 0)
			Bearing = Math.PI * 2 + Bearing;
		double angle = Math.PI * 2 / TargetBearing; 
		double newBearing = Bearing + angle / 2;
		if (newBearing > Math.PI * 2)
			newBearing -= Math.PI * 2;
		return (int)(newBearing / angle); 
	}
	
	public Point2D.Double guessPosition(long when) {
		double newY, newX;
		newX = x;
		newY = y;
		return new Point2D.Double(newX, newY);
	}
	
	public double guessX(long when) {
		long diff = when - ctime;
		return x; 
	}
	
	public double guessY(long when) {
		return y; 
	}


}

	
	

