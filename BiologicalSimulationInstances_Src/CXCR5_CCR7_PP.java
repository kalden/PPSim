

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import sim.util.Double2D;
import sim.util.IntBag;
import sim_platform.PPatchSim;

/**
 * Models the chemokine receptor on each LTi cell.  Will determine if there is enough chemokine in the local environment to
 * effect the movement of an LTi cell
 * @author kieran
 *
 */
public class CXCR5_CCR7_PP 
{	
	 /**
     * A map to store the strength of the chemokine in all the grid squares around a cell location.  Sorted so that the highest can be quickly retrieved
     */
    public SortedMap<Double,Integer> chemomap = new TreeMap<Double,Integer>();
    
    /**
	 * <a name = "chemokineEffectThreshold"></a>
	 * <b>Description:<br></b> 
	 * Threshold value at which chemokine will affect the movement of the LTi cell
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be a decimal number between 0 and 1
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Chemokine level is determined by a sigmoid function.  As this can give a result anywhere between 0 and 1, a lower level cut off is needed
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double chemokineEffectThreshold;
	
    /**
     * Constructor - store the sent in parameter values & initialise object
     * 
     * @param sp	Current simulation parameters	
     */
	public CXCR5_CCR7_PP(ArrayList<String> receptorDetail)
	{
		this.chemokineEffectThreshold = Double.parseDouble(receptorDetail.get(1));
	}
	
	public double generateAngleThroughChemokineExpression(PPatchSim ppsim, Double2D cellLocation)
	{
		int chosenDirection = Integer.parseInt(this.chemoEffect(ppsim, cellLocation));
		
		// Now we have used the grid to find the direction of the chemokine distribution & generate probabilities,
		// can use this to generate a random angle in that direction (see diagram for explanation)
		// if chosenDirection = 99, a random angle between 0 and 360 will be returned
		return this.calculateAngle(chosenDirection);
	}
	
