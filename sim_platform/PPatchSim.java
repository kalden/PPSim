package sim_platform;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.SortedMap;

import sim.util.*;
import ec.util.MersenneTwisterFast;
import sim.display.Display2D;
import sim.engine.*;
import sim.field.grid.ObjectGrid2D;



/**
 * The main class for the Peyer's Patch Simulation.The simulation begins when the start() method of this class is run.
 * Contains all the attributes used throughout the simulation, as well as the state of the simulation
 * at any given time.
 * 
 * @author Kieran Alden
 *
 */
public class PPatchSim extends SimState
{	
	
	/***************************************************************************************************
	 * ***************************************************************************************************
	 * ***************************************************************************************************
	 * 								PLATFORM PARAMETERS - FOR RUNNING & DATA COLLECTION 
	 */
	
	/***************************************************************************************************
	 * 								THE PARAMETERS FROM THE XML FILE & PROGRAM PARAMS
	 */
	
	/**
	 * <a name = "simParams"></a>
	 * <b>Description:<br></b> 
	 * Object of the class which reads the simulation parameters from the XML file, making these available in the simulation
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Simulation Parameters
	 * <br><br>
	 */
	//public SimParameters simParams;
	
	public static String xmlFilePath;
	
	/**
	 * Short description of this run.
	 * e.g. for a sensitivity analysis experiment, this could simply be a run number
	 */
	public static String runReplicate;
	
	public Setup_Simulation simulationSpec;
	
	/**
	 * <a name = "display"></a>
	 * <b>Description:<br></b> 
	 * If run with a GUI, this forms the window used to display the simulation
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Graphical Display
	 * <br><br>
	 */
	public static Display2D display;
	
	/**
	 * <a name = "filePath"></a>
	 * <b>Description:<br></b> 
	 * The directory path where the parameter file is and output will be stored
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Full File Path
	 * <br><br>
	 */
	//public static String filePath;
	
	/**
	 * <a name = "runDescription"></a>
	 * <b>Description:<br></b> 
	 * A description of this run
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Description
	 * <br><br>
	 */
	//public static String runDescription;
	
	/**
	 * <a name = "paramFilePath"></a>
	 * <b>Description:<br></b> 
	 * Full file path to parameter XML file
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * File Path
	 * <br><br>
	 */
	//public static String paramFilePath;
	
	
	/***************************************************************************************************
	 * 												ENVIRONMENT 
	 */
	
	public Object intestine_env;
	
	
	public SortedMap<String,Integer> simulatedCellCellularity;
	
	
	/**
	 * <a name = "stromalCellEnvironment"></a>
	 * <b>Description:<br></b> 
	 * Object used to set up the distribution of LTo and RLNonStromal Cells in the environment
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Object
	 * <br><br>
	 */
	public Setup_Stromal_Cell_Distribution stromalCellEnvironment; 
	
	/**
	 * <a name = "chemoGrid"></a>
	 * <b>Description:<br></b> 
	 * Value grid used when calculating the chemokine diffusion.  Note that no values are actually stored in the grid, the grid exists to make processing easier.
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Object
	 * <br><br>
	 */
	public ChemokineGrid chemoGrid;        // not in param file
	
	/**
	 * <a name = "cellColours"></a>
	 * <b>Description:<br></b> 
	 * Colour array be used to match a colour to the colourCode variable, representing cell state
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * ArrayList
	 * <br><br>
	 */
    public static ArrayList<Color> cellColours = new ArrayList<Color>();

    /**
	 * <a name = "lToGrid"></a>
	 * <b>Description:<br></b> 
	 * Grid of 1x1 squares which overlays the Continuous2D grid
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Grid Space
	 * <br><br>
	 */
	public ObjectGrid2D surfaceCellsGrid;
	
	
	/***************************************************************************************************
	 * 												CELLS 
	 */
	
	/**
	 * <a name = "numActiveLTo"></a>
	 * <b>Description:<br></b> 
	 * Stores the number of LTo cells calculated from the percentage of stromal cells that are active in the simulation
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Number of Cells
	 * <br><br>
	 */
	public static int numActiveLTo;
	
	/**
	 * <a name = "ltoCellsBag"></a>
	 * <b>Description:<br></b> 
	 * Collection which stores all the LTo Cell Objects in order that there locations can be easily accessed
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Data Storage (Bag)
	 * <br><br>
	 */
	public Bag ltoCellsBag;
	
	/**
	 * <a name = "activelToCellsBag"></a>
	 * <b>Description:<br></b> 
	 * Collection which stores all the stromal Cell Objects which are active in order that there locations can be easily accessed
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Data Storage (Bag)
	 * <br><br>
	 */
	public Bag activelToCellsBag;
	
