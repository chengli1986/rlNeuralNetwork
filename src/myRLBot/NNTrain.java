package myRLBot;

import java.io.IOException;
import java.util.Random;

import chengli.NeuralNet;


public class NNTrain {
	public static final double ALPHA = 0.1;
	public static final double GAMMA = 0.9;
	public static final double EPSILON = 0.9;
	
	private static final boolean isQLearning = true;
	private double diff;
	private int prevState;
	private int prevAction;
	private NeuralNet nn;
	private String weightsFile = "/Users/chwlo/Documents/workspace/EECE592/data/nnWeight.txt";

	private LUT table;

	public NNTrain() {
		nn = new NeuralNet(7, 10, true);
		try {
			nn.load(weightsFile);
		} catch (IOException e) {
			e.getStackTrace();
		}
	}

	public double train(double[] inputs, double imRwd) {
		if (isQLearning) {
			/* Q-Learning (off-policy) learning */
			double prevQValue = nn.outputFor(inputs, false);
			//double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * table.getMaxQValue(state) - prevQValue);
			// code is wrong...
			double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * prevQValue - prevQValue);
			diff = newQValue - prevQValue;
			//System.out.println("=== train() ===");
			//System.out.println("prevState: " + prevState + ", prevAction: " + prevAction);
			//System.out.println("Old Q-Value: " + prevQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - prevQValue) + " and " + diff);
			/* Bootstrapping - backup new Q-value to old ones */
			//table.setQValue(prevState, prevAction, newQValue);
			nn.train(inputs, newQValue, false);
		}
		/* store previous <state,action> pair for next train */
		//prevState = state;
		//prevAction = action;
		
		return diff;
	}
    
	public int selectAction() {
	  	Random rn = new Random();
	  	if(rn.nextDouble() <= EPSILON) {
	  		/* random move */
		  	int ranNum = rn.nextInt(Action.NumActions);
		  	/*
		  	double nnActionIdx = 0.0;
		  	if (ranNum == 0)
		  		nnActionIdx = -1.0;
		  	if (ranNum == 1)
		  		nnActionIdx = -0.5;
		  	if (ranNum == 2)
		  		nnActionIdx = 0.0;
		  	if (ranNum == 3)
		  		nnActionIdx = 0.5;
		  	if (ranNum == 4)
		  		nnActionIdx = 1.0;
		  	return nnActionIdx;
		  	*/
		  	return ranNum;
	  	} else {
		  	/* greedy move */
		  	// always fire...
	  		return 4;
	  	}
  	}
}