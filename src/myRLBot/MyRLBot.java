package myRLBot;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.io.PrintStream;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.WinEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.BulletHitBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.RoundEndedEvent;
import robocode.BattleEndedEvent;
import robocode.BattleResults;
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;

// NeuralNetwork stuff
import chengli.NeuralNet;

public class MyRLBot extends AdvancedRobot {
	public static final double PI = Math.PI;
	private Target enemy;
	private LUT lut;
	private Train rlTrainer;
	//private NeuralNet nn;
	private double imReward = 0.0;
	private double reward = 0.0;
	private double firePower;
	private short isHitWall = 0;
	private short isHitByBullet = 0;
	private int counter_hit = 0;
	private int counter_fire = 0;
	private int counter_ram = 0;
	/* DEBUG PURPOSE
	private int[][] stateIdx = new int[576][6];
	static double[] array = new double[100];
	static int roundCount = 0;
	static int round = 0;
	 */
	public void run() {;
		/* robocode colors */
		setBodyColor(new Color(128, 128, 50));
		setGunColor(new Color(50, 50, 20));
		setRadarColor(Color.RED);
		setScanColor(Color.WHITE);
		setBulletColor(Color.PINK);
		
		/* be careful here about LUT init and if
		 * LUT is really replaced or not?? */
		/* ok, at least this part is verified */
		lut = new LUT();
		//out.println("Creating a empty neural network, no training yet...");
		//nn = new NeuralNet(6,12);
		load();
		rlTrainer = new Train(lut);
		enemy = new Target();
		/* init of RLBot & enemey distance */
		enemy.distance = 1000;
		
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2 * PI);
		
