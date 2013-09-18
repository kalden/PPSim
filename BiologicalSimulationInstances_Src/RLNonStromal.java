

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import sim.engine.*;
import sim.portrayal.*;
import sim.util.*;
import sim_platform.PPatchSim;
  
/**
 * Class which defines the behaviour and attributes of the 'Decoy' Cell.  This is a cell which is on the 
 * stroma and expresses RET Ligand, yet is not an LTo
 * With some common attributes and behaviour shared with the other cell types, this
 * class extends the Cells superclass
 * 
 * @author Kieran Alden
 *
 */
public class RLNonStromal extends SimplePortrayal2D implements Steppable,Stoppable
{	
	/**
	 * <a name = "LTO_DIAMETER"></a>
	 * <b>Description:<br></b> 
	 * Diameter of LTo Cell (in pixels - 1 pixel = 4 micrometres)
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Set as has been verified experimentally
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public static double cell_diameter = 6;
	
	/**
	 * <a name = "imLToActiveTime"></a>
	 * <b>Description:<br></b> 
	 * Time (in hours) for which an immature cell is active before becoming inactive & being removed from the simulation
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be above 0 and less than 48
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public int immatureActiveTime;
	
	/**
	 * <a name = "lToDivisionTime"></a>
	 * <b>Description:<br></b> 
	 * Number of hours taken for an LTo cell to divide
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Set to 12 a an assumption
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Hours
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public static int lToDivisionTime = 12;
	
	/**
	 * A Collection with an x and y value detailing the position of the cell
	 */
	public Double2D agentLocation; 
    
    /**
	 * Representation of the state this cell is currently in. Used as a reference for cell colour on the display 
	 */
    public int cellState;          // Colour Code - will show cell state
    
    /**
     * Method to stop the class where necessary
     */
    public void stop(){stopper.stop();}
    
    /**
	 * Flag to show if this class has been stopped (when no longer needed)
	 */
    private Stoppable stopper = null;
    
    /**
	 * Method to change the value of the stopper
	 * @param stopper	Whether the class should be stopped or not
	 */
    public void setStopper(Stoppable stopper)   {this.stopper = stopper;}
    
    public void addToSchedule(Schedule sch)
	{
		this.setStopper(sch.scheduleRepeating(this));
	}
	
    public void setStopper(PPatchSim ppsim)
    {
    	ppsim.schedule.scheduleRepeating(this);
    }
    
    public ArrayList<Object> returnExpressors()
    {
    	return this.expressors;
    }
    
    public ArrayList<Object> expressors = new ArrayList<Object>();
    
    public ArrayList<String> expressorNames = new ArrayList<String>();
    
	/**
	 * Number of steps the LTo has been active - used to delete immature stromal cells after a set period
	 */
	public int activeTime;
	
	/**
	 * Flag to show that the decoy has been removed (not implemented but may be necessary)
	 */
	public boolean stopped;
	
	/**
	 * The x and y coordinates of where the LTo is on the underlying stromal grid
	 */
	public Int2D gridLoc;
	
