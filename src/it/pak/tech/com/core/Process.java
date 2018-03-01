package it.pak.tech.com.core;

import it.pak.tech.com.controllers.EPExperiment;
import it.pak.tech.com.core.batterystats.BatteryStatsParser;
import it.pak.tech.com.core.batterystats.EnergyInfo;
import it.pak.tech.com.core.exceptions.ADBNotFoundException;
import it.pak.tech.com.core.exceptions.NoDeviceFoundException;
import it.pak.tech.com.core.powerprofile.PowerProfile;
import it.pak.tech.com.core.systrace.CpuFrequency;
import it.pak.tech.com.core.systrace.SysTrace;
import it.pak.tech.com.core.systrace.SysTraceParser;
import it.pak.tech.com.core.traceview.TraceLine;
import it.pak.tech.com.core.traceview.TraceViewParser;
import it.pak.tech.com.core.traceview.TraceviewStructure;
import it.pak.tech.com.core.utils.EnergyProfilerContract;
import it.pak.tech.com.core.utils.Utility;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Process
{
	@FXML
	TextField textFieldId ;
	
  public Process() {}
  
  public void installApp(String platformToolsFolder) throws NoDeviceFoundException
  {
    
	  Utility.executeCommand(platformToolsFolder + "/adb shell dumpsys battery set ac 0", null);
	  Utility.executeCommand(platformToolsFolder + "/adb shell dumpsys battery set usb 0", null);
		
	  System.out.println("Installing app.");	
	  Utility.execCommand(EnergyProfilerContract.sourceCodePath, "./gradlew installDebug");
	  System.out.println("Apk Install Successfully!!!");
  }
  
  public void uninstallApp(String appName) throws NoDeviceFoundException, ADBNotFoundException
  {
    checkADBExists();
    
    System.out.println("Uninstalling app.");
    Utility.executeCommand("adb shell pm uninstall " + appName, null);
  }
  
  public void setTextField(TextField textField) {
	  this.textFieldId = textField ;
  }
  

  public List<TraceLine> Run(int run, String appName, String testCaseClassName, String outputLocation)
    throws InterruptedException, java.io.IOException, NoDeviceFoundException, ADBNotFoundException
  {
    
	  // Getting ANDROID_HOME Environment variable........
	  
	  String sdkFolderPath = EnergyProfilerContract.sdkpath;
	  
	  checkADBExists();
	  
	  int timeCapturing = 0 ;
	  String platformToolsFolder = sdkFolderPath + File.separator + "platform-tools";
    
	  String outputDataFolderName = outputLocation + File.separator + EnergyProfilerContract.appVersion + "/run_" + run + File.separator;
	  String runDataFolderName = "RawOutput" + "/run_" + run + File.separator;
    
	  File runDataFolder = new File(outputDataFolderName);
    
	  runDataFolder.mkdirs();
	  runDataFolder = new File(runDataFolderName);
	  runDataFolder.mkdirs();
    
	  String batteryStatsFilename = runDataFolderName + "batterystats";
	  String systraceFilename = runDataFolderName + "systrace";
	  String traceviewFilename = runDataFolderName + "tracedump";
	  String methodDetailsFileName = runDataFolderName + "TraceLogFileMethodDetail.txt";
	  String powerProfileFile = "RawOutput" + File.separator + "power_profile.xml" ;
	  
	  // Reset the target application stats......
	  resetApp(appName, run , platformToolsFolder);
	  
	  // Install and set unable to charge the device.....
	  installApp(platformToolsFolder);
	  textFieldId.setText("");
	  textFieldId.setText("Application Installed successfully!");
	  Thread.sleep(3000L);
	  // Start Systracer tool on separate thread....
	  SysTraceRunner sysTraceRunner = startProfiling(run, timeCapturing, systraceFilename, platformToolsFolder);
	  Thread systraceThread = new Thread(sysTraceRunner);
	  systraceThread.start();
	  textFieldId.setText("");
	  textFieldId.setText("Executing User Scenarios...");
	   
	  // Executes Test Case.....
	  executeTestCase(appName , testCaseClassName);
	  textFieldId.setText("");
	  textFieldId.setText("User scenarios executed successfully!");
	  Thread.sleep(3000L);
	  textFieldId.setText("");
	  textFieldId.setText("Extracting device information...");
	  // Extracts BatteryStats and dump trace file info........
	  extractInfo(appName, run, batteryStatsFilename, runDataFolderName, platformToolsFolder, traceviewFilename);
	  
		 
	  // wait while all information is extracted........
	  Robot r;
	try {
		r = new Robot();
		r.keyPress(KeyEvent.VK_ENTER);
		 r.keyRelease(KeyEvent.VK_ENTER);
	} catch (AWTException e) {
		
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  //sysTraceRunner.stopProcess();
	  systraceThread.join();
      Thread.sleep(3000L);
	  System.out.println("Run " + run + ": aggregating results.");
	  textFieldId.setText("");
	  textFieldId.setText("Aggregating results...");
	  // Extract Power Profile first here and then parse.................
    
	  extractPowerProfile("RawOutput" , platformToolsFolder);
    
	  System.out.println("Run " + run + ": parsing power profile.");
	  PowerProfile powerProfile = it.pak.tech.com.core.powerprofile.PowerProfileParser.parseFile(powerProfileFile);
    
	  List<TraceLine> traceLinesWiConsumptions = parseAndAggregateResults(traceviewFilename, batteryStatsFilename, systraceFilename, methodDetailsFileName, powerProfile, appName, run);
    
	  PrintWriter resultsWriter = new PrintWriter(outputDataFolderName+ "/" + "EnergyProfile_" + run + ".csv", "UTF-8");
	  resultsWriter.println("Signature, joule, seconds, NumOfCalls, Exculsive Time(Usecs), Inculsive Time(Usecs)");
    
	  for (TraceLine traceLine : traceLinesWiConsumptions) {
		  resultsWriter.println
		  (
    			traceLine.getSignature() + "," + traceLine.getConsumption() + "," + traceLine.getTimeLength() + "," +
    			traceLine.getNumOfCalls() + "," + traceLine.getExclTime() + "," + traceLine.getInclTime()
		  );
	  }
    
	  resultsWriter.flush();
    
	  Utility.executeCommand("adb shell dumpsys battery reset", null);
    
	  System.out.println("Run " + run + ": complete.");
	  return traceLinesWiConsumptions;
  }
  
  public void executeTestCase(String appName , String testCaseClassName) {
	  
	  String runTestCaseCommand = "./gradlew -Pandroid.testInstrumentationRunnerArguments.class=" + 
	            appName + "." + testCaseClassName  + " connectedDebugAndroidTest";
		//System.out.println("Test Case Running Command: " + runTestCaseCommand);
	  String writePermissionCommand = "./gradlew grantDebugPermissions";
	  
	  Utility.execCommand(EnergyProfilerContract.sourceCodePath, writePermissionCommand);
	  System.out.println("Permission Granted Successfully!!!");
	    // Execute test case here................
		
	  Utility.execCommand(EnergyProfilerContract.sourceCodePath, runTestCaseCommand);
	  System.out.println("Test Case Executed Successfully!!!");
  }
  
  public void extractPowerProfile(String outputLocation , String platformtoolsFolder) throws NoDeviceFoundException {
    
	  System.out.println("Extracting power profile.");
    
	  String jarDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
  
	  Utility.executeCommand(platformtoolsFolder + "/adb pull /system/framework/framework-res.apk", null);
	  Utility.executeCommand("jar xf " + jarDirectory + "/EnergyProfiler.jar apktool.jar", null);
	  Utility.executeCommand("java -jar apktool.jar if framework-res.apk", null);
	  Utility.executeCommand("java -jar apktool.jar d framework-res.apk", null);
	  Utility.executeCommand("mv " + jarDirectory + "/framework-res/res/xml/power_profile.xml " + outputLocation, null);
	  Utility.executeCommand("rm -rf " + jarDirectory + "/apktool.jar", null);
	  Utility.executeCommand("rm -rf " + jarDirectory + "/framework-res.apk", null);
	  Utility.executeCommand("rm -rf " + jarDirectory + "/framework-res", null);
  }
  
  private void resetApp(String appName, int run , String platformTools) throws NoDeviceFoundException {
    
	  System.out.println("Run " + run + ": resetting app and batterystats.");
	  Utility.executeCommand(platformTools + "/adb shell pm clear " + appName, null);
	  Utility.executeCommand(platformTools + "/adb shell dumpsys batterystats --reset", null);
  }
  
  private SysTraceRunner startProfiling(int run, int timeCapturing, String systraceFilename, String platformToolsFolder) throws NoDeviceFoundException
  {
//    System.out.println("Run " + run + ": starting profiling.");
//    executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null);
    
    System.out.println("Run " + run + ": capturing system traces.");
    return new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
  }
  
  private void extractInfo(String appName, int run, String batteryStatsFilename, String runDataFolderName, String platformToolsFolder, String traceviewFilename) throws NoDeviceFoundException {
    
//	  System.out.println("Run " + run + ": stop profiling.");
//	  Utility.executeCommand("adb shell am profile stop " + appName, null);
	  appName = appName.substring(0 , appName.lastIndexOf("."));
	  System.out.println("Run " + run + ": saving battery stats.");
	  Utility.executeCommand(platformToolsFolder + "/adb shell dumpsys batterystats", new File(batteryStatsFilename));
	    
	  System.out.println("Run " + run + ": saving traceviews.");
	  Utility.executeCommand(platformToolsFolder + "/adb pull /sdcard/TraceLogFile.trace " + runDataFolderName , null);
	  Utility.executeCommand(platformToolsFolder + "/dmtracedump -o " + runDataFolderName + "/TraceLogFile.trace", new File(traceviewFilename));
	  Utility.executeCommand( platformToolsFolder + "/dmtracedump -h " + runDataFolderName + "/TraceLogFile.trace ",new File(traceviewFilename+"HTML.html"));
	  Utility.executeCommand("python HtmlParser.py " + (traceviewFilename+"HTML.html ")  + appName, new File(runDataFolderName + File.separator + "TraceLogFileMethodDetail.txt"));
  }
  
  List<TraceLine> parseAndAggregateResults(String traceviewFilename, String batteryStatsFilename, String systraceFilename, String methodDetailsFileName , PowerProfile powerProfile, String filter, int run)
    throws java.io.IOException, InterruptedException
  {
    List<TraceLine> traceLinesWConsumption = new ArrayList();
    Thread.sleep(3000L);
    System.out.println("Run " + run + ": elaborating traceview info.");
    textFieldId.setText("");
    textFieldId.setText("Extracting methods trace info...");
    filter = filter.substring(0 , filter.lastIndexOf("."));
    TraceviewStructure traceviewStructure = TraceViewParser.parseFile(traceviewFilename, filter);
    List<TraceLine> traceLines = traceviewStructure.getTraceLines();
    int traceviewLength = traceviewStructure.getEndTime();
    int traceviewStart = traceviewStructure.getStartTime();
    Thread.sleep(3000L);
    System.out.println("Run " + run + ": elaborating battery stats info.");
    textFieldId.setText("");
    textFieldId.setText("Extracting battery info...");
    List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart);
    Thread.sleep(3000L);
    System.out.println("Run " + run + ": elaborating systrace stats info.");
    textFieldId.setText("");
    textFieldId.setText("Extracting device CPU info...");
    //System.out.println("TVS: " + traceviewStart + " TVL: " + traceviewLength);
    SysTrace cpuInfo = SysTraceParser.parseFile(systraceFilename, traceviewStart, traceviewLength);
    
    System.out.println("Run " + run + ": aggregating results.");
    Thread.sleep(3000L);
    textFieldId.setText("");
    textFieldId.setText("Preparing results...");
    //System.out.println("Number of CPU: " + cpuInfo.getNumberOfCpu());
    energyInfoArray = mergeEnergyInfo(energyInfoArray, cpuInfo, cpuInfo.getNumberOfCpu());
   // System.out.println("After Merging " + energyInfoArray.size());
    char [] data = Utility.ReadingFile(methodDetailsFileName);
    String [] dataLines = String.valueOf(data).split("\n");
    
    for (TraceLine traceLine : traceLines) {
      
    	TraceLine traceLineConsumptionCalculated = calculateConsumption(traceLine, energyInfoArray, powerProfile);
      if (traceLinesWConsumption.isEmpty()) {
    	  
    	  String line = Utility.getSignatureDetails(dataLines, traceLineConsumptionCalculated.getSignature());
		  String [] lineDetails = line.split("\\s");
		  int methodCount = 0;
		  traceLineConsumptionCalculated.setNumOfCalls(++methodCount);
		  traceLineConsumptionCalculated.setExclTime(Double.parseDouble(lineDetails[0]));
		  traceLineConsumptionCalculated.setInclTime(Double.parseDouble(lineDetails[1]));
    	  traceLinesWConsumption.add(traceLineConsumptionCalculated);  
      }
      else {
    	  
    	  int index = isTraceLineExist(traceLinesWConsumption, traceLineConsumptionCalculated.getSignature());
    	  
    	  if (index != -1) {
    		  
    		  TraceLine tempTraceLine = traceLinesWConsumption.get(index);
    		  double consumption = tempTraceLine.getConsumption();
    		  double timeLength = tempTraceLine.getTimeLength();
    		  int methodCount = tempTraceLine.getNumOfCalls();
    		  ++methodCount;
    		  consumption += traceLineConsumptionCalculated.getConsumption();
    		  timeLength += traceLineConsumptionCalculated.getTimeLength();
    		  traceLinesWConsumption.get(index).setNumOfCalls(methodCount);
    		  traceLinesWConsumption.get(index).setConsumption(consumption);
    		  traceLinesWConsumption.get(index).setTimeLength(timeLength);
    	  }
    	  
    	  else {
    		  
    		  String line = Utility.getSignatureDetails(dataLines, traceLineConsumptionCalculated.getSignature());
    		  String [] lineDetails = line.split("\\s");
    		  int methodCount = 0;
    		  traceLineConsumptionCalculated.setNumOfCalls(++methodCount);
    		  traceLineConsumptionCalculated.setExclTime(Double.parseDouble(lineDetails[0]));
    		  traceLineConsumptionCalculated.setInclTime(Double.parseDouble(lineDetails[1]));
    		  traceLinesWConsumption.add(traceLineConsumptionCalculated);
    	  }
    	  
      	}
    }
    
    return traceLinesWConsumption;
  }
  
  public static int isTraceLineExist(List<TraceLine> traceLines , String signature) {
	  
	  int index = -1 ;
	  
	  for (int i = 0 ; i < traceLines.size() ; i++) {
		  
		  if (traceLines.get(i).getSignature().equals(signature)) {
			  
			  index = i ;
			  break ;
		  }
	  }
	  
	  return index ;
  }
  
  private List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo, int numOfCore)
  {
    List<Integer> cpuFrequencies = new ArrayList();
    
    List<EnergyInfo> finalEnergyInfoArray = new ArrayList();
    
    for (int i = 0; i < numOfCore; i++) {
      cpuFrequencies.add(Integer.valueOf(0));
    }
    
    EnergyInfo energyInfo;
    int fixedEnergyInfoTime;
    for (Iterator<EnergyInfo> i = energyInfoArray.iterator(); i.hasNext();) { 
    	energyInfo = (EnergyInfo)i.next();
    //	System.out.println("SST: " + cpuInfo.getSystraceStartTime());
    //	System.out.println("EIE: " + energyInfo.getEntrance());
    	fixedEnergyInfoTime = cpuInfo.getSystraceStartTime() + energyInfo.getEntrance();
    	
    	for (CpuFrequency frequency : cpuInfo.getFrequencies()) {
    	//	System.out.println("FT: " + frequency.getTime() + " fixedEnergyInfoTime " + fixedEnergyInfoTime);
    		if (frequency.getTime() >= fixedEnergyInfoTime) 
    			break;
    		
    		EnergyInfo finalEnergyInfo = new EnergyInfo(energyInfo);
    		//System.out.println("Freq Value: " + frequency.getValue());
    		cpuFrequencies.set(frequency.getCore(), Integer.valueOf(frequency.getValue()));
        
    		int finalEnergyInfoTime = frequency.getTime() - cpuInfo.getSystraceStartTime();
    		finalEnergyInfo.setEntrance(finalEnergyInfoTime);
    		finalEnergyInfo.setCpuFrequencies(cpuFrequencies);
    		finalEnergyInfoArray.add(finalEnergyInfo);
      }
    }
    return finalEnergyInfoArray;
  }
 
 private TraceLine calculateConsumption(TraceLine traceLine, List<EnergyInfo> energyInfoArray, PowerProfile powerProfile)
  {
    double joule = 0.0D;
    double totalSeconds = 0.0D;
  //  System.out.println("EIAS: " + energyInfoArray.size());
  //  System.out.println("CPU Freq Size: " + energyInfoArray.get(0).getCpuFrequencies().size());
    int numberOfCores = ((EnergyInfo)energyInfoArray.get(0)).getCpuFrequencies().size();
    
    boolean[] previouslyIdle = new boolean[numberOfCores];
    
    for (EnergyInfo energyInfo : energyInfoArray)
    {
      if (traceLine.getEntrance() >= energyInfo.getEntrance())
      {
        double ampere = 0.0D;
        
        List<Integer> cpuFrequencies = energyInfo.getCpuFrequencies();
        
        for (int i = 0; i < numberOfCores; i++) {
          int coreFrequency = ((Integer)cpuFrequencies.get(i)).intValue();
          int coreCluster = powerProfile.getClusterByCore(i);
          //System.out.println("CoreFreq: " + coreFrequency);
          //System.out.println("CoreCluster: " + coreCluster );
          ampere += (powerProfile.getCpuConsumptionByFrequency(coreCluster, coreFrequency)) / 1000.0D;
          if (coreFrequency != 0) {
            if (previouslyIdle[i] != false) {
            	if (powerProfile.getDevices().get("cpu.awake") != null) {
            		
            		ampere += ((Double)powerProfile.getDevices().get("cpu.awake")).doubleValue() / 1000.0D;
            	}
              
            }
          } else {
            previouslyIdle[i] = true;
          }
        }
        
        for (String deviceString : energyInfo.getDevices()) {
          if (deviceString.contains("wifi")) {
            ampere += ((Double)powerProfile.getDevices().get("wifi.on")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("wifi.scanning")) {
            ampere += ((Double)powerProfile.getDevices().get("wifi.scan")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("wifi.running")) {
            ampere += ((Double)powerProfile.getDevices().get("wifi.active")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("phone.scanning")) {
            ampere += ((Double)powerProfile.getDevices().get("radio.scanning")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("phone.running")) {
            ampere += ((Double)powerProfile.getDevices().get("radio.active")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("bluetooth")) {
            ampere += ((Double)powerProfile.getDevices().get("bluetooth.on")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("bluetooth.running")) {
            ampere += ((Double)powerProfile.getDevices().get("bluetooth.active")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("screen")) {
            ampere += ((Double)powerProfile.getDevices().get("screen.on")).doubleValue() / 1000.0D;
          } else if (deviceString.contains("gps")) {
            ampere += ((Double)powerProfile.getDevices().get("gps.on")).doubleValue() / 1000.0D;
          }
        }
        
        int phoneSignalStrength = energyInfo.getPhoneSignalStrength();
        
        if (powerProfile.getRadioInfo().size() == phoneSignalStrength - 1) {
          ampere += ((Double)powerProfile.getRadioInfo().get(phoneSignalStrength - 1)).doubleValue() / 1000.0D;
        } else {
          ampere += ((Double)powerProfile.getRadioInfo().get(powerProfile.getRadioInfo().size() - 1)).doubleValue() / 1000.0D;
        }
        
        //System.out.println("Total Ampere: " + ampere + "Voltage: " + energyInfo.getVoltage() );
        double watt = ((ampere * energyInfo.getVoltage()) / 1000.0D);
        double nanoseconds = 0;
        if (traceLine.getExit() < energyInfo.getExit()) {
          nanoseconds = energyInfo.getEntrance() - traceLine.getExit();
         // System.out.println("NS1: " + nanoseconds);
        }
        double seconds = nanoseconds / 1.0E9D;
       // System.out.println("Seconds: " + nanoseconds);
        totalSeconds += seconds;
        joule += watt * seconds;
      }
    }
    
    traceLine.setTimeLength(totalSeconds);
    traceLine.setConsumption(joule);
    
    return traceLine;
  }
  
  private void checkADBExists() throws ADBNotFoundException {
    String sdkFolderPath = EnergyProfilerContract.sdkpath;
    String adbPath = sdkFolderPath + "/platform-tools/adb";
    File adbFile = new File(adbPath);
    if (!adbFile.exists()) {
      throw new ADBNotFoundException();
    }
  }
}
