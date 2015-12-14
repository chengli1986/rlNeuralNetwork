package chengli;

import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NeuralNet {

	/* Best configuration for RL NN training progress */
    public static final double STEPSIZE = 0.01;
    //public static final double STEPSIZE = 0.2;
    public static final double MOMENTUM = 0.9;
    public static final boolean DEBUG = false;

    /* neural network layers */
    InputNeuron[] input;
    HiddenNeuron[] hidden;
    OutputNeuron output;
    double[] nnWeights;
    
	/**
	 * Constructor - NeuralNet
	 * @param inputs 		number of inputs
	 * @param hiddenTotal 	total number of hidden neurons
	 */
    public NeuralNet(int inputs, int hiddenTotal, boolean load) {

    	/* add bias to input and hidden layers */
        input = new InputNeuron[inputs+1];
        hidden = new HiddenNeuron[hiddenTotal+1];
        if (DEBUG) {
        	System.out.println("input size: "+input.length);
        	System.out.println("hidden size: "+hidden.length);
        }
        
        /* instantiate input neurons */
        for (int i = 0; i < input.length-1; i++) {
            input[i] = new InputNeuron();
            input[i].setNeuronId(i);
        }
        
        /* instantiate hidden neurons */
        for (int i = 0; i < hidden.length-1; i++) {
            hidden[i] = new HiddenNeuron();
            hidden[i].setNeuronId(i);
        }

        /* instantiate bias neurons */
        input[input.length-1] = new InputNeuron(1);
        input[input.length-1].setNeuronId(input.length-1);
        hidden[hidden.length-1] = new HiddenNeuron(1);
        hidden[hidden.length-1].setNeuronId(hidden.length-1);

        /* instantiate output neuron */
        output = new OutputNeuron();
        output.setNeuronId(0);

        /* create connections: input layer to hidden layer */
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < hidden.length-1; j++) {
                Connection c = new Connection(input[i], hidden[j], load);
                input[i].addConnection(c);
                hidden[j].addConnection(c);
            }
        }
        
        /* create connections: hidden layer to output layer */
        for (int i = 0; i < hidden.length; i++) {
            Connection c = new Connection(hidden[i], output, load);
            hidden[i].addConnection(c);
            output.addConnection(c);
        }

    }

	/**
	 * This method process forward feed for each neuron in NN
	 * and output the weighted sum of each neurons at all layers
	 * @param input		the input vector
	 * @param flag	 	the flag to specify data representation (binary vs. bipolar)
	 * @return output 	weighted sum of each neurons at all layers
	 */
    public double forwardPropagation(double[] patterns, boolean flag) {
        
        /* take input patterns */
        for (int i = 0; i < patterns.length; i++) {
            input[i].input(patterns[i]);  
        }
        
        /* weighted sum of each input patterns
         * & their corresponding weights 
         */
        for (int i = 0; i < hidden.length-1; i++) {
            hidden[i].calcOutput(flag);
        }

        /* weighted sum of output neuron */
        output.calcOutput(flag);
        
        /* weighted sum results */
        return output.getOutput();
    }

	/**
	 * This method applies backpropagation to NN given the input patterns
	 * and expected output values and a flag specifying
	 * which activation function representation to be used
	 * @param input		the input vector
	 * @param answer 	the expected output values
	 * @param flag	 	the flag to specify data representation (binary vs. bipolar)
	 * @return output 	results of NN given each input pattern
	 */
    public double train(double[] inputs, double answer, boolean flag) {
        
    	double deltaOutput;
    	double deltaHidden;
    	double result = forwardPropagation(inputs, flag);
        
      	/* binary vs. bipolar */
    	if (flag) {
    		deltaOutput = result*(1-result)*(answer-result);
    	} else {
    		deltaOutput = 0.5*(1+result)*(1-result)*(answer-result);
    	}
    	
    	/* backpropagation: adjust output neuron weights */
        ArrayList connections = output.getConnections();
        
        if (DEBUG) {
        	System.out.println("output neuron connections: " + connections.size());
        	System.out.println("=== OUTPUT LAYER ===");
        }
        /* loop through each weights to OUTPUT neuron */
        for (int i = 0; i < connections.size(); i++) {
            Connection c = (Connection) connections.get(i);
            if (DEBUG)
            	System.out.println("--X hidden["+c.getFrom().getNeuronId()+"] to output["+c.getTo().getNeuronId()+"] weight: "+c.getWeight());
            Neuron neuron = c.getFrom();
            double output = neuron.getOutput();
            double deltaWeight = output*deltaOutput;
            c.adjustWeight(STEPSIZE*deltaWeight+MOMENTUM*c.getPrevWeightChange());
            if (DEBUG)
            	System.out.println("--> hidden["+c.getFrom().getNeuronId()+"] to output["+c.getTo().getNeuronId()+"] weight: "+c.getWeight());
        }
        
        /* backpropagation: adjust hidden neuron weights - hidden neurons = 4+1*/
        /* loop through each HIDDEN neuron */
        if (DEBUG)
        	System.out.println("=== HIDDEN LAYER ===");
        for (int i = 0; i < hidden.length; i++) {
        	double sum  = 0.0;
        	connections = hidden[i].getConnections();
            
            if (DEBUG) {
            	//System.out.println("hidden neuron connections: " + connections.size());
            }
            
            /* loop through each weights to THIS hidden neuron */
            for (int j = 0; j < connections.size(); j++) {
                Connection c = (Connection) connections.get(j);
                /* weighted sum error signal of the errors at units above */
                if (c.getFrom() == hidden[i]) {
                    sum += c.getWeight()*deltaOutput;
                }
            }    

            /* update error signal of THIS hidden neuron */
            for (int j = 0; j < connections.size(); j++) {
                Connection c = (Connection) connections.get(j);               
                if (c.getTo() == hidden[i]) {
                    double output = hidden[i].getOutput();
                    if (DEBUG)
                    	System.out.println("--X input["+c.getFrom().getNeuronId()+"] to hidden["+c.getTo().getNeuronId()+"] weight: "+c.getWeight());
                    /* binary vs. bipolar */
                	if (flag) {
                		deltaHidden = output*(1-output);
                	} else {
                		deltaHidden = 0.5*(1 - output)*(1 + output);
                	}
                    deltaHidden *= sum;
                    Neuron neuron = c.getFrom();
                    double deltaWeight = neuron.getOutput()*deltaHidden;
                    c.adjustWeight(STEPSIZE*deltaWeight+MOMENTUM*c.getPrevWeightChange());
                    if (DEBUG)
                    	System.out.println("--> input["+c.getFrom().getNeuronId()+"] to hidden["+c.getTo().getNeuronId()+"] weight: "+c.getWeight());
                }
            } /* end of for loop */
        } /* end of hidden neuron for loop */

        return result;
    }
    
    public void save(File argFile) {
    	
    	File temp = argFile; 
    	boolean remove = false;
    	if (remove) {
    		while (temp.exists()) {
    			System.out.println("File already exists, adding surfix");
    			String path = temp.getAbsolutePath();
    			String name = path.split("\\.")[0];
    			temp = new File(name + "-new." + path.split("\\.")[1]);
    		} 
    	} try {
    		PrintWriter pw = new PrintWriter(new FileOutputStream(temp));
    		// first two entries are the input layer size and hidden layer size pw.println(inputLayer.size());
    		//pw.println("=== OUTPUT LAYER ===");
    		ArrayList connections = output.getConnections();
    		for (int i = 0; i < connections.size(); i++) {
    			Connection c = (Connection) connections.get(i);
    	        //Neuron neuron = c.getFrom();
    	        //pw.println("--> hidden["+c.getFrom().getNeuronId()+"] to output["+c.getTo().getNeuronId()+"] weight: "+c.getWeight());
    	        pw.println(c.getWeight());
    	    }
    		//pw.println("=== HIDDEN LAYER ===");
    		for (int i = 0; i < hidden.length; i++) {
    			connections = hidden[i].getConnections();
    			for (int j = 0; j < connections.size(); j++) {
                    Connection c = (Connection) connections.get(j);               
                    if (c.getTo() == hidden[i]) {
                        //Neuron neuron = c.getFrom();
                        //pw.println("--> input["+c.getFrom().getNeuronId()+"] to hidden["+c.getTo().getNeuronId()+"] weight: "+c.getWeight());
                        pw.println(c.getWeight());
                    }
                } /* end of for loop */
    		}
    		pw.close();
    	} catch (FileNotFoundException e) {
    		System.out.println("could not save the weight to a tmp.txt file: " + e); 
    		e.printStackTrace();
    	}
    }
    
    public void load(String argFileName) throws IOException {
    	
    	File temp = new File(argFileName);
    	int size = output.getConnections().size()+(input.length)*(hidden.length-1);
    	if (DEBUG) {
    		System.out.println("output connections: "+output.getConnections().size());
    		System.out.println("input size: "+input.length);
    		System.out.println("hidden size: "+hidden.length);
    		System.out.println("Size of weight array: "+size);
    	}
    	nnWeights = new double[size];
    	
    	if (!temp.exists()) {
    		throw new IOException("File is not found...");
    	}
    	FileInputStream fin = new FileInputStream(temp);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
    	try {
    		String line;
    		int count = 0;
    		//line = reader.readLine();
    		while ((line = reader.readLine()) != null) {
    			nnWeights[count] = Double.parseDouble(line);
        		//System.out.println(nnWeights[count]);
    			count++;
    		}
    		
    		//System.out.println("=== OUTPUT LAYER ===");
    		ArrayList connections = output.getConnections();
    		//System.out.println("connection size: "+connections.size());
    		for (int i = 0; i < connections.size(); i++) {
    			Connection c = (Connection) connections.get(i);
    			if (DEBUG)
    				System.out.println("OUTPUT LAYER setting "+nnWeights[i]);
    	        c.setWeight(nnWeights[i]);
    	    }
    		//System.out.println("=== HIDDEN LAYER ===");
    		for (int i = 0; i < hidden.length; i++) {
    			connections = hidden[i].getConnections();
    			//System.out.println("connection size: "+connections.size());
    			for (int j = 0; j < connections.size(); j++) {
                    Connection c = (Connection) connections.get(j);               
                    if (c.getTo() == hidden[i]) { // this reduces the connections to 3 output is not counted here
                    	if (DEBUG)
                    		System.out.println("HIDDEN LAYER setting "+nnWeights[output.getConnections().size()+i*(connections.size()-1)+j]);
                        c.setWeight(nnWeights[output.getConnections().size()+i*(connections.size()-1)+j]);
                    }
                }
    		}
    		
    	} catch (Exception e) {
    		throw new IOException("Errors in input file: " + e);
    	} finally {
    		fin.close();
    		reader.close();
    	}
    	
    }
}