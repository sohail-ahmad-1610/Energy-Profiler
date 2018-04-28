package it.pak.tech.com.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;

import it.pak.tech.com.core.AppsBuilder;
import it.pak.tech.com.core.Downloads;
import it.pak.tech.com.core.exceptions.ADBNotFoundException;
import it.pak.tech.com.core.exceptions.BuildFailedException;
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
	
	// All identifier used in Main Config Screen.....
	@FXML
	Button browseReleasebtn;
	
	@FXML
	private Button measureEnergybtn;
	
	@FXML
	private Button showStatsbtn;
	
	@FXML
	private TextField selectReleasetxt;
	
	@FXML
	private Slider interactionsSlider ;
	
	@FXML
	private Slider maxrunSlider;
	
	@FXML
	private Slider timeBetweenInteractionsSlider;
	
	@FXML
	private TextArea outputConsole;

	// Local variables declarations..............
	public static boolean isConfigScene ;
	private boolean isStatsScene = false  ;
	private String appVersionPath;
	private ConfigManager configManager ;
	private boolean isEnergyProcessStart = false ;
	
	ShowStatsController showStatsController ;
	
	public static ObservableList<ShowStats> statsData = FXCollections.observableArrayList();
	
	@FXML
	private void initialize() 
	{
		if (isConfigScene) {
		}
	}
	
	@FXML
	private void startDownloading() {
		
		
		setInitialProperties();   // Initial user given configurations in config.properties file.......
		
		Downloads appDownloader = new Downloads(username.getText().toString() , repo.getText().toString());
		appDownloader.setProgressBar(bar , done);
		appDownloader.Download();
	}
	
	@FXML
	private void skipDownloading() {
		
		setInitialProperties();
		
		FXMLLoader loader = new FXMLLoader();
        // System.out.println(DownloadManager.class.getResource("../ui/startDownload.fxml"));
        EPExperiment.isConfigScene = true ;
        loader.setLocation(EPDriver.class.getResource("../ui/mainConfigScene.fxml"));
        AnchorPane rootLayout;
         
        try {
        	rootLayout = (AnchorPane) loader.load();
        	// Show the scene containing the root layout.
        	Scene scene = new Scene(rootLayout);
	        EPDriver.primaryStage.setScene(scene);
	        EPDriver.primaryStage.show();
	        EPDriver.primaryStage.setOnCloseRequest(event -> {
    		    System.out.println("Stage is closing");
    		    System.exit(0);
    		});
	        isConfigScene = true ;
        } 
        catch (IOException e) {
			e.printStackTrace();
        }
	}
	
	@FXML
	public void computeEnergy() {
		
		if (!isEnergyProcessStart) {
			
			// shortcut code to test results.... 
			
//			statsData.clear();
//			EnergyProfilerContract.appVersion = "1.0.3";
//			EnergyProfilerContract.appName = "com.enrico.sample" ;
//			if (showStatsbtn.isDisable()) {
//				showStatsbtn.setDisable(false);
//			}
//			ShowStats ss = new ShowStats(EnergyProfilerContract.appVersion  , 0 , 0 ,3);
//			statsData.add(ss);
//			ss = new ShowStats(EnergyProfilerContract.appVersion  , 1 , 0 , EnergyProfilerContract.maxRun);
//			statsData.add(ss);
//			return;
			
			// real code configuration......
			
			// Getting ANDROID_HOME Environment variable........
			EnergyProfilerContract.sdkpath = System.getenv("ANDROID_HOME");
			
			if (EnergyProfilerContract.sdkpath == null || EnergyProfilerContract.sdkpath.equals("")) {
				
				System.out.println("Unable to found Android Sdk");
				return ;
			}
			
			if (isValidInput()) {
				
				isEnergyProcessStart = true ;
				appVersionPath = selectReleasetxt.getText().toString() ;
				EnergyProfilerContract.maxRun = (int) maxrunSlider.getValue();
				
//				System.out.println("App Version Path: " + appVersionPath);
//				System.out.println("Interaction Slider Value: : " + interactionsSlider.getValue());
//				System.out.println("Time Between Interaction: " + timeBetweenInteractionsSlider.getValue());
//				System.out.println("Maximum Run: " + maxrunSlider.getValue());
				
				Thread t = new Thread(new Runnable() {
					public void run() {
						//System.out.println("run is called");
						try {
							startEnergyMeasurement(); // Calling Energy Measurement Procedure.
						} catch (BuildFailedException e) {
							e.printStackTrace();
						}
					}
				});
				t.start();
				
			}
			else {
				printError();
			}
			
		}
		else {
			
			System.out.println("Please wait to finish the commands... try again");
		}
	}
	
	public static void printAlert(String msg) {
		Platform.runLater(new Runnable(){
			@Override public void run() {
			Alert alert = new Alert(AlertType.INFORMATION);
			 alert.setTitle("Information Dialog");
			 alert.setHeaderText(null);
			 alert.setContentText(msg);
			 alert.showAndWait();}
		});
		
	}
	
	@FXML
	public void chooseRelease() {
		
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Select Release ");
	    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("ZIP FILES ONLY", "*.zip"));
	    File selectedFile = fileChooser.showOpenDialog(EPDriver.getPrimaryStage());
	
	    if (selectedFile != null) {
		    selectReleasetxt.setText(selectedFile.getAbsolutePath());
			selectReleasetxt.setEditable(false);
	    }
	}
	
	@FXML
	private void showStatsAction() {
		
//		System.out.println("Show Stats");
		
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
	            stage.setTitle("Application Stats");
	            stage.show();
	            stage.setOnCloseRequest(event -> {
	    		    System.out.println("Stage is closing");
	    		    //setShowStatsBtnProperty();
	    		    outputConsole.setEditable(false);
	    		    isStatsScene = false;
	    		    // Save file
	    		});
	            isStatsScene = true ;
	            showStatsController = loader.getController();
	            showStatsController.setTableItems();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			showStatsController.setTableItems();
			System.out.println("Stats Scene Loaded Already");
		}
	}
	
	/*
	 *  Method to start energy measurement process.....
	 */
	private void startProcess() throws BuildFailedException {
		
		String appName = "";
		Process process = new Process();  // user defined type
		process.setTextField(outputConsole);
		
		File dir = new File("RawOutput");
		dir.mkdir(); // making new directory with name "RawOutput"
		
		try {
			
			//EnergyProfilerContract.appName = Utility.extractAppName(EnergyProfilerContract.sourceCodePath + "/app/build/outputs/apk/app-debug.apk");
			outputConsole.appendText("Installing App...\n");
			// Install and set unable to charge the device.....
			if (!installApp()) {
				//FileUtils.forceDelete(new File(EnergyProfilerContract.tempOutputPath + File.separator + EnergyProfilerContract.appVersion));
				isEnergyProcessStart = false ;
				return ;
			}
			
			outputConsole.appendText("App is successfully installed  on the emulator/external device\n");
			int interactions = (int) interactionsSlider.getValue();
			int timeBetweenInteractions = (int) timeBetweenInteractionsSlider.getValue();
			
			EnergyProfilerContract.timeCapturing = interactions * timeBetweenInteractions / 1000;
			//System.out.println("Initial Time Capturing: " + EnergyProfilerContract.timeCapturing);
			double totalAppVerConsumption = 0.0 ;
			appName = EnergyProfilerContract.appName;
			
			if (!appName.isEmpty()) {
				
				for (int i = 0 ;i < EnergyProfilerContract.maxRun ; i++) {
					
					List<TraceLine> filterTraceLines = process.Run((i+1), appName, interactions, timeBetweenInteractions, EnergyProfilerContract.tempOutputPath);
			    	
					if (filterTraceLines != null) {
						
						String outputDataFolderName = EnergyProfilerContract.tempOutputPath +"/Results/"+
							      EnergyProfilerContract.appName+"/"+EnergyProfilerContract.appVersion + "/" + "run_" + (i+1);
						//System.out.println(outputDataFolderName);
						File outputPath = new File(outputDataFolderName);
						outputPath.mkdirs();
						PrintWriter resultsWriter = new PrintWriter(outputDataFolderName + File.separator + "EnergyProfile.csv", "UTF-8");
						resultsWriter.println("Signature, joule, seconds, NumOfCalls, Exculsive Time(secs), Inculsive Time(secs)");
					    
						for (TraceLine traceLine : filterTraceLines) {
							resultsWriter.println  	// writing to CSV file
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
						
						DecimalFormat df = new DecimalFormat("#.####");
						//System.out.println("TAVC1: "+totalAppVerConsumption); 
						totalAppVerConsumption = Double.parseDouble(df.format(totalAppVerConsumption/1000.0));
						//System.out.println("TAVC2: "+totalAppVerConsumption);
						ShowStats ss = new ShowStats(EnergyProfilerContract.appVersion  , filterTraceLines.size() , totalAppVerConsumption , EnergyProfilerContract.maxRun);
						statsData.add(ss);
						
						outputConsole.appendText("STATS ARE READY NOW.\n");
						
					}  // EOF If null block....
					else {
						
						System.out.println("Failed to calculate results at run " + (i+1));
						printAlert("Failed to calculate results at run " + (i+1) + ".\n Press ok to continue");
						isEnergyProcessStart = false ;
						break ;
					}
			    }
				
				outputConsole.appendText("Uninstalling App.\n");
				uninstallApp(appName);
				FileUtils.forceDelete(new File(EnergyProfilerContract.tempOutputPath + File.separator + EnergyProfilerContract.appVersion));
				isEnergyProcessStart = false;
			}
		}
		catch(IOException | NoDeviceFoundException e) {
			System.out.println(e.getMessage());
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ADBNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	// Helping methods...........
	private boolean isValidInput() {
		
		if (
				selectReleasetxt.getText().length() != 0 &&
				interactionsSlider.getValue() != 0 &&
				timeBetweenInteractionsSlider.getValue() != 0 &&
				maxrunSlider.getValue() != 0
			) {
			return true ;
		}
		
		return false ;
	}
	
	private void startEnergyMeasurement() throws BuildFailedException {
		
		//setInitialProperties();
		String appVer = appVersionPath ;
		
		// Extract app version from the selected path..... ( such as 1.0.1)
		appVer = appVer.substring(appVer.lastIndexOf('/')+1, appVer.lastIndexOf('.'));
		EnergyProfilerContract.appVersion = appVer ;
		
		AppsBuilder appBuilder = new AppsBuilder();
		appBuilder.initialize(selectReleasetxt.getText().toString());
		appBuilder.BuildApkFile();
		outputConsole.setEditable(false);
		System.out.println("APK build successfully");
		outputConsole.setText("Building APK file\n");
		//EnergyProfilerContract.maxRun = maxRun ;
		startProcess();
	}
	
	
	//printError: Prints error on the GUI
	public void printError(){
		Alert alert = new Alert(AlertType.WARNING);
        alert.initOwner(EPDriver.primaryStage);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText("Please Provide all Path");
        alert.showAndWait();
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
	
	private boolean installApp() throws NoDeviceFoundException, BuildFailedException
	{
		// Setting Debugable option
		String platformToolsFolder = EnergyProfilerContract.sdkpath + File.separator + "platform-tools";
		
		String outputString = 
				Utility.executeCommand(platformToolsFolder + "/adb shell dumpsys battery set ac 0", null);
		outputConsole.appendText(outputString);
		
		outputString = 
				Utility.executeCommand(platformToolsFolder + "/adb shell dumpsys battery set usb 0", null);
		outputConsole.appendText(outputString);
		
		System.out.println("Installing app");	
		
		String output = Utility.execCommand(EnergyProfilerContract.sourceCodePath, "./gradlew installDebug");
		outputConsole.appendText(output);
		//System.out.println(output);
		if (output.isEmpty() || output.contains("BUILD FAILED")) {
			printAlert("Error in Launcher Activity\nFatal Exception in Launcher Activity");
			return false ;
		}
		System.out.println("APK install successfully!!!");
		return true ;
	}
	
	private void uninstallApp(String appName) throws NoDeviceFoundException
	{
	    
	    System.out.println("Uninstalling app.");
	    String platformToolsFolder = EnergyProfilerContract.sdkpath + File.separator + "platform-tools";
	    Utility.executeCommand(platformToolsFolder + "/adb shell pm uninstall " + appName, null);
	}
	
	public void setShowStatsBtnProperty() {
		showStatsbtn.setDisable(true);
	}
}
