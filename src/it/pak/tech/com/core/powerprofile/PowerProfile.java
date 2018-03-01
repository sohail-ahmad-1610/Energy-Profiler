package it.pak.tech.com.core.powerprofile;

import java.util.HashMap;
import java.util.List;





public class PowerProfile
{
  private HashMap<String, Double> devices = new HashMap();
  private List<CpuClusterInfo> cpuInfo;
  
  public PowerProfile() {}
  
  public HashMap<String, Double> getDevices() { return devices; }
  
  private List<Double> radioInfo;
  void setDevices(HashMap<String, Double> devices) {
    this.devices = devices;
  }
  
  public double getCpuConsumptionByFrequency(int cluster, int frequency) {
    return ((CpuClusterInfo)cpuInfo.get(cluster)).getConsumption(frequency);
  }
  
  public int getClusterByCore(int core)
  {
    int lowerLimit = 0;
    int upperLimit = ((CpuClusterInfo)cpuInfo.get(0)).getNumCores();
    
    if (upperLimit == 0) {
      return 0;
    }
    
    for (int i = 0; i < cpuInfo.size(); i++) {
      if ((core >= lowerLimit) && (core < upperLimit)) {
        return i;
      }
      lowerLimit += ((CpuClusterInfo)cpuInfo.get(i)).getNumCores();
      upperLimit += ((CpuClusterInfo)cpuInfo.get(i + 1)).getNumCores();
    }
    

    return -1;
  }
  
  void setCpuInfo(List<CpuClusterInfo> cpuInfo)
  {
    this.cpuInfo = cpuInfo;
  }
  
  public List<Double> getRadioInfo() {
    return radioInfo;
  }
  
  void setRadioInfo(List<Double> radioInfo) {
    this.radioInfo = radioInfo;
  }
}
