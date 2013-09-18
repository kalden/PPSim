package sim_platform;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
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
 * Class to produce statistics on PP location, size etc at the end of the simulation
 * @author kieran
 *
 */
public class PatchStatistics implements Steppable,Stoppable
{
	/**
	 * Writer to output just the LTi cells that are in a patch to a CSV file
	 */
	public FileWriter patchWriter;
	
	/**
	 * Writer to output the positions of all LTi cells to a CSV file
	 */
	public FileWriter patchWriter2;
	
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
    
    
	
	/**
	 * <a name = "patchStatHours"></a>
	 * <b>Description:<br></b> 
	 * ArrayList of patch stat output hours.  Each is removed once that hour is complete
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public ArrayList<Integer> patchStatHours = new ArrayList<Integer>();
	
	/**
	 * <a name = "ltoStatHours"></a>
	 * <b>Description:<br></b> 
	 * ArrayList of lto stat output hours.  Each is removed once that hour is complete
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public ArrayList<Integer> ltoStatHours = new ArrayList<Integer>();
	
	/**
	 * <a name = "nextPatchOutputHour"></a>
	 * <b>Description:<br></b> 
	 * Next hour at which patch statistics will be output - reset as this passes
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double nextPatchOutputHour;
	
	/**
	 * <a name = "nextLToStatOutputHour"></a>
	 * <b>Description:<br></b> 
	 * Next hour at which LTo statistics will be output - reset as this passes
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Internal only
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public int nextLToStatOutputHour;

	/**
	 * Initialise the class and writer
	 * 
	 * @param sp	The current simulation parameters
	 */
	public PatchStatistics(PPatchSim ppsim)
	{
		if(ppsim.simulationSpec.generateLToStats)
		{
			this.processPatchStatsRanges(ppsim);
		}
	}
	
	public void processPatchStatsRanges(PPatchSim ppsim)
	{
		// Now split the hours output is required
    	StringTokenizer st = new StringTokenizer(ppsim.simulationSpec.patchStatsOutputHours,",");
    		
    	// now process the start and end for each
    	while(st.hasMoreTokens())
    	{
    		// now add the hour to the array
    		int statHour = Integer.parseInt(st.nextToken());
    		this.patchStatHours.add(statHour);
    		this.ltoStatHours.add(statHour);
    	}
    		
    	// Set the first output hour - this is reset as this passes
    	this.nextPatchOutputHour = this.patchStatHours.get(0);
    	this.nextLToStatOutputHour = this.ltoStatHours.get(0);
    	
	}
	
