package myRLBot;

public class State {
	public static final int NumHeading = 4; /* 4 headings */
	public static final int NumTargetDistance = 3; /* only 3 distance */
    public static final int NumTargetBearing = 4; /* 4 bearing */
    public static final int isHitWall = 2; /* Boolean */
    public static final int isHitByBullet = 2; /* Boolean */
    public static final int NumEnergyLvl = 3;
    public static final int NumStates;
    public static final double FullAngle = 360.0;
    public static final double NormalizeFactor = 150.0;
    public static final int stateTable[][][][][][];
    public static final double nnInputTable[][];
    public static final int nnNumInputs;
    public static final boolean SMART_CODE = true;
    public static final boolean NORMALIZED = true;

    static {
    	/* to check how many times this static function is called
    	 * because this is very expensive function...
    	 * GOOD thing is it's only called once at the beginning */
    	System.out.println("***CALLED***");
    	stateTable = new int[NumHeading][NumTargetDistance][NumTargetBearing][isHitWall][isHitByBullet][NumEnergyLvl];
    	
    	nnNumInputs = NumHeading*NumTargetDistance*NumTargetBearing*isHitWall*isHitByBullet*NumEnergyLvl;
    	nnInputTable = new double[nnNumInputs][6];
    	
    	int count = 0;
    	for (int i = 0; i < NumHeading; i++)
    		for (int j = 0; j < NumTargetDistance; j++)
    			for (int k = 0; k < NumTargetBearing; k++)    			
    				for (int l = 0; l < isHitWall; l++)
    					for (int m = 0; m < isHitByBullet; m++)
    						for (int n = 0; n < NumEnergyLvl; n++) {
    							if (NORMALIZED) {
    								if (i == 0)
    									nnInputTable[count][0] = -1.0;
    								if (i == 1)
    									nnInputTable[count][0] = -0.33;
    								if (i == 2)
    									nnInputTable[count][0] = 0.33;
    								if (i == 3)
    									nnInputTable[count][0] = 1.0;
	    							
	    							if (j == 0)
	    								nnInputTable[count][1] = -1.0;
	    							if (j == 1)
	    								nnInputTable[count][1] = 0.0;
	    							if (j == 2)
	    								nnInputTable[count][1] = 1.0;
	    							
	    							if (k == 0)
	    								nnInputTable[count][2] = -1.0;
	    							if (k == 1)
	    								nnInputTable[count][2] = -0.33;
	    							if (k == 2)
	    								nnInputTable[count][2] = 0.33;
	    							if (k == 3)
	    								nnInputTable[count][2] = 1.0;
	    							
	    							if (l == 0)
	    								nnInputTable[count][3] = -1.0;
	    							if (l == 1)
	    								nnInputTable[count][3] = 1.0;
	    							
	    							if (m == 0)
	    								nnInputTable[count][4] = -1.0;
	    							if (m == 1)
	    								nnInputTable[count][4] = 1.0;
	    							
	    							if (n == 0)
	    								nnInputTable[count][5] = -1.0;
	    							if (n == 1)
	    								nnInputTable[count][5] = 0.0;
	    							if (n == 2)
	    								nnInputTable[count][5] = 1.0;
	    							count++;
    							} else {
	    							nnInputTable[count][0] = i;
	    							nnInputTable[count][1] = j;
	    							nnInputTable[count][2] = k;
	    							nnInputTable[count][3] = l;
	    							nnInputTable[count][4] = m;
	    							nnInputTable[count][5] = n;
	    							count++;
    							}
    						}
    	
    	int cnt = 0;
    	for (int i = 0; i < NumHeading; i++)
    		for (int j = 0; j < NumTargetDistance; j++)
    			for (int k = 0; k < NumTargetBearing; k++)    			
    				for (int l = 0; l < isHitWall; l++)
    					for (int m = 0; m < isHitByBullet; m++)
    						for (int n = 0; n < NumEnergyLvl; n++)
    							stateTable[i][j][k][l][m][n] = cnt++;
    	
    	/* total number of states are 576
    	 * ok, here is the answer
    	 * for example,
    	 * heading=0 distance=0 targetBearing=2 isHitWall=0 isHitByBullet=0 energy=2 state=26
    	 * it's calculated as index*2*2*3+2, where index is targetBearing=2 and last 2 is energy
    	 * therefore 2*2*2*3+2=26
    	 * another example is
    	 * heading=1 distance=0 targetBearing=2 isHitWall=0 isHitByBullet=1 energy=1 state=172
    	 * 1*144 + 2*12 + 1*3 + 1 = 172
    	 */
    	NumStates = cnt; 
    	//System.out.println("number of cnt:" + cnt);
    	//System.out.println("number of states:" + NumStates);
    }

    /* state pre-processing */
    
    /* quantize energy level */
    public static int getEnergy(double energy) {
    		int newEnergy;
    		
    		if (energy < 0 )
    			newEnergy = 0;
    		
    		if (energy < 40.0)
    			newEnergy = 0;
    		else if (energy >= 40.0 && energy < 80.0)
    			newEnergy = 1;
    		else
    			newEnergy = 2;
    		return newEnergy;
    }
    
    /* get robot headings in degrees; normalize to 45 deg precision*/
    public static int getHeading(double heading) {
    	if (SMART_CODE) {
    		/*  
    		 *  316~45 is 0
    		 *  46~135 is 1
    		 *  136~225 is 2
    		 *  226~315 is 3
    		 * */
	    	double angle = FullAngle/NumHeading; //quantatized into 4
	    	double newHeading = heading + angle / 2; // advanced by 45 deg 1/8*360
	    	if (newHeading > FullAngle)
	    		newHeading -= FullAngle;
	    	//System.out.println("real heading: " + heading + 
	    	//		" quant head: " + (int)(newHeading / angle));
	    	return (int)(newHeading / angle);
    	} else {
    		int newHeading;
    		if (heading < 90.0)
    			newHeading = 0;
    		else if (heading >= 90.0 && heading < 180.0)
    			newHeading = 1;
    		else if (heading >= 180.0 && heading < 270.0)
    			newHeading = 2;
    		else if (heading >= 270.0 && heading < 360.0)
    			newHeading = 3;
    		else 
    			newHeading = 0;
    		return newHeading;
    	}
    		
    }
    /* linear quantization of distance */
    public static int getTargetDistance(double value) {
    	int distance;
    	if (SMART_CODE) {
    		distance = (int)(value / NormalizeFactor);
    		if (distance > NumTargetDistance - 1)
    			distance = NumTargetDistance - 1;    		
    	} else {
    		if (value < 0)
    			distance = 0;
    	
    		if (value <= 200)
    			distance = 0; //0~200 pixels distance
    		else if (value > 200 && value <= 600)
    			distance = 1; // 200~600 pixels distance
    		else
    			distance = 2;
    	}
    	//System.out.println("real distance: " + value + 
    	//		" quant dis: " + distance);
    	return distance;
    }

	public static int getTargetBearing(double bearing) {
		double twoPI = Math.PI * 2;  
	    // http://osdir.com/ml/java-robocode/2010-03/msg00003.html
		if (bearing < 0.0)  
			bearing = twoPI + bearing;  
	    
	    double rad = twoPI / NumTargetBearing; // quantatized into 4
	    double newBearing = bearing + rad / 2;  // advance by 1/8 rads
	    
	    if (newBearing > twoPI)  
	    	newBearing -= twoPI;  // normalized to 2pi
	    //System.out.println("real bearing: " + bearing + 
	    //		" quant bear: " + (int)(newBearing/rad));
	    return (int)(newBearing / rad);
	}
}