package it.pak.tech.com.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import it.pak.tech.com.controllers.EPExperiment;
import it.pak.tech.com.core.exceptions.BuildFailedException;
import it.pak.tech.com.core.exceptions.NoDeviceFoundException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Utility {

	public Utility() {	
	}
	
	// This method executes all Android Debug Bridge(ADB) commands
	public static String executeCommand(String command, File outputFile) throws it.pak.tech.com.core.exceptions.NoDeviceFoundException
	{
	    StringBuilder output = new StringBuilder();
	    try
	    {
	      List<String> listCommands = new ArrayList<String>();
	            
	      String[] arrayExplodedCommands = command.split(" ");
	      listCommands.addAll(java.util.Arrays.asList(arrayExplodedCommands));
	      
	      //ProcessBuilder: This class is used to create operating system processes 
	      ProcessBuilder pb = new ProcessBuilder(listCommands);
	      pb.redirectErrorStream(true);
	      
	      if (outputFile != null) {
	        pb.redirectOutput(outputFile);
	      }
	      
	      Process commandProcess = pb.start();  // The start() method creates a new Process instance with those attributes. 
	      // The start() method can be invoked repeatedly from the same instance to create new subprocesses with identical or related attributes.     
	      
	      BufferedReader in = new BufferedReader(new java.io.InputStreamReader(commandProcess.getInputStream()));
	      Throwable localThrowable3 = null;
	      
	      try { 
	    	  	String line;
		      	while ((line = in.readLine()) != null) {
		          output.append(line).append("\n");
		          if (line.contains("error: no devices/emulators found")) {
		        	  	 EPExperiment.printAlert("ERROR: No external Device/Emulator (AVD) found!");
		            throw new NoDeviceFoundException();
		          }
		      	}
	        
	        commandProcess.waitFor();
	      }
	      catch (Throwable localThrowable1)
	      {
	        localThrowable3 = localThrowable1;throw localThrowable1;
	      }
	      finally
	      {
	        if (in != null) 
	        	if (localThrowable3 != null) 
	        		try { in.close(); } 
	        		catch (Throwable localThrowable2) { localThrowable3.addSuppressed(localThrowable2); } 
	        	else in.close();
	      }
	    } catch (java.io.IOException|InterruptedException ex) { java.util.logging.Logger.getLogger(Process.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	    }
	    return output.toString();
	  }
	 
	 public static String execCommand(String path , String Command) throws it.pak.tech.com.core.exceptions.BuildFailedException  {
		 
		 String consoleOut = "";
		 try 
		 {
			 // This seems to run commands like on the terminal
			 Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd "+ path +  ";" + "chmod a+x gradlew;" + Command});
			
			 BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 String line;
			 while (true) {
				 
				 line = r.readLine();
				 if (line == null) { 
					 break; 
				 }
				 if (line.contains("BUILD FAILED")) {
					 
					 EPExperiment.printAlert("Unfortunately Build Failed");
			         throw new BuildFailedException();
		          }
				 
				 System.out.println(line);
				 
				 consoleOut += line + "\n";
	        }
			
			return consoleOut ;
				
		} 
		
		 catch (IOException e) {
			 e.printStackTrace();
		 }
		 
		 return null ;
	}
	 
	 public static String execBroadCastCommand(String path , String Command) {
		 
		 String consoleOut = "";
		 try 
		 {
			Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd "+ path +  ";" + Command});
			//System.out.println("Hello " + path);
			p.waitFor(); 
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 String line;
			 while (true) {
				 
				 line = r.readLine();
				 if (line == null) { 
					 break; 
				 }
				 
				 System.out.println(line);
				 consoleOut+=line + "\n";
	        }
			
			return consoleOut ;
				
		} 
		
		 catch (IOException e) {
			 e.printStackTrace();
		 } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 return null ;
	}
	 
	 public static String extractAppName(String apkLocationPath) throws it.pak.tech.com.core.exceptions.NoDeviceFoundException 
	 {	    
		String sdkFolderPath = System.getenv("ANDROID_HOME");
	    String aaptPath = sdkFolderPath + "/build-tools/26.0.1/aapt";
	    String aaptOutput = executeCommand(aaptPath + " dump badging " + apkLocationPath, null);
	    String appName = "";
	    Pattern pattern = Pattern.compile("package: name='([^']*)' versionCode='[^']*' versionName='[^']*' platformBuildVersionName='[^']*'");
	    
	    for (String line : aaptOutput.split("\n")) {
	      
	    	java.util.regex.Matcher matcher = pattern.matcher(line);    
			if (matcher.find()) {
			    
				appName = matcher.group(1);
			}
			
		//	System.out.println(line);
	    }
	    
//		    if (appName.isEmpty()) {
//		      throw new it.unisa.petra.core.exceptions.AppNameCannotBeExtractedException();
//		    }
	    
	    return appName;
	 }
	 
	 public static char [] ReadingFile(String fileName){
			
		 File inputFile = new File(fileName);
		 char [] data = null ;
		 FileReader reader = null ;
		
		 try {
			
			 if (inputFile.exists()){
				 if (((int) inputFile.length()) != 0){
					 
					 data = new char[(int)inputFile.length()] ;
					 reader = new FileReader(inputFile);
					 reader.read(data);
					 reader.close();
				 }
			 }
		 } 
		 catch (FileNotFoundException e) {
			 
			 System.out.println(e+" Exception occur that file not found.");
			//e.printStackTrace();
		 } 
		 catch (IOException e) {
			System.out.println(e+" Exception occur in reading file data.");
			//e.printStackTrace();
		 }
			
		return data ;
	}
	 
	 public static String getSignatureDetails(String [] dataLines , String signature) {
			
			String line = null ;
			
			for (int i = 0 ; i < dataLines.length ; i++) {
				
				//System.out.println("DL: " + dataLines[i]);
				String [] temp = dataLines[i].split(" ");
				if (signature.contains(temp[3])) {
					
					line = dataLines[i];
					break ;
				}
			}
			
			return line ;
		}
}
