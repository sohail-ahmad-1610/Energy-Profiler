package it.pak.tech.com.core.powerprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class CpuClusterInfo
{
  private final List<Integer> frequencies;
  private final List<Double> consumptions;
  private final HashMap<Integer, Double> info;
  private int numCores;
  
  CpuClusterInfo()
  {
    frequencies = new ArrayList();
    consumptions = new ArrayList();
    info = new HashMap();
  }
  
  int getNumCores() {
    return numCores;
  }
  
  void setNumCores(int numCore) {
    numCores = numCore;
  }
  
  void addFrequency(Integer frequency) {
    frequencies.add(frequency);
  }
  
  double getConsumption(int frequency) {
	  
	  // Here is an error map does not contain key mapping.......
	  double d = ((Double)info.get(Integer.valueOf(frequency))).doubleValue();
	  //System.out.println("Consumption Against Freq: " + frequency + " " + d);
	  return d;
  }
  
  void addConsumption(Double consumption) {
    consumptions.add(consumption);
  }
  
  void setIdleConsumption(double idleConsumption) {
    info.put(Integer.valueOf(0), Double.valueOf(idleConsumption));
  }
  
  void mergeFrequenciesAndConsumptions() {
    for (int i = 0; i < frequencies.size(); i++) {
      
    	if (i >= consumptions.size()) {
    		
   // 		System.out.println("Freq Added: " + frequencies.get(i) + " " + consumptions.get((consumptions.size()) - 1));
    		info.put(frequencies.get(i), consumptions.get((consumptions.size()) - 1));
    	}
    	else {
    //		System.out.println("Freq Added: " + frequencies.get(i) + " " + consumptions.get(i));
    		info.put(frequencies.get(i), consumptions.get(i));
    	}
    	
    }
  }
}
