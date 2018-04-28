package it.pak.tech.com.core;

import it.pak.tech.com.controllers.EPExperiment;
import it.pak.tech.com.core.batterystats.BatteryStatsParser;
import it.pak.tech.com.core.batterystats.EnergyInfo;
import it.pak.tech.com.core.exceptions.ADBNotFoundException;
import it.pak.tech.com.core.exceptions.BuildFailedException;
import it.pak.tech.com.core.exceptions.NoDeviceFoundException;
import it.pak.tech.com.core.powerprofile.PowerProfile;
import it.pak.tech.com.core.systrace.CpuFrequency;
import it.pak.tech.com.core.systrace.SysTrace;
import it.pak.tech.com.core.systrace.SysTraceParser;
import it.pak.tech.com.core.traceview.MedianTraceLine;
import it.pak.tech.com.core.traceview.TraceLine;
import it.pak.tech.com.core.traceview.TraceViewParser;
import it.pak.tech.com.core.traceview.TraceviewStructure;
import it.pak.tech.com.core.utils.EnergyProfilerContract;
import it.pak.tech.com.core.utils.Utility;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class Process
{
	@FXML
	TextArea consoleOutput ;
	
	// Declare the list to add traceLinesWConsumption objects.......
	private List<TraceviewStructure> traceLinesWConsumptionsList ;
	
  public Process() {
	  
	  // initializing the list to add traceLinesWConsumption objects.......
	  traceLinesWConsumptionsList = new ArrayList<>();
  }
  
  private void resetApp(String appName, int run , String platformTools) throws NoDeviceFoundException {
	    
	  System.out.println("Run " + run + ": resetting app and batterystats.");
	  Utility.executeCommand(platformTools + "/adb shell pm clear " + appName, null);
	  Utility.executeCommand(platformTools + "/adb shell dumpsys batterystats --reset", null);
  }
  
  public void setTextField(TextArea consoleOutput) {
	  this.consoleOutput = consoleOutput ;
  }
  
  // Calculates EnergyConsumption on specific:
  // run : 1, 2, 3, 4, 5...
  // appName : Application name under observation
  // interactions : how many times to interact with the application [Monkey tool Args]
  // timeBetweenInteractions : time between interactions [Monkey tool Args] 
  public List<TraceLine> Run(int run, String appName, int interactions, int timeBetweenInteractions, String outputLocation)
    throws InterruptedException, java.io.IOException, NoDeviceFoundException, ADBNotFoundException
  {  
	  String sdkFolderPath = EnergyProfilerContract.sdkpath;
	  
	  checkADBExists(); // check if adb module available ?
	  
	  Random random = new Random();
	  int seed = random.nextInt(Integer.SIZE - 1);
	  consoleOutput.appendText("Run:" + run + " seed:" + seed + "\n");
	  String platformToolsFolder = sdkFolderPath + File.separator + "platform-tools";
	  
	  String runDataFolderName = "RawOutput" + "/run_" + run + File.separator;
    
	  //runDataFolder.mkdirs();
	  File runDataFolder = new File(runDataFolderName);
	  runDataFolder.mkdirs();
    
	  // Getting names for all required files in the energy consumption process
	  String batteryStatsFilename = runDataFolderName + "batterystats";
	  String systraceFilename = runDataFolderName + "systrace";
	  String traceviewFilename = runDataFolderName + "tracedump";
	  String methodDetailsFileName = runDataFolderName + "TraceLogFileMethodDetail.txt";
	  
	  String jarDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
	  String powerProfileFile = jarDirectory + File.separator + "power_profile.xml" ;
	  
	  for (int r = 0 ; r < EnergyProfilerContract.seedRunner ; r++) {
	  	 
		  if (!runDataFolder.exists()) {
			  
			  runDataFolder = new File(runDataFolderName);
			  runDataFolder.mkdirs();
		  }
		
		  // Reset the target application stats......
		  consoleOutput.appendText("Seed Running " + (r + 1) + " RESETTING BETTARY STATS.\n");
		  resetApp(appName, run , platformToolsFolder);
		  
		  Date time1 = new Date();
		  
		  consoleOutput.appendText("Seed Running " + (r + 1) + " OPENING APP.\n");
		  consoleOutput.appendText("Seed Running " + (r + 1) + " STARTS PROFILING.\n");
		  consoleOutput.appendText("Seed Running " + (r + 1) + " CAPTURE SYSTEM TRACES.\n");
		  
		  // Start Systracer tool on separate thread....
		  SysTraceRunner sysTraceRunner = startProfiling(run, EnergyProfilerContract.timeCapturing, systraceFilename, platformToolsFolder);
		  Thread systraceThread = new Thread(sysTraceRunner);
		  systraceThread.start();
		   
		  consoleOutput.appendText("Seed Running " + (r + 1) + " execute random actions.\n");
		
		   
		  if ( 
				// executesActions() this method starts Monkey tool to record and replay test cases on the user device
				// with all parameters provided
				executeActions(appName, seed, interactions, timeBetweenInteractions, platformToolsFolder, runDataFolderName)
			 ) 
		  {  
			  consoleOutput.appendText("Seed Running " + (r + 1) + " STOP PROFILING.\n");
			  Date time2 = new Date();
			  long timespent = time2.getTime() - time1.getTime();
			    
			  EnergyProfilerContract.timeCapturing = (int)((timespent + 10000L) / 1000L);
			  
			  // Extracts BatteryStats and dump trace file info........
			  extractInfo(appName, run, batteryStatsFilename, runDataFolderName, platformToolsFolder, traceviewFilename); 
			  
			  // wait while all information is extracted........
			  systraceThread.join();
			  
			  // Extract Power Profile first here and then parse.................
			  if (run == 1 && r == 0) {
				  consoleOutput.appendText("Seed " + run + " extract power profile.\n");
				  extractPowerProfile(platformToolsFolder);
			  }
			  consoleOutput.appendText("Seed Running " + (r + 1) + " parsing power profile.\n");
			  System.out.println("Run " + run + ": parsing power profile.");
			  PowerProfile powerProfile = it.pak.tech.com.core.powerprofile.PowerProfileParser.parseFile(powerProfileFile);

			  System.out.println("Run " + run + ": aggregating results.");
			  consoleOutput.appendText("Run " + run + " aggregating results.\n");
			  parseAndAggregateResults(traceviewFilename, batteryStatsFilename, systraceFilename, methodDetailsFileName, powerProfile, appName, run);
		      
//			  PrintWriter resultsWriter = new PrintWriter(outputDataFolderName + "/" + "EnergyProfile_" + run + ".csv", "UTF-8");
//			  resultsWriter.println("Signature, joule, seconds, NumOfCalls, Exculsive Time(Usecs), Inculsive Time(Usecs)");
//		    
//			  for (TraceLine traceLine : traceLinesWiConsumptions) {
//				  resultsWriter.println
//				  (
//		    			traceLine.getSignature() + "," + traceLine.getConsumption() + "," + traceLine.getTimeLength() + "," +
//		    			traceLine.getNumOfCalls() + "," + traceLine.getExclTime() + "," + traceLine.getInclTime()
//				  );
//			  }
//		    
//			  resultsWriter.flush();
//		      resultsWriter.close();
			  System.out.println("Seed Run Times = " + (r + 1));
			  FileUtils.forceDelete(new File(runDataFolderName));
			  // here not return we need to calculate median value logic
		//	  return traceLinesWiConsumptions;
			  
		  } // if block if monkey fail to perform actions......
	  }  // End of for loop iterate over the seed value.......
	  
	  
	  // Final traceLines list to have final result with median value of each method.
	  List<TraceLine> traceLinesWConsumptions = null ;
	  List<MedianTraceLine> medianTraceLinesList = new ArrayList<>();
	  
	  if (!traceLinesWConsumptionsList.isEmpty()) {
		  
		  for (int i = 0 ; i < traceLinesWConsumptionsList.size() ; i++) {
			  
			  traceLinesWConsumptions = traceLinesWConsumptionsList.get(i).getTraceLines();
			  System.out.println("Total TraceLines: " + traceLinesWConsumptions.size());
			  //traceLinesWConsumptions.sort(null);
			  for (int j = 0 ; j < traceLinesWConsumptions.size(); j++) {
				  //System.out.print(traceLinesWConsumptions.get(j).getSignature() + ",");
				  String signature = traceLinesWConsumptions.get(j).getSignature() ;
				  if (medianTraceLinesList.isEmpty()) {
					  
					  MedianTraceLine mtl = new MedianTraceLine();
					  mtl.setSignature(signature);
					  mtl.setNumOfCalls(traceLinesWConsumptions.get(j).getNumOfCalls());
					  mtl.addConsumption(traceLinesWConsumptions.get(j).getConsumption());
					  mtl.addTimeLength(traceLinesWConsumptions.get(j).getTimeLength());
					  mtl.addInclusiveTime(traceLinesWConsumptions.get(j).getInclTime());
					  mtl.addExculsiveTime(traceLinesWConsumptions.get(j).getExclTime());
					  
					  medianTraceLinesList.add(mtl);
				  }
				  else {
					  
					  int index = isMedianTraceLineExist(medianTraceLinesList, signature);
					  
					  if (index != -1) {  // Condition means median trace line already exists.......
						  
						 // just add new consumption, timeLength, inclusiveTime and exculsiveTime......
						  Double value = 0.0 ;
						  
						  value = traceLinesWConsumptions.get(j).getConsumption();
						  medianTraceLinesList.get(index).addConsumption(value);
						  
						  value = traceLinesWConsumptions.get(j).getTimeLength();
						  medianTraceLinesList.get(index).addTimeLength(value);
						  
						  value = traceLinesWConsumptions.get(j).getExclTime();
						  medianTraceLinesList.get(index).addExculsiveTime(value);
						  
						  value = traceLinesWConsumptions.get(j).getInclTime();
						  medianTraceLinesList.get(index).addInclusiveTime(value);
					  }
					  else {
						  
						  MedianTraceLine mtl = new MedianTraceLine();
						  mtl.setSignature(signature);
						  mtl.setNumOfCalls(traceLinesWConsumptions.get(j).getNumOfCalls());
						  mtl.addConsumption(traceLinesWConsumptions.get(j).getConsumption());
						  mtl.addTimeLength(traceLinesWConsumptions.get(j).getTimeLength());
						  mtl.addInclusiveTime(traceLinesWConsumptions.get(j).getInclTime());
						  mtl.addExculsiveTime(traceLinesWConsumptions.get(j).getExclTime());
						  
						  medianTraceLinesList.add(mtl);
					  }
				  }
			  } // EOL Internal for loop........ 
			  //System.out.println();
		  }  // EOL External for loop........
		  
		  traceLinesWConsumptions.clear();
		  for (MedianTraceLine medianTraceLine : medianTraceLinesList) {
			System.out.print(medianTraceLine.getSignature() + ", " + medianTraceLine.getNumOfCalls());
			System.out.print(", " + medianTraceLine.getConsumptionList().size() + ", " 
			                 + medianTraceLine.getTimeLengthList().size() + ", " 
					         + medianTraceLine.getExclTimeList().size() + ", " 
			                 + medianTraceLine.getInclTimeList().size());
			System.out.println();
			
			Collections.sort(medianTraceLine.getConsumptionList());
			System.out.println("Sorted ConsumptionsList:");
			System.out.println(medianTraceLine.getConsumptionList());
			
			Collections.sort(medianTraceLine.getTimeLengthList());
			System.out.println("Sorted TimeLengthList:");
			System.out.println(medianTraceLine.getTimeLengthList());
			
			Collections.sort(medianTraceLine.getExclTimeList());
			System.out.println("Sorted ExculsiveTimeList:");
			System.out.println(medianTraceLine.getExclTimeList());
			
			Collections.sort(medianTraceLine.getInclTimeList());
			System.out.println("Sorted InculsiveTimeList:");
			System.out.println(medianTraceLine.getInclTimeList());
			
			TraceLine finalTraceLine = new TraceLine();
			finalTraceLine.setSignature(medianTraceLine.getSignature());
			finalTraceLine.setNumOfCalls(medianTraceLine.getNumOfCalls());
			
			DecimalFormat df = new DecimalFormat("#.####");
			double medianValue = findMedianValue(medianTraceLine.getConsumptionList());
			medianValue = Double.parseDouble(df.format(medianValue));
			finalTraceLine.setConsumption(medianValue);
			
			medianValue = findMedianValue(medianTraceLine.getTimeLengthList());
			medianValue = Double.parseDouble(df.format(medianValue));
			finalTraceLine.setTimeLength(medianValue);
			
			medianValue = findMedianValue(medianTraceLine.getExclTimeList());
			//medianValue = Double.parseDouble(df.format(medianValue));
			finalTraceLine.setExclTime(medianValue);
			
			medianValue = findMedianValue(medianTraceLine.getInclTimeList());
			//medianValue = Double.parseDouble(df.format(medianValue));
			finalTraceLine.setInclTime(medianValue);
			traceLinesWConsumptions.add(finalTraceLine);
		  }
		  
	  }  // EOB if block traceViewStructureList.......
	  System.out.println("Seed " + run + ": complete.");
	  consoleOutput.appendText("Seed " + run + " complete.\n");
	  // Unable to exercise on App make sure app is bug free and never stopped.
	  return traceLinesWConsumptions ;
  }
  
  public boolean executeActions(String appName , int seed , int interactions , int timeBetweenInteractions , String platformToolsFolder, String runDataFolderName) throws NoDeviceFoundException {
	  
	String writePermissionCommand = "./gradlew grantDebugPermissions";
	  
	try {
		Utility.execCommand(EnergyProfilerContract.sourceCodePath, writePermissionCommand);
	} catch (BuildFailedException e1) {
		e1.printStackTrace();
	}
	  //System.out.println("Permission Granted Successfully!!!");
	  
	  boolean isTraceFilePull = false ;
	  int numTries = 0 ;
		
	  while((numTries < 3) && (!isTraceFilePull)) {
		  
		    String output = Utility.executeCommand(platformToolsFolder + "/adb shell monkey -p " + appName + " -s " + seed + 
				                                       " --throttle " + timeBetweenInteractions + 
				                                       " --ignore-crashes --ignore-timeouts" + 
				                                       " --ignore-security-exceptions -v --pct-syskeys 0 " + 
				                                       interactions, null);
			System.out.println("Monkey Stopped Exercising");
			consoleOutput.appendText(output + "\nMONKEY STOPPED EXERCISING ^_^");
			output = Utility.execBroadCastCommand(platformToolsFolder, "./adb shell \"am broadcast -a foo.intent.action.SHUTDOWN\"");
			consoleOutput.appendText(output);
			
			try {
			//	System.out.println("Sleep1");
				Thread.sleep(3000L); //Sleep for 3 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	

			//System.out.println("saving trace views");
			File traceFile = new File(runDataFolderName + "/TraceLogFile.trace");
			System.out.println("saving trace file");
			if (!traceFile.exists()) {
				System.out.println("First Attempt pull");
				output = Utility.executeCommand(platformToolsFolder + "/adb pull /sdcard/TraceLogFile.trace " + runDataFolderName , null);
			}
			consoleOutput.appendText(output+"\n");
			
			if ((traceFile.length()) == 0) {
		
				try {
					System.out.println("Sleep2");
					Thread.sleep(3000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Second Attempt pull");				
				output = Utility.executeCommand(platformToolsFolder + "/adb pull /sdcard/TraceLogFile.trace " + runDataFolderName , null);
				consoleOutput.appendText(output+"\n");
				
				if (traceFile.length() != 0) {
					
					isTraceFilePull = true ;
					System.out.println("Trace File Pulled");
				}
			}
			else {
				System.out.println("Trace File Pulled");
				isTraceFilePull = true ;
			}
			++numTries;
		} // End of while loop.....
		
		if (isTraceFilePull) {
			
			System.out.println("Successfully Proceed to forward");
			return true ;
		}
		else {
			
			System.out.println("Unable to pull trace file can't Proceed to forward Please make sure your app is successfully exit");
		}
	
		return false ;
  }
  
  public void extractPowerProfile(String platformtoolsFolder) throws NoDeviceFoundException {
    
	  System.out.println("Extracting power profile.");
	  String jarDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
	  String output = "";
	  output = Utility.executeCommand(platformtoolsFolder + "/adb pull /system/framework/framework-res.apk", null);
	  output += "\n";
	  output += Utility.executeCommand("jar xf " + jarDirectory + "/EnergyProfiler.jar apktool.jar", null);
	  output += "\n";
	  output += Utility.executeCommand("java -jar apktool.jar if framework-res.apk", null);
	  output += "\n";
	  output += Utility.executeCommand("java -jar apktool.jar d framework-res.apk", null);
	  output += "\n";
	  output += Utility.executeCommand("mv " + jarDirectory + "/framework-res/res/xml/power_profile.xml " + jarDirectory, null);
	  output += "\n";
	  output += Utility.executeCommand("rm -rf " + jarDirectory + "/apktool.jar", null);
	  output += "\n";
	  output += Utility.executeCommand("rm -rf " + jarDirectory + "/framework-res.apk", null);
	  output += "\n";
	  output += Utility.executeCommand("rm -rf " + jarDirectory + "/framework-res", null);
	  consoleOutput.appendText(output);
  }
  
  private SysTraceRunner startProfiling(int run, int timeCapturing, String systraceFilename, String platformToolsFolder) throws NoDeviceFoundException
  { 
    System.out.println("Run " + run + ": capturing system traces.");
    return new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
  }
  
  private void extractInfo(String appName, int run, String batteryStatsFilename, String runDataFolderName, String platformToolsFolder, String traceviewFilename) throws NoDeviceFoundException 
  {
	  appName = appName.substring(0 , appName.lastIndexOf("."));
	  System.out.println("Run " + run + ": saving battery stats.");
	  consoleOutput.appendText("Run " + run + " saving battery stats.\n");
	  Utility.executeCommand(platformToolsFolder + "/adb shell dumpsys batterystats", new File(batteryStatsFilename));
	  
	  System.out.println("saving TraceFile Results...... " + appName);
	  consoleOutput.appendText("Run " + run + " saving traceviews.\n");
	  Utility.executeCommand(platformToolsFolder + "/dmtracedump -o " + runDataFolderName + "/TraceLogFile.trace", new File(traceviewFilename));
	  Utility.executeCommand( platformToolsFolder + "/dmtracedump -h " + runDataFolderName + "/TraceLogFile.trace ",new File(traceviewFilename+"HTML.html"));
	  Utility.executeCommand("python HtmlParser.py " + (traceviewFilename+"HTML.html ")  + appName, new File(runDataFolderName + File.separator + "TraceLogFileMethodDetail.txt"));
  }
  
  private void parseAndAggregateResults(String traceviewFilename, String batteryStatsFilename, String systraceFilename, String methodDetailsFileName , PowerProfile powerProfile, String filter, int run)
    throws java.io.IOException, InterruptedException
  {
    List<TraceLine> traceLinesWConsumption = new ArrayList<>();
    System.out.println("Run " + run + ": elaborating traceview info.");
    consoleOutput.appendText("Run " + run + " elaborating traceview info.\n");
    
    filter = filter.substring(0 , filter.lastIndexOf("."));
    TraceviewStructure traceviewStructure = TraceViewParser.parseFile(traceviewFilename, filter);
    List<TraceLine> traceLines = traceviewStructure.getTraceLines();
    System.out.println("Total TraceLines: " + traceLines.size());
    if (traceLines.size() == 0) {
    	return ;
    }
    int traceviewLength = traceviewStructure.getEndTime();
    int traceviewStart = traceviewStructure.getStartTime();
  
    System.out.println("Run " + run + ": elaborating battery stats info.");
    consoleOutput.appendText("Run " + run + " elaborting battery stats info.\n");
    List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart);
    
    System.out.println("Run " + run + ": elaborating systrace stats info.");
    consoleOutput.appendText("Run " + run + " elaborting systrace stats info.\n");
    SysTrace cpuInfo = SysTraceParser.parseFile(systraceFilename, traceviewStart, traceviewLength);
    
    if (cpuInfo.getFrequencies().size() != 0) {
    	
    	System.out.println("Run " + run + ": aggregating results.");
        consoleOutput.appendText("Run " + run + " prepare the results.\n");
        //System.out.println("Number of CPU: " + cpuInfo.getNumberOfCpu());
        
        energyInfoArray = mergeEnergyInfo(energyInfoArray, cpuInfo, cpuInfo.getNumberOfCpu());
       // System.out.println("After Merging " + energyInfoArray.size());
       
        char [] data = Utility.ReadingFile(methodDetailsFileName);
        String [] dataLines = String.valueOf(data).split("\n");
        
        for (TraceLine traceLine : traceLines) {
          
        	TraceLine traceLineConsumptionCalculated = calculateConsumption(traceLine, energyInfoArray, powerProfile);
          if (traceLinesWConsumption.isEmpty()) {
       // 	  System.out.println("Signature to Found: " + traceLineConsumptionCalculated.getSignature());
        	  String line = Utility.getSignatureDetails(dataLines, traceLineConsumptionCalculated.getSignature());
    		  if (line != null) {
    			  String [] lineDetails = line.split("\\s");
    			  int methodCount = 0;
    			  traceLineConsumptionCalculated.setNumOfCalls(++methodCount);
    			  traceLineConsumptionCalculated.setExclTime(Double.parseDouble(lineDetails[0]));
    			  traceLineConsumptionCalculated.setInclTime(Double.parseDouble(lineDetails[1]));
    	    	  traceLinesWConsumption.add(traceLineConsumptionCalculated);  
    		  }    
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
    }
    else {
    	// Display message here about can't proceed without system level information......
    	// message insufficient system level information try again
    	EPExperiment.printAlert("Failed to get system level information try again");
    }
    
    // update the traceView Structure object to have traceLines with consumptions....
    traceviewStructure.setTracelines(traceLinesWConsumption);
    traceLinesWConsumptionsList.add(traceviewStructure);
    
  }
  
  private List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo, int numOfCore)
  {
    List<Integer> cpuFrequencies = new ArrayList<>();
    
    List<EnergyInfo> finalEnergyInfoArray = new ArrayList<>();
    
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
        	
        	if (energyInfo.getEntrance() > traceLine.getExit()) {
        		nanoseconds = energyInfo.getEntrance() - traceLine.getExit() ;
        	}
        	else {
        		nanoseconds = traceLine.getExit() - energyInfo.getEntrance() ;
        	}
        	
        	//System.out.println("NS1: " + nanoseconds);
        	//System.out.println("TraceLine Exit: " + traceLine.getExit() + " EnergyInfo Entrance: " + energyInfo.getEntrance());
        }
        else {
        	
        	nanoseconds = energyInfo.getExit() - energyInfo.getEntrance();
        	//System.out.println("NS2: " + nanoseconds);
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
 
 private double findMedianValue(List<Double> doubleValues) {
	 
	 double median;
	 int size = doubleValues.size() ;
	 if (size % 2 == 0)
	     median = (doubleValues.get(size/2) + doubleValues.get(size/2 - 1))/2;
	 else
	     median =  doubleValues.get(size/2);
	 return median ;
 }
 
 private int isMedianTraceLineExist(List<MedianTraceLine> medialTraceLines , String signature) {
	  
	  int index = -1 ;
	  
	  for (int i = 0 ; i < medialTraceLines.size() ; i++) {
		  
		  if (medialTraceLines.get(i).getSignature().equals(signature)) {
			  
			  index = i ;
			  break ;
		  }
	  }
	  
	  return index ;
 }
 
 private int isTraceLineExist(List<TraceLine> traceLines , String signature) {
	  
	  int index = -1 ;
	  
	  for (int i = 0 ; i < traceLines.size() ; i++) {
		  
		  if (traceLines.get(i).getSignature().equals(signature)) {
			  
			  index = i ;
			  break ;
		  }
	  }
	  
	  return index ;
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
