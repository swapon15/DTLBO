package ec.tlbo;

import java.util.ArrayList;
import java.util.Random;

public class MovingPeaks {
	
	
	private Random rand = new Random();
	private double vLength = 1.0;
	
	private double minCoord = 0.0;
	private double maxCoord = 100.0;
	
	private double minHeight = 30.0;
	private double maxHeight = 70.0;
	
	private double minWidth = 1.0;
	private double maxWidth = 12.0;
	
	//private double minLambda = 0.0;
	//private double maxLambda = 1.0;
	
	private double lambda = 0.5;

	private double sevHeight = 7.0;
	private double sevWidth = 1.0;

	//private double minShift = 0.0;
	//private double maxShif = 3.0;
	
	//private int changeFrequency = 5000; // number of Evaluations - set in config file by generation
	public ArrayList<aPeakInObjFunction> landscape;
	
	
	private double getShiftVector (Double prevShift) {
		
		// FixMe: this code is adapted from another java library
		// need to verify the formula
		
		double sum = 0, sum2;
		double shift[] = new double[1];
		
		for (int j = 0; j < shift.length; j++) {
			shift[j] = rand.nextDouble() - 0.5;
			sum += shift[j] * shift[j];
		}
		if (sum > 0.0) {
			sum = this.vLength / Math.sqrt(sum);
		} else {/* only in case of rounding errors */
			sum = 0.0;
		}
		sum2 = 0.0;
		for (int j = 0; j < shift.length; j++) {
			shift[j] = sum * (1.0 - lambda) * shift[j] + lambda * prevShift;
			sum2 += shift[j] * shift[j];
		}
		if (sum2 > 0.0)
			sum2 = this.vLength / Math.sqrt(sum2);
		else
			/* only in case of rounding errors */
			sum2 = 0.0;
		
		double pm = 0;	
		for (int j = 0; j < shift.length; j++) {
			shift[j] *= sum2;
			pm = shift[j];
		}
		return pm;
	}
	
	public void changePeak() {
	
		
		ArrayList<aPeakInObjFunction> newLandScape = new ArrayList<aPeakInObjFunction>();
		double maxPeakHeight = 0;
			
		for (int p = 0; p < landscape.size(); p++) {
			aPeakInObjFunction peak = new aPeakInObjFunction(); 			
			double oldHeight = landscape.get(p).getPeakHeight();
			double offset = sevHeight * rand.nextGaussian();	
			
			if (oldHeight + offset < minHeight)
				peak.setPeakHeight(2.0 * minHeight - oldHeight - offset);
			else if (oldHeight + offset > maxHeight)
				peak.setPeakHeight(2.0 * maxHeight - oldHeight - offset);
			else
				peak.setPeakHeight(oldHeight + offset);
			
			if (peak.getPeakHeight() > maxPeakHeight)
				maxPeakHeight = peak.getPeakHeight();
			peak.setMaxPeakHeight(maxPeakHeight);
			
			double oldWidth = landscape.get(p).getPeakWidth();
			offset = sevWidth * rand.nextGaussian();
						
			if (oldWidth + offset < minWidth)
				peak.setPeakWidth(2.0 * minWidth - oldWidth - offset);
			else if (oldWidth + offset > maxWidth)
				peak.setPeakWidth(2.0 * maxWidth - oldWidth - offset);
			else
				peak.setPeakWidth(oldWidth + offset);
			
			ArrayList<Double> oldPosition = landscape.get(p).getPeakPosition();		
			ArrayList<Double> newPosition = new ArrayList<Double>();
			ArrayList<Double> newShiftVector = new ArrayList<Double>();
			
			for (int pos = 0; pos < oldPosition.size(); pos++) {
				double oldVal = oldPosition.get(pos);
				double shiftVector = getShiftVector(landscape.get(p).getShiftVector().get(pos));
				double disp = oldVal + shiftVector;
				
				if (disp < minCoord) {
					newPosition.add(2.0 * minCoord - disp);
					newShiftVector.add(-1.0 * shiftVector);
				}
				else if (disp > maxCoord) {
					newPosition.add(2.0 * maxCoord - disp);
					newShiftVector.add(-1.0 * shiftVector);
				}
				else {
				    newPosition.add(disp);
				    newShiftVector.add(shiftVector);
				}
			}
			peak.setPeakPosition(newPosition);
			peak.setShiftVector(newShiftVector);
			newLandScape.add(peak); // updating the landscape to new landscape
		}
		landscape = newLandScape;
	}
	
	/**
	 * create peaks individually and stored all the peaks to make the landscape
	 * @param dimension
	 * @param numPeaks
	 */
	public MovingPeaks(int dimension, int numPeaks) {
		landscape = new ArrayList<aPeakInObjFunction>();
		double maxPeakHeight = 0;
		for (int p = 0; p < numPeaks; p++) {
			aPeakInObjFunction peak = new aPeakInObjFunction();
			ArrayList<Double> pos = new ArrayList<Double>(dimension);
			ArrayList <Double> shiftVector = new ArrayList<Double>();
			
			for (int dim = 0; dim < dimension; dim++) {
				pos.add(minCoord + (maxCoord - minCoord) * rand.nextDouble());
				shiftVector.add(rand.nextDouble() - 0.5);
			}
				
			peak.setPeakPosition(pos);
			peak.setShiftVector(shiftVector);
			peak.setPeakHeight(minHeight + (maxHeight - minHeight) * rand.nextDouble());
			
			if (peak.getPeakHeight() > maxPeakHeight) {
				maxPeakHeight = peak.getPeakHeight();
			}
			peak.setMaxPeakHeight(maxPeakHeight);
			peak.setPeakWidth(minWidth + (maxWidth - minWidth) * rand.nextDouble());
			landscape.add(peak);
		}		
	}
	
	private double evaluate(double[] solution, ArrayList<Double> peakPosition, double height, double width) {
		   double val = 0;
		   for (int dim = 0; dim < solution.length; dim++) 
			   val += Math.pow(solution[dim] - peakPosition.get(dim), 2);
		   return height / (1 + width * val);
	   }
	
	public double evaluateASolution (double [] solution) {
		double globalMax = 0;
		for (int p = 0; p < landscape.size(); p++) {	
			double val = evaluate (solution, landscape.get(p).getPeakPosition(), landscape.get(p).getPeakHeight(), landscape.get(p).getPeakWidth());	
			if (val > globalMax) globalMax = val;		
		}
		return globalMax;
		
	}
}
