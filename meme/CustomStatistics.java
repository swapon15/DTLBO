package ec.meme;

import ec.*;
import ec.steadystate.*;
import java.io.IOException;
import ec.util.*;
import java.io.File;
import ec.simple.*;
import ec.vector.*;
import java.util.Arrays;
/*
 * Prints all the individuals in every generations.
 */
public class CustomStatistics extends SimpleStatistics  
{
	boolean warned = false;

	public void postEvaluationStatistics(final EvolutionState state)	
	{
		super.postEvaluationStatistics(state);         
		Individual[] best_i = new Individual[state.population.subpops.length];

		for(int x=0;x<state.population.subpops.length;x++)	
		{
			best_i[x] = state.population.subpops[x].individuals[0];

			for (int m = 0; m <state.population.subpops[x].individuals.length; m++)
				state.population.subpops[x].individuals[m].printIndividualForHumans(state, statisticslog);

			for(int y=1;y<state.population.subpops[x].individuals.length;y++)	
			{
				if (state.population.subpops[x].individuals[y] == null)	
				{
					if (!warned)	
					{
						state.output.warnOnce("Null individuals found in subpopulation");
						warned = true;  
					}
				}
				else if (best_i[x] == null || (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness)))
					best_i[x] = state.population.subpops[x].individuals[y];
				if (best_i[x] == null)	
				{
					if (!warned)	
					{
						state.output.warnOnce("Null individuals found in subpopulation");
						warned = true; 
					}
				}
			}

			if (best_of_run[x]==null || (best_i[x].fitness.betterThan(best_of_run[x].fitness)))
				best_of_run[x] = (Individual)(best_i[x].clone());            
		}
	}
}
