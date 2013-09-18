import java.awt.Color;

import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class Intestine_Environment 
{
	/**
	 * <a name = "tract"></a>
	 * <b>Description:<br></b> 
	 * A Continuous grid representing the small intestine tract being modelled
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * The Environment 
	 * <br><br>
	 */
	public Continuous2D tract = null;
	
	/**
	 * <a name = "initialGridHeight"></a>
	 * <b>Description:<br></b> 
	 * This value (in pixels) is the circumference of measurement of the intestine tract when the simulation begins. 
	 * The tract is modelled on a 2D plane, as if the intestine had been cut along it's length.  Any cells that leave 
	 * the bottom of the screen will reappear at the top
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be numeric and above 0
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels.  1 Pixel = 4 microns
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 * 
	 */
	public double initialGridHeight;
	
	/**
	 * <a name = "initialGridLength"></a>
	 * <b>Description:<br></b> 
	 * Value (in pixels) representing the length of the tract when the simulation begins.  Any cells that leave the left or right hand 
	 * side of the screen are deemed to be removed from the simulation
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be numeric and above 0
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels.  1 Pixel = 4 microns
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double initialGridLength;
	
	/**
	 * <a name = "upperGridHeight"></a>
	 * <b>Description:<br></b> 
	 * Upper bound height at which the tract should grow to over the simulation - the height increases with each step until this is measure is 
	 * reached at the end of the 48 hours
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be numeric and above 0
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels.  1 Pixel = 4 microns
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double upperGridHeight;
	
	/**
	 * <a name = "upperGridLength"></a>
	 * <b>Description:<br></b> 
	 * Upper bound length at which the tract should grow to over the simulation - the length increases with each step until this is length is 
	 * reached at the end of the 48 hours
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be numeric and above 0
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels.  1 Pixel = 4 microns
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double upperGridLength;
	
	/**
	 * <a name = "currentGridHeight"></a>
	 * <b>Description:<br></b> 
	 * Stores the current height of the intestine tract at that particular timestep
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels
	 * <br><br>
	 */
	public double currentGridHeight;
	
	/**
	 * <a name = "currentGridLength"></a>
	 * <b>Description:<br></b> 
	 * Stores the current length of the intestine tract at that particular timestep
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels
	 * <br><br>
	 */
	public double currentGridLength;
	
	/**
	 * <a name = "heightGrowth"></a>
	 * <b>Description:<br></b> 
	 * Calculated amount by which the tract will grow in height with each step
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels
	 * <br><br>
	 */
	public double heightGrowth;
	
	/**
	 * <a name = "lengthGrowth"></a>
	 * <b>Description:<br></b> 
	 * Calculated amount by which the tract will grow in length with each step
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Pixels
	 * <br><br>
	 */
	public double lengthGrowth;
	
	/**
	 * Color, if using a visual display
	 */
	public Color backdrop = Color.black;
	

	public Intestine_Environment(Double initGridLength, Double initGridHeight)
	{
		this.initialGridHeight = initGridHeight;
		this.initialGridLength = initGridLength;
		// 3: Set up the grid
		tract = new Continuous2D(6.0,this.initialGridLength,this.initialGridHeight);
		
		// Store the current grid height & length as these will change throughout the simulation
		currentGridLength = this.initialGridLength;
		currentGridHeight = this.initialGridHeight;
		
		
	}
	
	public Intestine_Environment()
	{
		tract = new Continuous2D(6.0,this.initialGridLength,this.initialGridHeight);
		currentGridLength = this.initialGridLength;
		currentGridHeight = this.initialGridHeight;
	}
	
	/**
	 * Consructor specifying lower and upper limits, and an amount of time (in 'seconds' that the tract grows
	 * @param initGridLength
	 * @param initGridHeight
	 * @param upGridLength
	 * @param upGridHeight
	 * @param growthTime
	 */
	public Intestine_Environment(Double initGridHeight, Double initGridLength,Double upGridHeight,Double upGridLength,Double growthTime)
	{
		this.initialGridHeight = initGridHeight;
		this.initialGridLength = initGridLength;
		this.upperGridHeight = upGridHeight;
		this.upperGridLength = upGridLength;
		
		currentGridLength = this.initialGridLength;
		currentGridHeight = this.initialGridHeight;
		
		// 3: Set up the grid
		tract = new Continuous2D(6.0,this.initialGridLength,this.initialGridHeight);
		
		if(this.upperGridHeight > this.initialGridHeight)
		{
			this.heightGrowth = (this.upperGridHeight-this.initialGridHeight)/((growthTime*60)*60);
		
		}
		if(this.upperGridLength > this.initialGridLength)
		{
			this.lengthGrowth = (this.upperGridLength-this.initialGridLength)/((growthTime*60)*60);
		}
	}
	
	/**
	 * Set an object to a given location on the tract
	 * 
	 * @param cellObject
	 * @param location
	 */
	public void setLocation(Object cellObject,Double2D location)
	{
		this.tract.setObjectLocation(cellObject,location);
	}
	
	/**
	 * Get objects around another object on the tract
	 * @param position
	 * @param distanceToCheck
	 * @return
	 */
	public Bag getObjectsExactlyWithinDistance(Double2D position,double distanceToCheck)
	{
		return this.tract.getObjectsExactlyWithinDistance(position,distanceToCheck);
	}
	
	public Bag getObjectsExactlyWithinDistanceWithFlag(Double2D position,double distanceToCheck,boolean tor)
	{
		return this.tract.getObjectsExactlyWithinDistance(position,distanceToCheck,tor);
	}
	
	/**
	 * Remove an object from the tract
	 * @param objToRemove
	 */
	public void remove(Object objToRemove)
	{
		this.tract.remove(this);
	}
	
}