		while (true) {
			firePower = 300.0/enemy.distance;
			if (firePower > 3.0)
				firePower = 3.0;
			setupRadarScan();
			setupGunMove();
			setupRobotMovement(firePower);
			/* give control back to robocode manager */
			execute();
		}
	}

	private void setupRobotMovement(double pwr) {
		
		int curState = getState();
		int curAction = rlTrainer.selectAction(curState);
		//out.println("=== setupRobotMovement() ===");
		//out.println("curState="+curState+" curAction="+curAction+" imReward="+imReward);
		
		/* RL train every robocode processing unit: turn */
		double diff = rlTrainer.train(curState, curAction, imReward);
		
		/* NNRL - use bipolar 
		 * answer is the newQValue from the LUT 
		 * assuming LUT is converged and will
		 * not be modified any further... */
		//nn.train(inputs, answer, false)
		//out.println("STATE="+curState+" REWARD="+reward+" DIFF="+diff);
		/* cumulative rewards */
		reward = reward + imReward;
		setDebugProperty("ImReward", " "+reward);

		imReward = 0.0;
		isHitWall = 0;
		isHitByBullet = 0;

		switch (curAction) {
			case Action.RobotAhead:
				//setAhead(Action.RobotMoveDistance+Action.RobotMoveAddDistance);
				//setAhead(Action.RobotMoveDistance);
				setAhead(Action.RobotMoveDistance);
				//setTurnLeft(Action.RobotTurnDegree);
				//gunFire(pwr);
				break;
			case Action.RobotBack:
				//setBack(Action.RobotMoveDistance+Action.RobotMoveAddDistance);
				//setBack(Action.RobotMoveDistance);
				setBack(Action.RobotMoveDistance);
				//setTurnLeft(Action.RobotTurnDegree);
				//gunFire(pwr);
				break;		
			case Action.RobotTurnLeft:
				setTurnLeft(Action.RobotTurnDegree);
				//gunFire(pwr);
				break;
			case Action.RobotTurnRight:
				setTurnRight(Action.RobotTurnDegree);
				//gunFire(pwr);
				break;
				/*
			case Action.RobotBackTurnLeft:
				setTurnLeft(Action.RobotTurnDegree);
				setBack(Action.RobotMoveDistance);
        		//gunFire(pwr);
        		break;
      		case Action.RobotBackTurnRight:
      			setTurnRight(Action.RobotTurnDegree);
      			setBack(Action.RobotMoveDistance);
        		//gunFire(pwr);
        		break;
        		*/
      		case Action.RobotFire:
      			gunFire(pwr);
        		break;
		}
	}

	private int getState() {
		int heading = State.getHeading(getHeading()); // 4 headings
		int targetDistance = State.getTargetDistance(enemy.distance); //3 distances
		int targetBearing = State.getTargetBearing(enemy.bearing); // 4 bearings
		int energy = State.getEnergy(getEnergy()); // 3 energy levels
		/* access to a particular state given indices */
		int state = State.stateTable[heading][targetDistance][targetBearing][isHitWall][isHitByBullet][energy];
		/* DEBUG
		setDebugProperty("heading", " "+heading);
		setDebugProperty("distance", " "+targetDistance);
		setDebugProperty("bearing", " "+targetBearing);
		setDebugProperty("state", " "+state);
		*/
		
		/*
		out.println("=== getState() ===");
		out.println("state="+state+
				" ["+heading+"]"+
				"["+targetDistance+"]"+
				"["+targetBearing+"]"+
				"["+isHitWall+"]"+
				"["+isHitByBullet+"]"+
				"["+energy+"]");
		*/
				
		/* only the last state being accessed will be recorded
		 * this is NOT a solution... good for checking the result */
		/*
		stateIdx[state][0] = heading;
		stateIdx[state][1] = targetDistance;
		stateIdx[state][2] = targetBearing;
		stateIdx[state][3] = isHitWall;
		stateIdx[state][4] = isHitByBullet;
		stateIdx[state][5] = energy;
		*/
		
		return state;
	}

  	private void gunFire(double pwr) {
  		if (getGunHeat() == 0)
  			setFire(pwr);
  		counter_fire++;
  	}
	
  	private void setupRadarScan() {
  		double radarOffset;
  		radarOffset = getRadarHeadingRadians() - 
  				(PI/2 - Math.atan2(enemy.y - getY(),enemy.x - getX()));
  		if (radarOffset > PI)
  			radarOffset -= 2*PI;
  		if (radarOffset < -PI)
  			radarOffset += 2*PI;
  		if (radarOffset < 0)
  			radarOffset -= PI/10;
  		else
  			radarOffset += PI/10;
  		// turn the radar
  		setTurnRadarLeftRadians(radarOffset);
  	}

    /** 
     * Move the gun to the predicted next bearing of the enemy.  
     */
  	private void setupGunMove() {
  		
  		long time;
  		long nextTime;
  		Point2D.Double p;
  		p = new Point2D.Double(enemy.x, enemy.y);
  		for (int i = 0; i < 20; i++) {
  			nextTime = (int)Math.round((getRange(getX(),getY(),p.x,p.y)/(20-(3*firePower))));
  			time = getTime() + nextTime - 10;
  			p = enemy.targetEnemy(time);
  		}
  		//offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
  		double gunOffset = getGunHeadingRadians() - 
  				(PI/2 - Math.atan2(p.y - getY(),p.x -  getX()));
  		setTurnGunLeftRadians(normaliseBearing(gunOffset));
  	}

  	/**
     * If a bearing is not within the -pi to pi range, 
     * alters it to provide the shortest angle.
     * @param angle The original angle.
     * @return The shortest angle.
     */
  	double normaliseBearing(double ang) {
  		if (ang > PI)
  			ang -= 2*PI;
  		if (ang < -PI)
  			ang += 2*PI;
  		return ang;
  	}

    /**
     * Returns the distance between two x,y coordinates.
     * @param x1 First x.
     * @param y1 First y.
     * @param x2 Second x.
     * @param y2 Second y.
     * @return The distance between (x1, y1) and (x2, y2).
     */
  	public double getRange( double x1,double y1, double x2,double y2 ) {
  		double xo = x2 - x1;
  		double yo = y2 - y1;
  		double h = Math.sqrt( xo * xo + yo * yo );
  		return h;
  	}

  	public void onBulletHit(BulletHitEvent e) {
  		// big reward when hitting the target
  		//double power = e.getBullet().getPower();
  		/*
  		if (power <= 1)
  			change = 4 * power;
  		else
  			change = 4 * power + 2 * (power - 1);
  			*/
  		double change = 8.0;
  		//imReward += change;
  		imReward += change;
  		counter_hit++;
  		setDebugProperty("onBulletHit", " "+change);
  	}

  	public void onBulletHitBullet(BulletHitBulletEvent e) {
  		// this means bad which I am on the target line of enemy...
  		//double change = -e.getBullet().getPower();
  		double change = -1.0;
  		imReward += change;
  		setDebugProperty("onBulletHitBullet", " "+change);
  	}

  	public void onBulletMissed(BulletMissedEvent e) {
  		//double change = -e.getBullet().getPower();
  		double change = -2.0;
  		imReward += change;
  		setDebugProperty("onBulletMissed", " "+change);
  	}

  	public void onHitByBullet(HitByBulletEvent e) {
  		//double power = e.getBullet().getPower();
  		double change = -10.0;
  		/*
  		if (power <= 1)
  			change = (-1) * (4 * power);
  		else
  			change = (-1) * (4 * power + 2 * (power - 1));
  			*/
  		imReward += change;
  		isHitByBullet = 1;
  		setDebugProperty("onHitByBullet", " "+change);
  	}

  	public void onHitRobot(HitRobotEvent e) {
  		//double change = -0.6 * 1;
  		double change = -1.0;
  		// let's try ramming the enemy ;-p, doesn't work at all... give up
  		//double change = 6;
  		imReward += change;
  		counter_ram++;
  		out.println("+++++++++++++++++++++++++");
  		out.println("Ramming the enemy "+counter_ram+" !");
  		out.println("+++++++++++++++++++++++++");
  		setDebugProperty("onHitRobot", " "+change);
  	}

  	public void onHitWall(HitWallEvent e) {
  		//double change = -1 * (Math.abs(getVelocity()) * 0.5 - 1);
  		//double change = -4;
  		double change = -10.0;
  		imReward += change;
  		isHitWall = 1;
  		setDebugProperty("onHitWall", " "+change);
  	}

  	public void onScannedRobot(ScannedRobotEvent e) {
  		/* only update enemey status when enemy distance is
  		 * less than last reported enemey distance
  		 * this may not be a good practice */
  		//if (e.getDistance() < enemy.distance) {
  			double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2 * PI);  			
  			double h = normaliseBearing(e.getHeadingRadians() - enemy.head);
  			h = h/(getTime() - enemy.ctime);
  			enemy.changehead = h;
  			enemy.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); 
  			enemy.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); 
  			enemy.bearing = e.getBearingRadians();
  			enemy.head = e.getHeadingRadians();
  			enemy.name = e.getName();
  			enemy.ctime = getTime();
  			enemy.speed = e.getVelocity();
  			enemy.distance = e.getDistance();
  			
  			// if seeing enemy enery is dropping, it's good
  			if (e.getEnergy() < enemy.energy) {
  				double change = e.getEnergy()-enemy.energy;
  				//out.println("energy diff: " + change);
  				// a buggy 100 energy drop due to init
  				// only add rewards when it's due to miss firing
  				// actually every bullet enemy fires his energy
  				// will drop 0~3 energy. but we have negative
  				// rewards when me is hit by a bullet so that
  				// should be cancelled anyways plus a big rewards
  				// for dodging the bullet.
  				if (change != 100 && change <= 3)
  					// for bullet dodge
  					imReward += 5.0;
  				setDebugProperty("onEnemyEnergyDrop", " "+ imReward);
  			}
  			enemy.energy = e.getEnergy();
  		//}
  	}

  	public void onRobotDeath(RobotDeathEvent e) {
  		/* init distance back to largest */
  		enemy.distance = 1000;
  	}

  	public void onWin(WinEvent event) {
  	/* not necessary
  		double change = 6;
  		imReward += change;
  		setDebugProperty("onWin", " "+change);
  		*/
  		/* do a victory dance */
  		/*
  		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
		*/
  		save();
  		//saveToNN(State.nnInputTable);
  		//saveToNNVerify(stateIdx);
  		//saveToNNSimple();
		//array[roundCount]=(double)counter_hit/counter_fire;
		//System.out.println(array[roundCount]);
		//roundCount++;
		//round++;
		//System.out.println("DEBUG win number:" + roundCount);
  		System.out.println("Fire: "+counter_fire+
  				" Hit: "+counter_hit+
  				" Ratio: "+(double)counter_hit/counter_fire);
		System.out.println(">>> Reward: " + reward);
  	}

  	public void onDeath(DeathEvent event) {
  		/* not necessary
  		double change = -6;
  		imReward += change;
  		setDebugProperty("onDeath", " "+change);
  		*/
  		save();
  		//saveToNN(State.nnInputTable);
  		//saveToNNVerify(stateIdx);
  		//saveToNNSimple();
  		// array[roundCount]=(double)counter_hit/counter_fire;
  		// System.out.println(array[roundCount]);
  		//roundCount++;
  		out.println("Fire: "+counter_fire+
  				" Hit: "+counter_hit+
  				" Ratio: "+(double)counter_hit/counter_fire);
  		out.println(">>> Reward: " + reward);
  	}

  	public void load() {
  		out.println(">>> load LUT");
  		try {
  			lut.loadFile(getDataFile("data.txt"));
  		} catch (Exception e) {
  		}
  	}

  	public void save() {
  		out.println(">>> save LUT");
  		try {
  			lut.saveFile(getDataFile("data.txt"));
  		} catch (Exception e) {
  			out.println("Exception trying to write: " + e);
  		}  
  	}
  	
  	public void saveToNN(double[][] states) {
  		out.println(">>> save LUT in NN format");
  		try {
  			lut.saveFileForNN(states, getDataFile("nn.txt"));
  		} catch (Exception e) {
  			out.println("Exception trying to write: " + e);
  		}  
  	}

  	public void saveToNNVerify(double[][] states) {
  		out.println(">>> save LUT in NN-Verify format");
  		try {
  			/* HMM... Not always right but I think because of the way
  			 * the 2D array is being init
  			 */
  			lut.saveFileForNNVerify(states, getDataFile("nnVerify.txt"));
  		} catch (Exception e) {
  			out.println("Exception trying to write: " + e);
  		}  
  	}
  	
  	/* also for verify purpose
  	 * to check if the Q-Values
  	 * are updated correctly at
  	 * target state-action pair
  	 * */
  	public void saveToNNSimple() {
  		out.println(">>> save LUT in NN-Simple format");
  		try {
  			lut.saveFileForNNSimple(getDataFile("nnSimple.txt"));
  		} catch (Exception e) {
  			out.println("Exception trying to write: " + e);
  		}  
  	}
  	
  	public void onBattleEnded (BattleEndedEvent e) {
  		// only save NN format when LUT is converged
  		// also to save to NN simple to do verification
  		saveToNN(State.nnInputTable);
  		//saveToNNSimple();
  	}
  	
