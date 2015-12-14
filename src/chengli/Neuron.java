package chengli;

import java.util.ArrayList;

public class Neuron {

    protected double output;
    protected int id;
    protected ArrayList connections; 
    protected boolean isBias = false;

	/**
	 * Constructor - A normal neuron
	 */
    public Neuron() {
        output = 0;
        connections = new ArrayList();  
        isBias = false;
    }
    
	/**
	 * Constructor - A bias neuron
	 * @param i		bias neuron output
	 */
    public Neuron(int i) {
        output = i;
        connections = new ArrayList();
        isBias = true;
    }

	/**
	 * This method calculates weighted sum of 
	 * each neurons at all layers
	 * @param flag	 the flag to specify data representation (binary vs. bipolar)
	 */
    public void calcOutput(boolean flag) {
        if (isBias) {
            /* bias neuron doesn't need to calculate output; only for backpropagation */
        } else {
            double sum = 0.0;
        	double weightedSum = 0.0;
            double biasSum = 0.0;
            
            for (int i = 0; i < connections.size(); i++) {
                Connection c = (Connection) connections.get(i);
                Neuron source = c.getFrom();
                Neuron sink = c.getTo();
                /* is sink the current object, otherwise bypass */
                if (sink == this) {
                    if (source.isBias) {
                    	biasSum = source.getOutput()*c.getWeight();
                    } else {
                    	/* weighted sum */
                    	sum += source.getOutput()*c.getWeight();
                    }
                }
            }
            /* sigmoid activation of each weighted sums */
            weightedSum = biasSum + sum;
            output = sigmoid(weightedSum, flag);
        }
    }

	/**
	 * This method adds connection to current neuron
	 * @param c			connection object
	 */
    void addConnection(Connection c) {
        connections.add(c);
    }

	/**
	 * This method queries current neuron's connection
	 * @return			connection object
	 */
    public ArrayList getConnections() {
        return connections;
    }
    
	/**
	 * Return a bipolar or binary sigmoid activation of the input x
	 * @param x 	the input
	 * @param flag	the flag to specify data representation (binary vs. bipolar)
	 * @return f(x) = 2 / (1+e(-x)) - 1 or 1/(1+e(-x)
	 */
    public static double sigmoid(double x, boolean flag) {
    	/* true = binary/ false = bipolar */
    	if (flag) {
    		return 1.0/(1.0+(double) Math.exp(-x));
    	} else {
    		return (2.0/(1.0+(double) Math.exp(-x))-1.0);
    	}
    }
    
	/**
	 * This method queries current 
	 * neuron weighted sum
	 * @return		weighted sum
	 */
    double getOutput() {
        return output;
    }
    
    void setNeuronId(int index) {
    	id = index;
    }
    
    int getNeuronId() {
    	return id;
    }
}