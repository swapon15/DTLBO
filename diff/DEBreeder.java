package ec.diff;
import java.util.ArrayList;
import java.util.Random;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.meme.*;
import ec.simple.*;
/**
 * 
 * A breeder that does crossover, mutation in place. Hence, bypass the conventional breeding pipeline in ECJ
 *
 */
public class DEBreeder extends Breeder
{
	public static final double CR_UNSPECIFIED = -1;
	public double F = 0.0;
	public double Cr = CR_UNSPECIFIED;
	public int retries = 0;

	public static final String P_F = "f";
	public static final String P_Cr = "cr";
	public static final String P_OUT_OF_BOUNDS_RETRIES = "out-of-bounds-retries";       
	public Population previousPopulation = null;  
	public int[] bestSoFarIndex = null;

	ArrayList<Integer> popIndex = new ArrayList<Integer>();
	Random rand = new Random();
	private static final double RANDOM_IMMIGRANT_RATIO = 0;
	private Benchmarks bmrks;
	
	private String TEST_FUNCTION_1 = "dejongn5";
	private String TEST_FUNCTION_2 = "dejongn5v1";

	public void setup(final EvolutionState state, final Parameter base) 
	{
		bmrks = Benchmarks.getInstance();
		if (!state.parameters.exists(base.push(P_Cr), null))  
			Cr = CR_UNSPECIFIED;
		else
		{
			Cr = state.parameters.getDouble(base.push(P_Cr),null,0.0);
			if ( Cr < 0.0 || Cr > 1.0 )
				state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_Cr), null );
		}

		F = state.parameters.getDouble(base.push(P_F),null,0.0);
		if ( F < 0.0 || F > 1.0 )
			state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_F), null );

		retries = state.parameters.getInt(base.push(P_OUT_OF_BOUNDS_RETRIES), null, 0);
		if (retries < 0)
			state.output.fatal(" Retries must be a value >= 0.0.", base.push(P_OUT_OF_BOUNDS_RETRIES), null);
	}

	public void prepareDEBreeder(EvolutionState state)
	{

		if( bestSoFarIndex == null || state.population.subpops.length != bestSoFarIndex.length )
			bestSoFarIndex = new int[state.population.subpops.length];

		for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
		{
			Individual[] inds = state.population.subpops[subpop].individuals;
			bestSoFarIndex[subpop] = 0;
			for( int j = 1 ; j < inds.length ; j++ )
				if( inds[j].fitness.betterThan(inds[bestSoFarIndex[subpop]].fitness) )
					bestSoFarIndex[subpop] = j;
		}
	}

	public Population breedPopulation(EvolutionState state)
	{
		if (!(state.evaluator instanceof DEEvaluator))
			state.output.warnOnce("DEEvaluator not used, but DEBreeder used.  This is almost certainly wrong.");

		prepareDEBreeder(state);
		Population newpop = (Population) state.population.emptyClone();

		for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
		{
			if (state.population.subpops[subpop].individuals.length < 4)  
				state.output.fatal("Subpopulation " + subpop + " has fewer than four individuals, and so cannot be used with DEBreeder.");

			Individual[] inds = newpop.subpops[subpop].individuals;
			for( int i = 0 ; i < inds.length ; i++ )
			{
				newpop.subpops[subpop].individuals[i] = createIndividual( state, subpop, i, 0);  
			}
		}

		previousPopulation = state.population;
		
		
		// Injects random immigrant into populations 
		int retries = 0;	
		int len = state.population.subpops[0].individuals.length;
		double sizeRandomImmigrant =  len * RANDOM_IMMIGRANT_RATIO;
		DoubleVectorIndividual immigrant, original;
		String problemType = (state.generation < state.numGenerations/2) ? TEST_FUNCTION_1 : TEST_FUNCTION_2 ;

		do
		{
			int index = rand.nextInt(len);
			retries++;

			if (!popIndex.contains(index))
				popIndex.add(index);

			if (retries > sizeRandomImmigrant * 100)
				break;


			immigrant = (DoubleVectorIndividual)state.population.subpops[0].species.newIndividual(state, 0);
			original  = (DoubleVectorIndividual)state.population.subpops[0].individuals[index];
            original  = immigrant;
		

			double fitImmigrant = bmrks.getFx(original.genome, problemType);
			((SimpleFitness)original.fitness).setFitness(state, fitImmigrant, false);

		} while(popIndex.size() <= sizeRandomImmigrant);

		return newpop;
	}

	public boolean valid(DoubleVectorIndividual ind)
	{
		return (ind.isInRange());
	}

	public DoubleVectorIndividual createIndividual(
			EvolutionState state,
			int subpop,
			int index,
			int thread)
	{
		Individual[] inds = state.population.subpops[subpop].individuals;

		DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops[subpop].species.newIndividual(state, thread));
		int retry = -1;
		do
		{
			retry++;
			int r0, r1, r2;
			do
			{
				r0 = state.random[thread].nextInt(inds.length);
			}while( r0 == index );
			do
			{
				r1 = state.random[thread].nextInt(inds.length);
			}while( r1 == r0 || r1 == index );
			do
			{
				r2 = state.random[thread].nextInt(inds.length);
			} while( r2 == r1 || r2 == r0 || r2 == index );

			DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
			DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
			DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);

			for(int i = 0; i < v.genome.length; i++)
				v.genome[i] = g0.genome[i] + F * (g1.genome[i] - g2.genome[i]);
		} while(!valid(v) && retry < retries);

		if (retry >= retries && !valid(v))  
		{
			v.reset(state, thread);
		}

		return crossover(state, (DoubleVectorIndividual)(inds[index]), v, thread);
	}

	public DoubleVectorIndividual crossover(EvolutionState state, DoubleVectorIndividual target, DoubleVectorIndividual child, int thread)
	{
		if (Cr == CR_UNSPECIFIED)
			state.output.warnOnce("Differential Evolution Parameter cr unspecified.  Assuming cr = 0.5");

		int index = state.random[thread].nextInt(child.genome.length);
		double val = child.genome[index];

		for(int i = 0; i < child.genome.length; i++)
		{
			if (state.random[thread].nextDouble() < Cr)
				child.genome[i] = target.genome[i];
		}

		child.genome[index] = val;
		return child;
	}

}
