package ec.tlbo;

import java.util.ArrayList;

public class aPeakInObjFunction {
   private ArrayList<Double> peakPosition = new ArrayList<Double>();
   private ArrayList <Double> shiftVector = new ArrayList<Double>();
   private double peakWidth = 0;
   private double peakHeight = 0;
   private double maxPeakHeight = 0;
   
   public void setShiftVector(ArrayList<Double> shiftVector) {
		this.shiftVector = shiftVector;
	}
	public ArrayList<Double> getShiftVector() {
		return shiftVector;
	}

   public void setPeakPosition(ArrayList <Double> peakPosition) {
	   this.peakPosition.addAll(peakPosition);	   
   }
   
   public ArrayList<Double> getPeakPosition() {
	   return peakPosition;
   }
   
   public void setPeakWidth(double peakWidth) {
	   this.peakWidth = peakWidth;
   }
   
   public double getPeakWidth() {
	   return peakWidth;
   }
   
   public void setPeakHeight (double peakHeight) {
	   this.peakHeight = peakHeight;
   }
   
   public double getPeakHeight() {
	   return peakHeight;
   }
   
   public void setMaxPeakHeight(double maxPeakHeight) {
	   this.maxPeakHeight = maxPeakHeight;
   }
   public double getMaxPeakHeight() {
	   return maxPeakHeight;
   }
}
