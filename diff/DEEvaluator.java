package ec.diff;
import ec.*;
import ec.vector.*;
import ec.simple.*;
import ec.meme.*;
/**
 * 
 * An evaluator that keeps the better individual into population
 *
 */
public class DEEvaluator extends SimpleEvaluator
{
	private String TEST_FUNCTION_1 = "dejongn5";
	private String TEST_FUNCTION_2 = "dejongn5v1";
	private static final String ALGORITHM = "de"; 	
	private Benchmarks bmrks = Benchmarks.getInstance();
	boolean firstTimeOnly = false;
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
					
					int environmentID = bmrks.getEnvironmentID(state.generation, ALGORITHM);
					String problemType = (environmentID % 2 == 0) ? TEST_FUNCTION_1 : TEST_FUNCTION_2;
					if ((environmentID % 2 == 1) && !firstTimeOnly) //For second epoch only in TDO
					{
						previousPopulation = state.population;
						firstTimeOnly = true;
					}
					else if ((environmentID % 2 == 0) && firstTimeOnly)
					{
						previousPopulation = state.population;
						firstTimeOnly = false;
					}
					
					for( int j = 0 ; j < state.population.subpops[i].individuals.length ; j++ )
						if( previousPopulation.subpops[i].individuals[j].fitness.betterThan( state.population.subpops[i].individuals[j].fitness ) )
							state.population.subpops[i].individuals[j] = previousPopulation.subpops[i].individuals[j];
					
				}
			}
		}
		else state.output.fatal("DEEvaluator requires DEBreeder to be the breeder.");
	}
}
