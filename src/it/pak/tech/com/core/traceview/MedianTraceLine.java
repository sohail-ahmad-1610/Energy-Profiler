package it.pak.tech.com.core.traceview;

import java.util.ArrayList;
import java.util.List;

public class MedianTraceLine {

	private String signature;
	private List<Double> timeLengthList;
	private List<Double> consumptionList;
	private List<Double> inclTimeList ;
	private List<Double> exclTimeList ;
	private int numOfCalls;
	
	// Default Constructor.......
	public MedianTraceLine() {
		
		signature = "" ;
		timeLengthList = new ArrayList<Double>();
		consumptionList = new ArrayList<Double>();
		inclTimeList = new ArrayList<Double>();
		exclTimeList = new ArrayList<Double>();
		numOfCalls = 0 ;
	}
	
	public void addConsumption(Double value) {
		this.consumptionList.add(value);
	}
	
	public void addExculsiveTime(Double value) {
		this.exclTimeList.add(value);
	}
	public void addInclusiveTime(Double value) {
		this.inclTimeList.add(value);
	}
	public void addTimeLength(Double value) {
		this.timeLengthList.add(value);
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public List<Double> getTimeLengthList() {
		return timeLengthList;
	}

	public void setTimeLengthList(List<Double> timeLengthList) {
		this.timeLengthList = timeLengthList;
	}

	public List<Double> getConsumptionList() {
		return consumptionList;
	}

	public void setConsumptionList(List<Double> consumptionList) {
		this.consumptionList = consumptionList;
	}

	public List<Double> getInclTimeList() {
		return inclTimeList;
	}

	public void setInclTimeList(List<Double> inclTimeList) {
		this.inclTimeList = inclTimeList;
	}

	public List<Double> getExclTimeList() {
		return exclTimeList;
	}

	public void setExclTimeList(List<Double> exclTimeList) {
		this.exclTimeList = exclTimeList;
	}

	public int getNumOfCalls() {
		return numOfCalls;
	}

	public void setNumOfCalls(int numOfCalls) {
		this.numOfCalls = numOfCalls;
	}
	
	
}
