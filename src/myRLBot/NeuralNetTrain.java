package myRLBot;

import java.io.IOException;
import java.util.Random;
import java.io.File;

import chengli.NeuralNet;


public class NeuralNetTrain {
	public static final double ALPHA = 0.1;
	public static final double GAMMA = 0.9;
	public static final double EPSILON = 0.9;
	private static final boolean isQLearning = true;
	
	private double diff;
	private NeuralNet nn;

	public NeuralNetTrain() {
		nn = new NeuralNet(7, 10, true, 0.01, 0.9);
	}

	public double nnTrain(double[] inputs, double imRwd) {
		if (isQLearning) {
			/* Q-Learning (off-policy) learning */
			double prevQValue = nn.outputFor(inputs, false);
			double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * getMaxQValue(inputs) - prevQValue);
			diff = newQValue - prevQValue;
			//System.out.println("=== nnTrain() ===");
			//System.out.println("prevState: " + prevState + ", prevAction: " + prevAction);
			//System.out.println("Old Q-Value: " + prevQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - prevQValue) + " and " + diff);
			nn.train(inputs, newQValue, false);
		}
		return diff;
	}
    
	public int selectAction(double[] inputs) {
	  	Random rn = new Random();
	  	if(rn.nextDouble() <= EPSILON) {
	  		/* random move */
		  	int ranNum = rn.nextInt(Action.NumActions);
		  	return ranNum;
	  	} else {
		  	/* greedy move */
	  		return getBestAction(inputs);
	  	}
  	}
	
    public double getMaxQValue(double[] inputs) {
    	
		double maxQ = Double.NEGATIVE_INFINITY;
		double[] temp;
		temp = inputs;
		/* go though 5 actions with that particular input sequence */
		//for (int i = 0; i < inputs[6]; i++) {
		for (int i = 0; i < Action.NumActions; i++) {
			// need to overwrite action element [6] with all 5 actions
			// then get only outputs from neural network
			// no backpropagation needed here
			temp[Action.NumActions] = i;
			double tempQ = nn.outputFor(temp, false);
			if (tempQ > maxQ)
				maxQ = tempQ;
		}
		return maxQ;
	}
    
    public int getBestAction(double[] inputs) {
    	
		double maxQ = Double.NEGATIVE_INFINITY;
		int bestAction = 0;
		double[] temp;
		temp = inputs;
		/* go though 5 actions with that particular input sequence */
		//for (int i = 0; i < inputs[6]; i++) {
		for (int i = 0; i < Action.NumActions; i++) {
			// need to overwrite action element [6] with all 5 actions
			// then get only outputs from neural network
			// no backpropagation needed here
			temp[Action.NumActions] = i;
			double tempQ = nn.outputFor(temp, false);
			if (tempQ > maxQ) {
				maxQ = tempQ;
				bestAction = i;
			}
		}
		return bestAction;
	}
    
    public void nnSave(File f) {
    	nn.save(f);
    }
    
    public void nnLoad(String s) {
    	try {
			nn.load(s);
		} catch (IOException e) {
			e.getStackTrace();
		}
    }
}