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
	private String TEST_FUNCTION_1 = "dejongn5";
	private String TEST_FUNCTION_2 = "dejongn5v1";
	private static final String ALGORITHM = "sga"; 
	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{		
		int environmentID = bmrks.getEnvironmentID(state.generation, ALGORITHM);
		String problemType = (environmentID % 2 == 0) ? TEST_FUNCTION_1 : TEST_FUNCTION_2  ;
				
		DoubleVectorIndividual indiv = (DoubleVectorIndividual)ind;
		double [] genome = (double[])indiv.getGenome();
		double fx = bmrks.getFx(genome, problemType);
		((SimpleFitness)indiv.fitness).setFitness(state, fx, false);
		indiv.evaluated = true;

	}
}