	/**
	 * Calculate the probabilities of the cell moving to within each grid square of the moore's neighbourhood around it
     * This takes into account both CCL19 and CCL13/21 diffusions.  The direction at which the strongest chemokine 
     * strength is felt is returned, after a process which determines how likely the cell is to move in that direction
	 *   
	 * Assesses every 'square' around the LTi, and makes a decision based on the local expression level, then chooses an angle in that direction.
	 * Should no level be detected, or it not be high enough, the cell will choose a random direction
	 * 
	 * @param ppsim	The current simulation state
	 * @param agentLocation	Where the LTi cell currently is
	 * @return	A string denoting the angle chosen to move
	 */
	public String chemoEffect(PPatchSim ppsim, Double2D agentLocation)
	{
		IntBag mooresX;
		IntBag mooresY;
		
		// Store the total of each of the probabilities - used in later calculations to standardise the probability
		double totalchemoLevels=0;
		
		try
		{
			Double environmentLength = ppsim.intestine_env.getClass().getDeclaredField("currentGridLength").getDouble(ppsim.intestine_env);
			Double environmentHeight = ppsim.intestine_env.getClass().getDeclaredField("currentGridHeight").getDouble(ppsim.intestine_env);
			
			// Firstly, calculate the moores neighbourhood around the cell
			mooresX = ppsim.chemoGrid.generateMooresN(agentLocation.x,agentLocation.y,true,environmentLength,environmentHeight);
			mooresY = ppsim.chemoGrid.generateMooresN(agentLocation.x,agentLocation.y,false,environmentLength,environmentHeight);
		
			// clear the map used to calculate the move
			chemomap.clear();
			
			// Now go through each grid in the neighbourhood, excluding the centre where the cell is
			for(int k=0;k<mooresX.size();k++)
			{
				// FIX 170311 - DUE TO THE TOROIDAL NATURE OF NEIGHBOURS (AS THE CELLS CAN MOVE BOTTOM ROUND TO TOP, THEY WILL BE ABLE TO MOVE ROUND THE EDGES
				// OF THE SCREEN (LEFT & RIGHT) - THIS NEEDS TO BE STOPPED - SO THE LEVEL OF CHEMOKINE FOR GRID SPACES THE OPPOSITE SIDE OF THE SCREEN IS NOT
				// CALCULATED
			
				// ONLY LOOK AT THIS SQUARE IF NOT ROLLED AROUND THE SCREEN
				boolean moveOk = checkRightLeftRollAround(ppsim,agentLocation,mooresX.get(k),environmentLength,environmentHeight);
			
				if(moveOk)
				{
					// store the highest effect for this cell
					double chemoHighEffect = 0.0;
		
					// Just check that this is not the middle cell of the neighbourhood (where the cell is)
					if((mooresX.get(k)!=ppsim.chemoGrid.roundX(agentLocation.x, environmentLength)) || (mooresY.get(k)!=ppsim.chemoGrid.roundY(agentLocation.y, environmentHeight)))
					{
						// Now look at each LTo Cell and examine the chemokine effect (Note, strongest may not be closest)
						for(int l=0;l<ppsim.ltoCellsBag.size();l++)
						{
							Object expressingCell = ppsim.ltoCellsBag.get(l);
							
							try
							{
								// Get the state of this cell object
								Field cellState = expressingCell.getClass().getDeclaredField("cellState");
								// Get the simulation state (i.e. has the cell been stopped
								Field cellStopped = expressingCell.getClass().getDeclaredField("stopped");
								// Get the location of this cell object
								Field cellLocField = expressingCell.getClass().getDeclaredField("agentLocation");
								Double2D cellLocation = (Double2D)cellLocField.get(expressingCell);
								@SuppressWarnings("unchecked")
								ArrayList<Object> cellExpressors = (ArrayList<Object>)expressingCell.getClass().getDeclaredField("expressors").get(expressingCell);
						
								if((cellState.getInt(expressingCell)==3 || cellState.getInt(expressingCell)==9) && !cellStopped.getBoolean(expressingCell))       // i.e. the cell is expressing chemokine & has not yet been 'removed'
								{
									// Firstly calculate the distance from the LTo Cell - this is needed for all calculations
									double distance = (Math.sqrt((Math.pow(mooresX.get(k)-cellLocation.x,2)+Math.pow(mooresY.get(k)-cellLocation.y,2))));
							
									
									// Get the expressing cell chemokine levels - this however should again be moved for full separation, but here as an example
									// PREVIOUSLY SUCCESSFUL CHEMOKINE BEFORE SPLIT OF EXPRESSOR:
									/*Field cellChemoLinearAdjust = expressingCell.getClass().getDeclaredField("chemoLinearAdjust");
									Field cellChemoSigThreshold = expressingCell.getClass().getDeclaredField("chemoSigThreshold");
									double chemoEffect = calcChemoLevel(distance,cellChemoLinearAdjust.getDouble(expressingCell),cellChemoSigThreshold.getDouble(expressingCell),simParams.chemokineEffectThreshold);
									
									// determine if this is the highest effect seen, if so change the variable
									if(chemoEffect>chemoHighEffect)
										chemoHighEffect = chemoEffect;*/
									
									boolean chemoExpressorFound = false;
										
									for(int i=0;i<cellExpressors.size() && !chemoExpressorFound;i++)
							    	{
										try
										{
								    		// get the receptor
								    		Object expressor = cellExpressors.get(i);
								    			
								    		Field cellChemoLinearAdjust = expressor.getClass().getDeclaredField("chemoLinearAdjust");
											Field cellChemoSigThreshold = expressor.getClass().getDeclaredField("chemoSigThreshold");
								    
											double chemoEffect = calcChemoLevel(distance,cellChemoLinearAdjust.getDouble(expressor),cellChemoSigThreshold.getDouble(expressor),this.chemokineEffectThreshold);
												
											// determine if this is the highest effect seen, if so change the variable
											if(chemoEffect>chemoHighEffect)
												chemoHighEffect = chemoEffect;
												
											chemoExpressorFound=true;
										}
										catch(NoSuchFieldException e)
										{
											//e.printStackTrace();
											// May not be the chemokine receptor - just catch and ignore
										}
										catch(Exception e)
										{
											e.printStackTrace();
										}
							    	}		
										
								}
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
							
						} // has finished looking at each LTo
				
						// Now for that square, add the level to the map for processing later in calculation of the move
						chemomap.put(chemoHighEffect*100, k);
						totalchemoLevels+=chemoHighEffect;
					}
				
					else   // looking at the middle square, where the cell currently is
					{
						chemomap.put(0.0, k);
					}
				}
			

			}  // has finished looking at every square
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
		
	
		
	
		// Now find the best move based on the chemokine levels around the cell
	
		// Adjuster - will determine the probability that the cell moves in the direction of the highest chemokine strength
		// Calculated based on the presence of the three chemokines
	
		int adjuster=0;
	
		// Now determine if the cell is in the amplification range (i.e. being affected by the chemokine level)
		if(totalchemoLevels>0)
		{
			if(chemomap.lastKey()>this.chemokineEffectThreshold)	// there is a strong chemokine in this area, the probability increases
			{
				adjuster = chemomap.lastKey().intValue();
				// get the strength of the chemokine (used to calculate the probability)
				// this is adjusted by a parameter which sets the strength of this chemokine
			}
		}

		// Now determine what to make the cell do
		if(adjuster>0)    // there is a chemokine level to act upon
		{
			//return Integer.toString(cxcl13map.get(cxcl13map.lastKey()));
			return selectDirection(ppsim,adjuster,totalchemoLevels);
		}
		else   // no chemokine level, return 99 so a random move is made
		{
			return "99";
		}
	}
	
	/**
	 * As get neighbours is toroidal, the algorithm may examine chemokine levels that roll around the right/left of screen.  It is right that it should consider
	 * top and bottom, but not left & right.  This stops this happening
	 * 
	 * @param ppsim	The current simulation state
	 * @param agentLocation	Where the LTi cell is
	 * @param xPosInNeighbourhood	It's x position in the Moore's Neighbourhood
	 * @return 	Boolean stating whether the level calculated has rolled round left to right
	 */
	public boolean checkRightLeftRollAround(PPatchSim ppsim, Double2D agentLocation, Integer xPosInNeighbourhood, double environmentLength,double environmentHeight)
	{
		// GET NEIGHBOURS IS TOROIDAL AS THE CELL MAY DIVIDE OVER THE TOP/BOTTOM OF SCREEN
		// HOWEVER THIS MEANS THE CELL WILL DIVIDE LEFT & RIGHT
		// THIS CANNOT HAPPEN AND NEEDS TO BE DETECTED
		
		// gather whether the cell is on the left hand or right hand edge of the tract (if either)
		//if(ppsim.chemoGrid.roundX(agentLocation.x, environmentLength) < ((int)(simParams.initialGridLength/simParams.LTO_DIAMETER))/2)
		//System.out.println(agentLocation.x+" "+environmentLength+" "+ppsim.surfaceCellsGrid.getWidth()/2);
		if(ppsim.chemoGrid.roundX(agentLocation.x,environmentLength) < ((int)ppsim.surfaceCellsGrid.getWidth()/2))
    	//if(this.gridLoc.x < ((int)(simParams.initialGridLength/simParams.LTO_DIAMETER))/2)	// on the left
		{
			//if((xPosInNeighbourhood-ppsim.chemoGrid.roundX(agentLocation.x,environmentLength))+1 < (int)(simParams.initialGridLength/simParams.LTO_DIAMETER))
			if((xPosInNeighbourhood-ppsim.chemoGrid.roundX(agentLocation.x,environmentLength))+1 < (int)ppsim.surfaceCellsGrid.getWidth())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else	// on the right
		{
			//if((ppsim.chemoGrid.roundX(agentLocation.x,environmentLength) - xPosInNeighbourhood)+1 < (int)(simParams.initialGridLength/simParams.LTO_DIAMETER))
			if((ppsim.chemoGrid.roundX(agentLocation.x,environmentLength) - xPosInNeighbourhood)+1 < (int)ppsim.surfaceCellsGrid.getWidth())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
			
	}
	
	/**
     * Calculate the strength of the chemokine expression at a given distance from an LTo cell expressing the chemokines    
     * @param distance	The distance of the cell from the LTo
     * @param linearAdjust	The chemokine linear adjustment used to calculate how much chemokine is being expressed
     * @param chemoSigThresold	The value used to adjust the sigmoid function to sit correctly between 1 and 0
     * @param threshold	The threshold at which this chemokine affects the cell movement
     * @return chemoEffect	Level of expression at this distance
     */
    public double calcChemoLevel(double distance,double linearAdjust,double chemoSigThreshold,double threshold)
    {	
    	// calculate the chemokine effect at this distance
    	double chemoEffect = (1/( 1 + Math.pow(Math.E,-(-linearAdjust*distance+chemoSigThreshold))));
    	
    	// check whether the chemokine effect is over the set threshold
    	if(chemoEffect<threshold)
    	{
    		return 0;
    	}
    	else
    	{ 
    		return chemoEffect;
    	}
    }
    
    
    /**
     * Evaluates the chemokine levels in the environment (calculated by chemoEffect method) and returns the square that the cell should move go to.  
     * This is done numerous ways to ensure that all cases where any of the chemokines are not present (level not high enough or has been knocked out) 
     * are taken into account.  Where the probability is that the cell will not necessarily follow the highest strength, a random square is returned
     * 
     * @param ppsim	The current simulation state
     * @param adjuster	The calculated probability that the cell will move in the direction of the strongest chemokine
     * @param chemoLev	The level of chemokine in the Moore's Neighbourhood around the cell
     * @return String representing the square to move to (numbered between 0 and 8)
     */
    public String selectDirection(PPatchSim ppsim, int adjuster,double chemoLev)
    {
    	int probability = ppsim.random.nextInt(100)+1;
    	
    	if(probability<adjuster)	// the cell will move in the direction of highest chemokine strength
    	{
    			// return the square with highest level pointed at by the Chemo map
    			return Integer.toString(chemomap.get(chemomap.lastKey()));
    	}
    	else		// the cell will move randomly
    	{
    		// return a random number square between 0 and 8
			return Integer.toString(ppsim.random.nextInt(9));
		}
    }
    
    
    /**
     * Takes the direction that the cell will be moving in (determined by chemokine levels) and calculates a random angle in that direction
     * This can be seen diagrammatically if required
     * 
     * @param chosenDirection	A number representing the cell in the moores neighbourhood that has been chosen to move to
     * @return	double angle	The angle at which the cell will move (in radians)
     */
    public double calculateAngle(int chosenDirection)
    {
    	double angle=0.0;
    	
    	// Now calculate the angle
    	// See diagram of how this was calculated if necessary
    	switch(chosenDirection)
		{
			case 0:angle = Math.toRadians(203 +(Math.random() * ((249 - 203) + 1)));break;
			case 1:angle = Math.toRadians(158 +(Math.random() * ((202 - 158) + 1)));break;
			case 2:angle = Math.toRadians(113 +(Math.random() * ((157 - 113) + 1)));break;
			case 3:angle = Math.toRadians(250 +(Math.random() * ((292 - 250) + 1)));break;
			// No case four as this is where the cell currently is
			case 5:angle = Math.toRadians(68 +(Math.random() * ((112 - 68) + 1)));break;
			case 6:angle = Math.toRadians(293 +(Math.random() * ((337 - 293) + 1)));break;
			
			// Note special case 7 as this involves angle range 338-360 and 0-22
			case 7:angle = 0 +(Math.random() * ((45 - 0) + 1));
				if(angle>22)
					angle=angle+337;
			
				angle = Math.toRadians(angle);
				break;
			
			case 8:angle = Math.toRadians(23 +(Math.random() * ((67 - 23) + 1)));break;
			
			// chosen square may also have been sent in as 99 - signifying a random angle should be chosen
			case 99:angle = Math.toRadians(0 +(Math.random() * ((360) + 1)));break;
		}
    	
    	return angle;
    }

}
