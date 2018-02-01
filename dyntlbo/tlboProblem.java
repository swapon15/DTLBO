package ec.tlbo;

import ec.util.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ec.*;
import ec.simple.*;
import ec.vector.*;
/*
 * Calculates the benchmark functions
 */

public class tlboProblem extends Problem implements SimpleProblemForm  {
	
    public static final String P_WHICH_PROBLEM = "type";
    public static final String NUM_EPOCH = "num-epoch";
    public static final String V_SPHERE = "sphere";
    public static final String V_ROSENBROC = "rosenbroc";
    public static final String V_SCHWEFEL = "schwefel";
    public static final String V_RASTARIGIN = "rastarigin";
    public static final String V_ACKLEY = "ackley";
    public static final String V_MPB = "mpb";
    
    public static final String V_INV_SPHERE = "inv-sphere";   
    public static final String V_DEJONG_N5 = "dejongn5";
    public static final String V_INV_DEJONG_N5 = "inv-dejongn5";
    
    public static final int PROB_SPHERE = 1;
    public static final int PROB_ROSENBROC = 2;
    public static final int PROB_SCHWEFEL = 3;
    public static final int PROB_RASTARIGIN = 4;
    public static final int PROB_ACKLEY = 5;
       
    public static final int PROB_INV_SPHERE = 6;    
    public static final int PROB_DEJONG_N5 = 7;
    public static final int PROB_INV_DEJONG_N5 = 8;
    public static final int PROB_MPB = 9;
    
    public int problemType = PROB_MPB;  
    public int epochLength;
    public int numEpoch;
    public static int environmentID = -1;
    private MovingPeaks mpb;
    private boolean [] epochStatus;
    private String FILE_MAX_PEAK = "max_peak.txt";
    File file = null;
    FileWriter fw = null;
    BufferedWriter bw =  null;
    
