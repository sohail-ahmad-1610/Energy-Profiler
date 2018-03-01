package it.pak.tech.com.core.utils;

import java.io.File;

public class EnergyProfilerContract {

	// Here we define some constants parameters initializing values are from the EnergyProfilerConfig.properties file....
	
	// When user download android sdk components which android component version 
	// he/she is used to compile and build, the android app.
	public static int compileSDKVersion ;    
	public static String buildToolsVersion ; 
	public static int minSDKVersion ;
	public static int targetSDKVersion ;
	public static int maxRun = 0 ;
	public static String androidStudioV;
	
	public static String sdkpath;
	public static String sourceCodePath;
	public static String tempOutputPath;
	public static String appVersion ;
}
