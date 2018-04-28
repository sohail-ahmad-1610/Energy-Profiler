package it.pak.tech.com.core;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import it.pak.tech.com.controllers.EPDriver;
import it.pak.tech.com.controllers.EPExperiment;
import it.pak.tech.com.core.exceptions.BuildFailedException;
import it.pak.tech.com.core.exceptions.NoDeviceFoundException;
import it.pak.tech.com.core.utils.ConfigManager;
import it.pak.tech.com.core.utils.EnergyProfilerContract;
import it.pak.tech.com.core.utils.Utility;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AppsBuilder {

	String filePath;
	String buildCommand ;
	private static final int BUFFER_SIZE = 4096;
	private String buildGradleFilePath = null ;
	private String gradlewFilePath = null;
	private String dirPath = null;		//# Integration.
	
	
	public void initialize(String filePath) {
			this.filePath = filePath ;
			
			this.buildCommand = "./gradlew assembleDebug";
			
	}
	public boolean checkAppGradlew(String dirName) {
		
		if (dirName == null) {
			return false ;
		}
		
		File dir = new File(dirName);
	
		File[] fList = dir.listFiles();
		
		for (File file:fList) {
			
			if (file.isDirectory()) {
				if (file.getName().equals("app")) {
					return true;
				}
			}
		}
		return false;
	}
	public void BuildApkFile() {
		
		try {
			
			// Extract the downloaded version....
			//System.out.println("File Path " + filePath);
			unzip(filePath, EnergyProfilerContract.tempOutputPath + File.separator + EnergyProfilerContract.appVersion);
			//System.out.println("SC: " + EnergyProfilerContract.sourceCodePath);

			if (EnergyProfilerContract.sourceCodePath == null) {
			
				EPExperiment.printAlert("Application Source Code cannot be compiled");
				return ;
			}
				
			ConfigManager.initBuildTools();
			
			// Configure the build.gradle file......
			ConfigManager.ConfigGradleFile(buildGradleFilePath);
			ConfigManager.RemovePackageGradleFile(EnergyProfilerContract.sourceCodePath + "/cpl/build.gradle");
			if (gradlewFilePath == null) {
				
				System.out.println("Copying Gradlew File");
				
				String command = "cp " + "gradlew" + " " + EnergyProfilerContract.sourceCodePath ;			
				Utility.executeCommand(command, null);
				
				//dirPath = gradlewFilePath.substring(0,gradlewFilePath.indexOf("gradlew"));
			}
			
			ConfigManager.configAndroidV(EnergyProfilerContract.sourceCodePath);
			
			ConfigManager.ConfigGradleWrapper(EnergyProfilerContract.sourceCodePath);
			ConfigManager.ConfigManifestFile(EnergyProfilerContract.sourceCodePath);
			String command = "cp " + "local.properties" + " " + EnergyProfilerContract.sourceCodePath ;
			
			Utility.executeCommand(command, null);
			command = "cp " + "local.properties" + " " + EnergyProfilerContract.sourceCodePath + "/app/" ;
			Utility.executeCommand(command, null);
			//System.out.println("Building APK file At " + EnergyProfilerContract.sourceCodePath);
			
			// Build apk
			String output = Utility.execCommand(EnergyProfilerContract.sourceCodePath , buildCommand);
			
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		} catch (NoDeviceFoundException e) {
			
			e.printStackTrace();
		} catch (BuildFailedException e) {
			
			e.printStackTrace();
		}
		
	}
	
	 public void unzip(String zipFilePath, String destDirectory) throws IOException {
	        
		 File destDir = new File(destDirectory);
		 
		 if (!destDir.exists()) {
			 destDir.mkdir();
		 }
		 
		 ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		 ZipEntry entry = zipIn.getNextEntry();
		 // iterates over entries in the zip file
		 boolean isGradleProject = false ;
		 
		 while (entry != null) {
            
        	String filePath = destDirectory + File.separator + entry.getName();
            //System.out.println("PATH: " + filePath);
            
            if (filePath.contains("/app/build.gradle")) {
            	EnergyProfilerContract.sourceCodePath = filePath.substring(0,filePath.indexOf("app"));
            	
            	buildGradleFilePath = filePath ;
            //	System.out.println(buildGradleFilePath);
            }
            if (filePath.endsWith("gradlew")) {
            	
            	gradlewFilePath = filePath ;
        //    	System.out.println(gradlewFilePath);
            }
            if (filePath.contains("/gradle/wrapper/gradle-wrapper.properties")) {
            	isGradleProject = true ;
            }
            
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
		 if (!isGradleProject) {
			 EnergyProfilerContract.sourceCodePath = null ;
		 }
        zipIn.close();
	}
	   
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
	}
}
