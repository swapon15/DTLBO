package ec.diff;
import ec.*;
import ec.simple.*;
/**
 * 
 * An evaluator that keeps the better individual into population
 *
 */
public class DEEvaluator extends SimpleEvaluator
{
	public void evaluatePopulation(EvolutionState state)
	{
		super.evaluatePopulation(state);

		if( state.breeder instanceof DEBreeder )
		{
			Population previousPopulation = ((DEBreeder)(state.breeder)).previousPopulation; 
			if( previousPopulation != null )
			{
				if( previousPopulation.subpops.length != state.population.subpops.length )
					state.output.fatal( "DEEvaluator requires that the population have the same number of subpopulations every generation.");
				for( int i = 0 ; i < previousPopulation.subpops.length ; i++ )
				{
					if( state.population.subpops[i].individuals.length != previousPopulation.subpops[i].individuals.length )
						state.output.fatal( "DEEvaluator requires that subpopulation " + i + " should have the same number of individuals in all generations." );
					for( int j = 0 ; j < state.population.subpops[i].individuals.length ; j++ )
						if( previousPopulation.subpops[i].individuals[j].fitness.betterThan( state.population.subpops[i].individuals[j].fitness ) )
							state.population.subpops[i].individuals[j] = previousPopulation.subpops[i].individuals[j];
				}
			}
		}
		else state.output.fatal("DEEvaluator requires DEBreeder to be the breeder.");
	}
}
