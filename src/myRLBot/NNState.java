package myRLBot;

import java.util.Random;

public class NNState {
	/*
	public static final int NumHeading = 4;
	public static final int NumTargetDistance = 3;
	*/
    public static final int NumTargetBearing = 4;
    /*
    public static final int isHitWall = 2;
    public static final int isHitByBullet = 2;
    public static final int NumEnergyLvl = 3;
    public static final int NumStates;
    public static final double FullAngle = 360.0;
    public static final double NormalizeFactor = 150.0;
    public static final int stateTable[][][][][][];
    public static final double nnInputTable[][];
    public static final int nnNumInputs;
    public static final boolean NORMALIZED = true;
    */

    /* state pre-processing */
    
    /* quantize energy level */
    public static double getEnergy(double energy) {
    	double newEnergy;
    		
    	if (energy < 0 )
    		newEnergy = -1.0;
    		
    	if (energy < 40.0)
    		newEnergy = -0.33;
    	else if (energy >= 40.0 && energy < 80.0)
    		newEnergy = 0.33;
    	else
    		newEnergy = 1.0;
    	return newEnergy;
    }
    
    /* get robot headings in degrees; normalize to 45 deg precision*/
    public static double getHeading(double heading) {
    	double newHeading;
    	if (heading >=0.0 && heading < 90.0)
    		newHeading = -1.0;
    	else if (heading >= 90.0 && heading < 180.0)
    		newHeading = -0.33;
    	else if (heading >= 180.0 && heading < 270.0)
    		newHeading = 0.33;
    	else if (heading >= 270.0 && heading < 360.0)
    		newHeading = 1.0;
    	else 
    		newHeading = -1.0;
    	return newHeading;    		
    }
    /* linear quantization of distance */
    public static double getTargetDistance(double value) {
    	double distance;
    	if (value < 0)
    		distance = -1.0;
    	
    	if (value >= 0 && value < 150)
    		distance = -1.0;
    	else if (value >= 150 && value < 300)
    		distance = 0.0;
    	else
    		distance = 1.0;
    	
    	return distance;
    }

	public static double getTargetBearing(double bearing) {
		double twoPI = Math.PI * 2;
		int index = 0;
		double result = -1.0;
	    // http://osdir.com/ml/java-robocode/2010-03/msg00003.html
		if (bearing < 0.0)  
			bearing = twoPI + bearing;  
	    
	    double rad = twoPI / NumTargetBearing; // quantatized into 4
	    double newBearing = bearing + rad / 2;  // advance by 1/8 rads
	    
	    if (newBearing > twoPI)  
	    	newBearing -= twoPI;
	    index = (int)(newBearing/rad);
	    if (index == 0)
	    	result = -1.0;
	    else if (index == 1)
	    	result = -0.33;
	    else if (index == 2)
	    	result = 0.33;
	    else if (index == 3)
	    	result = 1.0;
	    //System.out.println("real bearing: " + bearing + 
	    //		" quant bear: " + (int)(newBearing/rad));
	    return result;
	}
	
	public static double nnSelectAction() {
	  	Random rn = new Random();
	  	if(rn.nextDouble() <= 0.9) {
	  		/* random move */
		  	int ranNum = rn.nextInt(Action.NumActions);
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
	  	} else {
		  	/* greedy move */
		  	// always fire...
	  		return 1.0;
	  	}
  	}
}
