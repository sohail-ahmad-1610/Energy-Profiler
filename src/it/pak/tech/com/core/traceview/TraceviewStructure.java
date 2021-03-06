package it.pak.tech.com.core.traceview;

import java.util.List;

public class TraceviewStructure
{
  private List<TraceLine> tracelines;
  private final int startTime;
  private final int endTime;
  
  TraceviewStructure(List<TraceLine> tracelines, int startTime, int endTime)
  {
    this.tracelines = tracelines;
    this.startTime = startTime;
    this.endTime = endTime;
  }
  
  public List<TraceLine> getTraceLines() {
    return tracelines;
  }
  
  public void setTracelines(List<TraceLine> tracelines) {
	this.tracelines = tracelines;
}

public int getStartTime() {
    return startTime;
  }
  
  public int getEndTime() {
    return endTime;
  }
}
