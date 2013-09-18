package sim_platform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 * Class to control the functionality at each step of the simulation.  
 * (a) migration of LTin and LTi cells to the tract.  As a number of these cells enter per step, 
 * the best way to control migration was to have a class which is Steppable and manages this migration. From the start, LTin cells will migrate at a given rate per step and for a certain number of steps. 
 * LTi cells will then follow after a set delay, at a set rate, for a set time period
 * (b) generation of statistics per step (all saved to a csv file)
 * (c) division of LTo cells when this becomes necessary
 * (d) growth of the intestine tract as becomes necessary per step
 * 
 * Done this way to limit the need for a large number of steppable classes
 * 
 * @author Kieran Alden
 *
 */
public class CellInputControl implements Steppable,Stoppable
{	
	/**
	 * The current state of the simulation
	 */
	PPatchSim ppsim;
	
	/**
	 * The simulation schedule for which this class needs adding to
	 */
	Schedule sch;
	
	/**
	 * Flag which counts the 'parts' of LTi cells created by the input rate at each step, when 1 is reached that cell is also created
	 */
	double ltiInFlag = 0;
	
	/**
	 * Flag which counts the 'parts' of LTin cells created by the input rate at each step, when 1 is reached that cell is also created
	 */
	double ltinInFlag = 0;
	
	/**
	 * <a name = "cellSpeedMinLowBound"></a>
	 * <b>Description:<br></b> 
	 * Lower bound to use to generate cell speed. Can be changed on the console before running the simulation.
	 * As 1 pixel is 4micrometres, initial coding represents a lower bound move of 4micrometres a minute
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Set as has been verified experimentally (in speed per minute)
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels per minute
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double cellSpeedMinLowBound = 0.95;
	
	/**
	 * <a name = "cellSpeedMinUpBound"></a>
	 * <b>Description:<br></b> 
	 * Upper bound to use to generate cell speed. Can be changed on the console before running the simulation
	 * As 1 pixel is 4micrometres, initial coding represents an upper bound move of 16micrometres a minute
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Set as has been verified experimentally (in speed per minute)
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels per minute
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double cellSpeedMinUpBound = 2.2;
	
	/**
	 * <a name = "cellSpeedLowBound"></a>
	 * <b>Description:<br></b> 
	 * Used to calculate the cell speed lower bound used in the simulation - as the known lower bound is in speed per 
	 * minute, and the simulation steps may not be in minutes, this may need calculating
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Lower Cell Speed per amount of time set in secondsPerStep
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double cellSpeedLowBound;
	
	/**
	 * <a name = "cellSpeedUpBound"></a>
	 * <b>Description:<br></b> 
	 * Used to calculate the cell speed upper bound used in the simulation - as the known upper bound is in speed per 
	 * minute, and the simulation steps may not be in minutes, this may need calculating
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Lower Cell Speed per amount of time set in secondsPerStep
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double cellSpeedUpBound;
	
	ArrayList<ArrayList<Object>> cellsInSimulation;
	
	SortedMap<String,Double> simulatedCellInputRates;
	
	
	
	/**
	 * Creates the class and schedules this to run at each step
	 * @param schedule	The current simulation schedule
	 */
	public CellInputControl(Schedule schedule,PPatchSim ppsim,ArrayList<ArrayList<Object>> cells)
	{
		this.sch = schedule;
		
		
		this.cellsInSimulation = cells;
		this.simulatedCellInputRates = new TreeMap();
		ppsim.simulatedCellCellularity = new TreeMap();
		
		this.calculateCellPopulationFigures(ppsim);

	}
	
