package sim_platform;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JFrame;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;


/**
 * The display class for the Peyer's Patch Simulation.  Attaches and monitors a graphical display which 
 * shows the cell behaviour within the tract
 * 
 * @author Kieran Alden
 *
 */
public class PPatchSimUI extends GUIState
{
	
	/**
	 * Full path to the XML file created by the interface
	 */
	public String xmlFilePath;
	
	/**
	 * Short description of this run.
	 * e.g. for a sensitivity analysis experiment, this could simply be a run number
	 */
	public String runFilePath;
	
	/**
	 * MASON Console which controls the simulation (when run in graphical mode)
	 */
    public static Console c;
    
    /**
	 * If run with a GUI, this forms the Continuous grid onto which the cells will be mapped onto (using x and y coordinates)
	 */
    public static ContinuousPortrayal2D tractPortrayal = new ContinuousPortrayal2D();
    
    /**
	 * If run with a GUI, this is the frame used within the display to contain the intestine tract 
	 */
    public static JFrame displayFrame;   
    
    /**
     * Folder where simulation results should be output to
     */
    //public String filePath;
    
    /**
     * Description of this run - used to form the results folder name
     */
    //public String runDescription;
    
    /**
     * Full file path to the parameter XML file
     */
    //public String paramFilePath;
    
	
	/**
	 * Main Class - Begins the simulation in graphical mode
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Set up the simulation display & console
		// Check whether running from the command line or eclipse
		
		if(args.length > 0)
		{
			// Command line run (arguments should have been supplied!)			
			PPatchSimUI ppatchdisp = new PPatchSimUI(args[0], args[1]);
			
		}
		else
		{
			// Assume Eclipse Run (though will enter here if command line and no args supplied)
			// Else an exception will be generated
			PPatchSimUI ppatchdisp = new PPatchSimUI("/home/kieran/workspace/C2D2/TestForRichard/TestForRichard.xml","1");
		}
		
		
		//if(args.length > 0)
		//{
			// Command line run (arguments should have been supplied!)
			//PPatchSimUI ppatchdisp = new PPatchSimUI(args[0],args[1]);
			
			//PPatchSimUI ppatchdisp = new PPatchSimUI("/home/kieran/workspace/C2D2/robustness_150413/robustness_150413.xml", "1");
			
		//PPatchSimUI ppatchdisp = new PPatchSimUI("/C2D2/SimulationPlatforms/PPSim/DualParamAATest.xml");
		/*}
		else
		{
			// Assume Eclipse Run (though will enter here if command line and no args supplied)
			// Else an exception will be generated
			String runDescription = "NoThreshold";
			String paramFilePath = "/home/kieran/Desktop/CutLTinNumbers/parameters_45.xml";
			String filePath = "/home/kieran/workspace/SimRunTestBed/";
			
			PPatchSimUI ppatchdisp = new PPatchSimUI(runDescription,paramFilePath,filePath);
		}*/
	}
	
	/**
	 * Constructor - attaches the simulation to the graphics class
	 */
	public PPatchSimUI(String xmlFile, String desc)
	{		
		super(new PPatchSim(System.currentTimeMillis()));
		
		this.xmlFilePath = xmlFile;
		this.runFilePath = desc;
		
		c = new Console(this);
		c.setVisible(true);
		
		// set the console to always appear in the bottom right hand corner (not covering model)
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// Note 30 is taken from calculation so that the console appears above / away from any toolbars
		// on the edge of the screen
		c.setLocation((dim.width-c.getSize().width-30),(dim.height-c.getSize().height-30));
		c.pressPlay();
		
	}
	
	

	
	public PPatchSimUI(SimState state)
	{
		super(state);
	}
	
	/**
	 * Sets name of the display window
	 * @return Console Window Name
	 */
	public static String getName()
	{
		return "Peyer's Patch Simulation";
	}
	
	/**
	 * Begins the simulation when the start button is pressed
	 */
	public void start()
	{
		super.start();
		setupPortrayals();
				
	}
	
	/**
	 * Reloads the presentation if the stop button is pressed
	 */
	public void load(SimState state)
	{
		super.load(state);
		setupPortrayals();
	}
	
	/**
	 * Determines how each cell will be represented on the tract
	 */
	public void setupPortrayals()
	{
		try
		{
			PPatchSim ppsim = (PPatchSim)state;
			//tractPortrayal.setField(ppsim.intestine_env.tract);
			Field environment = ppsim.intestine_env.getClass().getDeclaredField("tract");
			tractPortrayal.setField(environment.get(ppsim.intestine_env));
			ppsim.display.reset();
			ppsim.display.repaint();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
	}	
	
	/**
	 * Initialises the display once the simulation starts & creates the relevant window
	 */
	public void init(Controller c)
	{	
		PPatchSim ppsim = (PPatchSim)state;	

		ppsim.simulationSpec = new Setup_Simulation(this.xmlFilePath,this.runFilePath);
		
		
		try
		{
			// NOTE THAT THE READ IN PROCESSES GENERIC XML FILES CREATED BY THE INTERFACE
			// WHERE SOME SIMUALTIONS CAN HAVE MORE THAN ONE COMPARTMENT
			// AS THIS IS NEVER THE CASE HERE, WE SIMPLY READ THE FIRST ARRAYLIST FROM THE ENVINFO ATTRIBUTE
			
			ArrayList<Object> simEnvironment = ppsim.simulationSpec.envInfo.get(0);
			
			Class<?> environment = Class.forName(simEnvironment.get(0).toString());
			
			// CONSTRUCTOR WITH INPUT SIZES
			Constructor<?> con = environment.getConstructor(new Class[]{Double.class, Double.class, Double.class, Double.class, Double.class});
			ppsim.intestine_env = con.newInstance(Double.parseDouble(simEnvironment.get(1).toString()),
					Double.parseDouble(simEnvironment.get(2).toString()),
					Double.parseDouble(simEnvironment.get(3).toString()),
					Double.parseDouble(simEnvironment.get(4).toString()),
					Double.parseDouble(simEnvironment.get(5).toString()));
				
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		// Set up the display to the dimensions the user entered
		try
		{
			//ppsim.display = new Display2D(ppsim.simParams.initialGridLength,ppsim.simParams.initialGridHeight,this,1);
			ppsim.display = new Display2D(ppsim.intestine_env.getClass().getDeclaredField("initialGridLength").getDouble(ppsim.intestine_env),
					ppsim.intestine_env.getClass().getDeclaredField("initialGridHeight").getDouble(ppsim.intestine_env),this,1);
		
		
			// create the display frame to hold it
			displayFrame = ppsim.display.createFrame();
			
			// initialise all features of the simulation software
			super.init(c);
			
			// Set the display frame to be the width of the users screen, plus around 400 (so as much of intestine as possible is shown)
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			displayFrame.setSize(dim.width, 400);
			c.registerFrame(displayFrame);
	
			// attach all representations of the environment & cells
			ppsim.display.attach(tractPortrayal,"Agents");
			
			// Get the colour from the environment class
			ppsim.display.setBackdrop((Color)ppsim.intestine_env.getClass().getDeclaredField("backdrop").get(ppsim.intestine_env));
			//ppsim.display.setBackdrop(Color.DARK_GRAY);
			displayFrame.setTitle("Intestine Tract Display");
			
			displayFrame.setVisible(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	
	}
	
	/**
	 * Destroys the frame when the simulation ends
	 */
	public void quit()
	{
		//super.quit();
		
		//if (displayFrame!=null) displayFrame.dispose();
        //	displayFrame = null;
        //PPatchSim.display = null;
	}
	
	/**
	 * Returns the state - used for inspection of variables within the console
	 */
	public Object getSimulationInspectedObject()
	{
		return state;
	}
	
	/**
	 * Set up an inspector with which to use to monitor variables and create graphs within the console
	 */
	public Inspector getInspector()
	{
		Inspector i = super.getInspector();
		i.setVolatile(true);
		return i;
	}
}