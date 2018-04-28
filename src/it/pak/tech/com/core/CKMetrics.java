package it.pak.tech.com.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CKMetrics {
	
	private final StringProperty Classs;
	private final StringProperty CBO;
	private final StringProperty DIT;
	private final StringProperty LCOM;
	private final StringProperty NOC;
	private final StringProperty RFC;
	private final StringProperty WMC;
	
	
	
	public CKMetrics(String classs, String cBO, String dIT, String lCOM,
			String nOC, String rFC, String wMC) {
		super();
		this.Classs = new SimpleStringProperty(classs);
		this.CBO = new SimpleStringProperty(cBO);
		this.DIT = new SimpleStringProperty(dIT);
		this.LCOM = new SimpleStringProperty(lCOM);
		this.NOC = new SimpleStringProperty(nOC);
		this.RFC = new SimpleStringProperty(rFC);
		this.WMC = new SimpleStringProperty(wMC);
		
	}
	public StringProperty getClasss() {
		return Classs;
	}
	public StringProperty getCBO() {
		return CBO;
	}
	public StringProperty getDIT() {
		return DIT;
	}
	public StringProperty getLCOM() {
		return LCOM;
	}
	public StringProperty getNOC() {
		return NOC;
	}
	public StringProperty getRFC() {
		return RFC;
	}
	public StringProperty getWMC() {
		return WMC;
	}
	
	
	
	
}
