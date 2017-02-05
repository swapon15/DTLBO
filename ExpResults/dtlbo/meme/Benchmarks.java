package ec.meme;
import ec.vector.*;
/**
 * Benchmark calculator
 * */
public class Benchmarks 
{
	private static  Benchmarks NEW_INSTANCE  = null;
	public static final String P_WHICH_PROBLEM = "type";
	public static final String P_SPHERE = "sphere";
	public static final String P_ROSENBROC = "rosenbrocs";
	public static final String P_SCHAWFEL = "schawfel";
	public static final String P_INV_SCHAWFEL = "invschawfel";
	public static final String P_CUSTOM_MAXIMA = "custom-maxima1";
	public static final String P_ACKLEY = "ackley";

	public static final int PROB_SPHERE = 0;
	public static final int PROB_ROSENBROC = 1;
	public static final int PROB_SCHAWEFL = 2;

	public static final int PROB_CUSTOM_MAXIMA = 3;

	public int problemNo = 4; //default

	private Benchmarks() {}

	public static Benchmarks getInstance()
	{
		if (NEW_INSTANCE == null)
		{
			NEW_INSTANCE = new Benchmarks();
		}
		return NEW_INSTANCE;
	}

	public double getFx(double [] genome, String problemType)
	{
		if (problemType.compareTo(P_ACKLEY) == 0)
			problemNo = 7;
		else if (problemType.compareTo(P_INV_SCHAWFEL) == 0)
			problemNo = 6;

		double fx = 0;
		switch(problemNo)
		{
		case 0:        	// Sphere	
			for (int i = 0; i < genome.length; i++)
				fx += genome[i] * genome[i];
			return -fx;
		case 1:       // SumSquares
			for (int i = 0; i < genome.length; i++)
				fx += genome[i] * genome[i] * (i+1);
			return -fx;
		case 2: //Step int
			for (int i = 0; i < genome.length; i++)
				fx += Math.abs(genome[i]);
			fx += 25;
			return -fx;
		case 3: //rosenbroc
			for( int i = 1 ; i < genome.length ; i++ )
			{
				double gj = genome[i-1] ;
				double gi = genome[i] ;
				fx += 100 * (gj*gj - gi) * (gj*gj - gi) +  (1-gj) * (1-gj);
			}
			return -fx;
		case 4: //rastargrin
			final double A = 10.0;
			fx = genome.length * A;
			for( int i = 0 ; i < genome.length ; i++ )
			{
				double gi = genome[i]  ;
				fx += ( gi*gi - A * Math.cos( 2 * Math.PI * gi ) + A);
			}
			return -fx;
		case 5: // schwefe
			double B = 418.9829 * genome.length;
			for( int i = 0 ; i < genome.length ; i++ )
			{
				double gi = genome[i] ;
				fx += -gi * Math.sin(Math.sqrt(Math.abs(gi)));
			}  
			fx += B;
			return -fx;
		case 6: // inverted schwefel for maximization
			for( int i = 0 ; i < genome.length ; i++ )
			{
				double gi = genome[i] ;
				fx += -gi * Math.sin(Math.sqrt(Math.abs(gi)));
			}  
			return -fx;   
		case 7: // Ackley
			int len = genome.length;
			double sum1 = 0.0;   
			double sum2 = 0.0;   

			for (int i = 0 ; i < len ; i ++) 
			{   
				sum1 += (genome[i] * genome[i]);   
				sum2 += (Math.cos(2*Math.PI*genome[i]));   
			}   
			fx = -20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / ((double )len))) -  Math.exp(sum2 / ((double)len)) + 20.0 + Math.E;   
			return -fx;
		default:
			System.out.println("FATAL ERROR: ");
			return 0;
		}
	}
}