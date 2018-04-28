package it.pak.tech.com.core;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MoodMetrics {

	private final StringProperty Project;
	
	private final StringProperty AHF;
	private final StringProperty AIF;
	private final StringProperty CF;
	private final StringProperty MHF;
	private final StringProperty MIF;
	private final StringProperty PF;
	
	public MoodMetrics(String project, String aHF, String aIF, String cF,
			String mHF, String mIF, String pF) {
		super();
		Project = new SimpleStringProperty(project);
		AHF = new SimpleStringProperty(aHF);
		AIF = new SimpleStringProperty(aIF);
		CF = new SimpleStringProperty(cF);
		MHF = new SimpleStringProperty(mHF);
		MIF = new SimpleStringProperty(mIF);
		PF = new SimpleStringProperty(pF);
	}
	public StringProperty getProject() {
		return Project;
	}
	public StringProperty getAHF() {
		return AHF;
	}
	public StringProperty getAIF() {
		return AIF;
	}
	public StringProperty getCF() {
		return CF;
	}
	public StringProperty getMHF() {
		return MHF;
	}
	public StringProperty getMIF() {
		return MIF;
	}
	public StringProperty getPF() {
		return PF;
	}
	
	
		
	
	
}
