package chengli;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class xorMain {

	/* input patterns for XOR problem */
    static final double binary_inputs[][] = { { 1.0, 1.0 }, 
    								 		  { 1.0, 0.0 },
    								  		  { 0.0, 1.0 }, 
    								  		  { 0.0, 0.0 } };
    /*
    static final double bipolar_inputs[][] = { { 1.0, 1.0 }, 
    								   		   { 1.0, -1.0 }, 
    								   		   { -1.0, 1.0 }, 
    								   		   { -1.0, -1.0 } };
    */
    static double bipolar_inputs[][];
    /* expected XOR outputs given above training data */
    static final double expectedBinaryOutputs[] = { 0.0, 1.0, 1.0, 0.0 };
    //static final double expectedBipolarOutputs[] = { -1.0, 1.0, 1.0, -1.0 };
    static double expectedBipolarOutputs[];
    static final int MAX_EPOCHS = 1000;
	static final double CRITERIA = 0.05;
	
	public static void main(String[] args) {		
		
		final boolean isBinary = false;
		final boolean DEBUG = false;
		final boolean isRLNN = true;
		boolean isLoaded = true;
		
		double err = 0.0;
		double totalError = 0.0;
		int cnt = 0;
		
		bipolar_inputs = new double[2880][7]; //6 states + 1 action
		expectedBipolarOutputs = new double[2880];
		
		NeuralNet nn = new NeuralNet(7, 10, isLoaded);
		//NeuralNet nn = new NeuralNet(2, 4, isLoaded);
		 
		String nnInputFile = "/Users/chwlo/Documents/workspace/EECE592/bin/myRLBot/MyRLBot.data/nn.txt";
		BufferedReader br = null;
		String line = "";
		String delimiter = " ";
		String nnErrData = "nnErr.txt";
		String nnWgtData = "nnWeight.txt";
		
		File errFile = new File(nnErrData);
		File weightFile = new File(nnWgtData);
		
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(errFile));
			if (isRLNN) {
				br = new BufferedReader(new FileReader(nnInputFile));
				while ((line = br.readLine()) != null) {
			        // use space as separator
					//cnt++;
					String[] nnStates = line.split(delimiter);
					//System.out.print(nnStates.length);
					// length = 8 
					for (int i = 0; i < nnStates.length; i++) {
						//System.out.print(nnStates[i]+" ");
						if (i < nnStates.length - 1) {
							//System.out.print("["+i+"] ");
							bipolar_inputs[cnt][i] = Double.parseDouble(nnStates[i]);
							//System.out.print(bipolar_inputs[cnt][i]+" ");
						}
						if (i == nnStates.length-1) {
							//System.out.print("["+i+"] ");
							expectedBipolarOutputs[cnt] = Double.parseDouble(nnStates[i]);
							/* check values that has special string 'E', e.g.  2.579793928069868E-4 */
							/*
							if (cnt == 257) {
								System.out.print("CHECK: "+expectedBipolarOutputs[cnt]+"\n");
								System.out.print("CHECK: "+expectedBipolarOutputs[cnt]*1E4+"\n");
							}
							*/
							//System.out.print(" "+expectedBipolarOutputs[cnt]);
						}
					}
					//System.out.print("\n");
					
					cnt++;
					//System.out.println(cnt);
					//bipolar_inputs[line][i] = Double.parseDouble(line.split(delimiter));
				}
			}
			
			/* LOAD TEST */
			if (isLoaded) {
				nn.load(nnWgtData);
			}
			
			/* train neural network */			
			for (int i=0; i < MAX_EPOCHS; i++) {
				//System.out.println("binary input length"+binary_inputs.length);
				//System.out.println("bipolar input length"+bipolar_inputs.length);
				/* go through each input sequences */
				// ORIGINAL: xor only has 4 input sequences 
				int size = 0;
				if (isRLNN) {
					size = bipolar_inputs.length;
				} else {
					size = binary_inputs.length;
				}
					
				for (int p=0; p < size; p++) {
					double result = 0.0;
					if (DEBUG)
						System.out.println("\n\t\t*** Input Sequence "+p+" ***\n");
					if (isBinary) {
						result = nn.train(binary_inputs[p], expectedBinaryOutputs[p], true);
						if (DEBUG)
							System.out.println("##### EPOCH: " + i + 
									" | result: " + result + 
									"| expected: " + expectedBinaryOutputs[p]);
						err = (double) Math.pow((result - expectedBinaryOutputs[p]), 2);
					} else {
						//if (p == 0) {
							//System.out.println("check 1st input sequence");
						/*
							for (int a = 0; a < 7; a++) {
								System.out.print(bipolar_inputs[p][a]+" ");
							}
							System.out.println();
							*/
						//}
						result = nn.train(bipolar_inputs[p], expectedBipolarOutputs[p], false);
						if (DEBUG)
							System.out.println(" ##### EPOCH: " + i + 
									" | result: " + result + 
									"| expected: " + expectedBipolarOutputs[p]);
						err = (double) Math.pow((result - expectedBipolarOutputs[p]), 2);
					}	
					totalError += err;
				}
				
				totalError *= 0.5;
				//if (i%100 == 0)
				System.out.println("##### EPOCH: " + i + " | TotalError: " + totalError);
				writer.println(i + " | " + totalError);
				
				if (totalError < CRITERIA) {
					System.out.println("##### DONE! ");
					break;
				}
			}
			nn.save(weightFile);
			System.out.println("##### DONE!");
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}