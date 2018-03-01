package it.pak.tech.com.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import it.pak.tech.com.core.AppsBuilder;
import it.pak.tech.com.core.Downloads;
import it.pak.tech.com.core.exceptions.ADBNotFoundException;
import it.pak.tech.com.core.exceptions.NoDeviceFoundException;
import it.pak.tech.com.core.traceview.TraceLine;
import it.pak.tech.com.core.utils.ConfigManager;
import it.pak.tech.com.core.utils.EnergyProfilerContract;
import it.pak.tech.com.core.utils.Utility;
import it.pak.tech.com.core.Process;
import it.pak.tech.com.core.ShowStats;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

public class EPExperiment {
	
	@FXML
	TextField username;
	
	@FXML
	TextField repo;
	
	@FXML
	Button start;
	
	@FXML
	ProgressBar bar;
	
	@FXML
	Label done;
	
	@FXML
	Button browseRelease;
	
	@FXML
	Button browseTestcase;
	
	@FXML
	public TextField textFieldId;
	
	@FXML
	Button browsesdk;
	
	@FXML
	Button measureEnergy;
	
	@FXML
	Button showStatsbtn ;
	
	@FXML
	Label label ;
	
	@FXML
	TextField selectRelease;
	
	@FXML
	TextField selectTestcase;
	
	@FXML
	TextField selectSdk;
	
	
	@FXML
	Slider horizontalSlider ;
	
	public static boolean isConfigScene ;
	private boolean isStatsScene = false  ;
	String appVersionPath;
	String testCasePath ;
	private ConfigManager configManager ;
	
	ShowStatsController showStatsController ;
	
	public static ObservableList<ShowStats> statsData = FXCollections.observableArrayList();
	
