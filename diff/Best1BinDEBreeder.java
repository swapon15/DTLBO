package ec.diff;

import ec.*;
import ec.util.*;
import ec.vector.*;

/**
 * 
 * the breeding implementation of x/y/z where x is whom you select, y is how many you select and z means what type of operation you do
 *
 */
public class Best1BinDEBreeder extends DEBreeder
{
	/** limits on uniform noise for F */
	public double F_NOISE = 0.0;

	public static final String P_FNOISE = "f-noise";

	public void setup(final EvolutionState state, final Parameter base) 
	{
		super.setup(state,base);

		F_NOISE = state.parameters.getDouble(base.push(P_FNOISE), null, 0.0);
		if ( F_NOISE < 0.0 )
			state.output.fatal( "Parameter not found, or its value is below 0.0.", base.push(P_FNOISE), null );
	}


	public DoubleVectorIndividual createIndividual( final EvolutionState state,
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

			// select three indexes different from each other and from that of the current parent
			int r0, r1, r2;
			// do
			{
				r0 = bestSoFarIndex[subpop];
			}
			// while( r0 == index );
			do
			{
				r1 = state.random[thread].nextInt(inds.length);
			}
			while( r1 == r0 || r1 == index );
			do
			{
				r2 = state.random[thread].nextInt(inds.length);
			}
			while( r2 == r1 || r2 == r0 || r2 == index );

			DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
			DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
			DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);

			for(int i = 0; i < v.genome.length; i++)
				v.genome[i] = g0.genome[i] + 
				(F + state.random[thread].nextDouble() * F_NOISE - (F_NOISE / 2.0)) *
				(g1.genome[i] - g2.genome[i]);
		}
		while(!valid(v) && retry < retries);
		if (retry >= retries && !valid(v))  // we reached our maximum
		{
			// completely reset and be done with it
			v.reset(state, thread);
		}

		return crossover(state, (DoubleVectorIndividual)(inds[index]), v, thread);
	}

}
