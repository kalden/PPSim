package sim_platform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import sim.engine.Schedule;
import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Class to create the stromal cell grid environment upon which interactions take place, and populate it with the 
 * required number of LTo and RLNS cells.  This number is calculated from the percentage read in from the XML file, 
 * and based on the size of the tract being modelled.
 * 
 * @author kieran
 *
 */
public class Setup_Stromal_Cell_Distribution 
{

	ArrayList<Object> stromalCellDetails;
	
	/**
	 * Initialises the grid environment, and calculates how many stromal cells are required
	 * 
	 * @param ppsim	The current simulation state
	 * @param simParams	The simulation parameters read in from the XML file
	 * 
	 */
	public Setup_Stromal_Cell_Distribution(PPatchSim ppsim,ArrayList<Object> stromalDetails)
	{
		try
		{
			// stromalDetails is an arraylist of arraylists - one for each cell that should exist on the stroma
			this.stromalCellDetails = stromalDetails;
			
			// Process each cell type
			for(int i=0;i<stromalDetails.size();i++)
			{
				ArrayList<String> stromalTypeCell = (ArrayList<String>)this.stromalCellDetails.get(i);
				
			
				// Get the class of cells that will be on this grid - Name is at location zero
				Class<?> cls = Class.forName(stromalTypeCell.get(0).toString());
				
				// Second is the stromal cell density (i.e. percentage of area occupied by this cell type (in this case)
				double stromalCellDensity = Double.parseDouble(stromalTypeCell.get(1));
				// Third is the percentage of these that express RET Ligand
				double percentStromaRETLigands = Double.parseDouble(stromalTypeCell.get(2));
				
				
				// Get the size of this cell
				Double cellSize = cls.getDeclaredField("cell_diameter").getDouble(null);
				
				
				
				// Set up the grid
				double currentGridLength = ppsim.intestine_env.getClass().getDeclaredField("currentGridLength").getDouble(ppsim.intestine_env);
				double currentGridHeight = ppsim.intestine_env.getClass().getDeclaredField("currentGridHeight").getDouble(ppsim.intestine_env);
				ppsim.surfaceCellsGrid = new ObjectGrid2D((int)(currentGridLength/cellSize),(int)(currentGridHeight/cellSize));
				//ppsim.lToGrid = new ObjectGrid2D((int)(simParams.initialGridLength/cellSize),(int)(simParams.initialGridHeight/cellSize));
				
			
				// Normal situation
			
				// Firstly, work out the number of immature stromal cells to create based on the density measure
				
				// Work out what 100% of the cells would be:
				double totalCells = ((((int)(currentGridLength/cellSize)*(int)(currentGridHeight/cellSize))));
				//System.out.println("Total Cells: "+totalCells);
			
				// Now work out what the required number of potentially active stromal cells will need to be placed on the stroma
				double numActiveLTo = (totalCells / 100)*stromalCellDensity;
			
				// add all the LTo cells to the tract, randomly placed
				// However, a certain percentage of these need to be artn active at the start of the simulation - so calculate this number
				double numARTNActive = (numActiveLTo * percentStromaRETLigands)/100;
				
				// ADD THESE CELLS
				this.distribute_Stromal_Cells(ppsim, numARTNActive,numActiveLTo,cellSize,stromalTypeCell);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Experimental function to place one active LTo in the centre of the simulation
	 * 
	 * @param ppsim	The current simulation state
	 * @param simParams	The current simulation parameters
	 */
	/*public void insertExperimentalLTo(PPatchSim ppsim, SimParameters simParams,Double cellSize)
	{
		int xLoc = (int)(350/cellSize);
		int yLoc = (int)(127/cellSize);
		
		Int2D gridLocation = new Int2D(xLoc,yLoc);
		// put the object at that location in the grid
		// needs adjusting as boxes are the diameter of the LTo cell - plus add half to correct drawing function
		// else draws on edge of screen
		Double2D location = new Double2D((xLoc*cellSize)+cellSize/2,(yLoc*cellSize)+cellSize/2);
		LTo ltoCell = new LTo(location,simParams,gridLocation,1,true);
		
		// add to the bag storing the LTo objects
		ppsim.ltoCellsBag.add(ltoCell);
		
		// set the grid to show the cell has been placed here
		ppsim.surfaceCellsGrid.set(xLoc, yLoc, ltoCell);
		
		// SET THE OBJECT LOCATION ON THE ENVIRONMENT
		try
		{
			// SET THE OBJECT LOCATION ON DYANMIC TRACT
			Class<?> environmentClass = ppsim.intestine_env.getClass();
			Method meth = environmentClass.getMethod("setLocation",new Class[]{Object.class,Double2D.class});
			meth.invoke(ppsim.intestine_env,ltoCell,location);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		//ppsim.intestine_env.tract.setObjectLocation(ltoCell,location);
		
		ltoCell.setStopper(ppsim.schedule.scheduleRepeating(ltoCell));
		
		// activate the RET ligand on this cell
		ltoCell.activateRETLigand();
		ppsim.activelToCellsBag.add(ltoCell);
	
	}*/
	
	/**
	 * Adds the required number of LTo cells at random locations in the environment
	 * 
	 * @param ppsim	The current simulation state
	 * @param simParams	The simulation parameters read in from the XML file
	 * @param numActiveLTo	The number of LTo cells that need placing in the environment
	 */
	public void distribute_Stromal_Cells(PPatchSim ppsim, double numActiveLTo, double numARTNActive, double cellSize, ArrayList<String> stromalTypeCell)
	{	
		// ADD THE LTo CELLS
		// Each will be placed randomly in the grid
		// So as the length and width have been divided by 6, we can find random grid squares, and fill if not yet chosen
		// Also avoids collision problems
		// Pattern may be box like but will be adjusted as the tract grows
		int numCellsARTNActivated=0;
		
		try
		{
			double currentGridLength = ppsim.intestine_env.getClass().getDeclaredField("currentGridLength").getDouble(ppsim.intestine_env);
			double currentGridHeight = ppsim.intestine_env.getClass().getDeclaredField("currentGridHeight").getDouble(ppsim.intestine_env);
			
			for(int k=0;k<numActiveLTo;k++)   // if the percentage is a decimal, this will be rounded up
			{	
				boolean locationFree = false;
				
				while(!locationFree)
				{
					int xLoc = ppsim.random.nextInt((int)(currentGridLength/cellSize));
					int yLoc = ppsim.random.nextInt((int)(currentGridHeight/cellSize));
				
					if(ppsim.surfaceCellsGrid.get(xLoc, yLoc)==null)
					{
						locationFree = true;
						Int2D gridLocation = new Int2D(xLoc,yLoc);
						// put the object at that location in the grid
						// needs adjusting as boxes are the diameter of the LTo cell - plus add half to correct drawing function
						// else draws on edge of screen
						Double2D location = new Double2D((xLoc*cellSize)+cellSize/2,(yLoc*cellSize)+cellSize/2);
						
						try
						{
							Class<?> cls = Class.forName(stromalTypeCell.get(0).toString());
							
							// try to create an LTo
							try
							{
								
								// A random number of these cells need to express RET ligand - thus two different constructors
								Object obj;
								if(numCellsARTNActivated<numARTNActive)
								{
									Constructor<?> con = cls.getConstructor(new Class[]{Double2D.class,Int2D.class,int.class,Boolean.class,double.class,ArrayList.class});
									obj = con.newInstance(location,gridLocation,k,true,Double.parseDouble(stromalTypeCell.get(3)),stromalTypeCell.get(4));
	
									numCellsARTNActivated++;
									ppsim.activelToCellsBag.add(obj);
								}
								else
								{
									Constructor<?> con = cls.getConstructor(new Class[]{Double2D.class,Int2D.class,int.class,Boolean.class,double.class,ArrayList.class});
									obj = con.newInstance(location,gridLocation,k,false,Double.parseDouble(stromalTypeCell.get(3)),stromalTypeCell.get(4));
									
								}
								ppsim.ltoCellsBag.add(obj);
								
								// set the grid to show the cell has been placed here
								ppsim.surfaceCellsGrid.set(xLoc, yLoc, obj);
								
								//ppsim.intestine_env.tract.setObjectLocation(obj,location);
								
								// SET THE OBJECT LOCATION ON DYANMIC TRACT
								Class<?> environmentClass = ppsim.intestine_env.getClass();
								Method meth = environmentClass.getMethod("setLocation",new Class[]{Object.class,Double2D.class});
								meth.invoke(ppsim.intestine_env,obj,location);
								
								Method setStopperMeth = cls.getMethod("addToSchedule",new Class[]{Schedule.class});
								setStopperMeth.invoke(obj,ppsim.schedule);
								
								//Method setStopperMeth = cls.getMethod("setStopper",new Class[]{PPatchSim.class});
								//setStopperMeth.invoke(obj,ppsim);
								
								//obj.setStopper(ppsim.schedule.scheduleRepeating(obj));
								
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
							
						
						}
						catch(ClassNotFoundException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Sets a percentage of the LTo cells on the stromal to be expressing RET Ligand, and thus have the
	 * potential to become PP
	 * 
	 * @param numARTNActive	Number of LTo cells to set to be expressing RET Ligand
	 */
	/*public void setCellsExpressingRETLigand(PPatchSim ppsim,double numARTNActive,SimParameters simParams)
	{
		if(simParams.stromalCellRETLigandPlacement.equals("random"))
		{
			// A random selection of LTo cells are set to be expressing RET ligand.  In this case, they can be anywhere on the tract 
			for(int j=0;j<numARTNActive;j++)
			{
				// Pick a random LTo
				int randomLTo = ppsim.random.nextInt(ppsim.ltoCellsBag.size());
			
				// get the LTo from the bag
				LTo ltoCell = (LTo) ppsim.ltoCellsBag.get(randomLTo);
			
				// change the state of the LTo to make this express ARTN if not already expressing ARTN
				if(ltoCell.cellState!=1)	
				{
					ltoCell.activateRETLigand();
					ppsim.activelToCellsBag.add(ltoCell);
				}
			}
		}
		else
		{
			// RET Ligand expression is restricted to LTo cells in a band.
			// If no LTo cells in a band, then no RET ligand expression.  Means a high enough percentage of the stromal cells must be set to express RET Ligand
			
			// Calculate the available circumference area
			Double rangeHeight = (simParams.initialGridHeight/100)*simParams.stromalCellCircumferencePercentage;
			
			// Now need to adjust so this range is centered around the centre of the simulation space.
			// Using the middle ensures no cells roll around the screen straight away when a range is used
			Double midpoint = simParams.initialGridHeight/2;
			
			for(int j=0;j<numARTNActive;j++)
			{
				int randomLTo = ppsim.random.nextInt(ppsim.ltoCellsBag.size());
				
				// get the LTo from the bag
				LTo ltoCell = (LTo)ppsim.ltoCellsBag.get(randomLTo);
				
				if(ltoCell.cellState!=1)
				{
					if((ltoCell.agentLocation.y >= midpoint-(rangeHeight/2)) && (ltoCell.agentLocation.y <= midpoint+(rangeHeight/2)))
					{
						ltoCell.activateRETLigand();
						ppsim.activelToCellsBag.add(ltoCell);
					}
				}
			}
			
		}
	}*/
}
