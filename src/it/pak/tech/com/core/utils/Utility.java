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

import it.pak.tech.com.core.exceptions.NoDeviceFoundException;

public class Utility {

	public Utility() {
		
	}
	
	public static String executeCommand(String command, File outputFile) throws it.pak.tech.com.core.exceptions.NoDeviceFoundException
	  {
	    StringBuilder output = new StringBuilder();
	    try
	    {
	      List<String> listCommands = new ArrayList();
	      
	      String[] arrayExplodedCommands = command.split(" ");
	      listCommands.addAll(java.util.Arrays.asList(arrayExplodedCommands));
	      ProcessBuilder pb = new ProcessBuilder(listCommands);
	      pb.redirectErrorStream(true);
	      if (outputFile != null) {
	        pb.redirectOutput(outputFile);
	      }
	      
	      Process commandProcess = pb.start();
	      
	      BufferedReader in = new BufferedReader(new java.io.InputStreamReader(commandProcess.getInputStream()));Throwable localThrowable3 = null;
	      try { String line;
	        while ((line = in.readLine()) != null) {
	          output.append(line).append("\n");
	          if (line.contains("error: no devices/emulators found")) {
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



	        if (in != null) if (localThrowable3 != null) try { in.close(); } catch (Throwable localThrowable2) { localThrowable3.addSuppressed(localThrowable2); } else in.close();
	      }
	    } catch (java.io.IOException|InterruptedException ex) { java.util.logging.Logger.getLogger(Process.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	    }
	    return output.toString();
	  }
	 
	 public static String execCommand(String path , String Command) {
		 
		 String consoleOut = "";
		 try 
		 {
			 Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd "+ path +  ";" + "chmod +x gradlew;" + Command});
			
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
		 }
		 
		 return null ;
	}
	 
	 public static String extractAppName(String apkLocationPath) throws it.pak.tech.com.core.exceptions.NoDeviceFoundException {
		    
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
