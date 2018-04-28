package it.pak.tech.com.core;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MartinMetrics {

	private final StringProperty Package;
	private final DoubleProperty A;
	private final DoubleProperty Ca;
	private final DoubleProperty Ce;
	private final DoubleProperty D;
	private final DoubleProperty I;
	
	
	
	public MartinMetrics(String package1, double a, double ca, double ce,
			double d, double i) {
		super();
		Package = new SimpleStringProperty(package1);
		A = new SimpleDoubleProperty(a);
		Ca = new SimpleDoubleProperty(ca);
		Ce = new SimpleDoubleProperty(ce);
		D = new SimpleDoubleProperty(d);
		I = new SimpleDoubleProperty(i);
	}

	public StringProperty getPackage() {
		return Package;
	}

	public DoubleProperty getA() {
		return A;
	}

	public DoubleProperty getCa() {
		return Ca;
	}

	public DoubleProperty getCe() {
		return Ce;
	}

	public DoubleProperty getD() {
		return D;
	}

	public DoubleProperty getI() {
		return I;
	}
	
	
	
	
}
