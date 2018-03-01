package it.pak.tech.com.core;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ShowStats {
	
	private final StringProperty appVerison;
	private final StringProperty testcase;
	private final IntegerProperty noOfMethods;
	private final DoubleProperty totalEnergy;
	private final IntegerProperty maxRun;
	
	public ShowStats(String appVerison, String testcase, int noOfMethods,
			double totalEnergy, int maxRun) {
		super();
		this.appVerison = new SimpleStringProperty(appVerison);
		this.testcase = new SimpleStringProperty(testcase);
		this.noOfMethods = new SimpleIntegerProperty(noOfMethods);
		this.totalEnergy = new SimpleDoubleProperty(totalEnergy);
		this.maxRun = new SimpleIntegerProperty(maxRun);
	}




	public String getAppVerison() {
		return appVerison.get();
	}
	public void setAppVerison(String appVerison) {
		this.appVerison.set(appVerison);
	}
	
	public StringProperty appVersionProperty() {
		return appVerison;
	}
	
	public String getTestcase() {
		return testcase.get();
	}
	public void setTestcase(String testcase) {
		this.testcase.set(testcase);
	}
	
	public StringProperty testcaseProperty() {
		return testcase;
	}
	
	public int getNoOfMethods() {
		return noOfMethods.get();
	}
	public void setNoOfMethods(int noOfMethods) {
		this.noOfMethods.set(noOfMethods);
	}
	public IntegerProperty noOfMethodsProperty() {
		return noOfMethods;
	}
	
	public double getTotalEnergy() {
		return totalEnergy.get();
	}
	public void setTotalEnergy(double totalEnergy) {
		this.totalEnergy.set(totalEnergy);
	}
	public DoubleProperty totalEnergyProperty() {
		return totalEnergy;
	}
	public int getMaxRun() {
		return maxRun.get();
	}
	public void setMaxRun(int maxRun) {
		this.maxRun.set(maxRun);
	}
	public IntegerProperty maxRunProperty() {
		return maxRun;
	}
}
