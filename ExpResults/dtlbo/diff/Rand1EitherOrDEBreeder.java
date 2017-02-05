package ec.diff;

import ec.*;
import ec.util.*;
import ec.vector.*;
public class Rand1EitherOrDEBreeder extends DEBreeder
    {
    public double PF = 0.0;
        
    public static final String P_PF = "pf";
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);

        PF = state.parameters.getDouble(base.push(P_PF),null,0.0);
        if ( PF < 0.0 || PF > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_PF), null );
                        
        if (state.parameters.exists(base.push(P_Cr), null))
            state.output.warning("Crossover parameter specified, but Rand1EitherOrDEBreeder does not use crossover.", base.push(P_Cr));
        }
        
    public DoubleVectorIndividual createIndividual( final EvolutionState state,
        int subpop,
        int index,
        int thread )
        {
        Individual[] inds = state.population.subpops[subpop].individuals;

        DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops[subpop].species.newIndividual(state, thread));
        int retry = -1;
        do
            {
            retry++;
            
            // select three indexes different from each other and from that of the current parent
            int r0, r1, r2;
            do
                {
                r0 = state.random[thread].nextInt(inds.length);
                }
            while( r0 == index );
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
                if (state.random[thread].nextBoolean(PF))
                    v.genome[i] = g0.genome[i] + F * (g1.genome[i] - g2.genome[i]);
                else
                    v.genome[i] = g0.genome[i] + 0.5 * (F+1) * (g1.genome[i] + g2.genome[i] - 2 * g0.genome[i]);
            }
        while(!valid(v) && retry < retries);
        if (retry >= retries && !valid(v))  // we reached our maximum
            {
            // completely reset and be done with it
            v.reset(state, thread);
            }

        return v;       // no crossover is performed
        }

    }
