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
	
	public void BuildApkFile() {
		
		try {
			
			// Extract the downloaded version....
			System.out.println("File Path " + filePath);
			unzip(filePath, EnergyProfilerContract.tempOutputPath + File.separator + EnergyProfilerContract.appVersion);
			ConfigManager.initBuildTools();
			// Configure the build.gradle file......
			ConfigManager.ConfigGradleFile(buildGradleFilePath);
			ConfigManager.RemovePackageGradleFile(EnergyProfilerContract.sourceCodePath + "/cpl/build.gradle");
			dirPath = gradlewFilePath.substring(0,gradlewFilePath.indexOf("gradlew"));
			
			//System.out.println(selectedFile.getParent());
			
			ConfigManager.configAndroidV(dirPath);
			
			ConfigManager.ConfigGradleWrapper(dirPath);
			
			ConfigManager.ConfigManifestFile(dirPath);
			
			String command = "cp " + "local.properties" + " " + EnergyProfilerContract.sourceCodePath ;
			Utility.executeCommand(command, null);
			System.out.println("Building APK file At " + EnergyProfilerContract.sourceCodePath);
			
			// Build apk
			String output = Utility.execCommand(EnergyProfilerContract.sourceCodePath , buildCommand);
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoDeviceFoundException e) {
			// TODO Auto-generated catch block
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
        while (entry != null) {
            
        	String filePath = destDirectory + File.separator + entry.getName();
            //System.out.println("PATH: " + filePath);
            
            if (filePath.contains("/app/build.gradle")) {
            	
            	buildGradleFilePath = filePath ;
            }
            if (filePath.endsWith("gradlew")) {
            	
            	EnergyProfilerContract.sourceCodePath = filePath.substring(0,filePath.indexOf("gradlew"));
            	gradlewFilePath = filePath ;
        //    	System.out.println(gradlewFilePath);
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
