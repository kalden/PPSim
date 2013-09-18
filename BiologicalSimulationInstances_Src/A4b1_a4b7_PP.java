

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;


/**
 * Models the effect of adhesion factors on LTin/LTi cell behaviour.  An object of this clas is attached to each LTo cell, and the effect on 
 * cell behaviour determined by the level of adhesion factor expressed.
 * 
 * @author kieran
 *
 */
public class A4b1_a4b7_PP 
{
	
	/**
	 * <a name = "maxProbabilityOfAdhesion"></a>
	 * <b>Description:<br></b> 
	 * Ensures the probability of 'sticking' cannot go past a set threshold - ensures some stochasticity
	 * <br><br>
	 * <b>Restrictions:<br></b>
	 * Must be a double between 0 and 1
	 * <br><br>
	 * <b>Units & Representation:<br></b>
	 * Probability
	 * <br><br>
	 * <b>Link to Domain and Platform Models:</b>
	 */
	public double maxProbabilityOfAdhesion;
	
	public A4b1_a4b7_PP(ArrayList<String> receptorDetail)
	{
		this.maxProbabilityOfAdhesion = Double.parseDouble(receptorDetail.get(1));
		
		// The parameter for this receptor is the 2nd element
		
	}
	
	
	/**
	 * Determine if the level of expression affects LTin/LTi cell behaviour
	 * 
	 * @param vcamSlope	The slope of VCAM expression - how this rises with each stable contact
	 * @param maxVCAMeffectProbabilityCutoff	The maximum probability adhesion factors hold a cell in place around an LTo
	 * @param randNum	Represents the probability calculated that the adhesion factor has an influence
	 * @return	Boolean determining whether the cell stays put or moves away
	 */
	public boolean examineVCAMEffect(Object expressor)
	{
		Random rand = new Random();
		//double probability = ppsim.random.nextDouble();
		double probability = rand.nextDouble();
		
		double expressionLevel=0;
		double adhesionSlope=0;
		try
		{
			// Get the expression level of the expressor
			Method meth = expressor.getClass().getMethod("returnAdhesionExpressionLevel",new Class[]{});
			expressionLevel = (Double)meth.invoke(expressor);
			
			// Get the adhesion expression slope from the expressor
			Method getAdhesionSlope = expressor.getClass().getMethod("returnAdhesionSlope",new Class[]{});
			adhesionSlope = (Double)getAdhesionSlope.invoke(expressor);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		double probabilityProlongedAdhesion = adhesionSlope * expressionLevel;
    	
    	if(probabilityProlongedAdhesion > this.maxProbabilityOfAdhesion)
    	{
    		probabilityProlongedAdhesion = this.maxProbabilityOfAdhesion;
    	}
    	
    	if(probability<probabilityProlongedAdhesion)	// the cell will remain in contact with an LTo within the patch, held by vcam
    	{
    		return true;
    	}
    	else			// the cell will move away
    	{
    		return false;
    	}
		
	}
}
