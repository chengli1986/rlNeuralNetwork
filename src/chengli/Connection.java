package chengli;

public class Connection {

	private Neuron from;
    private Neuron to;
    private double weight;
    private double prevWeightChange;

    /* Constructor -  */
	/**
	 * Constructor - random weights connection [-0.5,0.5]
	 * @param source 	source neuron where the connection is associated
	 * @param sink	 	sink neuron where the connection is associated
	 */
    public Connection(Neuron source, Neuron sink, boolean isLoaded) {
    	from = source;
        to = sink;
        /*  IMPORTANT - change it to range -1, +1*/
        // looks like init within -0.5, 0.5 is better
        /* if isLoad is not set, then random weights
         * otherwise do nothing */
        if (!isLoaded) {
        	//weight = (double) Math.random()*1.0f-0.5f;
        	weight = (double) Math.random()*1.0-1.0;
        }
    }
    
    /**
	 * This method queries connection from which neuron
	 * @return	neuron object
	 */
    public Neuron getFrom() {
        return from;
    }
    
    /**
	 * This method queries connection to which neuron
	 * @return	neuron object
	 */
    public Neuron getTo() {
        return to;
    }  
    
    /**
	 * This method sets connection weight
	 * @param wgt	weight to be set
	 */
    public void setWeight(double wgt) {
    	weight = wgt;
    }
    
    /**
	 * This method queries connection weight
	 * @return	connection weight
	 */
    public double getWeight() {
        return weight;
    }

    /**
	 * This method adjusts connection weight
	 * and store previous weight change
	 * @param deltaWeight	delta weight to be adjusted
	 */
    public void adjustWeight(double deltaWeight) {
    	prevWeightChange = deltaWeight;
    	weight += deltaWeight;
    }
    
    /**
	 * This method queries previous weight change
	 * @return 	previous weight change
	 */
    public double getPrevWeightChange() {
        return prevWeightChange;
    }
}