	/**
	 * Calculates the number of LTin and LTi cells on the tract at E15.5 (from percentages gathered experimentally), and from this generates an input
	 * rate for both cell types
	 * 
	 */
	public void calculateCellPopulationFigures(PPatchSim ppsim)
	{
		try
		{
			// We are making the assumption here that all cell numbers will be based on percentage of area (taking into account cell size)
			// at a certain timepoint )(i.e here, the percentage is for 24 hours)
			// So calculate the required number of cells at this timepoint for each cell
			for(int i=0;i<this.cellsInSimulation.size();i++)
			{
				// Get the information about the cell (in another arraylist)
				ArrayList<Object> cellInfo = this.cellsInSimulation.get(i);
				// Now get the class of that cell
				Class<?> cellClass = Class.forName(cellInfo.get(0).toString());
				
				// CELL SIZE SHOULD BE A STATIC FIELD SO IT CAN BE ACCESSED WITHOUT HAVING TO EXPLICITLY CREATE AN OBJECT
				Double cellSize = cellClass.getDeclaredField("cell_diameter").getDouble(null);
				
				// Now work out the total number of cells the environment grid could hold
				double totalCells = ((((int)(ppsim.intestine_env.getClass().getDeclaredField("initialGridLength").getDouble(ppsim.intestine_env)
						/ cellSize) * (int)(ppsim.intestine_env.getClass().getDeclaredField("initialGridHeight").getDouble(ppsim.intestine_env) / 
								cellSize))));
				
				// Now work out the required number of these cells
				// Now get the percentage of the area of this cell (sent in as parameter 2 of the arraylist for the cell details
				double numOfCellsRequired = (totalCells/100)*Double.parseDouble(cellInfo.get(1).toString());
				
				Double cellInputRate = numOfCellsRequired/(((24*60)*60)/ppsim.simulationSpec.secondsPerStep);
				
				// Add the input rate to the cell info map, as required later
				this.simulatedCellInputRates.put(cellClass.getName(),cellInputRate);
				
				// Add to the map that contains a count for each cell - useful for cellularity and input rates
				ppsim.simulatedCellCellularity.put(cellClass.getName(), 0);
				
				// Work out the cell speeds for this simulation
				// Calculate the upper and lower cell speed bounds for this run (from the number of seconds represented by each step)
				this.cellSpeedLowBound = (this.cellSpeedMinLowBound/60)*ppsim.simulationSpec.secondsPerStep;
				this.cellSpeedUpBound = (this.cellSpeedMinUpBound/60)*ppsim.simulationSpec.secondsPerStep;
				
			}
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Adds the required number of LTin or LTi cells to the simulation in this step as set by the input rate
	 * 
	 * @param ppsim	The current simulation
	 * 
	 */
	public void createCellObject(PPatchSim ppsim,ArrayList<Object> cellInfo)
	{
		Object agent = null;
		boolean collision = true;
		Double2D loc = null;
			
		try
		{
			// get current environment dimensions
			double currentGridLength = ppsim.intestine_env.getClass().getDeclaredField("currentGridLength").getDouble(ppsim.intestine_env);
			double currentGridHeight = ppsim.intestine_env.getClass().getDeclaredField("currentGridHeight").getDouble(ppsim.intestine_env);
		
		
			while(collision)
			{
				Double x,y;
					
				// Just work out random position in whole environment
				x = ppsim.random.nextDouble()*currentGridLength;
				y = ppsim.random.nextDouble()*currentGridHeight;
					
				// Set the location of the cell to these coordinates
				loc = new Double2D(x,y);
					
				// Make the cell at this location 
				agent = this.declareCellObject(ppsim, loc, cellInfo);
				
				Class<?> cellClass = agent.getClass();
				Method ltiltinCollision = cellClass.getMethod("ltiltinCollision",new Class[]{Continuous2D.class});
				collision = (Boolean)ltiltinCollision.invoke(agent,(Continuous2D)ppsim.intestine_env.getClass().getDeclaredField("tract").get(ppsim.intestine_env));
				
				//collision = agent.ltiltinCollision((Continuous2D)ppsim.intestine_env.getClass().getDeclaredField("tract").get(ppsim.intestine_env));
			}
			
			Class newCellClass = agent.getClass();
				
			// Now there are guarantees as to no collisions, add this cell to the tract and to the scheduler
			// Do this using reflection as the example is dynamic
			
			Class<?> environmentClass = ppsim.intestine_env.getClass();
			Method meth = environmentClass.getMethod("setLocation",new Class[]{Object.class,Double2D.class});
			meth.invoke(ppsim.intestine_env,agent,loc);
			//ppsim.intestine_env.tract.setObjectLocation(agent,loc);
			
			
			Method setStopperMeth = newCellClass.getMethod("addToSchedule",new Class[]{Schedule.class});
			setStopperMeth.invoke(agent,sch);
			
			//agent.setStopper(sch.scheduleRepeating(agent));
			
			// add this cell to the list of those being tracked if tracking has started and is enabled
			// The cell tracking class removes any which have been tracked for over than an hour
			
			if(ppsim.simulationSpec.cellTrackingEnabled)
			{
				if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep < ((ppsim.cellTrackStats.trackingSnapStartHr*60)*60) 
					&& ppsim.simulationSpec.cellTrackingEnabled)	// no point adding the cell if tracking has ended
				{
					// Set Track Start Location
					Method setAgentTrackStartLocation = newCellClass.getMethod("setAgentTrackStartLocation", new Class[]{});
					setAgentTrackStartLocation.invoke(agent);
					
					//agent.agentTrackStartLocation = agent.agentLocation;
					// Add the cell to the list
					ppsim.cellTrackStats.trackedCells_Away.add(agent);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
			
	public Object declareCellObject(PPatchSim ppsim,Double2D loc,ArrayList<Object> cellInfo)
	{
		Object cell = null;
		
		try
		{	
			Class<?> cellType = Class.forName(cellInfo.get(0).toString());
			Constructor<?> con = cellType.getConstructor(new Class[]{PPatchSim.class,Double2D.class,ArrayList.class});
			cell = con.newInstance(ppsim,loc,cellInfo.get(6));
			
			// get the current cellularity for this cell from the map
			int cellCount = ppsim.simulatedCellCellularity.get(cellInfo.get(0));
			// increase by one
			cellCount++;
			// remove the count from the map
			ppsim.simulatedCellCellularity.remove(cellInfo.get(0));
			// add the new count back in
			ppsim.simulatedCellCellularity.put(cellInfo.get(0).toString(),cellCount);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return cell;
	}
	
	public void addCells(PPatchSim ppsim,ArrayList<Object> cellInfo)
	{
		double reqCells =0; //required number of cells at point in time as predicted by graph
		double cellsPrev = 0; //number of cells one step previous
		double steps = 0;
		steps = ppsim.schedule.getSteps();	
	
		// Get the cell input rate graph type parameter from the cell info arraylist sent in - this is at index 4 in the cell parameter list
		
		if (cellInfo.get(4).toString().equalsIgnoreCase("exp"))
		{
			//exponential graphs
			// this requires a constant, set at cell parameter index 5
			
			reqCells = Math.pow(Double.parseDouble(cellInfo.get(5).toString()),steps); //from equation: y=1.00345^x to get 168 cells at 24 hours.
			cellsPrev = Math.pow(Double.parseDouble(cellInfo.get(5).toString()),(steps-1)); //work out the required number of cells by getting the number at one previous step
			
			// Have to change the input rate of this cell in the map
			// Remove the original entry from the map
			this.simulatedCellInputRates.remove(cellInfo.get(0));
			// Put new input rate into map
			this.simulatedCellInputRates.put(cellInfo.get(0).toString(), reqCells-cellsPrev);
			
		}
		else if (cellInfo.get(4).toString().equalsIgnoreCase("sqrt"))
		{
			// inverse exponentials using square root
			reqCells = Math.pow(Double.parseDouble(cellInfo.get(5).toString())*steps, 0.5); //from equation: y=sqrt(20.5x) to get 168 cells at 24 hours.
			cellsPrev = Math.pow(Double.parseDouble(cellInfo.get(5).toString())*(steps-1), 0.5); //work out the required number of cells by getting the number at one previous step
			
			// Have to change the input rate of this cell in the map
			// Remove the original entry from the map
			this.simulatedCellInputRates.remove(cellInfo.get(0));
			// Put new input rate into map
			this.simulatedCellInputRates.put(cellInfo.get(0).toString(), reqCells-cellsPrev);
			
		}
		
		// if the graphType is not either of these, the lTiInputRate will remain the default constant rate.
		
		// NOW THE INPUT RATE IS CORRECT, CREATE THE CELL OBJECT
		
		// add the cell if rate is a whole number
		
		for(int i=0;i<(int)(this.simulatedCellInputRates.get(cellInfo.get(0)).intValue());i++)
		{
			this.createCellObject(ppsim,cellInfo);
		}
		
		// deal with decimal input rates
		if(this.simulatedCellInputRates.get(cellInfo.get(0)) %1 > 0)
		{
			// add the decimal/remainder to the flag, and when one is reached, add that cell
			ltinInFlag = ltinInFlag+(this.simulatedCellInputRates.get(cellInfo.get(0)) %1);
			//System.out.println(ltinInFlag);
			if(ltinInFlag>=1)
			{
				this.createCellObject(ppsim,cellInfo);
				// take the cell from the flag
				ltinInFlag = ltinInFlag-1;
			}
		}
	}
	

	/**
	 * Adds the required number of cells (LTin and LTi) per step, adjusts the input rates if required, takes twelve our snaps if necessary, triggers LTo cell
	 * division, and resizes the tract if necessary. All done for every simulation step
	 */
	public void step(final SimState state)
	{
		PPatchSim ppsim = (PPatchSim)state;
		
		if((ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep)<((ppsim.simulationSpec.simulationTime*60)*60))
		{	
			// The simulation is running
			try
			{
				// Examine each of the cell types that the user wants in this simulation
				for(int i=0;i<this.cellsInSimulation.size();i++)
				{
					// Get the information about the cell (in another arraylist
					ArrayList<Object> cellInfo = this.cellsInSimulation.get(i);
					// Now get the class of that cell
					Class<?> cellClass = Class.forName(cellInfo.get(0).toString());
					
					// Now get the input time so can determine if cells still need to be added to the simulation - cell input time is parameter 4 in the list
					int cellInputHours = Integer.parseInt(cellInfo.get(3).toString());
					
					// Now get the input delay time and check that cell should be entering
					int cellInputDelayHours = Integer.parseInt(cellInfo.get(2).toString());
					
					// Now do the checks
					if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep>((cellInputDelayHours*60)*60))
					{
						if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep<((cellInputHours*60)*60))
						{
							this.addCells(ppsim,cellInfo);
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			// Take a snap of the tract if required (could have been done elsewhere, but saves an extra steppable class
			if(ppsim.simulationSpec.twelveHourSnaps)
				ppsim.captureTrackImage.takeDisplaySnaps(ppsim);	
			
			// Take a snap if within a period where each step is being imaged
			if(ppsim.simulationSpec.stepBystepTrackingImages)
				ppsim.captureTrackImage.timePeriodImaging(ppsim);
			
		}
		else
		{	
			this.stop();
		}
	}
	
	/**
	 * Flag to show if this class has been stopped (i.e. no more cell entry)
	 */
	private Stoppable stopper = null;
	
	/**
	 * Function to set the stopper
	 * @param stopper	Whether the class has been stopped or not
	 */
    public void setStopper(Stoppable stopper)   {this.stopper = stopper;}
    
    /**
     * Function to stop the class 
     */
    public void stop(){stopper.stop();}
}
