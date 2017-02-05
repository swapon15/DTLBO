package ec.meme;
import ec.*;
import ec.util.*;
import ec.simple.*;
import ec.vector.*;

/**
 * 
 * Evaluator class for SGA that sets fitness of indivduals
 *
 */
public class SGAProblem extends Problem implements SimpleProblemForm
{
	private Benchmarks bmrks = Benchmarks.getInstance();

	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{		
		String problemType = (state.generation < state.numGenerations/2) ? "sphere" : "invschawfel" ;

		DoubleVectorIndividual indiv = (DoubleVectorIndividual)ind;
		double [] genome = (double[])indiv.getGenome();
		double fx = bmrks.getFx(genome, problemType);
		((SimpleFitness)indiv.fitness).setFitness(state, fx, false);
		indiv.evaluated = true;

	}
}
