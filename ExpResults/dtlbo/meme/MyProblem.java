package ec.meme;
import ec.*;
import ec.util.*;
import ec.simple.*;
import ec.vector.*;
/**
 * 
 * A dummy problem class that does nothing. We can't escape this because of ECJ's design guideline
 *
 */
public class MyProblem extends Problem implements SimpleProblemForm
{
	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{
		ind.evaluated = true;
		return;
	}
}