	/**
	 * <a name = "RETLigandNonStromalCellsBag"></a>
	 * <b>Description:<br></b> 
	 * Collection which stores all the Decoy Cell Objects in order that there locations can be easily accessed
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Data Storage (Bag)
	 * <br><br>
	 */
	public static Bag RETLigandNonStromalCellsBag;
	
	
	/***************************************************************************************************
	 * 												STATISTICS 
	 */
	
	/**
	 * <a name = "stats"></a>
	 * <b>Description:<br></b> 
	 * Object used to generate the patch statistics at the end of the run
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Object of PatchStatistics
	 * <br><br>
	 */
	public PatchStatistics patchStatsGeneration;
	
	/**
	 * <a name = "cellTrackStats"></a>
	 * <b>Description:<br></b> 
	 * Object used to perform the cell tracking during the run 
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Object of CellTracking
	 * <br><br>
	 */
	public CellTracking cellTrackStats;
	
	/**
	 * <a name = "captureTrackImage"></a>
	 * <b>Description:<br></b> 
	 * Object used to take snapshots of the tract (if required) during and at the end of the run 
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Object of TrackImaging
	 * <br><br>
	 */
	public TrackImaging captureTrackImage;	
	
	public CellInputControl hemCells;
	
	public Bag allLTis = new Bag();
	
	
	/********************************
	 * THIS IS A HACK AND NEEDS FIXING ON LTI/LTIN CELL SEPARATION FROM SIMULATOR
	 */
	
	public double lookupDistance = 5;
	
	/***************************************************************************************************
	 * 												CLASS METHODS
	 */
	
	/**
	 * @param seed
	 */
	public PPatchSim(long seed)
	{
		super(new MersenneTwisterFast(seed),new Schedule());

	}
	
	public PPatchSim(long seed,String[] args)
	{
		super(new MersenneTwisterFast(seed),new Schedule());
		
		// START THE RUN
		doLoop(PPatchSim.class,args);
	}
	
	
	
	/**
	 * Begins the simulation by calling doLoop to run the start method until the simulation exits
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Set up the simulation display & console
		// Check whether running from the command line or eclipse
		
		if(args.length > 0)
		{
			// Command line run (arguments should have been supplied!)
			xmlFilePath = args[0];
			runReplicate = args[1];
			
		}
		else
		{
			// Assume Eclipse Run (though will enter here if command line and no args supplied)
			// Else an exception will be generated
			xmlFilePath = "/home/kieran/workspace/C2D2/TestForRichard/ppsim_consistency_analysis2.xml";
			runReplicate = "1";
		}
		
		// START THE RUN
		doLoop(PPatchSim.class,args);		
		
	}
	
	/**
	 * Ends the simulation after the 72 hour period - taking the final snapshot if image output is required
	 */
	public void finish()
	{	
		super.finish();
		
	}
	
