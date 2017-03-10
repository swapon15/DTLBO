package ec.meme;
import ec.vector.*;
/**
 * Benchmark calculator
 * */
public class Benchmarks 
{
	
	private static final double MAX_INV_SCHWEFEL = 2513.9;
	private static final double MAX_DE_JONG_N5_MAX = 3905.93;
	private static  Benchmarks NEW_INSTANCE  = null;
	public static final String P_WHICH_PROBLEM = "type";
	
	public static final String P_SPHERE = "sphere";
	public static final String P_SUM_SQUARE = "sumsquare";
	public static final String P_STEPINT = "stepint";
	public static final String P_ROSENBROC = "rosenbrocs";
	public static final String P_RASTARIGIN = "rastarigin";
	public static final String P_SCHAWFEL = "schawfel";
	public static final String P_INV_SCHAWFEL = "invschawfel";
	public static final String P_ACKLEY = "ackley";
	public static final String P_SPHERE_V1 = "spherev1";
	public static final String P_INV_SCHAWFEL_V1 = "invschawfelv1";
	public static final String P_DE_JONG_N5_MAX = "dejongn5";
	public static final String P_INV_DE_JONG_N5_MAX = "dejongn5v1";
	

	public static final int PROB_SPHERE = 0;
	public static final int PROB_SUM_SQUARE = 1;
	public static final int PROB_STEPINT = 2;
	public static final int PROB_ROSENBROCK = 3;
	public static final int PROB_RASTARIGIN = 4;
	public static final int PROB_SCHWAFEL = 5;
	public static final int PROB_INV_SCHWAFEL = 6;
	public static final int PROB_ACKLEY = 7;
	public static final int PROB_SPHERE_V1 = 8;
	public static final int PROB_INV_SCHWAFEL_V1 = 9;
	public static final int PROB_DE_JONG_N5_MAX = 10;
	public static final int PROB_INV_DE_JONG_N5_MAX = 11;
	
	private static final String ALGORITHM_TLBO = "tlbo";
	private static final String ALGORITHM_DE = "de";
	private static final String ALGORITHM_SGA = "sga";
	
	private String TEST_FUNCTION_1 = P_DE_JONG_N5_MAX;
	private String TEST_FUNCTION_2 = P_INV_DE_JONG_N5_MAX;
	
	public int problemNo = PROB_SPHERE;

	private Benchmarks() {}

	public static Benchmarks getInstance()
	{
		if (NEW_INSTANCE == null)
		{
			NEW_INSTANCE = new Benchmarks();
		}
		return NEW_INSTANCE;
	}
    public int getEnvironmentID(int currentGeneration, String Algorithm)
    {
    	int epochLength = 0;
    	if (ALGORITHM_DE.compareTo(Algorithm) == 0)
    		epochLength = 600;
    	else if (ALGORITHM_TLBO.compareTo(Algorithm) == 0)
    		epochLength = 300;
    	else if (ALGORITHM_SGA.compareTo(Algorithm) == 0)
    		epochLength = 600;
    	return (currentGeneration) / epochLength;
    }
	public double getFx(double [] genome, String problemType)
	{
		if (problemType.compareTo(TEST_FUNCTION_1) == 0)
			problemNo = PROB_DE_JONG_N5_MAX;
		else if (problemType.compareTo(TEST_FUNCTION_2) == 0)
			problemNo = PROB_INV_DE_JONG_N5_MAX;

		double fx = 0;
		int len = genome.length;
		switch(problemNo)
		{
		case PROB_SPHERE:        	// Sphere	
			for (int i = 0; i < len; i++)
				fx += genome[i] * genome[i];
			return -fx;
		case PROB_SUM_SQUARE:       // SumSquares
			for (int i = 0; i < len; i++)
				fx += genome[i] * genome[i] * (i+1);
			return -fx;
		case PROB_STEPINT: //Step int
			for (int i = 0; i < len; i++)
				fx += Math.abs(genome[i]);
			fx += 25;
			return -fx;
		case PROB_ROSENBROCK: //rosenbroc
			for( int i = 1 ; i < len ; i++ )
			{
				double gj = genome[i-1] ;
				double gi = genome[i] ;
				fx += 100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj);
			}
			return -fx;
		case PROB_RASTARIGIN: //rastargrin
			final double A = 10.0;
			fx = len * A;
			for( int i = 0 ; i < len ; i++ )
			{
				double gi = genome[i]  ;
				fx += ( gi*gi - A * Math.cos( 2 * Math.PI * gi ) + A);
			}
			return -fx;
		case PROB_SCHWAFEL: // schwefe
			double B = 418.9829 * len;
			for( int i = 0 ; i < len ; i++ )
			{
				double gi = genome[i] ;
				fx += -gi * Math.sin(Math.sqrt(Math.abs(gi)));
			}  
			fx += B;
			return -fx;
		case PROB_INV_SCHWAFEL: // inverted schwefel for maximization
			for( int i = 0 ; i < len ; i++ )
			{
				double gi = genome[i] ;
				fx += -gi * Math.sin(Math.sqrt(Math.abs(gi)));
			}  
			fx = -1 * fx;
			return fx;   
		case PROB_ACKLEY: // Ackley
			double sum1 = 0.0;   
			double sum2 = 0.0;   

			for (int i = 0 ; i < len ; i ++) 
			{   
				sum1 += (genome[i] * genome[i]);   
				sum2 += (Math.cos(2*Math.PI*genome[i]));   
			}   
			fx = -20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / ((double )len))) -  Math.exp(sum2 / ((double)len)) + 20.0 + Math.E;   
			return -fx;
		case PROB_SPHERE_V1:        	// Sphere V1	
			for (int i = 0; i < len; i++)
				fx += genome[i] * genome[i];
			return MAX_INV_SCHWEFEL - fx; //Fix Me
		 
		case PROB_INV_SCHWAFEL_V1:	// inverted schwefel tdo
             for( int i = 0 ; i < len ; i++ )
                 {
                 double gi = genome[i] ;
                 fx += -gi * Math.sin(Math.sqrt(Math.abs(gi)));
                 }    
                 fx = -1* fx;
             return MAX_INV_SCHWEFEL - fx;
		case PROB_DE_JONG_N5_MAX: //De Jong's N5
			for( int i = 1 ; i < len ; i++ )
			{
				double gj = genome[i-1] ;
				double gi = genome[i] ;
				fx += 100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj);
			}
			fx = MAX_DE_JONG_N5_MAX - fx;
			return fx;
			
		case PROB_INV_DE_JONG_N5_MAX: //De Jong's N5 inverted
			for( int i = 1 ; i < len ; i++ )
			{
				double gj = genome[i-1] ;
				double gi = genome[i] ;
				fx += 100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj); 
			}
			return fx;
			
		default:
			System.out.println("FATAL ERROR: ");
			return 0;
		}
	}
}