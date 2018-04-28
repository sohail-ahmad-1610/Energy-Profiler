package it.pak.tech.com.core.batterystats;

import java.util.ArrayList;
import java.util.List;



public class EnergyInfo
{
  private final List<String> devices;
  private int entrance;
  private int exit;
  private int volt;
  private List<Integer> cpuFrequencies;
  private int phoneSignalStrength;
  
  EnergyInfo()
  {
    cpuFrequencies = new ArrayList();
    devices = new ArrayList();
  }
  
  public EnergyInfo(EnergyInfo toClone) {
    entrance = toClone.getEntrance();
    exit = toClone.getExit();
    volt = toClone.getVoltage();
    devices = new ArrayList(toClone.getDevices());
    cpuFrequencies = new ArrayList(toClone.getCpuFrequencies());
    phoneSignalStrength = toClone.getPhoneSignalStrength();
  }
  
  public int getEntrance() {
    return entrance;
  }
  
  public void setEntrance(int entrance) {
    this.entrance = entrance;
  }
  
  public int getExit() {
    return exit;
  }
  
  void setExit(int exit) {
    this.exit = exit;
  }
  
  public int getVoltage() {
    return volt;
  }
  
  void setVoltage(int volt) {
    this.volt = volt;
  }
  
  public List<String> getDevices() {
    return devices;
  }
  
  void addDevice(String device) {
    devices.add(device);
  }
  
  void removeDevice(String device) {
    for (int i = 0; i < getDevices().size(); i++) {
      if (((String)getDevices().get(i)).equals(device)) {
        devices.remove(i);
      }
    }
  }
  
  public List<Integer> getCpuFrequencies() {
    return cpuFrequencies;
  }
  
  public void setCpuFrequencies(List<Integer> cpuFrequencies) {
    this.cpuFrequencies = new ArrayList(cpuFrequencies);
  }
  
  public int getPhoneSignalStrength() {
    return phoneSignalStrength;
  }
  
  void setPhoneSignalStrength(int phoneSignalStrength) {
    this.phoneSignalStrength = phoneSignalStrength;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    EnergyInfo that = (EnergyInfo)o;
    
    return (entrance == entrance) && (exit == exit) && (volt == volt) && (cpuFrequencies.equals(cpuFrequencies)) && 
      (devices.equals(devices)) && (phoneSignalStrength == phoneSignalStrength);
  }
}
