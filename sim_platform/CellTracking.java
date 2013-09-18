package sim_platform;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * Class to track cells for a set period in the simulation.  This can be done for a number of periods
 * @author kieran
 *
 */
public class CellTracking implements Steppable,Stoppable
{
	
	
	/**
	 * <a name = "trackingStartHours"></a>
	 * <b>Description:<br></b> 
	 * ArrayList of just the tracking start hours.  Each is removed once that track is complete
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public ArrayList<Integer> trackingStartHours = new ArrayList<Integer>();
	
	/**
	 * <a name = "trackingEndHours"></a>
	 * <b>Description:<br></b> 
	 * ArrayList of just the tracking end hours.  Each is removed once that track is complete
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public ArrayList<Integer> trackingEndHours = new ArrayList<Integer>();
	
	/**
	 * <a name = "trackingSnapStartHr"></a>
	 * <b>Description:<br></b> 
	 * Hour at which the next set of tracking is due to start. Changes if more than one range is used
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal Only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public int trackingSnapStartHr;;
	
	/**
	 * <a name = "trackingSnapStartHr"></a>
	 * <b>Description:<br></b> 
	 * Hour at which the next set of tracking is due to end. Changes if more than one range is used
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal Only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * n/a
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public int trackingSnapEndHr;
	
	/**
	 * <a name = "trackedCells_Close"></a>
	 * <b>Description:<br></b> 
	 * Collection which stores all the LTi/LTin Cell Objects that are being tracked close from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Data Store
	 * <br><br>
	 */
	public Bag trackedCells_Close;
	
	/**
	 * <a name = "trackedCells_Close"></a>
	 * <b>Description:<br></b> 
	 * Collection which stores all the LTi/LTin Cell Objects that are being tracked away from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Data Store
	 * <br><br>
	 */
	public Bag trackedCells_Away;
	
	/**
	 * <a name = "averageLength"></a>
	 * <b>Description:<br></b> 
	 * Average length of cells tracked far from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Length Statistic
	 * <br><br>
	 */
	public double averageLength;
	
	/**
	 * <a name = "averageVelocity"></a>
	 * <b>Description:<br></b> 
	 * Average velocity of cells tracked far from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Velocity Statistic
	 * <br><br>
	 */
	public double averageVelocity;
	
	/**
	 * <a name = "averageDisplacement"></a>
	 * <b>Description:<br></b> 
	 * Average displacement of cells tracked far from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Displacement Statisitic
	 * <br><br>
	 */
	public double averageDisplacement;
	
	/**
	 * <a name = "averageDisplacementRate"></a>
	 * <b>Description:<br></b> 
	 * Average displacement rate of cells tracked far from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Displacement Statistic
	 * <br><br>
	 */
	public double averageDisplacementRate;
	
	/**
	 * <a name = "averageMeanderingIndex"></a>
	 * <b>Description:<br></b> 
	 * Average meandering index of cells tracked far from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Meandering Statistic
	 * <br><br>
	 */
	public double averageMeanderingIndex;
	
	/**
	 * <a name = "averageLengthNear"></a>
	 * <b>Description:<br></b> 
	 * Average length of cells tracked close from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Length Statistic
	 * <br><br>
	 */
	public double averageLengthNear;
	
	/**
	 * <a name = "averageVelocityNear"></a>
	 * <b>Description:<br></b> 
	 * Average velocity of cells tracked close from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Length Statistic
	 * <br><br>
	 */
	public double averageVelocityNear;
	
	/**
	 * <a name = "averageDisplacementNear"></a>
	 * <b>Description:<br></b> 
	 * Average displacement of cells tracked close from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Velocity Statistic
	 * <br><br>
	 */
	public double averageDisplacementNear;
	
	/**
	 * <a name = "averageDisplacementRateNear"></a>
	 * <b>Description:<br></b> 
	 * Average displacement rate of cells tracked close from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Displacement Statistic
	 * <br><br>
	 */
	public double averageDisplacementRateNear;
	
	/**
	 * <a name = "averageMeanderingIndexNear"></a>
	 * <b>Description:<br></b> 
	 * Average meandering index of cells tracked close from a patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Average Meandering Statistic
	 * <br><br>
	 */
	public double averageMeanderingIndexNear;
	
