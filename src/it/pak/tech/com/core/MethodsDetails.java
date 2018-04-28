package it.pak.tech.com.core;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MethodsDetails {
	
	private final StringProperty signature;
	//private final StringProperty testcase;
	private final DoubleProperty joules;
	private final DoubleProperty seconds;
	private final DoubleProperty inclusiveTime;
	private final DoubleProperty exclusiveTime;
	private final IntegerProperty noOfCalls;
	
	public MethodsDetails(String signature, double joules, double seconds,
			int noOfCalls,double exclusiveTime, double inclusiveTime) {
		super();
		this.signature = new SimpleStringProperty(signature);
		this.joules = new SimpleDoubleProperty(joules);
		this.seconds = new SimpleDoubleProperty(seconds);
		this.inclusiveTime = new SimpleDoubleProperty(inclusiveTime);
		this.exclusiveTime = new SimpleDoubleProperty(exclusiveTime);
		this.noOfCalls = new SimpleIntegerProperty(noOfCalls);
	}

	public StringProperty getSignature() {
		return signature;
	}

	public DoubleProperty getJoules() {
		return joules;
	}

	public DoubleProperty getSeconds() {
		return seconds;
	}

	public DoubleProperty getInclusiveTime() {
		return inclusiveTime;
	}

	public DoubleProperty getExclusiveTime() {
		return exclusiveTime;
	}

	public IntegerProperty getNoOfCalls() {
		return noOfCalls;
	}

}
