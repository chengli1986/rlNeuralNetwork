package myRLBot;

import java.io.File;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import robocode.RobocodeFileOutputStream;
import java.util.Random;

public class LUT {
	private double[][] lut;
	private static final boolean zeroInit = true;
	//private int cnt = 0;
	
	public LUT() {
		lut = new double[State.NumStates][Action.NumActions];
		/* dummy init as it will be overwritten by load() */
		init();
		//System.out.println("state: "+State.NumStates);
		//System.out.println("actions: "+Action.NumActions);
		//System.out.println("rows: "+lut.length);
		//System.out.println("cols: "+lut[0].length);
	}

	private void init() {
		for (int i = 0; i < State.NumStates; i++) {
			for (int j = 0; j < Action.NumActions; j++) {
				/* init to double can easily overflow the data file... */
				//cnt++;
				if (zeroInit) {
					lut[i][j] = 0.0;
				} else {
					Random nn = new Random();
					lut[i][j] = nn.nextInt(4)-2;
				}
			}
		}
		//System.out.println("entry: "+cnt);
	}

	public double getMaxQValue(int state) {
		double max = Double.NEGATIVE_INFINITY;
		/* go though all available actions in that state */
		for (int i = 0; i < lut[state].length; i++) {
			if (lut[state][i] > max)
				max = lut[state][i];
		}
		return max;
	}

	public int getBestAction(int state) {
		double max = Double.NEGATIVE_INFINITY;
		int bestAction = 0;
		for (int i = 0; i < lut[state].length; i++) {
			double qValue = lut[state][i];
			/* find the best Q-Value */
			if (qValue > max) {
				max = qValue;
				bestAction = i;
			}
		}
		return bestAction;
	}

	public double getQValue(int state, int action) {
		return lut[state][action];
	}

  	public void setQValue(int state, int action, double value) {
  		lut[state][action] = value;
  	}

  	public void loadFile(File file) {
  		BufferedReader reader = null;
  		try {
  			reader = new BufferedReader(new FileReader(file));
  			for (int i = 0; i < State.NumStates; i++)
  				for (int j = 0; j < Action.NumActions; j++)
  					lut[i][j] = Double.parseDouble(reader.readLine());
  		} catch (IOException e) {
  			System.out.println("IOException trying to open file: " + e);
  			init();
  		} catch (NumberFormatException e) {
  			init();
  		} finally {
  			try {
  				if (reader != null)
  					reader.close();
  			} catch (IOException e) {
  				System.out.println("IOException trying to close file: " + e);
  			}
  		}
  	}

  	public void saveFile(File file) {
  		PrintStream writer = null;
  		try {
  			writer = new PrintStream(new RobocodeFileOutputStream(file));
  			for (int i = 0; i < State.NumStates; i++)
  				for (int j = 0; j < Action.NumActions; j++)
  					writer.println(new Double(lut[i][j]));
  			if (writer.checkError())
  				System.out.println("Could not save data to file...");
  			writer.close();
  		} catch (IOException e) {
  			System.out.println("IOException trying to save: " + e);
  		} finally {
  			try {
  				if (writer != null)
  					writer.close();
  			} catch (Exception e) {
  				System.out.println("Exception trying to close file: " + e);
  			}
  		}
  	}
  	
  	public void saveFileForNN(double[][] states, File file) {
  		PrintStream writer = null;
  		try {
  			writer = new PrintStream(new RobocodeFileOutputStream(file));
  			/* just for debug for file dump
  			for (int i = 0; i < 576; i++) {
  				for (int j = 0; j < 6; j++) {
  					writer.print(states[i][j]+" ");
  				}
  				writer.print("\n");
  			}
  			*/
  			
  			/* dump NN-friendly file for offline NN training */
  			for (int i = 0; i < State.NumStates; i++) {
  				for (int j = 0; j < Action.NumActions; j++) {
  					for (int k = 0; k < 6; k++) {
  						writer.print(states[i][k]+" ");
  					}
  					if (j == 0)
  						writer.print(+-1.0+" ");
  					if (j == 1)
  						writer.print(+-0.5+" ");
  					if (j == 2)
  						writer.print(+0.0+" ");
  					if (j == 3)
  						writer.print(+0.5+" ");
  					if (j == 4)
  						writer.print(+1.0+" ");
  					/* max is at 16 and min is at -5
  					 * normalized by 21
  					 *  */
  					writer.println(new Double(lut[i][j]/21));
  				}
  			}
  			
  			/* read friendly print */
  			/* DO NOT USE for normal operation
  			for (int i = 0; i < State.NumStates; i++) {
  				for (int j = 0; j < Action.NumActions; j++) {
  					writer.print(i+" [ ");
  					for (int k = 0; k < 6; k++)
  						writer.print(states[i][k]+" ");
  					writer.print("] "+j+" ");
  					writer.println(new Double(lut[i][j]));
  				}
  			}
  			*/
  			if (writer.checkError())
  				System.out.println("Could not save data to file...");
  			writer.close();
  		} catch (IOException e) {
  			System.out.println("IOException trying to save: " + e);
  		} finally {
  			try {
  				if (writer != null)
  					writer.close();
  			} catch (Exception e) {
  				System.out.println("Exception trying to close file: " + e);
  			}
  		}
  	}
  	
  	public void saveFileForNNVerify(double[][] states, File file) {
  		PrintStream writer = null;
  		try {
  			writer = new PrintStream(new RobocodeFileOutputStream(file));

  			for (int i = 0; i < State.NumStates; i++) {
  				for (int j = 0; j < Action.NumActions; j++) {
  					writer.print(i+" [ ");
  					for (int k = 0; k < 6; k++)
  						writer.print(states[i][k]+" ");
  					writer.print("] "+j+" ");
  					writer.println(new Double(lut[i][j]));
  				}
  			}
  			
  			if (writer.checkError())
  				System.out.println("Could not save data to file...");
  			writer.close();
  		} catch (IOException e) {
  			System.out.println("IOException trying to save: " + e);
  		} finally {
  			try {
  				if (writer != null)
  					writer.close();
  			} catch (Exception e) {
  				System.out.println("Exception trying to close file: " + e);
  			}
  		}
  	}
  	
  	public void saveFileForNNSimple(File file) {
  		PrintStream writer = null;
  		try {
  			writer = new PrintStream(new RobocodeFileOutputStream(file));
  			for (int i = 0; i < State.NumStates; i++) {
  				for (int j = 0; j < Action.NumActions; j++) {
  					writer.print(i+" "+j+" ");
  					writer.println(new Double(lut[i][j]));
  				}
  			}
  			if (writer.checkError())
  				System.out.println("Could not save data to file...");
  			writer.close();
  		} catch (IOException e) {
  			System.out.println("IOException trying to save: " + e);
  		} finally {
  			try {
  				if (writer != null)
  					writer.close();
  			} catch (Exception e) {
  				System.out.println("Exception trying to close file: " + e);
  			}
  		}
  	}
}