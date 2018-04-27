package ec.diff;
import java.util.ArrayList;
import java.util.Random;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.simple.*;
/**
 * 
  * Implementing self adaptive differential evolution
 */
public class SaDEBreeder extends Breeder
{
	public static final double CR_UNSPECIFIED = -1;
	public double F = 0.0;
	public double Cr = CR_UNSPECIFIED;
	public int retries = 0;

	public static final String P_F = "f";
	public static final String P_Cr = "cr";   
	private double Fl = 0.1;
	private double Fu = 0.9 ;
	private double tau1 = 0.1;
	private double tau2 = 0.1;
			
	public Population previousPopulation = null;  
	public int[] bestSoFarIndex = null;

	ArrayList<Integer> popIndex = new ArrayList<Integer>();
	Random rand = new Random();
		
	public void setup(final EvolutionState state, final Parameter base) 
	{
		Cr = state.parameters.getDouble(base.push(P_Cr),null,0.0);
		F = state.parameters.getDouble(base.push(P_F),null,0.0);
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
		prepareDEBreeder(state);
		Population newpop = (Population) state.population.emptyClone();
        
		double rand1 = rand.nextDouble();
        double rand2 = rand.nextDouble();
        double rand3 = rand.nextDouble();
        double rand4 = rand.nextDouble();
        
		for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
		{		
			Individual[] inds = newpop.subpops[subpop].individuals;
			for( int i = 0 ; i < inds.length ; i++ )
			{
				newpop.subpops[subpop].individuals[i] = createIndividual( state, subpop, i, 0, rand1, rand2, rand3, rand4);  
			}
		}

		previousPopulation = state.population;
		return newpop;
	}

	public boolean valid(DoubleVectorIndividual ind)
	{
		return (ind.isInRange());
	}

	private double [] updateControlParameters(EvolutionState state, DoubleVectorIndividual v, double rand1, double rand2, double rand3, double rand4)
	{
		if (state.generation == 0)
		{
			v.genome[v.genome.length - 1] = F;
			v.genome[v.genome.length - 2] = Cr;
		}
		double Fg = v.genome[v.genome.length - 1];
		double Crg = v.genome[v.genome.length - 2];
		double Fgnext = Fg;
		double Crgnext = Crg;
		double [] parameters = new double[2];
		
		if (rand2 < tau1)
		{
			Fgnext = Fl + rand1*Fu;
		}
		parameters[0] = Fg;
		
		if (rand4 < tau2)
		{
			Crgnext = rand3;
		}
		parameters[1] = Cr;
		return parameters;
	}
	public DoubleVectorIndividual createIndividual(
			EvolutionState state,
			int subpop,
			int index,
			int thread,
			double rand1, 
			double rand2, 
			double rand3, 
			double rand4)
	{
		Individual[] inds = state.population.subpops[subpop].individuals;

		DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops[subpop].species.newIndividual(state, thread));
		
		//adjust F and CR
		double [] parameters = updateControlParameters(state, v, rand1, rand2, rand3, rand4);
		v.genome[v.genome.length - 1] = parameters[0];
		v.genome[v.genome.length - 2] = parameters[1];
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

			
		// mutation
			for(int i = 0; i < v.genome.length; i++)
				v.genome[i] = g0.genome[i] +  v.genome[v.genome.length - 1] * (g1.genome[i] - g2.genome[i]); 
			
		} while(!valid(v) && retry < retries);

		if (retry >= retries && !valid(v))  
		{
			v.reset(state, thread);
		}
        //crossover
		return crossover(state, (DoubleVectorIndividual)(inds[index]), v, thread);
	}

	public DoubleVectorIndividual crossover(EvolutionState state, DoubleVectorIndividual target, DoubleVectorIndividual child, int thread)
	{
		int index = state.random[thread].nextInt(child.genome.length);
		double val = child.genome[index];

		for(int i = 0; i < child.genome.length; i++)
		{
			if (state.random[thread].nextDouble() < child.genome[child.genome.length - 2]) 
				child.genome[i] = target.genome[i];
		}

		child.genome[index] = val;
		return child;
	}

}
