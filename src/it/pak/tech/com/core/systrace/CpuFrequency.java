package it.pak.tech.com.core.systrace;

public class CpuFrequency
{
  private int cpuId;
  private int time;
  private int value;
  
  public CpuFrequency() {}
  
  public int getCore() {
    return cpuId;
  }
  
  void setCpuId(int cpuId) {
    this.cpuId = cpuId;
  }
  


  public int getTime()
  {
    return time;
  }
  


  public void setTime(int time)
  {
    this.time = time;
  }
  


  public int getValue()
  {
    return value;
  }
  


  void setValue(int value)
  {
    this.value = value;
  }
}