	/**
	 * <a name = "velocities"></a>
	 * <b>Description:<br></b> 
	 * Array used to hold the velocities of cells tracked away from a patch - used to calculate variance
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Array
	 * <br><br>
	 */
	public double[] velocities;
	
	/**
	 * <a name = "lengths"></a>
	 * <b>Description:<br></b> 
	 * Array used to hold the lengths of cells tracked away from a patch - used to calculate variance
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Array
	 * <br><br>
	 */
	public double[] lengths;
	
	/**
	 * <a name = "displacements"></a>
	 * <b>Description:<br></b> 
	 * Array used to hold the displacement of cells tracked away from a patch - used to calculate variance
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Array
	 * <br><br>
	 */
	public double[] displacements;
	
	/**
	 * <a name = "velocitiesNear"></a>
	 * <b>Description:<br></b> 
	 * Array used to hold the velocities of cells tracked close to a patch - used to calculate variance
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Array
	 * <br><br>
	 */
	public double[] velocitiesNear;
	
	/**
	 * <a name = "lengthsNear"></a>
	 * <b>Description:<br></b> 
	 * Array used to hold the lengths of cells tracked close to a patch - used to calculate variance
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Array
	 * <br><br>
	 */
	public double[] lengthsNear;
	
	/**
	 * <a name = "displacementsNear"></a>
	 * <b>Description:<br></b> 
	 * Array used to hold the displacements of cells tracked close to a patch - used to calculate variance
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Array
	 * <br><br>
	 */
	public double[] displacementsNear;
	
	/**
	 * File writer for cells close to a forming patch
	 */
	public FileWriter trackedCells_Close_Writer;
	
	public FileWriter trackedCells_Close_Writer_XML;
	
	/**
	 * File writer for cells away from a forming patch
	 */
	public FileWriter trackedCells_Away_Writer;
	
	public FileWriter trackedCells_Away_Writer_XML;
	
	/**
	 * File writer for the summary file, used in batch run situations, where the average of the displacement, length & velocity can then be calculated over
	 * multiple runs
	 */
	//public FileWriter summaryWriter;
	
	public FileWriter tableWriter; 
	
	
	/**
	 * Initilise the class and the bags in which the tracked cells are stored
	 * 
	 * @param sp	The simulation parameters used in this run
	 * @param ppsim	The current simulation state
	 */
	public CellTracking(PPatchSim ppsim)
	{
		if(ppsim.simulationSpec.cellTrackingEnabled)
		{
			this.processCellTrackingRanges(ppsim);
		}
		
		// Set up the bag that keeps track of the cells being tracked
		this.trackedCells_Close = new Bag();
		this.trackedCells_Away = new Bag();

	}
	
	public void processCellTrackingRanges(PPatchSim ppsim)
	{
		// split the ranges by the comma
		StringTokenizer st = new StringTokenizer(ppsim.simulationSpec.trackingHourRanges,",");
	
		// now process the start and end for each
		while(st.hasMoreTokens())
		{
			// now split the substring by the '-'
			StringTokenizer st2 = new StringTokenizer(st.nextToken(),"-");
			this.trackingStartHours.add(Integer.parseInt(st2.nextToken()));
			this.trackingEndHours.add(Integer.parseInt(st2.nextToken()));
		}
		
		// Set the first tracking range
		this.trackingSnapStartHr = this.trackingStartHours.get(0);
		this.trackingSnapEndHr = this.trackingEndHours.get(0);
	}
	
	
	/**
	 * Begin tracking at the required timepoint.  Where this occurs, cells that are in the far bag, yet are close enough to an LTo to be in the near bag,
	 * are moved as required
	 * 
	 * @param ppsim	The current simulation state
	 */
	public void beginTracking(PPatchSim ppsim)
	{		
		for(int k=0;k<this.trackedCells_Away.size();k++)
		{
			Object trackedCell = this.trackedCells_Away.get(k);
			
			if(findNearestLTo(trackedCell,ppsim)*4<=50)
			{
				this.trackedCells_Away.remove(trackedCell);
				this.trackedCells_Close.add(trackedCell);
				
			}
		}
	}
	
