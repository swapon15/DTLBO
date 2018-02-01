package ec.tlbo;
import ec.*;
import ec.vector.*;
import ec.simple.*;
import ec.tlbo.*;
import java.util.Random;
/*
 * The evaluator moves strictly better individual into next generation
 */
public class tlboEvaluator extends SimpleEvaluator {
	boolean firstTimeOnly = false;
	public void evaluatePopulation(EvolutionState state) {
		super.evaluatePopulation(state);
		Population previousPopulation =  (Population)tlboBreeder.previousPopulation;
		int environmentID = tlboProblem.environmentID;
		
			if( previousPopulation != null ) {				
				if ((environmentID % 2 == 1) && !firstTimeOnly) {
					previousPopulation = state.population;
					firstTimeOnly = true;
				}
				else if ((environmentID % 2 == 0) && firstTimeOnly) {
					previousPopulation = state.population;
					firstTimeOnly = false;
				}
				for( int i = 0 ; i < previousPopulation.subpops.length ; i++ ) {					
					for( int j = 0 ; j < state.population.subpops[i].individuals.length ; j++ )
						if( previousPopulation.subpops[i].individuals[j].fitness.betterThan( state.population.subpops[i].individuals[j].fitness )){
					        	state.population.subpops[i].individuals[j] = previousPopulation.subpops[i].individuals[j];
						}
					
				    }
			    } 
	 }
}