	public void outputPatchStats(PPatchSim ppsim)
	{
		try
		{
			patchWriter = new FileWriter(ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/Results/"+ppsim.simulationSpec.runReplicate+"/patchStats_"+this.nextPatchOutputHour+".csv");
			// add the first column headings
			patchWriter.append("LTi_X,LTi_Y\n");
			patchWriter2 = new FileWriter(ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/Results/"+ppsim.simulationSpec.runReplicate+"/patchStatsAll_"+this.nextPatchOutputHour+".csv");
			patchWriter2.append("LTi_X,LTi_Y\n");
			
			// Now set up the XML files too
			// XML OUTPUT SETUP VARIABLES - FIRST FOUR ARE THE XML DOCUMENT RELATED, NEXT ARE FOR FORMATTING
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document docWritingProcessed = docBuilder.newDocument();
			Document docWritingAll = docBuilder.newDocument();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			
			Element rootElementProcessed = docWritingProcessed.createElement("SimulationResult");
			docWritingProcessed.appendChild(rootElementProcessed);
			Element rootElementAll = docWritingAll.createElement("SimulationResult");
			docWritingAll.appendChild(rootElementAll);
			
			// Now run the output for both formats
			this.outputLTiPositions(ppsim,docWritingProcessed,docWritingAll,rootElementProcessed,rootElementAll);
			
			// Close the CSV files
			patchWriter.close();
			patchWriter2.close();
			
			// Write the XML files
			// WRITE OUT THE CLOSE XML FILE, FORMATTED CORRECTLY
			String xmlOutputAddress = ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/Results/"+ppsim.simulationSpec.runReplicate+"/";
			docWritingProcessed.normalizeDocument();
			DOMSource source = new DOMSource(docWritingProcessed);
			StreamResult result =  new StreamResult(new File(xmlOutputAddress +"patchStats_"+ppsim.cellTrackStats.trackingSnapStartHr+".xml"));
			transformer.transform(source, result);	
			
			docWritingAll.normalizeDocument();
			source = new DOMSource(docWritingAll);
			result =  new StreamResult(new File(xmlOutputAddress +"patchStatsAll_"+ppsim.cellTrackStats.trackingSnapStartHr+".xml"));
			transformer.transform(source, result);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Gathers the stats for each timestep
	 */
	public void step(final SimState state)
	{
		PPatchSim ppsim = (PPatchSim)state;
		
		// while the simulation is running
		if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep < ((ppsim.simulationSpec.simulationTime*60)*60))
		{
			if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep == ((this.nextPatchOutputHour*60)*60))
			{	
				// OUTPUT THE LTO AND LTI POSITIONS AT THIS TIMEPOINT
				this.outputPatchStats(ppsim);
				
				// NOW THIS OUTPUT TIME HAS ENDED, PREPARE FOR THE NEXT HOUR (IF APPLICABLE)
				// delete this hour from those being done
				this.patchStatHours.remove(0);
				// set the next range (if there is one)
				if(!this.patchStatHours.isEmpty())
				{
					this.nextPatchOutputHour = this.patchStatHours.get(0);
					
				}
				else
				{
					// Register the end hour - thus when the simulation ends and the produce stat function is called, 
					// the file will have an hour to be labelled with
					this.nextPatchOutputHour = ppsim.simulationSpec.simulationTime;
				}
			}
		}
		else
		{
			// Simulation has ended - output the end stats
			this.outputPatchStats(ppsim);
			
			this.stop();
		}
	}

	
	public void outputLTiPositions(PPatchSim ppsim,Document docWritingProcessed,Document docWritingAll,Element rootElementProcessed,Element rootElementAll)
	{
		for(int i=0;i<ppsim.allLTis.size();i++)
		{
			Object ltiCell = ppsim.allLTis.get(i);
			
			try
			{
				// find out what is around the LTi cell
				// Get the cell location & cell size
				Double2D agentLocation = (Double2D)ltiCell.getClass().getDeclaredField("agentLocation").get(ltiCell);
				Double cellSize = ltiCell.getClass().getDeclaredField("cell_diameter").getDouble(ltiCell);
				
				Bag nearCells = null;
				
				try
				{
					Class<?> environmentClass = ppsim.intestine_env.getClass();
					Method getBagOfCells = environmentClass.getMethod("getObjectsExactlyWithinDistanceWithFlag",new Class[]{Double2D.class,double.class,boolean.class});
					nearCells = (Bag)getBagOfCells.invoke(ppsim.intestine_env,agentLocation,cellSize*2,true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				Object temp = null;
				Boolean ltiFound = false;
				
				if( nearCells != null )
				{
					for( int j = 0 ; j < nearCells.numObjs && !ltiFound; j++ )
					{
						temp = nearCells.get(j);
						// check cell is not null, not itself, is an LTi
						if(temp!=null && temp!=ltiCell && temp.getClass().getName().equals("LTi"))
						{
							// there is an LTi near, so it is possible that it could be in a patch
							// however, now check that there is an LTo within a range
							Boolean ltoFound = false;
							Object ltoSeek = null;
							
							Bag nearCells2 = null;
							
							try
							{
								Class<?> environmentClass = ppsim.intestine_env.getClass();
								Method getBagOfCells = environmentClass.getMethod("getObjectsExactlyWithinDistanceWithFlag",new Class[]{Double2D.class,double.class,boolean.class});
								nearCells2 = (Bag)getBagOfCells.invoke(ppsim.intestine_env,agentLocation,cellSize*4,true);
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						
							
							for( int k=0;k<nearCells2.numObjs && !ltoFound;k++)
							{
								ltoSeek = nearCells2.get(k);
							
								// Get the cell state of the cell found in the bag
								Double cellState = temp.getClass().getDeclaredField("cellState").getDouble(temp);
								
								if(ltoSeek!=null && ltoSeek.getClass().getName().equals("LTo") && cellState>0)
								{
									// Write to CSV file
									patchWriter.append(agentLocation.x+","+agentLocation.y+"\n");
									// Write to XML file
									// Make the subroot 'cell' node
									Element cell = docWritingProcessed.createElement("cell");
									rootElementProcessed.appendChild(cell);
									
									// X position 
									Element cellX = docWritingProcessed.createElement("LTi_X");
									cellX.appendChild(docWritingProcessed.createTextNode(Double.toString(agentLocation.x)));
									cell.appendChild(cellX);
									
									// Y position
									Element cellY = docWritingProcessed.createElement("LTi_Y");
									cellY.appendChild(docWritingProcessed.createTextNode(Double.toString(agentLocation.y)));
									cell.appendChild(cellY);
									
									ltoFound=true;
									ltiFound=true;
								}
								
							}
							
						}
					}
				}
				// Write to CSV file
				patchWriter2.append(agentLocation.x+","+agentLocation.y+"\n");
				// Write to XML file
				Element cell = docWritingAll.createElement("cell");
				rootElementAll.appendChild(cell);
				
				// X position 
				Element cellX = docWritingAll.createElement("LTi_X");
				cellX.appendChild(docWritingAll.createTextNode(Double.toString(agentLocation.x)));
				cell.appendChild(cellX);
				
				// Y position
				Element cellY = docWritingAll.createElement("LTi_Y");
				cellY.appendChild(docWritingAll.createTextNode(Double.toString(agentLocation.y)));
				cell.appendChild(cellY);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
	}
		
}