/* comment out for now due to robocode log data folder size issue
  	public void onBattleEnded (BattleEndedEvent e) {
  		BattleResults result =e.getResults();
  		PrintStream s = null;
	  
  		RobocodeFileOutputStream writer = null;
  		try {
  			s = new PrintStream(new RobocodeFileOutputStream(getDataFile("score.txt")));
  			// writer = new RobocodeFileOutputStream(getDataFile("score.txt"));
  			s.println("Score                "+result.getScore());;
  			s.println("Survival             "+result.getSurvival());;
  			s.println("Bullet Damage        "+result.getBulletDamage());;
  			s.println("Bullet Damage Bonus  "+result.getBulletDamageBonus());;
  			s.println("Ram Damage           "+result.getRamDamage());;
  			s.println("Ram Damage Bonus     "+result.getRamDamageBonus());;
  			s.close(); 
  		} catch (IOException a) {
  		} finally {
  			try {
  				if (writer != null)
  					writer.close( );
  			} catch (IOException a) {
  			
  			}
  		}
	  
  		PrintStream w = null;
  		try {
  			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("win.txt")));
  			for( int i = 0; i<array.length;i++)
  				w.println(array[i]);
  			w.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
  	}
  	*/
  	
  	/*
  	public void onRoundEnded(RoundEndedEvent event) {
  		RobocodeFileWriter fw = null;
  		try {
  			fw = new RobocodeFileWriter("/Users/chwlo/Documents/workspace/EECE592/bin/myRLBot/MyRLBot.data/reward.txt",true);
  			//fw.write("reward: "+(int)reward+"\n");
  			fw.write((int)reward+"\n");
  			fw.flush();
  			fw.close(); 
  		} catch (IOException e) {
  			System.out.println("IOException trying to write: " + e);
  		} finally {
  			try {
  				if (fw != null)
  					fw.close( );
  			} catch (IOException e) {
  				System.out.println("Exception trying to close witer: " + e);
  				e.printStackTrace();
  			}
  		}
  	}
  	 */
}