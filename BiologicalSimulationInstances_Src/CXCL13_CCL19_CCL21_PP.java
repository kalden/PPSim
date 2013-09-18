

import java.util.ArrayList;

public class CXCL13_CCL19_CCL21_PP 
{
	/**
	 * Stores the current Threshold used in the sigmoid function which determines chemokine diffusion from this cell - used for both CXCL13 & CCL21
	 */
	public double chemoSigThreshold;
	
	/**
	 * Stores the adjustment to the sigmoid curve which is what determines the level of expression of chemokine
	 */
	public double chemoLinearAdjust;
	
	/**
	 * For an inactive LTo, the number of times a cell has been in contact. Once a threshold is past, the cell becomes active
	 */
	//public int imLToCellContactCount;
	
	/**
	 * <a name = "chemoLinearAdjustmentReducer"></a>
	 * <b>Description:<br></b> 
	 * Adjustment applied to the linear adjustment of the chemokine sigmoid curve with each contact
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * None
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Curve Adjustment Function
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double chemoLinearAdjustmentReducer = 0.005;
	
	/**
	 * <a name = "maxChemokineExpressionValue"></a>
	 * <b>Description:<br></b> 
	 * Upper bound to use for the linear adjustment applied to the chemokine sigmoid function.
	 * Upper bound makes the s curve steep. The curve is adjusted from this point to the lower bound to make it more linear
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be a double
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Unit at which sigmoid function stops being adjusted
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double maxChemokineExpressionValue;

	public CXCL13_CCL19_CCL21_PP(ArrayList<String> expressorDetails)
	{	
		this.chemoSigThreshold = 3;
        this.chemoLinearAdjust = Double.parseDouble(expressorDetails.get(1));
        this.maxChemokineExpressionValue = Double.parseDouble(expressorDetails.get(2));
  	}
	
	public void increaseChemokineExpression()
	{
		if(this.chemoLinearAdjust>this.maxChemokineExpressionValue)
		{
			this.chemoLinearAdjust-=this.chemoLinearAdjustmentReducer;
		}
	}

}
