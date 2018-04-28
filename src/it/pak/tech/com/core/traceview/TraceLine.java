package it.pak.tech.com.core.traceview;

public class TraceLine
{
  private int entrance;
  private int exit;
  private String signature;
  private double timeLength;
  private double consumption;
  private double inclTime ;
  private double exclTime ;
  private int numOfCalls;
  
  public double getInclTime() {
	
	  inclTime = inclTime / 1000000 ;
	  return inclTime;
}

public void setInclTime(double inclTime) {
	this.inclTime = inclTime;
}

public double getExclTime() {
	
	exclTime = exclTime / 1000000 ;
	return exclTime;
}

public void setExclTime(double exclTime) {
	this.exclTime = exclTime;
}

public int getNumOfCalls() {
	return numOfCalls;
}

public void setNumOfCalls(int numOfCalls) {
	this.numOfCalls = numOfCalls;
}

public TraceLine() {}
  
  public int getEntrance()
  {
    return entrance;
  }
  
  void setEntrance(int entrance) {
    this.entrance = entrance;
  }
  
  public int getExit() {
    return exit;
  }
  
  void setExit(int exit) {
    this.exit = exit;
  }
  
  public String getSignature() {
    return signature;
  }
  
  public void setSignature(String signature) {
    this.signature = signature;
  }
  
  public double getTimeLength() {
    return timeLength;
  }
  
  public void setTimeLength(double timeLength) {
    this.timeLength = timeLength;
  }
  
  public double getConsumption() {
    return consumption;
  }
  
  public void setConsumption(double consumption) {
    this.consumption = consumption;
  }
}
