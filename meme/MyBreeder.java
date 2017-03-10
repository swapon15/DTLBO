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
	private static final double RANDOM_IMMIGRANT_RATIO = 0;

	private String TEST_FUNCTION_1 = "dejongn5";
	private String TEST_FUNCTION_2 = "dejongn5v1";
	private static final String ALGORITHM = "tlbo"; 
	
	private Benchmarks bmrks;
	public Population breedPopulation(EvolutionState state) 
	{
		ArrayList<Integer> popIndex = new ArrayList<Integer>();
		Random rand = new Random();
		Population newPop = (Population) state.population; // current population
		Subpopulation subpop = newPop.subpops[0];				
		bmrks = Benchmarks.getInstance();
 
		int environmentID = bmrks.getEnvironmentID(state.generation, ALGORITHM);
		String problemType = (environmentID % 2 == 0) ? TEST_FUNCTION_1 : TEST_FUNCTION_2  ;  

		int retries = 0;
		DoubleVectorIndividual immigrant, original;
		int len = subpop.individuals.length;
		double sizeRandomImmigrant =  len * RANDOM_IMMIGRANT_RATIO;
		do
		{
			int index = rand.nextInt(len);
			retries++;

			if (!popIndex.contains(index))
				popIndex.add(index);

			if (retries > sizeRandomImmigrant * 100)
				break;


			immigrant = (DoubleVectorIndividual)subpop.species.newIndividual(state, 0);
			original  = (DoubleVectorIndividual)subpop.individuals[index];
			original  = immigrant; // replace original individual[index] by immigrant
					
			double fitImmigrant = bmrks.getFx(original.genome, problemType);
			((SimpleFitness)original.fitness).setFitness(state, fitImmigrant, false);

		} while(popIndex.size() <= sizeRandomImmigrant);

		return newPop;
	}

}