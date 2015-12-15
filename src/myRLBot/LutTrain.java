package myRLBot;

import java.util.Random;

public class LutTrain {
	public static final double ALPHA = 0.1;
	public static final double GAMMA = 0.9;
	public static final double EPSILON = 0.1;
	
	private static final boolean isQLearning = true;
	private double diff;
	private int prevState;
	private int prevAction;

	private LUT table;

	public LutTrain(LUT table) {
		this.table = table;
	}

	public double lutTrain(int state, int action, double imRwd) {
		if (isQLearning) {
			/* Q-Learning (off-policy) learning */
			double prevQValue = table.getQValue(prevState, prevAction);
			double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * table.getMaxQValue(state) - prevQValue);
			diff = newQValue - prevQValue;
			System.out.println("=== train() ===");
			System.out.println("prevState: " + prevState + ", prevAction: " + prevAction);
			System.out.println("Old Q-Value: " + prevQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - prevQValue) + " and " + diff);
			/* Bootstrapping - backup new Q-value to old ones */
			table.setQValue(prevState, prevAction, newQValue);
		} else {
			/* SARSA (on-policy) learning */
			double curQValue = table.getQValue(state, action);
			double prevQValue = table.getQValue(prevState, prevAction);
			double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * curQValue - prevQValue);
			// System.out.println("Old Q-Value: " + prevQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - prevQValue));
			table.setQValue(prevState, prevAction, newQValue);
		}
		/* store previous <state,action> pair for next train */
		prevState = state;
		prevAction = action;
		
		return diff;
	}
    
	public int selectAction(int state) {
	  	Random rn = new Random();
	  	if(rn.nextDouble() <= EPSILON) {
	  		/* random move */
		  	int ranNum = rn.nextInt(Action.NumActions);	
		  	return ranNum;
	  	} else {
		  	/* greedy move */
		  	return table.getBestAction(state);
	  	}
  	}
}