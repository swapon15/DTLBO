package ec.meme;
import ec.*;
import ec.simple.*;
import ec.vector.*;
import java.util.Random;
import ec.util.*;
/**
 * 
 * Evaluation state that conforms teacher and learner phase
 * The design may be done in different ways as in DE.
 * But in that case, the flow would be 
 * initialization > [evaluation > breeding > evaluation > breeding > evaluation]
 *
 */
public class MyEvolutionState extends SimpleEvolutionState 
{
	private MyBreeder breeder;
	private Benchmarks bmrks;
	private MersenneTwisterFast mtf;
	private int minGene;
	private int maxGene;
	private int popSize;
	private Population pop;
	private Subpopulation subpop;
	private static final String TEST_FUNCTION_1 = "dejongn5";
	private static final String TEST_FUNCTION_2 = "dejongn5v1";
	private static final String ALGORITHM = "tlbo"; 

	public void startFresh() 
	{
		double _minGene;
		double _maxGene;
		mtf = this.random[0];
		super.startFresh();
		pop = this.population;
		subpop = pop.subpops[0];           
		popSize = subpop.individuals.length;
		_minGene = ((FloatVectorSpecies)subpop.species).minGene(0);
		_maxGene = ((FloatVectorSpecies)subpop.species).maxGene(0);
		minGene = (int)_minGene;
		maxGene = (int) _maxGene;
		breeder = new MyBreeder();
		bmrks = Benchmarks.getInstance();
	}

	/*** Get teacher index ***/
	private int getTeacherIndex(Individual[] inds)
	{
		int index = 0;
		DoubleVectorIndividual ind  = (DoubleVectorIndividual)(inds[0]);
		double fitDummy = ((SimpleFitness)ind.fitness).fitness();

		for (int i = 1; i <inds.length ; i++)
		{
			ind  = (DoubleVectorIndividual)(inds[i]);
			double fitness = ((SimpleFitness)ind.fitness).fitness();
			if (fitness > fitDummy)
			{
				index = i;
				fitDummy = fitness;
			}	        	
		}
		return index;
	}

	/** Get mean of each decision variable ***/
	private double[] getMean(Individual[] inds)
	{
		DoubleVectorIndividual ind = (DoubleVectorIndividual)(inds[0]);
		double [] mean = new double[ind.genomeLength()];

		for (int gene = 0; gene < ind.genomeLength(); gene++)
		{
			double sum = 0;
			for (int indiv = 0; indiv < inds.length; indiv++)
			{ 		 
				sum += ((DoubleVectorIndividual)inds[indiv]).genome[gene];
			}
			mean[gene] = sum/inds.length;
		}
		return mean;
	}

	private void teacherPhase()
	{
		DoubleVectorIndividual dummyInd = (DoubleVectorIndividual)(subpop.individuals[0]);
		int tIndex = getTeacherIndex(subpop.individuals);
		DoubleVectorIndividual teacher = (DoubleVectorIndividual)(subpop.individuals[tIndex]);
		double tFactor = mtf.nextInt(2) + 1;

		double [] mean = getMean(subpop.individuals);
		int len = dummyInd.genomeLength();
		double [] rands = new double[len];
		double [] genome = new double[len];

		for (int i = 0; i < dummyInd.genomeLength(); i++)
			rands[i] = mtf.nextDouble();			

		/** Bring each learners to the class average **/		
		for (int indis = 0; indis < popSize; indis++)
		{
			DoubleVectorIndividual original = (DoubleVectorIndividual)(subpop.individuals[indis]);

			for (int x  = 0; x < len; x++)
				genome[x] = original.genome[x];

			int environmentID = bmrks.getEnvironmentID(this.generation, ALGORITHM);
			String problemType = (environmentID % 2 == 0) ? TEST_FUNCTION_1 : TEST_FUNCTION_2  ;

			double originalFx = bmrks.getFx(genome, problemType);

			double [] differenceMean = new double[len];
			double [] originalGenome = (double[]) original.getGenome();
			double [] variantGenome = new double[originalGenome.length]; 

			for (int i = 0; i < originalGenome.length; i++)			
				variantGenome[i] = originalGenome[i];

			for (int gene = 0; gene <original.genomeLength(); gene++)
			{ 
				differenceMean[gene] = rands[gene] * (teacher.genome[gene] - tFactor * mean[gene]);
				variantGenome[gene] += differenceMean[gene];
				if (problemType.compareTo("ackley") == 0)
				{
					minGene = -32;
					maxGene = 32;
				}
				variantGenome[gene] = Math.max(minGene, variantGenome[gene]);
				variantGenome[gene] = Math.min(maxGene, variantGenome[gene]);			
			}

			//			for (int i = 0; i < original.genomeLength(); i++)
			//			  System.out.print(originalGenome[i] + " ");
			//			System.out.println();
			//			for (int i = 0; i < original.genomeLength(); i++)
			//				  System.out.print(variantGenome[i] + " ");
			//			System.out.println();
			environmentID = bmrks.getEnvironmentID(this.generation, ALGORITHM);
			problemType = (environmentID % 2 == 0) ?  TEST_FUNCTION_1 : TEST_FUNCTION_2 ;  

			double variantFx = bmrks.getFx(variantGenome, problemType);
			//	System.out.println("Index " +indis + " original " +bmrks.getFx(originalGenome) + " variant " +bmrks.getFx(variantGenome));
			if (variantFx > originalFx)
			{
				original.setGenome(variantGenome);
				((SimpleFitness)original.fitness).setFitness(this, variantFx, false);
				original.evaluated = true;
			}	
			//	System.out.println(original.genotypeToStringForHumans() + "  " + ((SimpleFitness)original.fitness).fitness());
		}
	}

