package ec.meme;
import ec.simple.*;
import ec.*;
import ec.vector.*;

import java.util.ArrayList;
import java.util.Random;
/***
 * 
 * A breeder that injects random immigrant inside population
 *
 */
public class MyBreeder extends SimpleBreeder 
{
	private static final double RANDOM_IMMIGRANT_RATIO = 0.30;
	private Benchmarks bmrks;
	public Population breedPopulation(EvolutionState state) 
	{
		ArrayList<Integer> popIndex = new ArrayList<Integer>();
		Random rand = new Random();
		Population newPop = (Population) state.population;
		Subpopulation subpop = newPop.subpops[0];
		int len = subpop.individuals.length;
		double sizeRandomImmigrant =  len * RANDOM_IMMIGRANT_RATIO;
		int retries = 0;	
		DoubleVectorIndividual immigrant, original;
		bmrks = Benchmarks.getInstance();

		String problemType = (state.generation < state.numGenerations/2) ? "ackley" : "invschawfel" ;  

		do
		{
			int index = rand.nextInt(len);
			retries++;

			if (!popIndex.contains(index))
				popIndex.add(index);

			if (retries > sizeRandomImmigrant * 10)
				break;


			immigrant = (DoubleVectorIndividual)subpop.species.newIndividual(state, 0);
			original = (DoubleVectorIndividual)subpop.individuals[index];

			for (int i = 0; i < original.genome.length; i++)
				original.genome[i] = immigrant.genome[i];

			double fitImmigrant = bmrks.getFx(immigrant.genome, problemType);
			((SimpleFitness)immigrant.fitness).setFitness(state, fitImmigrant, false);

		} while(popIndex.size() <= sizeRandomImmigrant);

		return newPop;
	}

}