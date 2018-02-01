package ec.tlbo;
import ec.*;
import ec.simple.*;
import ec.vector.*;

import java.util.ArrayList;
import java.util.Random;

import ec.util.*;
/**
*
*  The evaluation state performs teacher and learner evaluation in a single generation except for the first
*  generation where initial evaluation is also performed.
* 
 */
public class tlboEvaluationState extends SimpleEvolutionState 
{
	private tlboBreeder breeder;
	private MersenneTwisterFast mtf;
	private boolean oneTimeEval = false;
    public static final String DIVERSITY_RATIO = "d-ratio";   
    double diversityRatio;
  
    	
	public void startFresh() {
		super.startFresh();	
		Parameter dRatio  = new Parameter(new String[]{"state","d-ratio"});  		
		diversityRatio  = this.parameters.getDouble(dRatio, null);
		breeder = new tlboBreeder();
	}
	
	public int evolve()  {
		if (generation > 0) 
			output.message("Generation " + generation);
		int count = 0;
		
		while(count < 2) {
			statistics.preEvaluationStatistics(this);
			
			if (!oneTimeEval)  {// Initializtion
				evaluator.evaluatePopulation(this);
				oneTimeEval = true;
			}
									
			if (evaluator.runComplete(this) && quitOnRunComplete) {
				output.message("found ideal individual");
				return R_SUCCESS;
			}
	
			if (generation == numGenerations-1) 
				return R_FAILURE;
			
	
			if(count == 1) {
				statistics.prePreBreedingExchangeStatistics(this);
				population = exchanger.preBreedingExchangePopulation(this);
				statistics.postPreBreedingExchangeStatistics(this);
				statistics.preBreedingStatistics(this);
			}
			String exchangerWantsToShutdown = exchanger.runComplete(this);
			
			if (exchangerWantsToShutdown!=null) { 
				output.message(exchangerWantsToShutdown);                     
				return R_SUCCESS;
			}
	
			population = breeder.breedPopulation(this, count, diversityRatio); // count = 0 : teacher phase, count = 1 : learner phase		
			evaluator.evaluatePopulation(this);
			
			if (count == 1) {
				statistics.postBreedingStatistics(this);
				statistics.prePostBreedingExchangeStatistics(this);
				population = exchanger.postBreedingExchangePopulation(this);
				statistics.postPostBreedingExchangeStatistics(this);
			}		
			count++;	
		}
		statistics.postEvaluationStatistics(this);						
		generation++;
		return R_NOTDONE;	
	}



}