	@FXML
	private void initialize() {
		
		//showStatsController = new ShowStatsController();
		if (isConfigScene) {
			
			horizontalSlider.valueProperty().addListener((obs, oldval, newVal) ->
		    horizontalSlider.setValue(Math.round(newVal.doubleValue())));
			label.textProperty().bindBidirectional(horizontalSlider.valueProperty(),new NumberStringConverter());
		}
	}
	private void setInitialProperties() {
		
		// rootDirectory = root path of project in eclipse.......
		String rootDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
		
		configManager = new ConfigManager(rootDirectory + "/EnergyProfiler.property");
		
		try {
			
			//EnergyProfilerContract.maxRun = configManager.getRuns();
			EnergyProfilerContract.tempOutputPath = configManager.getOutputLocation() ;
			 
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void startDownloading() {
		
		setInitialProperties();
		
		//configManager = new ConfigManager("/EP_Project_V2/EnergyProfiler.property");
		Downloads appDownloader = new Downloads(username.getText().toString() , repo.getText().toString());
		appDownloader.setProgressBar(bar , done);
		appDownloader.Download();
	}
	
	@FXML
	private void computeEnergy() {
		
		if (
				selectRelease.getText().toString().length() != 0 && 
				selectTestcase.getText().toString().length() != 0 &&
				selectSdk.getText().toString().length() != 0 &&
				!label.getText().toString().equals("0")
		    ) {
			
//			System.out.println("Selected Release: " + selectRelease.getText().toString());
//			System.out.println("Selected TestCasePath: " + selectTestcase.getText().toString());
//			System.out.println("SDK Location: " + EnergyProfilerContract.sdkpath);
//			System.out.println("MaxRun: " + Integer.parseInt(label.getText().toString()));
			
			appVersionPath = selectRelease.getText().toString() ;
			testCasePath = selectTestcase.getText().toString() ;
			EnergyProfilerContract.maxRun = Integer.parseInt(label.getText().toString());
			textFieldId.setText("Start Building Apk");
			Thread t = new Thread(new Runnable() {
				public void run() {
					System.out.println("run is called");
					startEnergyMeasurement(EnergyProfilerContract.maxRun, appVersionPath, testCasePath);
					// TODO Auto-generated method stub
					
				}
			});

			t.start();
			
		}
		else {
			printError();
		}
	}
	
	private void startEnergyMeasurement(int maxRun , String appVersionPath , String testCaseClassPath) {
		
		setInitialProperties();
		
		String appVer = selectRelease.getText().toString();
		appVer = appVer.substring(appVer.lastIndexOf('/')+1, appVer.lastIndexOf('.'));
		System.out.println("Ver: " + appVer);
		EnergyProfilerContract.appVersion = appVer ;
		
		AppsBuilder appBuilder = new AppsBuilder();
		appBuilder.initialize(selectRelease.getText().toString());
		appBuilder.BuildApkFile();
		System.out.println("APK Build Successfully");
		textFieldId.setText("APK Build Successfully");
		//EnergyProfilerContract.maxRun = maxRun ;
		startProcess(testCaseClassPath);
	}
	
	public void printError(){
		Alert alert = new Alert(AlertType.WARNING);
        alert.initOwner(EPDriver.primaryStage);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText("Please Provide all Path");
        alert.showAndWait();
	}
	
	@FXML
	private void chooseRelease() {
		
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Select Release ");
	    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("ZIP FILES ONLY", "*.zip"));
	    File selectedFile = fileChooser.showOpenDialog(EPDriver.getPrimaryStage());
	    if (selectedFile != null) {
		    selectRelease.setText(selectedFile.getAbsolutePath());
			selectRelease.setEditable(false);
	    }
	}
	
	@FXML
	private void chooseTestcase () {
		
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Select Test Case ");
	    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JAVA FILES ONLY", "*.java"));
	    File selectedFile = fileChooser.showOpenDialog(EPDriver.getPrimaryStage());
	    
	    if (selectedFile != null) {
	    	selectTestcase.setText(selectedFile.getAbsolutePath());
			selectTestcase.setEditable(false);
	    }
		
	}
	
	@FXML
	private void chooseSdkPath() {
		
		DirectoryChooser fileChooser = new DirectoryChooser();
	    fileChooser.setTitle("Select SDK Path");
	    File selectedFile = fileChooser.showDialog(EPDriver.getPrimaryStage());
	    if (selectedFile != null) {
	  	    selectSdk.setText(selectedFile.getAbsolutePath());
			selectSdk.setEditable(false);
			EnergyProfilerContract.sdkpath = selectSdk.getText().toString();
		 }
	}
	
	@FXML
	private void showStatsAction() {
		
		System.out.println("Show Stats");
		if (!isStatsScene) {
			
			// Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
           // System.out.println(DownloadManager.class.getResource("../ui/startDownload.fxml"));
            EPExperiment.isConfigScene = true ;
            loader.setLocation(EPDriver.class.getResource("../ui/showStats.fxml"));
            AnchorPane rootLayout;
			try {
				rootLayout = (AnchorPane) loader.load();
				// Show the scene containing the root layout.
	            Scene scene = new Scene(rootLayout);
	            Stage stage = new Stage();
	            stage.setScene(scene);
	            stage.show();
	            isStatsScene = true ;
	            showStatsController = loader.getController();
	            showStatsController.setTableItems();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			showStatsController.setTableItems();
			System.out.println("Stats Scene Loaded Already");
		}
	}
	
	// Method to start energy measurement process.....
	private void startProcess(String testCasePath) {
		
		String appName = "";
		String testCaseClassName = "" ;
		Process process = new Process();
		process.setTextField(textFieldId);
		File dir = new File("RawOutput");
		dir.mkdir();
		try {

			System.out.println("Source Code: " + EnergyProfilerContract.sourceCodePath);
			appName = Utility.extractAppName(EnergyProfilerContract.sourceCodePath + "/app/build/outputs/apk/app-debug.apk");
			//System.out.println("appName: " + appName);
			
			String testCase = testCasePath.substring(testCasePath.lastIndexOf('/')+1 , testCasePath.length());
			System.out.println("TestCase: " + testCase);
			double totalAppVerConsumption = 0.0 ;
			
			if (!appName.isEmpty()) {
				
				String folderNames = appName.replace('.', '/');
				String childFolders = CreateDirectories(folderNames);
				String command = "cp " + testCasePath + " " + EnergyProfilerContract.sourceCodePath + childFolders;
				Utility.executeCommand(command, null);
				testCaseClassName = testCasePath.substring(testCasePath.lastIndexOf('/')+1, testCasePath.lastIndexOf('.'));
				//System.out.println("TCN: " + testCaseClassName);
				
				List<TraceLine> avgTraceLines=null;
				
				for (int i = 0 ;i < EnergyProfilerContract.maxRun ; i++) {
					
					List<TraceLine> filterTraceLines = process.Run((i+1), appName, testCaseClassName, EnergyProfilerContract.tempOutputPath);
			    	
					if(i==0) {
			    		
			    		avgTraceLines = filterTraceLines;
			    	}
			    	else {
			    		for (TraceLine traceLine: filterTraceLines) {
			    			int index = Process.isTraceLineExist(avgTraceLines, traceLine.getSignature());
			    			if  (index != -1) {
			    				double prev_value = avgTraceLines.get(index).getExclTime();//getExculsiveTime();
			    				double new_value = traceLine.getExclTime() + prev_value;
			    				avgTraceLines.get(index).setExclTime(new_value);
			    				
			    				prev_value = avgTraceLines.get(index).getInclTime();
			    				new_value  = traceLine.getInclTime() + prev_value;
			    				avgTraceLines.get(index).setInclTime(new_value);
			    				
			    				prev_value = avgTraceLines.get(index).getConsumption();
			    				new_value  = traceLine.getConsumption() + prev_value;
			    				avgTraceLines.get(index).setConsumption(new_value);
			    				
			    				prev_value = avgTraceLines.get(index).getTimeLength();
			    				new_value  = traceLine.getTimeLength() + prev_value;
			    				avgTraceLines.get(index).setTimeLength(new_value);
			    				
			    			}else {
			    				System.out.println("Something Wrong! --- Signatures do not matched");
			    				System.exit(0);
			    			}
			    		}
			    		
			    		textFieldId.setText("Run "+(i+1)+" completed!");
			    	}
			    }
				int maxRun = EnergyProfilerContract.maxRun;
			    for (int i=0; i<avgTraceLines.size(); i++) {
	//			    	System.out.println("Iteration :"+i);
			    	double value = avgTraceLines.get(i).getExclTime();
			    	avgTraceLines.get(i).setExclTime(value/maxRun);
			    	
			    	value = avgTraceLines.get(i).getInclTime();
			    	avgTraceLines.get(i).setInclTime(value/maxRun);
			    	
			    	value = avgTraceLines.get(i).getTimeLength();
			    	avgTraceLines.get(i).setTimeLength(value/maxRun);
			    	
			    	value = avgTraceLines.get(i).getConsumption();
			    	avgTraceLines.get(i).setConsumption(value/maxRun);
			    }
			  String outputDataFolderName = EnergyProfilerContract.tempOutputPath + File.separator + EnergyProfilerContract.appVersion;
			  //System.out.println(outputDataFolderName);
			  PrintWriter resultsWriter = new PrintWriter(outputDataFolderName+ "/" + "EnergyProfile.csv", "UTF-8");
			  resultsWriter.println("Signature, joule, seconds, NumOfCalls, Exculsive Time(secs), Inculsive Time(secs)");
		    
			  for (TraceLine traceLine : avgTraceLines) {
				  resultsWriter.println
				  (
		    			traceLine.getSignature() + "," + traceLine.getConsumption() + "," + traceLine.getTimeLength() + "," +
		    			traceLine.getNumOfCalls() + "," + traceLine.getExclTime() + "," + traceLine.getInclTime()
				  );
				  totalAppVerConsumption += traceLine.getConsumption();
			  }
		    
			  resultsWriter.flush();
			  resultsWriter.close();
			  
			  if (showStatsbtn.isDisable()) {
				  showStatsbtn.setDisable(false);
			  }
			  DecimalFormat df = new DecimalFormat("#.#####");
			  //System.out.println("0.912385:"+df.format(0.912385));
			  
			  totalAppVerConsumption = Double.parseDouble(df.format(totalAppVerConsumption/ 1000.0));
			  ShowStats ss = new ShowStats(EnergyProfilerContract.appVersion , testCase , avgTraceLines.size() , totalAppVerConsumption , EnergyProfilerContract.maxRun);
			  statsData.add(ss);
			  
			  //File dir = new File("RawOutput");
			  dir.delete();
			}
		}
		catch(IOException | NoDeviceFoundException e) {
		
			System.out.println(e.getMessage());
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ADBNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String CreateDirectories(String folderNames) {
		
		String childFolders = "/app/src/androidTest/java/" + folderNames;
		System.out.println("Child Direc: " + childFolders);
		File dir = new File(EnergyProfilerContract.sourceCodePath + childFolders);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
		
		return childFolders;
	}
	
}
