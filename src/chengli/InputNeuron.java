package chengli;

public class InputNeuron extends Neuron {
    
	/* inherited from normal neuron */
	public InputNeuron() {
        super();
    }
    
	/* inherited from bias neuron */
    public InputNeuron(int i) {
        super(i);
    }

    /* set input values */
    public void input(double d) {
        output = d;
    }
}