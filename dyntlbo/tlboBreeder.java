package ec.tlbo;
import java.util.ArrayList;
import java.util.Random;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.simple.*;
/**
 * 
 * Breeds the individual for teacher and learner phase
 *
 */
public class tlboBreeder extends Breeder {
	    
	public static Population previousPopulation ;  
	public int[] bestSoFarIndex;
	private MersenneTwisterFast mtf;
	
	public tlboBreeder() {
		previousPopulation = null;
		bestSoFarIndex = null;
	}

	public void setup(final EvolutionState state, final Parameter base) { return;}
	
	public void prepareTLBOBreeder(EvolutionState state) {

		if( bestSoFarIndex == null || state.population.subpops.length != bestSoFarIndex.length )
			bestSoFarIndex = new int[state.population.subpops.length];

		for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ ) {
			Individual[] inds = state.population.subpops[subpop].individuals;
			bestSoFarIndex[subpop] = 0;
			
			for( int j = 1 ; j < inds.length ; j++ )
				if( inds[j].fitness.betterThan(inds[bestSoFarIndex[subpop]].fitness) )
					bestSoFarIndex[subpop] = j;
		}
	}
	
	public int getTeacherIndex(EvolutionState state, Population oldPopulation) {
		/** find out the teacher individual***/		
		
		Individual [] inds = oldPopulation.subpops[0].individuals;
		Individual teacher =  inds[0];
	    int i = 1, tIndex = 0;			
		for (; i < inds.length; i++) 		  
		  if (((SimpleFitness)teacher.fitness).fitness() < ((SimpleFitness)inds[i].fitness).fitness()) {
			  teacher = inds[i];
			  tIndex = i;
		  }
		return tIndex;		
	}
	
	private Individual teacherPhase (EvolutionState state, Population oldPopulation, int idx, Individual teacher, double[] mean, int tF, int genomeLength) {
		
		Individual ind = (Individual)oldPopulation.subpops[0].individuals[idx].clone();
		Individual myCopy = (Individual)oldPopulation.subpops[0].individuals[idx];
		
		for (int x = 0; x < genomeLength; x++) {
			double diffMean = mtf.nextDouble() * (((DoubleVectorIndividual)teacher).genome[x] - tF * mean[x]);
			((DoubleVectorIndividual)ind).genome[x] += diffMean;
		}
		
		if (!valid((DoubleVectorIndividual)ind)) // if not in range then return original copy 
			return myCopy;
				
		return ind;
	}
	
	private Individual learnerPhase(EvolutionState state, Population oldPopulation, int idx,  int genomeLength) {
		int popSize = oldPopulation.subpops[0].individuals.length;
		int peerId = mtf.nextInt(popSize);
		Individual me = (Individual)oldPopulation.subpops[0].individuals[idx].clone();
		Individual myCopy = (Individual)oldPopulation.subpops[0].individuals[idx];
		Individual peer = oldPopulation.subpops[0].individuals[peerId];
		
		if (((SimpleFitness)me.fitness).fitness() > ((SimpleFitness)peer.fitness).fitness()) {
			for (int x = 0; x < genomeLength; x++) {
				 double rand = mtf.nextDouble();   
				((DoubleVectorIndividual)me).genome[x] +=  rand * (((DoubleVectorIndividual)me).genome[x] - ((DoubleVectorIndividual)peer).genome[x]);				
			}			
		}
		else {
			for (int x = 0; x < genomeLength; x++) {
				 double rand = mtf.nextDouble();    
				((DoubleVectorIndividual)me).genome[x] += rand * (((DoubleVectorIndividual)peer).genome[x] - ((DoubleVectorIndividual)me).genome[x]);				
			}
		}
		if (!valid((DoubleVectorIndividual)me)) return myCopy;
		return me;
	}
	private double[] getMean(Population oldPopulation, int genomeLength) {
		
	    double [] mean = new double[genomeLength];
	    int popSize = oldPopulation.subpops[0].individuals.length;
		for (int gene = 0; gene < genomeLength; gene++) {
			double sum = 0;
			for (int i = 0; i < popSize ; i++) {
				DoubleVectorIndividual ind = (DoubleVectorIndividual)oldPopulation.subpops[0].individuals[i];
				sum += ((DoubleVectorIndividual)ind).genome[gene];
			}
			mean[gene] = sum/(popSize*1.0);
		}
		return mean;
	}
	
    public Population breedPopulation(EvolutionState state) { return null;    }
	
	private double getFitnessMean(Subpopulation subpop) {
		double sum = 0;
		for (int x = 0; x < subpop.individuals.length; x++) {
			DoubleVectorIndividual ind = (DoubleVectorIndividual)subpop.individuals[x];		
			sum += ((SimpleFitness)(ind.fitness)).fitness();
			
		}
		return sum/subpop.individuals.length;
	}
	
	private double getFitnessStd(Subpopulation subpop, double mu) {
		return Math.sqrt(getVariance(subpop, mu));
	}
	
	private double getVariance(Subpopulation subpop, double mu) {
		double tmp = 0;
		for (int x = 0; x < subpop.individuals.length; x++) {
			DoubleVectorIndividual ind = (DoubleVectorIndividual)subpop.individuals[x];		
			double dataPoint = ((SimpleFitness)(ind.fitness)).fitness();
			tmp += (dataPoint - mu) * (dataPoint - mu);
		}
		return tmp/(subpop.individuals.length - 1);
	}
    
	private double getZScore(double mu, double std, double data) {
		return (data - mu)/std;
	}
	
	private double [] getQuantumGenome(double [] pg) {
		Random rand = new Random();
		
		double [] tmp = new double[pg.length];
		double [] newGenome = new double[pg.length];
		double t = 0;
		for (int x = 0; x < pg.length; x++) { 
			tmp[x] = rand.nextGaussian();
			t += tmp[x] * tmp[x];		
		}
		double dist = Math.sqrt(t);
		
		double r = rand.nextDouble(); // r_cloud = 1
		for (int x = 0; x < pg.length; x++) {
			newGenome[x] = pg[x] + (r * tmp[x])/dist;
		}
		return newGenome;
	}
	
	private void populationStat (Subpopulation subpop) {
		Individual [] inds = subpop.individuals;
		double mu = getFitnessMean(subpop);
		double std = getFitnessStd(subpop, mu);
		int minThreeToTwo = 0, minOneToTwo = 0, minZeroToOne = 0;
		int plusThreeToTwo = 0, plusOneToTwo = 0, plusZeroToOne = 0;
		
		for (int x = 0; x < inds.length; x++) {
			DoubleVectorIndividual ind = (DoubleVectorIndividual) inds[x];
			double fit = ((SimpleFitness)(ind.fitness)).fitness();
			double zScore = getZScore(mu, std, fit);
			if (zScore < -2) 
				minThreeToTwo++;
			else if(zScore < -1 && zScore >= -2)
				minOneToTwo++;
			else if (zScore < 0 && zScore >= -1)
				minZeroToOne++;
			else if (zScore <1 && zScore >= 0)
				plusZeroToOne++;
			else if (zScore < 2 && zScore >= 1)
				plusOneToTwo++;
			else if (zScore >= 2)
				plusThreeToTwo++;			
		}
		System.out.println(minThreeToTwo + ", " + minOneToTwo + ", " + minZeroToOne + " : ");
		System.out.print(plusThreeToTwo + ", " + plusOneToTwo + ", " + plusZeroToOne);
		System.out.println();
	}
    public Population breedPopulation(EvolutionState state, int whichPhase, double diversityRatio) {
		
		prepareTLBOBreeder(state);		
		mtf = state.random[0];
		Population oldPopulation = state.population;
		//populationStat(oldPopulation.subpops[0]);
		previousPopulation = oldPopulation; // old population		
		
		int k = getTeacherIndex(state, oldPopulation);
		Individual teacher = (Individual)oldPopulation.subpops[0].individuals[k];
		
		double [] pg = ((DoubleVectorIndividual)teacher).genome;
		int genomeLength = pg.length;
		double [] meanByDimension = new double[genomeLength];
		meanByDimension = getMean(oldPopulation, genomeLength);
		
		double mu = getFitnessMean(oldPopulation.subpops[0]);
		double std = getFitnessStd(oldPopulation.subpops[0], mu);
		
		Population newpop = (Population) state.population.emptyClone();
		
		int diverseIndiv = (int)(oldPopulation.subpops[0].individuals.length * diversityRatio);
		int countTeacherPhase = 0, countLearnerPhase = 0;
		
		for( int i = 0 ; i < newpop.subpops[0].individuals.length ; i++ ) {
			
			DoubleVectorIndividual indiv = (DoubleVectorIndividual) oldPopulation.subpops[0].individuals[i];
			double fit = ((SimpleFitness)(indiv.fitness)).fitness();
			double zScore = getZScore(mu, std, fit);
			
			if (whichPhase == 0) { // Teacher Phase
				int tFactor = mtf.nextInt(2) + 1;
				newpop.subpops[0].individuals[i] = teacherPhase(state, oldPopulation, i, teacher, meanByDimension, tFactor, genomeLength);
				
				if (diversityRatio > 0.0 && zScore < 0 && countTeacherPhase <= diverseIndiv && i != k) {
					double[] qtIndivGenome = getQuantumGenome(pg);
					((DoubleVectorIndividual)newpop.subpops[0].individuals[i]).setGenome(qtIndivGenome);
					countTeacherPhase++;
				} 
					
				
			} else { // Learner Phase
				newpop.subpops[0].individuals[i] = learnerPhase(state, oldPopulation, i,  genomeLength);
				if (diversityRatio > 0.0 && zScore < 0 && countLearnerPhase <= diverseIndiv && i != k) {
					double[] qtIndivGenome = getQuantumGenome(pg);
					((DoubleVectorIndividual)newpop.subpops[0].individuals[i]).setGenome(qtIndivGenome);
					countLearnerPhase++;
				} 
					
			}			
		 }		
		return newpop;
	}

	public boolean valid(DoubleVectorIndividual ind) {
		return (ind.isInRange());
	}
}
