

import java.util.ArrayList;

public class VCAM_ICAM_MAdCAM_PP 
{
	/**
	 * Level of adhesion factor expressed by this LTo cell
	 */
	public double vcamExpressionLevel;
	
	/**
	 * VCAM Expression level reached
	 */
	public double endingVCAMExpressionLevel;
	
	/**
	 * <a name = "adhesionFactorExpressionSlope"></a>
	 * <b>Description:<br></b> 
	 * Slope of the linear VCAM line which determines the probability that VCAM will hold the cells in contact with
	 * a forming patch
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be a double
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Unit at which the linear function rises (i.e. the slope)
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
    public double adhesionFactorExpressionSlope;
    
	
	public VCAM_ICAM_MAdCAM_PP(ArrayList<String> expressorDetail)
	{
		this.adhesionFactorExpressionSlope = Double.parseDouble(expressorDetail.get(1));
	}
	
	/**
	 * <a name = "vcamIncrement"></a>
	 * <b>Description:<br></b> 
	 * Value at which to increment the vcam expression on each contact
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be a decimal between 0 and 1
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Numerical Increase
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double vcamIncrement = 0.05;
	
	/**
	 * Increase the level of expression with each stable contact
	 * 
	 * @param vcamIncrement	The level to increment expression by
	 */
	public void incrementAdhesionExpression()
	{
		this.vcamExpressionLevel = this.vcamExpressionLevel + this.vcamIncrement;
	}
	
	/**
	 * Returns the vcam expression level of the LTo
	 * @return
	 */
	public double returnAdhesionExpressionLevel()
	{
		return this.vcamExpressionLevel;
	}
	
	public double returnAdhesionSlope()
	{
		return this.adhesionFactorExpressionSlope;
	}
	

}