    public void setup(final EvolutionState state, final Parameter base) {
	    super.setup(state, base);
	    String wp = state.parameters.getStringWithDefault( base.push( P_WHICH_PROBLEM ), null, "" );
	    numEpoch = state.parameters.getIntWithDefault( base.push( NUM_EPOCH ), null, 0 );
	    epochLength = state.numGenerations / numEpoch; 
	   	if (numEpoch > 1) { // A single time during a run
	 
	   		epochStatus = new boolean[numEpoch];
	   		mpb = new MovingPeaks(5, 10); // #peak = #dim = 1;
	   		
	   		try {
	   			BufferedWriter bw  = new BufferedWriter(new FileWriter(FILE_MAX_PEAK, true));
	   			bw.newLine();
			    bw.write(mpb.landscape.get(mpb.landscape.size() - 1).getMaxPeakHeight() + ",");		
				bw.close();
	   		} catch(IOException e) {
	   			e.printStackTrace();
	   		}
	   		
	 
	   		for (int x = 0; x < numEpoch; x++) 
	   			epochStatus[x] = false;		
	   	}
	    if (wp.compareTo(V_SPHERE) == 0)
	        problemType = PROB_SPHERE;
	    else if(wp.compareTo(V_ROSENBROC) == 0)
	    	problemType = PROB_ROSENBROC;
	    else if(wp.compareTo(V_SCHWEFEL) == 0)
	    	problemType = PROB_SCHWEFEL;
	    else if(wp.compareTo(V_RASTARIGIN) == 0)
	    	problemType = PROB_RASTARIGIN;
	    else if(wp.compareTo(V_ACKLEY) == 0)
	    	problemType = PROB_ACKLEY;
	    else if(wp.compareTo(V_ACKLEY) == 0)
	    	problemType = PROB_INV_SPHERE;
	    else if(wp.compareTo(V_DEJONG_N5) == 0)
	    	problemType = PROB_DEJONG_N5;
	    else if(wp.compareTo(V_INV_DEJONG_N5) == 0)
	    	problemType = PROB_INV_DEJONG_N5;
	    else if(wp.compareTo(V_MPB) == 0)
	    	problemType = PROB_MPB;
	     else 
	        System.out.println("ERROR: problem type is not set");           
    }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum) {

    	if( !( ind instanceof DoubleVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be DoubleVectorIndividuals." );

        DoubleVectorIndividual temp = (DoubleVectorIndividual)ind;
        double[] genome = temp.genome;

        double fit = (function(state, problemType, temp.genome, threadnum));
      
        if (fit < (0.0 - Double.MAX_VALUE))  {
            ((SimpleFitness)(ind.fitness)).setFitness( state, 0.0 - Double.MAX_VALUE, false );
            state.output.warnOnce("'Product' type used: some fitnesses are negative infinity, setting to lowest legal negative number.");
            }
        else if (fit > Double.MAX_VALUE) {
            ((SimpleFitness)(ind.fitness)).setFitness( state, Double.MAX_VALUE, false );
            state.output.warnOnce("'Product' type used: some fitnesses are negative infinity, setting to lowest legal negative number.");
            }
        else 
            ((SimpleFitness)(ind.fitness)).setFitness( state, fit, false );
            
        ind.evaluated = true;
       }

    public double function(EvolutionState state, int function, double[] genome, int threadnum) {

        double value = 0;
        int len = genome.length;
        final double MAX_DEJONG_N5 = 3905.93;
        final double MAX_SPHERE = 10000.0 ;
        
//        if (numEpoch > 1) { // Time Dependent Optimization
//        	environmentID = state.generation / epochLength;
//	    	if ( environmentID % 2 == 0) // f(x), even epoch
//	    		problemType = PROB_DEJONG_N5; 
//	    	else  // 1 - fx, odd epoch
//	    		problemType = PROB_INV_DEJONG_N5;
//	    }
                
        if (numEpoch > 1) { //TDO with MPB
        	environmentID = state.generation / epochLength;
	    	if (( environmentID > 0) && (!epochStatus[environmentID] ))
	    	    {
	    			mpb.changePeak();
	    			try {
	    	   			BufferedWriter bw  = new BufferedWriter(new FileWriter(FILE_MAX_PEAK, true));
	    			    bw.write(mpb.landscape.get(mpb.landscape.size() - 1).getMaxPeakHeight() + ",");		
	    				bw.close();
	    	   		} catch(IOException e) {
	    	   			e.printStackTrace();
	    	   		}
	    			//System.out.println("Multiple Changes " + mpb.landscape.get(mpb.landscape.size() - 1).getMaxPeakHeight());
	    			epochStatus[environmentID] = true;
	    		} 
        }
             
        switch(problemType) {
            case PROB_SPHERE:
                for( int i = 0 ; i < len ; i++ ) {
                    double gi = genome[i] ;
                    value += (gi * gi);
                  }
                return -value;
            case PROB_ROSENBROC: //rosenbroc
    			for( int i = 1 ; i < len ; i++ ){
    				double gj = genome[i-1] ;
    				double gi = genome[i] ;
    				value += (100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj));
    			}
    			return -value;
            case PROB_SCHWEFEL: // schwefel
    			double B = 418.9829 * len;
    			for( int i = 0 ; i < len ; i++ ) {
    				double gi = genome[i] ;
    				value += (gi * Math.sin(Math.sqrt(Math.abs(gi))));
    			}  
    			value = B - value;
    			return -value;
            case PROB_RASTARIGIN: //rastargrin
    			final double A = 10.0;
    			for( int i = 0 ; i < len ; i++ )
    			{
    				double gi = genome[i]  ;
    				value += (gi*gi - A * Math.cos( 2 * Math.PI * gi ));
    			}
    			value = value + A * len;
    			return -value;
            case PROB_ACKLEY: // Ackley
    			double sum1 = 0.0;   
    			double sum2 = 0.0;   
    			for (int i = 0 ; i < len ; i ++) 
    			{   
    				sum1 += (genome[i] * genome[i]);   
    				sum2 += (Math.cos(2*Math.PI*genome[i]));   
    			}   
    			value = -20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / ((double )len))) -  Math.exp(sum2 / ((double)len)) + 20.0 + Math.E;   
    			return -value;
            case PROB_INV_SPHERE:
            	
                for( int i = 0 ; i < len ; i++ ) {
                    double gi = genome[i] ;
                    value += (gi * gi);
                  }
                double fx = -value;
                double invFx = MAX_SPHERE + fx;
                return -invFx;
                
            case PROB_DEJONG_N5:
            	 
            	for( int i = 1 ; i < len ; i++ ) {
    				double gj = genome[i-1] ;
    				double gi = genome[i] ;
    				value += (100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj));
    			}
    			value = MAX_DEJONG_N5 - value;
    			return value;
           case PROB_INV_DEJONG_N5:
            	for( int i = 1 ; i < len ; i++ ) {
    				double gj = genome[i-1] ;
    				double gi = genome[i] ;
    				value += (100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj));
    			}   			
    			return value;
           case PROB_MPB:
        	   return mpb.evaluateASolution(genome); 
            default:
                state.output.fatal( "ERROR: Invalid problem -- how on earth did that happen?" );
                return 0;  // never happens
           }
     }


    }