	/**
     * Creates a new Decoy Agent (or Cell)
     * 	
     * @param location	Double2D Coordinate location of the LTo Cell on the grid
     */
    public RLNonStromal(Double2D location,Int2D gridLocation, int decoyNum,ArrayList<ArrayList<String>> cellExpressors) 
    {
    	this.agentLocation = location;
    	this.gridLoc = gridLocation;
        
        // Set up the expressors for this object
        try
    	{
        	for(int i=0;i<cellExpressors.size();i++)
    		{
    			// get the receptor
    			ArrayList<String> expressorDetail = cellExpressors.get(i);
    			
    			Class<?> receptorType = Class.forName(expressorDetail.get(0));
    			this.expressorNames.add(expressorDetail.get(0));
    			Constructor<?> con = receptorType.getConstructor(new Class[]{ArrayList.class});
    			Object receptorObj = con.newInstance(expressorDetail);
    			this.expressors.add(receptorObj);

    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}

          
        // 6 used to represent this cell is a decoy in initial state
        cellState = 6;
        activeTime = 0;
        stopped = false;
    }
    
    public String getType() { return "Decoy"; }
    
    public void removeRETLigandDecoys(PPatchSim ppsim)
    {
    	this.stopped=true;
		//System.out.println(this+" Stopped");
    	
    	// REMOVE USING ENVIRONMENT JAVA REFLECTION
		try
		{
			Class<?> environmentClass = ppsim.intestine_env.getClass();
			Method meth = environmentClass.getMethod("remove",new Class[]{Object.class});
			meth.invoke(ppsim.intestine_env,this);
			//ppsim.intestine_env.tract.remove(this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    	
		this.stop();
    }

    
    /* (non-Javadoc)
     * @see sim.engine.Steppable#step(sim.engine.SimState)
     */
    public void step( final SimState state )
    {
    	PPatchSim ppsim = (PPatchSim)state;
    	
    	if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep < ((ppsim.simulationSpec.simulationTime*60)*60))
		{
    		if(!this.stopped)
    		{
    			// Determine if the cell needs to be removed from the system
    			if(this.activeTime>((this.immatureActiveTime*60)*60))
    			{
    				this.stopped=true;
    				
    				// REMOVE USING ENVIRONMENT JAVA REFLECTION
    				try
    				{
    					Class<?> environmentClass = ppsim.intestine_env.getClass();
    					Method meth = environmentClass.getMethod("remove",new Class[]{Object.class});
    					meth.invoke(ppsim.intestine_env,this);
    					//ppsim.intestine_env.tract.remove(this);
    				}
    				catch(Exception e)
    				{
    					e.printStackTrace();
    				}
    				
    				this.stop();
    			}
    			else if(activeTime%(this.lToDivisionTime*60)*60==0)
    			{
    				// Cell should divide if not in an immature state
    				if(this.cellState>1)
					{		
    					this.divideSingleLTo(ppsim,1);
					}
    				
    				
    				activeTime++;
    				
    			}
    			else
    			{
    				// carry on as cell is active - increasing steps has been active for
    				activeTime++;
    			}
    		}
		}
    	else
		{
			this.stop();
		}
    }
    
    /**
	 * Used to simulate cell division.  This method deals with the creation of the divided cell, while ensuring
	 * this is not placed over an LTo cell that already exists
	 * 
	 * @param cellDividing	The existing LTo cell that is dividing
	 * @param ppsim	The current simulation state
	 * @return LTo ltoCell	New LTo cell representing the divided cell
	 */
	public void divideSingleLTo(PPatchSim ppsim,int distance)
	{
		
		// convertDone - flag to show if an inactive cell has been found to be converted or not
		boolean convertDone = false;
		
		Int2D blankSpace = null;
		
		// Need to get the LTo Cells that are around itd
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		
		// get the cells that are in the lto grid squares around this
		// Get the location of this cell object
		try
		{
			//Field cellGridLocField = cellDividing.getClass().getDeclaredField("gridLoc");
			//Int2D cellGridLocation = (Int2D)cellGridLocField.get(cellDividing);
			
			ppsim.surfaceCellsGrid.getNeighborsMaxDistance(this.gridLoc.x, this.gridLoc.y, distance, true, xPos, yPos);
			//System.out.println("XPOS SIZE: "+xPos.size()+" "+distance);
			
			// Now got through attempting to find an immature LTo that can be converted(or blank square if necessary for later!)
			
			// Now go through each grid in the neighbourhood, excluding the centre where the cell is
			for(int k=0;k<xPos.size() && !convertDone && blankSpace==null;k++)
			{	
				// FIX TO STOP DIVISION OVER RIGHT/LEFT OF SCREEN
				// CHECK THE LOCATION IS NOT ON THE OTHER SIDE OF THE TRACT
				// To check this, take the x grid location of where the cell wants to move, subtract the location of where the cell is (if on left,
				// if on right side swap them over), add the distance the cell wants to move, and check this is greater than the upper limit of the tract
				boolean moveOk = checkRightLeftRollAround(ppsim,this.gridLoc,xPos.get(k),distance);
				//System.out.println("MOVEOK: "+moveOk);
					
				if(moveOk)
				{
					try
					{
						Object stromalCell = ppsim.surfaceCellsGrid.get(xPos.get(k),yPos.get(k));
						
					
						if(stromalCell!=null)
						{
							// Get the state of this cell object
							Field cellState = stromalCell.getClass().getDeclaredField("cellState");
							
							if(cellState.getInt(stromalCell)==0)
							{
								this.makeNewLTo(stromalCell, ppsim);
								convertDone = true;
							}
						}
						else
						{
							if(blankSpace==null)
							{
								blankSpace = new Int2D(xPos.get(k),yPos.get(k));
							}
						}
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
		
		// Now need to check a cell has been created - if not (there were no inactive cells around to convert), need to create a new one
		if(!convertDone)
		{
			if(blankSpace!=null)
			{
				// take the first empty grid space that was found & place an LTo there
				// work out where this square represents on the grid (as the intestine itself will have grown, so the squares will be a different
				// size to originally
			
				try
				{
					// Get the size of the object dividing
					//Field cellSize = cellDividing.getClass().getDeclaredField("cell_diameter");
					// work out the size of each grid square
					// get current environment dimensions
					double currentGridLength = ppsim.intestine_env.getClass().getDeclaredField("currentGridLength").getDouble(ppsim.intestine_env);
					double currentGridHeight = ppsim.intestine_env.getClass().getDeclaredField("currentGridHeight").getDouble(ppsim.intestine_env);
					// get the original when the boxes were set
					double initialGridLength = ppsim.intestine_env.getClass().getDeclaredField("initialGridLength").getDouble(ppsim.intestine_env);
					double initialGridHeight = ppsim.intestine_env.getClass().getDeclaredField("initialGridHeight").getDouble(ppsim.intestine_env);
					
					double xAdj = currentGridLength / ((int)(initialGridLength/this.cell_diameter));
					double yAdj = currentGridHeight / ((int)(initialGridHeight/this.cell_diameter));
					
					Double2D location = new Double2D((blankSpace.x*xAdj)+this.cell_diameter/2,(blankSpace.y*yAdj)+this.cell_diameter/2);
					
					// set the ltoCell to be at this location
					//LTo ltoCell = new LTo(location,simParams,blankSpace,ppsim.ltoCellsBag.size(),false);
					// Get the expressors to send to the cell creator
					//ArrayList<String> expressors = (ArrayList<String>)this.expressors;
					
					Constructor<?> con = this.getClass().getConstructor(new Class[]{Double2D.class,Int2D.class,int.class,Boolean.class,int.class,ArrayList.class});
					Object newCellObject = con.newInstance(location,blankSpace,ppsim.ltoCellsBag.size(),true,this.immatureActiveTime,this.expressorNames);
					
					// copy the properties of the dividing LTo cell to the new cell
					this.makeNewLTo(newCellObject, ppsim);
	
					// add to the storage and display
					ppsim.ltoCellsBag.add(newCellObject);
					
					ppsim.surfaceCellsGrid.set(blankSpace.x,blankSpace.y, newCellObject);
					
					// Add to the environment tract (using reflection)
					Class<?> environmentClass = ppsim.intestine_env.getClass();
					Method setLoc = environmentClass.getMethod("setLocation",new Class[]{Object.class,Double2D.class});
					setLoc.invoke(ppsim.intestine_env,newCellObject,location);
					//ppsim.intestine_env.tract.setObjectLocation(ltoCell,location);
					
					Method setStopperMeth = newCellObject.getClass().getMethod("setStopper",new Class[]{PPatchSim.class});
					setStopperMeth.invoke(newCellObject,ppsim);
					//ltoCell.setStopper(ppsim.schedule.scheduleRepeating(ltoCell));		
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
			else
			{
				// there were no blank squares around, need to look further afield for a free location
				this.divideSingleLTo(ppsim, distance+1);
			}
		}
	}
	
	/**
	 * As the tract has been modelled as 2D, with cells allowed to roll round top and bottom, the cells can potentially find a divide location 
	 * on the opposite side of the tract (left and right)- this checks that this does not happen
	 * 
	 * @param ppsim	The current simulation state
	 * @param dividingCell	The LTo cell object that is dividing
	 * @param xPosInNeighbourhood	The x position that has been found by the Moore's Neighborhood algorithm which needs checking to ensure it is not on opposite side
	 * @param distance	The distance away from the dividing cell that the cell is looking to divide to
	 * @return a boolean stating whether the location is valid or not
	 */
	public boolean checkRightLeftRollAround(PPatchSim ppsim,Int2D cellGridLocation, Integer xPosInNeighbourhood,int distance)
	{
		// GET NEIGHBOURS IS TOROIDAL AS THE CELL MAY DIVIDE OVER THE TOP/BOTTOM OF SCREEN
		// HOWEVER THIS MEANS THE CELL WILL DIVIDE LEFT & RIGHT
		// THIS CANNOT HAPPEN AND NEEDS TO BE DETECTED
		
		// get the cells that are in the lto grid squares around this
		// Get the location of this cell object
		 
		
		
		// gather whether the cell is on the left hand or right hand edge of the tract (if either)
		if(cellGridLocation.x < ((int)ppsim.surfaceCellsGrid.getWidth()/2))	// on the left
		{
			if((xPosInNeighbourhood-cellGridLocation.x)+distance < (int)ppsim.surfaceCellsGrid.getWidth())
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
			if((cellGridLocation.x - xPosInNeighbourhood)+distance < (int)ppsim.surfaceCellsGrid.getWidth())
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
	 * Creates the duplicate of an existing LTo cell (i.e. simulating cell division)
	 * @param temp	The cell that is dividing
	 * @param ppsim	The current simulation state
	 * @return LTo ltoCell	The new LTo cell, sharing all properties of the original
	 */
	public void makeNewLTo(Object newCell, PPatchSim ppsim)
	{
		
		// Get the state of this cell object
		try
		{
			//Field cellState = dividingCell.getClass().getDeclaredField("cellState");
			
			// The new LTo cell needs to have all the properties of the original, so copy these over
			
			newCell.getClass().getDeclaredField("cellState").setInt(newCell, this.cellState);
			
			newCell.getClass().getDeclaredField("expressors").set(newCell,this.expressors);
			
			newCell.getClass().getDeclaredField("activeTime").set(newCell,this.activeTime+1);
			
			//newCell.getClass().getDeclaredField("chemoLinearAdjust").setDouble(newCell, dividingCell.getClass().getDeclaredField("chemoLinearAdjust").getDouble(dividingCell));
			//newCell.getClass().getDeclaredField("chemoSigThreshold").setDouble(newCell, dividingCell.getClass().getDeclaredField("chemoSigThreshold").getDouble(dividingCell));
			//newCell.getClass().getDeclaredField("vcamAdhesionEffect").set(newCell, dividingCell.getClass().getDeclaredField("vcamAdhesionEffect").get(dividingCell));
			
			
			
				
			// add the stats for the start of this cells life
			//newCell.startingChemoLinearAdjust = dividingCell.chemoLinearAdjust;
			//newCell.startingVCAMExpressionLevel = dividingCell.vcamAdhesionEffect.vcamExpressionLevel;
			
			if(this.cellState>0)  // an activeexpressing LTo
			{
				// add to the bag used to do patch size
				ppsim.activelToCellsBag.add(newCell);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
   				
   				
    
    /** 
	 * Draws the Decoy cell to the screen, with the size dependent on the scale currently being represented
	 * @see sim.portrayal.SimplePortrayal2D#draw(java.lang.Object, java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
	 */
    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	// info.draw.width & height - the current level of zoom on the object (same as shown in display
    	
    	// UNCOMMENT IF WANT TO SEE THE RLNONSTROMAL
    	
        /*double diamx = info.draw.width*simParams.LTO_DIAMETER;
        double diamy = info.draw.height*simParams.LTO_DIAMETER;
    	   
        graphics.setColor(PPatchSim.cellColours.get(cellState));
        
        // THE ORIGINAL COORDINATES ARE ADJUSTED FOR SCALE ON SCREEN, AND THE FACT THAT THE ELLIPSE IS ELSE DRAWN UPWARDS FROM THE
        // BOTTOM RIGHT
        Ellipse2D.Double cell = new Ellipse2D.Double((int)info.draw.x-diamx/2,(int)info.draw.y-diamy/2,(int)diamx,(int)diamy);
        
        graphics.fill(cell);*/

    }
}
