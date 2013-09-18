package sim_platform;

import java.util.ArrayList;

/**
 * Takes snapshots of the environment for output and further analysis - at both the end of the simulation, at 12 hour timepoints if required,
 * and every step if timelapse imaging is required
 * 
 * @author kieran
 *
 */
public class TrackImaging 
{


	/**
	 * Take a snap of the display at 12 hour intervals
	 * @param ppsim	The current simulation state
	 */
	public void takeDisplaySnaps(PPatchSim ppsim)
	{
		if(ppsim.schedule.getSteps()==1)
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+ppsim.simulationSpec.experimentDescription+"/Initial");
		}
		else if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep==((12*60)*60))
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+ppsim.simulationSpec.experimentDescription+"/12Hours");
		}
		else if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep==((24*60)*60))
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+ppsim.simulationSpec.experimentDescription+"/24Hours");
		}
		else if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep==((36*60)*60))
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+ppsim.simulationSpec.experimentDescription+"/36Hours");
		}
		else if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep==((48*60)*60))
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+ppsim.simulationSpec.experimentDescription+"/48Hours");
		}
		else if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep==((ppsim.simulationSpec.simulationTime*60)*60))
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/End Snapshot");
		}
	}
	
	/**
	 * Images can also be taken every step if required - great for timelapse imaging
	 * 
	 * @param ppsim	The current simulation state
	 */
	public void timePeriodImaging(PPatchSim ppsim)
	{
		// IMAGING EVERY STEP FOR A SET PERIOD (IF ENABLED)
		// take images of the tract at every step from a start hour to finish hour for analysis in Volocity if required
		// start and end hours set as parameters in the xml file
		
		if(ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep >= ((ppsim.cellTrackStats.trackingSnapStartHr*60)*60) 
				&& ppsim.schedule.getSteps()*ppsim.simulationSpec.secondsPerStep < ((ppsim.cellTrackStats.trackingSnapEndHr*60)*60))
		{
			ppsim.display.takeTractSnap(ppsim.simulationSpec.resultStoreFilePath+"/"+ppsim.simulationSpec.experimentDescription+"/"+ppsim.schedule.getSteps());
		}
		
		
	}
	
	
}
