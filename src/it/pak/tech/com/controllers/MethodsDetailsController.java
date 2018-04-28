package it.pak.tech.com.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import it.pak.tech.com.core.CKMetrics;
import it.pak.tech.com.core.MartinMetrics;
import it.pak.tech.com.core.MethodsDetails;
import it.pak.tech.com.core.MoodMetrics;
import it.pak.tech.com.core.ShowStats;
import it.pak.tech.com.core.utils.EnergyProfilerContract;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import javafx.application.Platform;
//import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
//import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
//import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MethodsDetailsController {

	@FXML
	private TableView<MethodsDetails> statsTable;
	@FXML
	private TableColumn<MethodsDetails, String> signatureCol;
	@FXML
	private TableColumn<MethodsDetails, Double> joulesCol;
	@FXML
	private TableColumn<MethodsDetails, Double> secondsCol;
	@FXML
	private TableColumn<MethodsDetails, Integer> noOfCallsCol;
	@FXML
	private TableColumn<MethodsDetails, Double> inclusiveTimeCol;
	@FXML
	private TableColumn<MethodsDetails, Double> exclusiveTimeCol;
	@FXML
	private ComboBox runComboBox;
	@FXML
	private ComboBox metricsProfileComboBox ;
	
	private ObservableList<MethodsDetails> methodsData = FXCollections.observableArrayList();
	
	
	@FXML
	private TableView<CKMetrics> ckTable;
	@FXML
	private TableColumn<CKMetrics, String> classCol;
	@FXML
	private TableColumn<CKMetrics, String> CBOCol;
	@FXML
	private TableColumn<CKMetrics, String> DITCol;
	@FXML
	private TableColumn<CKMetrics, String> LCOMCol;
	@FXML
	private TableColumn<CKMetrics, String> NOCCol;
	@FXML
	private TableColumn<CKMetrics, String> RFCCol;
	@FXML
	private TableColumn<CKMetrics, String> WMCCol;
	
	private ObservableList<CKMetrics> ckData = FXCollections.observableArrayList();
	
	@FXML
	private TableView<MartinMetrics> martinTable;
	@FXML
	private TableColumn<MartinMetrics, String> packageCol;
	@FXML
	private TableColumn<MartinMetrics, Double> ACol;
	@FXML
	private TableColumn<MartinMetrics, Double> CaCol;
	@FXML
	private TableColumn<MartinMetrics, Double> CeCol;
	@FXML
	private TableColumn<MartinMetrics, Double> DCol;
	@FXML
	private TableColumn<MartinMetrics, Double> ICol;
	
	private ObservableList<MartinMetrics> martinData = FXCollections.observableArrayList();
	
	@FXML
	private TableView<MoodMetrics> moodTable;
	@FXML
	private TableColumn<MoodMetrics, String> ProjectCol;
	@FXML
	private TableColumn<MoodMetrics, String> AHFCol;
	@FXML
	private TableColumn<MoodMetrics, String> AIFCol;
	@FXML
	private TableColumn<MoodMetrics, String> CFCol;
	@FXML
	private TableColumn<MoodMetrics, String> MHFCol;
	@FXML
	private TableColumn<MoodMetrics, String> MIFCol;
	@FXML
	private TableColumn<MoodMetrics, String> PFCol;
	
	private ObservableList<MoodMetrics> moodData = FXCollections.observableArrayList();
	
	private String appVersion;
	

	@FXML
	private void initialize() {
		
		statsTable.setEditable(false);
		signatureCol.setCellValueFactory(cellData -> cellData.getValue().getSignature());
		joulesCol.setCellValueFactory(cellData -> cellData.getValue().getJoules().asObject());
		secondsCol.setCellValueFactory(cellData -> cellData.getValue().getSeconds().asObject());
		noOfCallsCol.setCellValueFactory(cellData -> cellData.getValue().getNoOfCalls().asObject());
		inclusiveTimeCol.setCellValueFactory(cellData -> cellData.getValue().getInclusiveTime().asObject());
		exclusiveTimeCol.setCellValueFactory(cellData -> cellData.getValue().getExclusiveTime().asObject());
		
		ckTable.setEditable(false);
		classCol.setCellValueFactory(cellData -> cellData.getValue().getClasss());
		CBOCol.setCellValueFactory(cellData -> cellData.getValue().getCBO());
		DITCol.setCellValueFactory(cellData -> cellData.getValue().getDIT());
		LCOMCol.setCellValueFactory(cellData ->  cellData.getValue().getLCOM());
		NOCCol.setCellValueFactory(cellData -> cellData.getValue().getNOC());
		RFCCol.setCellValueFactory(cellData -> cellData.getValue().getRFC());
		WMCCol.setCellValueFactory(cellData -> cellData.getValue().getWMC());
		
		martinTable.setEditable(false);
		packageCol.setCellValueFactory(cellData -> cellData.getValue().getPackage());
		ACol.setCellValueFactory(cellData -> cellData.getValue().getA().asObject());
		CaCol.setCellValueFactory(cellData -> cellData.getValue().getCa().asObject());
		CeCol.setCellValueFactory(cellData -> cellData.getValue().getCe().asObject());
		DCol.setCellValueFactory(cellData -> cellData.getValue().getD().asObject());
		ICol.setCellValueFactory(cellData -> cellData.getValue().getI().asObject());
		
		moodTable.setEditable(false);
		ProjectCol.setCellValueFactory(cellData->cellData.getValue().getProject());
		AHFCol.setCellValueFactory(cellData -> cellData.getValue().getAHF());
		AIFCol.setCellValueFactory(cellData -> cellData.getValue().getAIF());
		CFCol.setCellValueFactory(cellData -> cellData.getValue().getCF());
		MHFCol.setCellValueFactory(cellData -> cellData.getValue().getMHF());
		MIFCol.setCellValueFactory(cellData -> cellData.getValue().getMIF());
		PFCol.setCellValueFactory(cellData -> cellData.getValue().getPF());
		
		metricsProfileComboBox.getItems().removeAll(metricsProfileComboBox.getItems());
		metricsProfileComboBox.setPromptText("Select Metrics Profile");
		metricsProfileComboBox.getItems().addAll("Chidamber-Kemerer Metrics","MOOD Metrics", "Martin Packaging Metrics" );
		//metricsProfileComboBox.getSelectionModel().select("");
	}
	
	@FXML
	public void showSelectedItem() {
		try {
			readEnergyProfile(appVersion);
			statsTable.setItems(methodsData);
			//statsTable.re
		} catch (Exception e) {
			System.out.println("Error in reading CSV file or methodsData is not properly set into the table\n\n");
			e.printStackTrace();
		}
	}
	@FXML
	public void showStructuralMetrics() {
		try {
			
			String metric = metricsProfileComboBox.getSelectionModel().getSelectedItem().toString();
			if (metric.equals("Select Metrics Profile")) {
				EPExperiment.printAlert("Select Structural Metric from Drop down");
			}
			else if (metric.equals("Chidamber-Kemerer Metrics")) {
				readCkMetrics();
				showckTable();
			}
			else if (metric.equals("Martin Packaging Metrics")) {
				readMartinMetrics();
				showMartinTable();
			}
			else if (metric.equals("MOOD Metrics")) {
				readMoodMatrix();
				martinTable.setVisible(false);
				ckTable.setVisible(false);
				moodTable.setVisible(true);
				moodTable.setItems(moodData);
			}
			
		} catch (Exception e) {
			System.out.println("Error in reading CSV file or methodsData is not properly set into the table\n\n");
			e.printStackTrace();
		}
	}
	
	private void showckTable () {
		
		ObservableList<CKMetrics> tempList = FXCollections.observableArrayList();
		
		for (int i=0; i<ckData.size(); i++) {
			for (int j=0; j<methodsData.size(); j++) {
				
				String str = methodsData.get(j).getSignature().getValue();
				str = str.substring(0,str.lastIndexOf("."));
				System.out.println("Str: " + str );
				String ckStr = ckData.get(i).getClasss().getValue().replace("\"", "");
				if (ckStr.equals(str)) {
					tempList.add(ckData.get(i));
					break;
				}
			}
		
			
		}
		martinTable.setVisible(false);
		moodTable.setVisible(false);
		ckTable.setVisible(true);
		ckTable.setItems(tempList);
	}
	private void showMartinTable() {
		
		ObservableList<MartinMetrics> tempList = FXCollections.observableArrayList();
		
		for (int i=0; i<martinData.size(); i++) {
			for (int j=0; j<methodsData.size(); j++) {
				String str = methodsData.get(j).getSignature().getValue();
				str = str.substring(0, str.lastIndexOf("."));
				str = str.substring(0,str.lastIndexOf("."));
				String strmartin = martinData.get(i).getPackage().getValue().replace("\"", "");
				if (strmartin.equals(str)) {
					tempList.add(martinData.get(i));
					break;
				}
			}
		}
		ckTable.setVisible(false);
		moodTable.setVisible(false);
		martinTable.setVisible(true);
		martinTable.setItems(tempList);
	}
	private void readMartinMetrics() {
		String splitBy = ",";
	    String line="";
	    martinData.clear();
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(EnergyProfilerContract.tempOutputPath + "/StructuralMetrics/" +EnergyProfilerContract.appName + "/martin-metrics.csv"));
		
	    boolean isFirstLine=true;
	      while((line = br.readLine()) != null){
			  if (isFirstLine) {
				  isFirstLine = false;
				  line = br.readLine();
				  continue;
			  }
		       String[] b = line.split(splitBy);
		       if (b.length <= 5) {
		    	   System.out.println("This row has incomplete martin-metrics data...\n");
		    	   continue;
		       }
		       MartinMetrics obj = new MartinMetrics(b[0], Double.parseDouble(b[1]), Double.parseDouble(b[2]), Double.parseDouble(b[3]), Double.parseDouble(b[4]), Double.parseDouble(b[5]));
		       //System.out.println(b[2]+"  "+b[3]+"\n");
		       martinData.add(obj);
		  }
	      br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void readCkMetrics() {
		
		String splitBy = ",";
	    String line="";
	    ckData.clear();
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(EnergyProfilerContract.tempOutputPath + "/StructuralMetrics/" +EnergyProfilerContract.appName + "/ck-metrics.csv"));
		
	    boolean isFirstLine=true;
	      while((line = br.readLine()) != null){
			  if (isFirstLine) {
				  isFirstLine = false;
				  continue;
			  }
		       String[] b = line.split(splitBy);
		       System.out.println(line);
		       if (b.length <= 6) {
		    	   System.out.println("This row has incomplete ck-metrics data...\n");
		    	   continue;
		       }
		       CKMetrics obj = new CKMetrics(b[0], b[1], b[2], b[3], b[4], b[5], b[6]);
		       //System.out.println(b[2]+"  "+b[3]+"\n");
		       ckData.add(obj);
		  }
	      br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	private void readMoodMatrix() {
		String splitBy = ",";
	    String line="";
	    moodData.clear();
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(EnergyProfilerContract.tempOutputPath + "/StructuralMetrics/" +EnergyProfilerContract.appName + "/mood-metrics.csv"));
		
	    boolean isFirstLine=true;
	      while((line = br.readLine()) != null){
			  if (isFirstLine) {
				  isFirstLine = false;
				  continue;
			  }
		       String[] b = line.split(splitBy);
		       if (b.length <= 6) {
		    	   System.out.println("This row has incomplete ck-metrics data...\n");
		    	   continue;
		       }
		       MoodMetrics obj = new MoodMetrics(b[0], b[1], b[2], b[3], b[4], b[5], b[6]);
		       //System.out.println(b[2]+"  "+b[3]+"\n");
		       moodData.add(obj);
		  }
	      br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	public MethodsDetailsController() {
		
	}
	
	public void setTableItems(int maxRun, String appVersion) {
		System.out.println("Methods Details Controller: setTableItems called\n\n");
		this.appVersion = appVersion;
		//initialize();
		runComboBox.getItems().removeAll(runComboBox.getItems());
		for (int i=1; i<=maxRun; i++) {
			runComboBox.getItems().add(i);
		}
		runComboBox.getSelectionModel().select(0);
	}
	
	private double round(double value) {
	    
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(6, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	public void readEnergyProfile(String appVersion) {
	      String splitBy = ",";
	      String line="";
	      methodsData.clear();
	      BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(EnergyProfilerContract.tempOutputPath+"/Results/"+
			  EnergyProfilerContract.appName+"/"+appVersion+ "/run_" + runComboBox.getSelectionModel().getSelectedItem() + "/EnergyProfile.csv"));
		
	    boolean isFirstLine=true;
	      while((line = br.readLine()) != null){
			  if (isFirstLine) {
				  isFirstLine = false;
				  continue;
			  }
		       String[] b = line.split(splitBy);
		       if (b.length < 6) {
		    	   System.out.println("This row has incomplete Martin Matrix data...\n");
		    	   continue;
		       }
		       MethodsDetails obj = new MethodsDetails(b[0], round(Double.valueOf(b[1]).doubleValue()), round(Double.valueOf(b[2]).doubleValue()),
		    		   Integer.valueOf(b[3]), Double.valueOf(b[4]).doubleValue(), Double.valueOf(b[5]).doubleValue());
		       //System.out.println(b[2]+"  "+b[3]+"\n");
		       methodsData.add(obj);
		  }
	      br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	  }
}