	private void learnerPhase()
	{
		DoubleVectorIndividual dummyInd = (DoubleVectorIndividual)(subpop.individuals[0]);
		double [] rands = new double[dummyInd.genomeLength()];
		for (int i = 0; i < dummyInd.genomeLength(); i++)			
			rands[i] = mtf.nextDouble();			   

		for (int learner = 0; learner < popSize; learner++)
		{
			DoubleVectorIndividual me = (DoubleVectorIndividual)(subpop.individuals[learner]);
			int index = mtf.nextInt(popSize);
			DoubleVectorIndividual mate = (DoubleVectorIndividual)(subpop.individuals[index]);

			double [] myGenome = new double[dummyInd.genomeLength()]; 
			double [] mateGenome = new double[dummyInd.genomeLength()];
			double [] newGenome = new double[dummyInd.genomeLength()];
			double [] differenceMean = new double[dummyInd.genomeLength()];

			for (int i = 0; i < dummyInd.genomeLength() ; i++)
			{
				myGenome[i] = me.genome[i];
				mateGenome[i] = mate.genome[i];
				newGenome[i] = myGenome[i];
			}

			for (int gene = 0; gene < dummyInd.genomeLength(); gene++)
				differenceMean[gene] = rands[gene]*(myGenome[gene] - mateGenome[gene]);

			if (((SimpleFitness)mate.fitness).fitness() > ((SimpleFitness)me.fitness).fitness()) 
			{	
				for (int gene = 0; gene < dummyInd.genomeLength(); gene++)
					differenceMean[gene] =  -rands[gene]*(myGenome[gene] - mateGenome[gene]);				
			}
			int environmentID = bmrks.getEnvironmentID(this.generation, ALGORITHM);
			String problemType = (environmentID % 2 == 0) ?  TEST_FUNCTION_1 : TEST_FUNCTION_2 ;  
			for (int gene = 0; gene < dummyInd.genomeLength(); gene++)
			{
				newGenome[gene] += differenceMean[gene];
				//				if (problemType.compareTo("ackley") == 0)
				//				{
				//					minGene = -32;
				//					maxGene = 32;
				//				}
				newGenome[gene] = Math.max(minGene, newGenome[gene]);
				newGenome[gene] = Math.min(maxGene, newGenome[gene]);
			}
			if (bmrks.getFx(newGenome, problemType) > bmrks.getFx(myGenome, problemType))
			{
				((SimpleFitness)me.fitness).setFitness(this, bmrks.getFx(newGenome, problemType) , false);
				me.setGenome(newGenome);
				me.evaluated = true;
			}
		}
	}

	public int evolve() 
	{
		if (generation > 0) 
			output.message("Generation " + generation);

		statistics.preEvaluationStatistics(this);
		/** START : POPULATION INITIALIZATION ***/
		if (this.generation == 0)
		{
			DoubleVectorIndividual ind0 = (DoubleVectorIndividual)(subpop.individuals[0]);
			int len = ind0.genome.length;
			double [] genome = new double[len];
			for (int i = 0; i <popSize ; i++)
			{
				DoubleVectorIndividual ind = (DoubleVectorIndividual)(subpop.individuals[i]);

				for (int x = 0; x < len; x++)
					genome[x] = ind.genome[x];

				int environmentID = bmrks.getEnvironmentID(this.generation, ALGORITHM);
				String problemType = (environmentID % 2 == 0) ?  TEST_FUNCTION_1 : TEST_FUNCTION_2 ;   

				double fx = bmrks.getFx(genome, problemType);
				((SimpleFitness)ind.fitness).setFitness(this, fx, false);
				ind.evaluated = true;
			}
			//			for (int i = 0; i < subpop.individuals.length; i++)
			//			 {
			//			   DoubleVectorIndividual indi = (DoubleVectorIndividual)subpop.individuals[i];
			//			   System.out.println("Index@Gen=0 "+i + " Indiv " + indi.genotypeToStringForHumans() + " Fitness " + ((SimpleFitness)indi.fitness).fitness());
			//			 }
		}
		/** END : POPULATION INITIALIZATION ***/
		else
		{
			teacherPhase();
			learnerPhase();
			//			for (int i = 0; i < subpop.individuals.length; i++)
			//			 {
			//			   DoubleVectorIndividual indi = (DoubleVectorIndividual)subpop.individuals[i];
			//			   System.out.println("Index@END "+i + " Indiv " + indi.genotypeToStringForHumans() + " Fitness " + ((SimpleFitness)indi.fitness).fitness());
			//			 }
		}

		if (evaluator.runComplete(this) && quitOnRunComplete)
		{
			output.message("Found Ideal Individual");
			return R_SUCCESS;
		}

		if (generation == numGenerations-1)
		{
			return R_FAILURE;
		}

		statistics.postEvaluationStatistics(this);
		statistics.prePreBreedingExchangeStatistics(this);
		population = exchanger.preBreedingExchangePopulation(this);
		statistics.postPreBreedingExchangeStatistics(this);

		String exchangerWantsToShutdown = exchanger.runComplete(this);
		if (exchangerWantsToShutdown!=null)
		{ 
			output.message(exchangerWantsToShutdown);                     
			return R_SUCCESS;
		}

		statistics.preBreedingStatistics(this);
		population = breeder.breedPopulation(this);
		statistics.postBreedingStatistics(this);
		statistics.prePostBreedingExchangeStatistics(this);
		population = exchanger.postBreedingExchangePopulation(this);
		statistics.postPostBreedingExchangeStatistics(this);
		generation++;
		return R_NOTDONE;	
	}
}
