package it.pak.tech.com.core.systrace;

import java.util.ArrayList;
import java.util.List;





public class SysTrace
{
  private List<CpuFrequency> frequencies = new ArrayList();
  private int systraceStartEntrance;
  
  public SysTrace() {}
  
  public List<CpuFrequency> getFrequencies() { return this.frequencies; }
  
  private int numberOfCpu;
  void setFrequencies(List<CpuFrequency> freq) {
    this.frequencies = freq;
//    for (CpuFrequency freq1 : this.frequencies) {
//    	
//    	System.out.println("FT: " + freq1.getTime());
//    }
  }
  
  public int getSystraceStartTime() {
    return systraceStartEntrance;
  }
  
  void setSystraceStartTime(int startSystraceEntrance) {
    systraceStartEntrance = startSystraceEntrance;
  }
  
  public int getNumberOfCpu() {
    return numberOfCpu;
  }
  
  public void setNumberOfCpu(int numberOfCpu) {
    this.numberOfCpu = numberOfCpu;
  }
}