	public double calculateDisplacement(Object trackedCell)
	{
		double trackDisplacement = 0;
		
		try
		{
		
			Double2D agentTrackEndLocation = (Double2D)trackedCell.getClass().getDeclaredField("agentTrackEndLocation").get(trackedCell);
			Double2D agentTrackStartLocation = (Double2D)trackedCell.getClass().getDeclaredField("agentTrackStartLocation").get(trackedCell);
			
			trackDisplacement = distanceBetweenTwoPoints(agentTrackEndLocation,agentTrackStartLocation,0);
		
			if(trackDisplacement>200)		// must have rolled around the screen, and is therefore incorrect
			{
				if(agentTrackEndLocation.y<agentTrackStartLocation.y)
					trackDisplacement = distanceBetweenTwoPoints(agentTrackEndLocation,agentTrackStartLocation,254);
				else
					trackDisplacement = distanceBetweenTwoPoints(agentTrackEndLocation,agentTrackStartLocation,-254);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return trackDisplacement;
	}
	
	/**
	 * Output cell tracking results at the end of the tracking period.  These are output as two CSV files - one for close and one for far
	 * 
	 * @param ppsim	The current simulation state
	 * @param cellsTracked	The cells tracked (either far or away)
	 * @param outputTrackStatsWriter	The writer which writes the file
	 */
	public void outputTrackCellsResults(PPatchSim ppsim,Bag cellsTracked,FileWriter outputTrackStatsWriter,Document docWriting,Element rootElement)
	{
		// Initialise the averages
		this.averageDisplacement = 0;
		this.averageLength = 0;
		this.averageDisplacementRate = 0;
		this.averageVelocity = 0;
		this.averageMeanderingIndex = 0;
		
		
		this.velocities = new double[cellsTracked.size()];
		this.displacements = new double[cellsTracked.size()];
		this.lengths = new double[cellsTracked.size()];
				
		// Now write out the tracked cells to the file
		
		try
		{	
			for(int k=0;k<cellsTracked.size();k++)
			{	
				Object trackedCell = cellsTracked.get(k);
				
				// check that the cell was tracked for an hour
				Integer timeTracked = trackedCell.getClass().getDeclaredField("timeTracked").getInt(trackedCell);
				
				if(timeTracked == (3600/ppsim.simulationSpec.secondsPerStep))
				{
					// Set up the child node for XML output of this cell
					Element closeCell = docWriting.createElement("cell");
					rootElement.appendChild(closeCell);
					
					// CALCULATE CELL DISPLACEMENT (TAKING INTO ACCOUNT THE CELL MAY HAVE ROLLED AROUND THE SCREEN)
					double trackDisplacement = calculateDisplacement(trackedCell);
					
					// NOW THIS FUNCTION IS USED BOTH BY THE FILE WRITING AND WEB VERSIONS OF THE SIMULATION
					// SO ONLY WRITE TO FILE IF NOT ON THE WEB
					// IN ALL CASES, FIRST LINE IS CSV OUTPUT, NEXT 3 ARE XML NODE OUTPUT
					
					// CSV
					outputTrackStatsWriter.append(trackedCell.getClass().getName()+",");
					// XML
					Element cellType = docWriting.createElement("cellType");
					cellType.appendChild(docWriting.createTextNode(trackedCell.getClass().getName()));
					closeCell.appendChild(cellType);
						
					outputTrackStatsWriter.append(timeTracked+",");
					Element cellTimeSpan = docWriting.createElement("TimeSpan");
					cellTimeSpan.appendChild(docWriting.createTextNode(Integer.toString(timeTracked)));
					closeCell.appendChild(cellTimeSpan);
			
					// append cell state
					Integer trackedCellState = trackedCell.getClass().getDeclaredField("cellState").getInt(trackedCell);
					outputTrackStatsWriter.append(Integer.toString(trackedCellState)+",");
					Element cellState = docWriting.createElement("CellState");
					cellState.appendChild(docWriting.createTextNode(Integer.toString(trackedCellState)));
					closeCell.appendChild(cellState);
				
					// append cell speed
					Double trackedCellSpeed = trackedCell.getClass().getDeclaredField("cellState").getDouble(trackedCell);
					outputTrackStatsWriter.append(Double.toString(trackedCellSpeed*4)+",");
					Element cellSpeed = docWriting.createElement("CellSpeed");
					cellSpeed.appendChild(docWriting.createTextNode(Double.toString(trackedCellSpeed*4)));
					closeCell.appendChild(cellSpeed);
			
					// append the locations of where the cell started
					Double2D trackedCellStart = (Double2D)trackedCell.getClass().getDeclaredField("agentTrackStartLocation").get(trackedCell);
					outputTrackStatsWriter.append(Double.toString(trackedCellStart.x)+",");
					Element cellStartPositionX = docWriting.createElement("CellStartPositionX");
					cellStartPositionX.appendChild(docWriting.createTextNode(Double.toString(trackedCellStart.x)));
					closeCell.appendChild(cellStartPositionX);
						
					outputTrackStatsWriter.append(Double.toString(trackedCellStart.y)+",");
					Element cellStartPositionY = docWriting.createElement("CellStartPositionY");
					cellStartPositionY.appendChild(docWriting.createTextNode(Double.toString(trackedCellStart.y)));
					closeCell.appendChild(cellStartPositionY);
				
					// append the locations of where the cell ended up
					Double2D trackedCellEnd = (Double2D)trackedCell.getClass().getDeclaredField("agentTrackEndLocation").get(trackedCell);
					outputTrackStatsWriter.append(Double.toString(trackedCellEnd.x)+",");
					Element cellEndPositionX = docWriting.createElement("CellEndPositionX");
					cellEndPositionX.appendChild(docWriting.createTextNode(Double.toString(trackedCellEnd.x)));
					closeCell.appendChild(cellEndPositionX);
						
					outputTrackStatsWriter.append(Double.toString(trackedCellEnd.y)+",");
					Element cellEndPositionY = docWriting.createElement("CellEndPositionY");
					cellEndPositionY.appendChild(docWriting.createTextNode(Double.toString(trackedCellEnd.y)));
					closeCell.appendChild(cellEndPositionY);
				
					// TRACK LENGTH
					Double trackedCellLength = trackedCell.getClass().getDeclaredField("trackLength").getDouble(trackedCell);
					outputTrackStatsWriter.append(Double.toString((trackedCellLength)*4)+",");
					Element cellLength = docWriting.createElement("Length");
					cellLength.appendChild(docWriting.createTextNode(Double.toString((trackedCellLength)*4)));
					closeCell.appendChild(cellLength);

					// TRACK VELOCITY
					outputTrackStatsWriter.append(Double.toString(trackedCellLength*4/60)+",");
					Element cellVelocity = docWriting.createElement("Velocity");
					cellVelocity.appendChild(docWriting.createTextNode(Double.toString(trackedCellLength*4/60)));
					closeCell.appendChild(cellVelocity);
						
					// OUTPUT DISPLACEMENT
					outputTrackStatsWriter.append(Double.toString(trackDisplacement*4)+",");
					Element cellDisplacement = docWriting.createElement("Displacement");
					cellDisplacement.appendChild(docWriting.createTextNode(Double.toString(trackDisplacement*4)));
					closeCell.appendChild(cellDisplacement);
				
					// DISPLACEMENT RATE
					outputTrackStatsWriter.append(Double.toString(trackDisplacement*4/60)+",");
					Element cellDisplacementRate = docWriting.createElement("DisplacementRate");
					cellDisplacementRate.appendChild(docWriting.createTextNode(Double.toString(trackDisplacement*4/60)));
					closeCell.appendChild(cellDisplacementRate);
					// note always divided by 60 as after the displacement rate per minute
						
					// MEANDERING INDEX
					outputTrackStatsWriter.append(Double.toString(trackDisplacement/trackedCellLength)+",");
					Element cellMeanderingIndex = docWriting.createElement("MeanderingIndex");
					cellMeanderingIndex.appendChild(docWriting.createTextNode(Double.toString(trackDisplacement/trackedCellLength)));
					closeCell.appendChild(cellMeanderingIndex);
					
					// CLOSEST LTO
					outputTrackStatsWriter.append(Double.toString(findNearestLTo(trackedCell,ppsim)*4)+",");
					outputTrackStatsWriter.append("\n");
						
					Element cellClosestLTo = docWriting.createElement("NearestLToCell");
					cellClosestLTo.appendChild(docWriting.createTextNode(Double.toString(findNearestLTo(trackedCell,ppsim)*4)));
					closeCell.appendChild(cellClosestLTo);
					
					// ADD TO THE ARRAYS USED TO DO MANN-WHITNEY
					displacements[k] = trackDisplacement*4;
					velocities[k] = trackedCellLength*4/60;
					lengths[k] = trackedCellLength*4;
					
					// ADD TO THE STATS TO CALCULATE AVERAGES
					averageLength += (trackedCellLength*4);
					averageVelocity += (trackedCellLength*4)/60;
					averageDisplacement += (trackDisplacement*4);
					averageDisplacementRate += (trackDisplacement*4/60);
					averageMeanderingIndex += (trackDisplacement/trackedCellLength);
				}
			}
			
			int divider = 0;
			divider = cellsTracked.size();
			
			this.averageDisplacement = this.averageDisplacement/divider;
			this.averageLength = this.averageLength/divider;
			this.averageDisplacementRate = this.averageDisplacementRate/divider;
			this.averageVelocity = this.averageVelocity/divider;
			this.averageMeanderingIndex = this.averageMeanderingIndex/divider;
			
			
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates the distance between two coordinates
	 * 
	 * @param endloc	Where the cell has moved to
	 * @param startloc	Where the cell started
	 * @param adjuster	Adjuster applied to the calculation where the cell has rolled around the screen
	 * @return
	 */
	public double distanceBetweenTwoPoints(Double2D endloc, Double2D startloc, double adjuster)
	{
		return Math.sqrt(
				Math.pow(endloc.x - startloc.x,2)
				+
				Math.pow((endloc.y - startloc.y)+adjuster,2)
				);
	}
	
	/**
	 * Finds nearest LTo cell to the LTin/LTi
	 * 
	 * @param trackedCell	The cell being examined
	 * @param ppsim	The current simulation state
	 * @return	The distance to the nearest LTo
	 */
	public double findNearestLTo(Object trackedCell,PPatchSim ppsim)
	{
		// now go through each LTo and work out where the nearest active LTo to a tracked cell is
		double closestLTo= Double.POSITIVE_INFINITY;
		double distance;
		
		for(int l=0;l<ppsim.ltoCellsBag.size();l++)
		{
			try
			{
				Object ltoCell = ppsim.ltoCellsBag.get(l);
		
				// Get the state of this object
				Integer cellState = ltoCell.getClass().getDeclaredField("cellState").getInt(ltoCell);
				
				if(cellState>=1)
				{
					// Get the locations of the tracked cell
					// this is a bit of a hack and will need sorting propertly later (as this should no longer be accepting Cells)
					Double2D trackedCellLocation = (Double2D)trackedCell.getClass().getDeclaredField("agentLocation").get(trackedCell);
					
					// Get the location of this LTo cell
					Double2D lToCellLocation = (Double2D)ltoCell.getClass().getDeclaredField("agentLocation").get(ltoCell);
					
					// calculate the distance from this cell to the LTo
					distance = (Math.sqrt((Math.pow(trackedCellLocation.x-lToCellLocation.x,2)+Math.pow(trackedCellLocation.y-lToCellLocation.y,2))));
					if(distance<closestLTo)
					{
						closestLTo = distance;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		}
		return closestLTo;
		
	}
	
	/**
	 * Generates a summary file at the run end showing averages of cell measures.  Not really used and may be removed
	 * @param ppsim
	 */
	public void generateStatsAtRunEnd(PPatchSim ppsim)
	{
		try 
		{	
			// XML OUTPUT SETUP VARIABLES - FIRST FOUR ARE THE XML DOCUMENT RELATED, NEXT ARE FOR FORMATTING
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document docWritingClose = docBuilder.newDocument();
			Document docWritingAway = docBuilder.newDocument();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			
			// OUTPUT THE CELL TRACKING STATS TO CSV
			
			trackedCells_Close_Writer = new FileWriter(ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/Results/"+ppsim.simulationSpec.runReplicate+"/trackedCells_Close_"+this.trackingSnapStartHr+".csv");
			
			//trackedCells_Close_Writer = new FileWriter("Results/"+ppsim.simulationSpec.runReplicate+"/trackedCells_Close_"+this.trackingSnapStartHr+".csv");
			trackedCells_Close_Writer.append("Cell Type,Time Span,Cell State,Cell Speed,Cell Start Position X,Cell Start Position Y,Cell End Position X,Cell End Position Y,Length,Velocity,Displacement,Displacement Rate,Meandering Index,Nearest LTo Cell (microns)\n");
			
			//trackedCells_Away_Writer = new FileWriter("Results/"+ppsim.simulationSpec.runReplicate+"/trackedCells_Away_"+this.trackingSnapStartHr+".csv");
			trackedCells_Away_Writer = new FileWriter(ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/Results/"+ppsim.simulationSpec.runReplicate+"/trackedCells_Away_"+this.trackingSnapStartHr+".csv");
			trackedCells_Away_Writer.append("Cell Type,Time Span,Cell State,Cell Speed,Cell Start Position X,Cell Start Position Y,Cell End Position X,Cell End Position Y,Length,Velocity,Displacement,Displacement Rate,Meandering Index,Nearest LTo Cell (microns)\n");
				
			// SETUP THE XML FILES FOR CELL TRACKING RESULTS
			// CLOSE
			Element rootElementClose = docWritingClose.createElement("SimulationResult");
			docWritingClose.appendChild(rootElementClose);
			// AWAY
			Element rootElementAway = docWritingAway.createElement("SimulationResult");
			docWritingAway.appendChild(rootElementAway);
				
			// RUN THE OUTPUT CELL TRACKS WHETHER WRITING TO FILE OR NOT - USED BY BOTH THE WEB AND NON WEB VERSIONS
			// write the tracks of cells close to the stromal cells
			this.outputTrackCellsResults(ppsim,this.trackedCells_Close,trackedCells_Close_Writer,docWritingClose,rootElementClose);
			
			// WRITE OUT THE CLOSE XML FILE, FORMATTED CORRECTLY
			docWritingClose.normalizeDocument();
			DOMSource source = new DOMSource(docWritingClose);
			String xmlOutputAddress = ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/Results/"+ppsim.simulationSpec.runReplicate+"/";
			StreamResult result =  new StreamResult(new File(xmlOutputAddress +"trackedCells_Close_"+this.trackingSnapStartHr+".xml"));
			transformer.transform(source, result);	
			
			this.averageLengthNear = this.averageLength;
			this.averageVelocityNear = this.averageVelocity;
			this.averageDisplacementNear = this.averageDisplacement;
			this.averageDisplacementRateNear = this.averageDisplacementRate;
			this.averageMeanderingIndexNear = this.averageMeanderingIndex;
			this.velocitiesNear = this.velocities;
			this.displacementsNear = this.displacements;
			this.lengthsNear = this.lengths;
			
			// write the tracks of the cells away from the stromal cell
			this.outputTrackCellsResults(ppsim,this.trackedCells_Away,trackedCells_Away_Writer,docWritingAway,rootElementAway);
			// IN XML FORMAT:
			docWritingAway.normalizeDocument();
			source = new DOMSource(docWritingAway);
			result =  new StreamResult(new File(xmlOutputAddress +"trackedCells_Away_"+this.trackingSnapStartHr+".xml"));
			transformer.transform(source, result);
			
			// CLOSE THE CSV FILES
			trackedCells_Close_Writer.close();
			trackedCells_Away_Writer.close();

			
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	

	/**
	 * Performed each step in the simulation - monitors the two tracked bags during cell tracking, moving cells between them as necessary
	 */
	public void step(final SimState state)
	{
		PPatchSim ppsim = (PPatchSim)state;
		
		// while the simulation is running
		if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep <= ((ppsim.simulationSpec.simulationTime*60)*60))
		{
			if(ppsim.simulationSpec.cellTrackingEnabled)
			{
				// THERE WILL BE CELLS IN THE BAG BEING TRACKED, THOUGH TRACKING IS YET TO START
				// SET THE TRACK START LOCATION OF ALL OF THESE CELLS NOW THE TIME HAS COMMENCED
				// IF MULTIPLE TRACKING RANGES ARE USED, BOTH BAGS WILL NEED TO BE RESET
				if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep == ((this.trackingSnapStartHr*60)*60))
				{	
					for(int k=0;k<this.trackedCells_Away.size();k++)
					{
						Object trackedCell = this.trackedCells_Away.get(k);
						
						try
						{
							trackedCell.getClass().getDeclaredField("agentTrackStartLocation").set(trackedCell, trackedCell.getClass().getDeclaredField("agentLocation").get(trackedCell));
							trackedCell.getClass().getDeclaredField("agentTrackEndLocation").set(trackedCell,null);
							trackedCell.getClass().getDeclaredField("timeTracked").setInt(trackedCell,0);
							trackedCell.getClass().getDeclaredField("trackLength").setInt(trackedCell,0);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					
					for(int k=0;k<this.trackedCells_Close.size();k++)
					{
						Object trackedCell = this.trackedCells_Close.get(k);
						
						try
						{
							trackedCell.getClass().getDeclaredField("agentTrackStartLocation").set(trackedCell, trackedCell.getClass().getDeclaredField("agentLocation").get(trackedCell));
							trackedCell.getClass().getDeclaredField("agentTrackEndLocation").set(trackedCell,null);
							trackedCell.getClass().getDeclaredField("timeTracked").setInt(trackedCell,0);
							trackedCell.getClass().getDeclaredField("trackLength").setInt(trackedCell,0);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}

					}
				}
				
				
				// TRACKING HAS NOW ENDED - ADD THE END LOCATION FOR ALL CELLS THAT ARE STILL BEING TRACKED
				else if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep == ((this.trackingSnapEndHr*60)*60))
				{
					for(int k=0;k<this.trackedCells_Away.size();k++)
					{
						try
						{
							Object trackedCell = this.trackedCells_Away.get(k);
							
							if(trackedCell.getClass().getDeclaredField("agentTrackEndLocation").get(trackedCell) == null)
							{
								trackedCell.getClass().getDeclaredField("agentTrackEndLocation").set(trackedCell,trackedCell.getClass().getDeclaredField("agentLocation").get(trackedCell));
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						
					}
					
					for(int k=0;k<this.trackedCells_Close.size();k++)
					{
						try
						{
							Object trackedCell = this.trackedCells_Close.get(k);
							
							if(trackedCell.getClass().getDeclaredField("agentTrackEndLocation").get(trackedCell) == null)
							{
								trackedCell.getClass().getDeclaredField("agentTrackEndLocation").set(trackedCell,trackedCell.getClass().getDeclaredField("agentLocation").get(trackedCell));
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
	
					}
					
					// NOW TRACKING HAS ENDED, PREPARE FOR THE NEXT TRACKING RANGE
					// FIRSTLY OUTPUT THE CURRENT TRACKED RANGE
					this.generateStatsAtRunEnd(ppsim);
					
					// delete this tracked range from those being done
					this.trackingStartHours.remove(0);
					this.trackingEndHours.remove(0);
					// set the next range (if there is one)
					if(!this.trackingStartHours.isEmpty())
					{
						this.trackingSnapStartHr = this.trackingStartHours.get(0);
						this.trackingSnapEndHr = this.trackingEndHours.get(0);
					}
				}
			}
		}
		else
		{
			this.stop();
		}
		
	}
	
	/**
	 * Flag to show if this class has been stopped (when no longer needed)
	 */
	private Stoppable stopper = null;
	
	/**
	 * Method to change the value of the stopper
	 * @param stopper	Whether the class should be stopped or not
	 */
    public void setStopper(Stoppable stopper)   {this.stopper = stopper;}
    
    /**
     * Method to stop the class where necessary
     */
    public void stop(){stopper.stop();}
}
