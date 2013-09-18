package sim_platform;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Setup_Simulation 
{
	
	/**
	 * <a name = "description"></a>
	 * <b>Description:<br></b> 
	 * Short description of this run, used to create the output folder containing all the data 
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Text String
	 */
	public String experimentDescription;
	
	/**
	 * Full path to the XML file created by the interface
	 */
	public String xmlFilePath;
	
	/**
	 * Number representing a replicate number of this run.
	 * e.g. for a sensitivity analysis experiment, this could simply be a run number
	 */
	public String runReplicate;
	
	
	/**
	 * <a name = "filePath"></a>
	 * <b>Description:<br></b> 
	 * FilePath of where the results files should be output to
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Text String
	 */
	public String resultStoreFilePath;
	
	/**
	 * Arraylist of the cells that take part in this simulation and related parameters
	 */
	public ArrayList<ArrayList<Object>> cells;
	
	/**
	 * Arraylist to hold the details of the environment specified in this simulation
	 */
	public ArrayList<ArrayList<Object>> envInfo;
	
	/**
	 * Arraylist of stroma associated cells
	 */
	public ArrayList<Object> enviromentCells;
	
	/**
	 * <a name = "secondsPerStep"></a>
	 * <b>Description:<br></b> 
	 * The number of seconds of development time represented by 1 step of the simulation
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be numeric and above 0
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Seconds
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double secondsPerStep;

	/**
	 * <a name = "simulationTime"></a>
	 * <b>Description:<br></b> 
	 * The number of hours the simulation will run (in developmental time)
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be numeric and above 0
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Seconds
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double simulationTime;
	
	/**
	 * <a name = "twelveHourSnaps"></a>
	 * <b>Description:<br></b> 
	 * Knockout flag determining if the simulation is taking snapshots at 12 hour intervals
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be true or false
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * n/a
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public Boolean twelveHourSnaps;
	
	/**
	 * <a name = "stepBystepTrackingImages"></a>
	 * <b>Description:<br></b> 
	 * Knockout flag determining if the simulation is taking snapshots (for movie generation)
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be true or false
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * n/a
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public Boolean stepBystepTrackingImages;
	
	/**
	 * <a name = "cellTrackingEnabled"></a>
	 * <b>Description:<br></b> 
	 * Knockout flag determining if cell tracking is on
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be true or false
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * n/a
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public Boolean cellTrackingEnabled;
	
	/**
	 * <a name = "trackingHourRanges"></a>
	 * <b>Description:<br></b> 
	 * Hour ranges at which cell tracking should be performed.
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Should be a string of two hours, separated by -, with the ranges separated by a ,
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public String trackingHourRanges;
	
	/**
	 * <a name = "generateLToStats"></a>
	 * <b>Description:<br></b> 
	 * Knockout flag determining if the simulation is producing statistics of LTo data
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be true or false
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * n/a
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public Boolean generateLToStats;
	
	/**
	 * <a name = "patchStatsOutputHours"></a>
	 * <b>Description:<br></b> 
	 * List of hours LTo and patch stats should be output, enabling analysis to be done over time
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * List of numbers separated by commas
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public String patchStatsOutputHours;
	
	/**
	 * Constructor - reads in the XML file and processes the classes that will be part of this 
	 * simulation
	 */
	public Setup_Simulation(String xmlFileLocation,String runRep)
	{
		try
		{
			this.xmlFilePath = xmlFileLocation;
			this.runReplicate = runRep;
			
			// Set up the necessary storage for environment and component objects
			this.cells = new ArrayList<ArrayList<Object>>();
			this.envInfo = new ArrayList<ArrayList<Object>>();
			this.enviromentCells = new ArrayList<Object>();
			
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc;
			
			doc = docBuilder.parse(new File(xmlFileLocation));
			
			NodeList allGroups = doc.getDocumentElement().getChildNodes();   // ALL CHILD NODES OF XML
			
			/**
			 * First is the experimental file setup detail - file store and description
			 */
			NodeList filepathComponents = allGroups.item(0).getChildNodes();
			this.setupSimulationOutputFolders(filepathComponents,runRep);
			
			/**
			 * Simulation Specific parameters - i.e. number of seconds per step, etc
			 */
			NodeList baselineComponents = allGroups.item(1).getChildNodes();
			this.processSimulationPlatformParams(baselineComponents);
			
			/**
			 * Deal with each cellular component that was specified first
			 */
			NodeList allComponents = allGroups.item(2).getChildNodes();
			
			for(int i=0;i<allComponents.getLength();i++)
			{
				// Get the component
				NodeList simulationComponent = allComponents.item(i).getChildNodes();
				this.processSimulationComponent(simulationComponent);
				
			}
			
			/**
			 * Now to specify the environment to be used with this simulation
			 */
			NodeList environmentComponent = allGroups.item(3).getChildNodes();
			
			for(int i=0;i<environmentComponent.getLength();i++)
			{
				// Get the component
				NodeList oneEnvironmentComponent = environmentComponent.item(i).getChildNodes();
				this.processEnvironmentComponent(oneEnvironmentComponent);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setupSimulationOutputFolders(NodeList filepathComponents,String runReplicate)
	{
		this.resultStoreFilePath = filepathComponents.item(1).getTextContent();
		this.experimentDescription = filepathComponents.item(2).getTextContent();
		
		// Will store all in the Results folder that was created by the simulator
		// Thus it is vital that the simulation is run in the folder it was placed (though the whole structure can be moved)
		new File(this.resultStoreFilePath+"/"+this.experimentDescription+"/Results/"+runReplicate).mkdir();
	}
	
	public void processSimulationPlatformParams(NodeList simulationParams)
	{
		// First Node is the simulation platform - not required in this instance
		
		// Next is a set of child nodes that contain simulation specific parameters
		NodeList simSpecificParams = simulationParams.item(1).getChildNodes();
		
		this.secondsPerStep = Double.parseDouble(simSpecificParams.item(0).getTextContent());
		this.simulationTime = Double.parseDouble(simSpecificParams.item(1).getTextContent());
		this.stepBystepTrackingImages = Boolean.parseBoolean(simSpecificParams.item(2).getTextContent());
		this.twelveHourSnaps = Boolean.parseBoolean(simSpecificParams.item(3).getTextContent());
		this.cellTrackingEnabled = Boolean.parseBoolean(simSpecificParams.item(4).getTextContent());
		this.trackingHourRanges = simSpecificParams.item(5).getTextContent();
		this.generateLToStats = Boolean.parseBoolean(simSpecificParams.item(6).getTextContent());
		this.patchStatsOutputHours = simSpecificParams.item(7).getTextContent();
		
		if(this.patchStatsOutputHours.equals("NULL"))
			this.patchStatsOutputHours = null;

	}
	
	public void processSimulationComponent(NodeList simulationComponent)
	{
		// Each cell type is described by an arraylist - set this up for this cell object
		ArrayList<Object> cellInfo = new ArrayList<Object>();
		
		// First item is the class of this simulation component - add this to the array
		cellInfo.add(simulationComponent.item(0).getTextContent());
		//System.out.println(simulationComponent.item(0).getTextContent());
		
		// Now get the parameters associated with it
		
		NodeList componentParameters = simulationComponent.item(1).getChildNodes();
		for(int k=0;k<componentParameters.getLength();k++)
		{
			// Now need to get the Parameter Child node
			NodeList paramChilds = componentParameters.item(k).getChildNodes();
			
			// 0 is the parameter name, 1 is the value. We're only adding values
			cellInfo.add(paramChilds.item(1).getTextContent());
			System.out.println(paramChilds.item(1).getTextContent());
		}
		
		// Now deal with the receptors that this cell may express
		NodeList componentReceptors = simulationComponent.item(2).getChildNodes();
		
		// Set up an array to hold these descriptions
		ArrayList<Object> cellReceptors = new ArrayList<Object>();
		
		for(int k=0;k<componentReceptors.getLength();k++)
		{
			// Now get each receptor
			// Each receptor will also have its own array, holding the class and related parameters
			ArrayList<String> receptorDetail = new ArrayList<String>();
			
			NodeList receptorDetailNodes = componentReceptors.item(k).getChildNodes();
			
			// The first item is the name of the java class of this receptor - add this to the array
			receptorDetail.add(receptorDetailNodes.item(0).getTextContent());
			
			// This is followed by a Parameters tag, within which each parameter is a child tag
			
			NodeList receptorParameters = receptorDetailNodes.item(1).getChildNodes();
			
			// Now iterate through each parameter
			for(int l=0;l<receptorParameters.getLength();l++)
			{
				// each parameter is a set of child tags - one for name and one for value
				NodeList recepParamChilds = receptorParameters.item(l).getChildNodes();
				
				// 0 is the parameter name, 1 is the value. We're only adding values
				receptorDetail.add(recepParamChilds.item(1).getTextContent());
				
				//System.out.println(recepParamChilds.item(1).getTextContent());
				//cellInfo.add(paramChilds.item(1).getTextContent());
				
				//receptorDetail.add(receptorDetailNodes.item(l).getTextContent());
				//System.out.println(receptorParameters.item(l).getTextContent());
			}
			cellReceptors.add(receptorDetail);
		}
		
		cellInfo.add(cellReceptors);
		
		// Now add this to the global list of components in this simulation
		this.cells.add(cellInfo);
		
	}
	
	public void processEnvironmentComponent(NodeList envComponent)
	{
		// Each environment type is described by an arraylist - set this up for this compartment object
		ArrayList<Object> compartmentInfo = new ArrayList<Object>();
		
		// First item is the class of this simulation component - add this to the array
		compartmentInfo.add(envComponent.item(0).getTextContent());
		
		// Now get the parameters associated with it
		// These are in a Parameters tag, and each in a further Parameter Child
		
		NodeList envParameters = envComponent.item(1).getChildNodes();
		for(int k=0;k<envParameters.getLength();k++)
		{
			// Now need to get the Parameter Child node
			NodeList paramChilds = envParameters.item(k).getChildNodes();
			
			// 0 is the parameter name, 1 is the value. We're only adding values
			//System.out.println(paramChilds.item(1).getTextContent());
			compartmentInfo.add(paramChilds.item(1).getTextContent());
		}
		
		// Now deal with the stromal based cells (last item)
		
		NodeList environmentCells = envComponent.item(2).getChildNodes();
		
		
		// Set up an array to hold these descriptions
		ArrayList<String> envBasedCells = new ArrayList<String>();
		
		if(environmentCells.getLength()>0)
		{
			for(int j=0;j<environmentCells.getLength();j++)
			{
				// Get each receptor and add this to an arraylist for this cell
				// These are dealt with when the cell object is created
				envBasedCells.add(environmentCells.item(j).getTextContent());
			}
		}
		
		// Add the receptors to the environment info array
		compartmentInfo.add(envBasedCells);
		
		// Now add this to the global list of components in this simulation
		this.envInfo.add(compartmentInfo);
		
		//System.out.println(envInfo);
	}
	
}