	/**
	 * Begins the simulation run.  Calls are made to the required methods to initialise the environment, add the cells, deal with tract 
	 * growth etc
	 */
	public void start()
	{	
		// READ IN THE DETAILS TO BUILD THE SIMULATION IF LAUNCHED WITHOUT A GUI
		if(this.simulationSpec==null)			// launched without a GUI or wrapper
		{
			this.simulationSpec = new Setup_Simulation(this.xmlFilePath,this.runReplicate);
		}
		
		// GET THE CELL TYPE(S) THAT ARE STROMAL BASED
		// THESE WILL HAVE BEEN ADDED TO THE CELLS ARRAYLIST TO MAKE INPUT EASIER BUT NEED TO BE REMOVED FROM HERE AND SENT WITH THE STROMAL ENVIRONMENT
		// SETUP
		
		@SuppressWarnings("unchecked")
		
		// NOTE THAT THE READ IN PROCESSES GENERIC XML FILES CREATED BY THE INTERFACE
		// WHERE SOME SIMUALTIONS CAN HAVE MORE THAN ONE COMPARTMENT
		// AS THIS IS NEVER THE CASE HERE, WE SIMPLY READ THE FIRST ARRAYLIST FROM THE ENVINFO ATTRIBUTE
		
		
		ArrayList<Object> simEnvironment = this.simulationSpec.envInfo.get(0);
		
		
		ArrayList<String> stromalEnvCells = (ArrayList<String>)simEnvironment.get(simEnvironment.size()-1);
		
		for(int i=0;i<stromalEnvCells.size();i++)
		{
			String stromalCellClass = stromalEnvCells.get(i).toString();
			
			// Go through the cells array list
			int limit = this.simulationSpec.cells.size();
			for(int j=0;j<limit;j++)
			{
				if(this.simulationSpec.cells.get(j).get(0).equals(stromalCellClass))
				{
					// Move the cell into a stromal object that can be sent to the stromal environment setup. These cells are set up differently from
					// those that migrate
					//System.out.println(this.cells.get(j));
					this.simulationSpec.enviromentCells.add(this.simulationSpec.cells.get(j));
					this.simulationSpec.cells.remove(this.simulationSpec.cells.get(j));
					
					// adjust the limit as the size of the array has changed - stops array location errors
					limit--;
				}
				
			}
		}
		
		// Set up track imaging if required
		captureTrackImage = new TrackImaging();
		
		// 1: Populate the colour array for use in the display class (If GUI on)
		this.popColourArray();
		
		// 2: Declare the cell storage bags
		ltoCellsBag = new Bag();
		activelToCellsBag = new Bag();
		RETLigandNonStromalCellsBag = new Bag();
		
		// SET UP THE ENVIRONMENT FROM EXTERNAL CLASS
		
		if(this.intestine_env == null)
		{
			try
			{
				// Have read in the environment array earlier (simEnvironment)
				Class<?> environment = Class.forName(simEnvironment.get(0).toString());
				
				// CONSTRUCTOR WITH INPUT SIZES
				Constructor<?> con = environment.getConstructor(new Class[]{Double.class, Double.class, Double.class, Double.class, Double.class});
				this.intestine_env = con.newInstance(Double.parseDouble(simEnvironment.get(1).toString()),
						Double.parseDouble(simEnvironment.get(2).toString()),
						Double.parseDouble(simEnvironment.get(3).toString()),
						Double.parseDouble(simEnvironment.get(4).toString()),
						Double.parseDouble(simEnvironment.get(5).toString()));
				
				// CONSTRUCTORS WITHOUT
				//Constructor<?> con = environment.getConstructor(new Class[]{});
				//this.intestine_env = con.newInstance();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	
		
		
		// 5. Calculate the environment growth rate per minute. 
		
		// As growth time is in hours (to standardise with all other variables), needs converting to seconds
		//heightGrowth = (simParams.upperGridHeight-simParams.initialGridHeight)/((simParams.growthTime*60)*60);
		//lengthGrowth = (simParams.upperGridLength-simParams.initialGridLength)/((simParams.growthTime*60)*60);
		
		// 6. Set up grid to use to calculate chemokine diffusion
		
		try
		{
			chemoGrid = new ChemokineGrid(this.intestine_env.getClass().getDeclaredField("initialGridLength").getDouble(this.intestine_env),
				this.intestine_env.getClass().getDeclaredField("initialGridLength").getDouble(this.intestine_env));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		// B: Start the simulation back functions
		super.start();
		
		// C: Place the LTo and Decoy cells on the stroma
		// GET THE SIZE OF AN LTO CELL (OR STROMAL CELL OBJECT)
		stromalCellEnvironment = new Setup_Stromal_Cell_Distribution(this,this.simulationSpec.enviromentCells);
				
		// G: Begin cell tracking if enabled - tracks cells over a set period, captures images of the tract etc
		if(this.simulationSpec.cellTrackingEnabled)
		{
			cellTrackStats = new CellTracking(this);
			cellTrackStats.setStopper(schedule.scheduleRepeating(cellTrackStats));
		}
		
		// D: Initialise Cell Input Control
		hemCells = new CellInputControl(schedule,this,this.simulationSpec.cells);	
		hemCells.setStopper(schedule.scheduleRepeating(hemCells));
		
		// E: Initialise Patch Statistics collection
		if(this.simulationSpec.patchStatsOutputHours != null)
		{
			patchStatsGeneration = new PatchStatistics(this);
			patchStatsGeneration.setStopper(schedule.scheduleRepeating(patchStatsGeneration));
		}
		
		
	}
	
	/**
	 * Creates an array of cell colours to use for the on screen display
	 */
	public void popColourArray()
    {
    	PPatchSim.cellColours.add(Color.black);
    	
    	PPatchSim.cellColours.add(Color.LIGHT_GRAY);   // lighter blue
    	PPatchSim.cellColours.add(Color.LIGHT_GRAY);   // lighter blue
    	PPatchSim.cellColours.add(Color.LIGHT_GRAY);   // lighter blue
		
    	// LTIN COLOURS
    	PPatchSim.cellColours.add(new Color(255,0,0));   // red
    	PPatchSim.cellColours.add(new Color(255,165,0));   // orange
    	PPatchSim.cellColours.add(Color.orange);
    	    	
    	// LTI COLOURS
    	PPatchSim.cellColours.add(new Color(0,255,0));   // green
    	PPatchSim.cellColours.add(new Color(0,255,0));   // green
    	
    	// red in twice as LTi changes state, yet no need to demonstrate this
    	PPatchSim.cellColours.add(Color.black);
    }
	
	
}
