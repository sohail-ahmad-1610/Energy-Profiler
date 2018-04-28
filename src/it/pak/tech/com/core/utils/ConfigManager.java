package it.pak.tech.com.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

public class ConfigManager {

	private final String propertiesPath ;
	
	public ConfigManager(String propertiesPath) {
		
		this.propertiesPath = propertiesPath;
		InitialConfiguration();
		
	}
	
	private void InitialConfiguration() {
		
		Properties prop = new Properties();
	    try {
			
	    	InputStream inputStream = getPropertiesStream();
			prop.load(inputStream);
			//initBuildTools();
			EnergyProfilerContract.minSDKVersion = 19;
			EnergyProfilerContract.targetSDKVersion = 23 ;
			EnergyProfilerContract.tempOutputPath = prop.getProperty("OutputLocation");
			EnergyProfilerContract.androidStudioV = prop.getProperty("Android_Studio_Version");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void initBuildTools() {
		
		File folder = new File(EnergyProfilerContract.sdkpath + "/platforms");
		File[] listOfFiles = folder.listFiles();
		
		List<String> buildToolsVerList = new ArrayList<>();
		List<Integer> platformsVerList = new ArrayList<>();
	    
		for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isDirectory()) {
	        //System.out.println("Directory " + listOfFiles[i].getName());
	        String temp = listOfFiles[i].getName() ;
	        temp = temp.substring(temp.lastIndexOf('-')+1 , temp.length());
	        platformsVerList.add(Integer.parseInt(temp));
	      }
	    }
		//System.out.println("PlatformsVer: " + Collections.max(platformsVerList));
	    EnergyProfilerContract.compileSDKVersion = Collections.max(platformsVerList);
	    
	    folder = new File(EnergyProfilerContract.sdkpath + "/build-tools");
	    listOfFiles = folder.listFiles();
	    for (int i = 0; i < listOfFiles.length; i++) {
		     if (listOfFiles[i].isDirectory()) {
		       // System.out.println("Directory " + listOfFiles[i].getName());
		        buildToolsVerList.add(listOfFiles[i].getName());
		      }
		    }
	    //System.out.println("BuildToolsVer: " + Collections.max(buildToolsVerList));
		EnergyProfilerContract.buildToolsVersion = Collections.max(buildToolsVerList);
	}
	
	public static void configAndroidV(String dirPath) {
		
		File build_gradle = new File(dirPath+"/build.gradle");
		
		InputStream inStream = null;
		List<String> instructions = new ArrayList<String>();
		
		try {
			inStream = new FileInputStream(build_gradle);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = null;
			while ((line = in.readLine()) != null) {
				
				if (line.contains("com.android.tools.build:gradle:")) {
					
					line = line.substring(0, line.lastIndexOf(':')+1) + EnergyProfilerContract.androidStudioV.trim() + "\'";
				//	System.out.println(line);
				}
				instructions.add(line + "\n");
			}
			in.close();
			inStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		build_gradle.delete();
		updateAndroidV(instructions, dirPath);
		
	}
	public static void updateAndroidV(List<String> instructions,String dirPath) {
		
		File buildGradleFile = new File(dirPath +"/build.gradle");
		OutputStream outputStream = null;
		
		try {
			outputStream = new FileOutputStream(buildGradleFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
			for (int i=0; i<instructions.size(); i++) {
				out.write(instructions.get(i));
			}
			out.close();
			outputStream.close();
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private InputStream getPropertiesStream() throws IOException {
		
		return new FileInputStream(this.propertiesPath);
	}
		
	public String getOutputLocation() throws IOException {
		
	    Properties prop = new Properties();
	    InputStream inputStream = getPropertiesStream();
	    Throwable localThrowable3 = null;
	    
	    try { 
	    	prop.load(inputStream);
	    }
	    catch (Throwable localThrowable1)
	    {
	      localThrowable3 = localThrowable1;
	      throw localThrowable1;
	    } 
	    finally {
	      if (inputStream != null) 
	    	  if (localThrowable3 != null) 
	    		  try { inputStream.close(); 
	    		  } 
	              catch (Throwable localThrowable2) { 
	            	  localThrowable3.addSuppressed(localThrowable2); 
	            	  }
	    	  else 
	    		  inputStream.close();
	    }
	    return prop.getProperty("OutputLocation");
	  }
	
	public static void ConfigGradleFile(String buildGradleFilePath) {
		
		//System.out.println("GFP: " + buildGradleFilePath);
		
		File myGradleFile = new File(buildGradleFilePath);
		
		InputStream inStream = null;
		List<String> instructions = new ArrayList<String>();
		
		try {
			
			inStream = new FileInputStream(myGradleFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = null ;
			String updatedLine = null ;
			boolean isChange = false ;
			int index = 0 ;
			
			while((line = in.readLine()) != null) {
				
				if (line.contains("compileSdkVersion")) {
					
					updatedLine = line.substring(0 , (line.length() - 3)) + " " + EnergyProfilerContract.compileSDKVersion;   // Give your SDK version.....
					isChange = true ;
				}
				else if (line.contains("buildToolsVersion")) {
		
					updatedLine = line.substring(0 , (line.length() - 8)) + "\"" + EnergyProfilerContract.buildToolsVersion + "\"";   // Give your BuildTools version.....
					isChange = true ;
				}
				else if (line.contains("minSdkVersion")) {
					
					//updatedLine = line.substring(0 , (line.length() - 3)) + " " + EnergyProfilerContract.minSDKVersion;   // Give your MinSDK version.....
					updatedLine = line ;
					isChange = true ;
				}
				else if (line.contains("targetSdkVersion")) {
					
					updatedLine = line.substring(0 , (line.length() - 3)) + " " + EnergyProfilerContract.targetSDKVersion;   // Give your Target SDK version.....
					isChange = true ;
				}
				else if (line.contains("com.android.support:appcompat")) {
					
					index = line.indexOf('-');
					updatedLine = "compile \"com.android.support:appcompat" + "-v7:" + EnergyProfilerContract.compileSDKVersion + ".+\"";   // Give your Support Activity version.....
					isChange = true ;
				}
				
				if (isChange) {
					
					//System.out.println(updatedLine);
					instructions.add(updatedLine + "\n");
					isChange = false ;
				}
				else {
					
				//	System.out.println(line);
					instructions.add(line + "\n");
				}
				
			} // End While Loop......
		}  // End Try block.... 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		myGradleFile.delete();
		
		UpdateGradleFile(instructions , buildGradleFilePath);
	}
	
	private static void UpdateGradleFile(List<String> instructions , String buildGradleFilePath) {
		
		File myGradleFile = new File(buildGradleFilePath);
		OutputStream outStream = null ;
		String gradlePermissionCommand = "android.applicationVariants.all { variant ->\n"
                + "\tdef applicationId = variant.applicationId\n"
                + "\tdef adb = android.adbExe as String\n"
                + "\tdef variantName = variant.name.capitalize()\n" 
                + "\tdef grantPermissionTask = tasks.create(\"grant${variantName}Permissions\") << {\n" 
                + "\t\t\"${adb} devices\".execute().text.eachLine {\n" 
                + "\t\t\tif(it.endsWith(\"device\")){\n"
                + "\t\t\t\tdef device = it.split()[0]\n"
                + "\t\t\t\tprintln \"Granting permissions on devices ${device}\"\n"
                + "\t\t\t\t\"${adb} -s ${device} shell pm grant ${applicationId} android.permission.READ_EXTERNAL_STORAGE\".execute()\n"
                + "\t\t\t\t\"${adb} -s ${device} shell pm grant ${applicationId} android.permission.WRITE_EXTERNAL_STORAGE\".execute()\n"
                + "\t\t\t}\n"
             + "\t\t}\n" 
          + "\t}\n"
          + "grantPermissionTask.description = \"Grants Permission on Marshmallow or later\"\n"
          + "grantPermissionTask.group = \"extras\"\n"
       + "}";
		
//		String testDependency = "\ttestCompile 'junit:junit:4.12'\n";
/*		String espressoDependency = "\tandroidTestCompile 'com.android.support.test.espresso:espresso-core:2.2.2', {\n" + 
				"        exclude group: 'com.android.support', module: 'support-annotations'\n" + 
				"    }\n";
		String testRunner = "testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'\n";*/
		
		try {
			
			outStream = new FileOutputStream(myGradleFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream));
			
			for (int i = 0 ; i < instructions.size() ; i++) {
				
				if (instructions.get(i).contains("defaultConfig")) {
					out.write(instructions.get(i));
	//				out.write(testRunner);
				}
				
				else if (instructions.get(i).contains("dependencies")) {
					out.write(instructions.get(i));
	//				out.write(testDependency);
	//				out.write(espressoDependency);
				}
				else {
					out.write(instructions.get(i));
				}
				
			}
			out.write(gradlePermissionCommand);
			out.close();
			outStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void ConfigGradleWrapper(String dirPath) {
		
		String wrapperPath = dirPath +"/gradle/wrapper/gradle-wrapper.properties";
		File wrapperFile = new File(wrapperPath);
		InputStream inStream = null;
		List<String> instructions = new ArrayList<String>();
		
		try {
			inStream = new FileInputStream(wrapperFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = null;
			while ((line = in.readLine()) != null) {
				
				if (line.contains("distributionUrl")) {
					
					line = "distributionUrl=https\\://services.gradle.org/distributions/gradle-3.3-all.zip";
				}
				instructions.add(line + "\n");
			}
			in.close();
			inStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		wrapperFile.delete();
		UpdateGradleWrapper(instructions,wrapperPath);
	}
	
	public static void UpdateGradleWrapper(List<String> instructions, String wrapperPath) {
		
		File wrapperFile = new File(wrapperPath);
		OutputStream outputStream = null;
		
		try {
			outputStream = new FileOutputStream(wrapperFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
			for (int i=0; i<instructions.size(); i++) {
				out.write(instructions.get(i));
			}
			out.close();
			outputStream.close();
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void ConfigManifestFile(String dirPath) {
		
		String manifestPath = dirPath + "/app/src/main/AndroidManifest.xml";
		File manifestFile = new File(manifestPath);
		InputStream inStream = null;
		boolean writePerm = false;
		boolean readPerm = false;
		String MainActivityPath = null ;
		
		List<String> instructions = new ArrayList<String>();
		try {
			inStream = new FileInputStream(manifestFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = null;
			String updateLine = null;
			
			while ((line = in.readLine()) != null) {
				
				if (line.trim().contains("package")) {
					
					MainActivityPath = line.substring(line.indexOf("package=")+9,line.lastIndexOf("\""));
					EnergyProfilerContract.appName = MainActivityPath ;
					MainActivityPath = MainActivityPath.replace('.', '/');
					//System.out.println("MPath: 1 " + MainActivityPath);
				}
				
				if (line.contains("<uses-permission")) {
					
					if (line.endsWith("/>") || line.endsWith("</uses-permission>")) {
					
						if (!writePerm && line.substring(line.indexOf("=")+2,line.lastIndexOf("\"")).equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
							writePerm = true;
				//			System.out.println("Write Found");
						}
						if (!readPerm && line.substring(line.indexOf("=")+2,line.lastIndexOf("\"")).equals("android.permission.READ_EXTERNAL_STORAGE")) {
							readPerm = true;

	//						System.out.println("Read Found");
						}
					}
					else {
						
						while ((line = in.readLine()) != null) {
							
							if (line.contains("android:name")) {
								
								if (!writePerm && line.substring(line.indexOf("=")+2,line.lastIndexOf("\"")).equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
									writePerm = true;

		//							System.out.println("Write Found");
									break;
								}
								if (!readPerm && line.substring(line.indexOf("=")+2,line.lastIndexOf("\"")).equals("android.permission.READ_EXTERNAL_STORAGE")) {
									readPerm = true;

			//						System.out.println("Read Found");
									break;
								}
							}
							if (line.endsWith("/>")) {
								break;
							}
						}
					}
					
				}
				
				instructions.add(line+"\n");
			}
			in.close();
			inStream.close();
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		manifestFile.delete();
		//System.out.println("MP1: " + MainActivityPath);
		MainActivityPath = UpdateManifest(instructions,writePerm,readPerm,manifestPath, MainActivityPath);
		MainActivityPath = EnergyProfilerContract.sourceCodePath + "app/src/main/java/" + MainActivityPath;
		//System.out.println("MP2: " + MainActivityPath1);
		ConfigLauncherActivity(MainActivityPath);
	}
	public static void ConfigLauncherActivity (String MainActivityPath) {
		
		File mainActivityFile = new File(MainActivityPath);
		InputStream istream = null;
		boolean onDestroy = false;
		List<String> instructions = new ArrayList<String>();
		
		try {
			
			istream = new FileInputStream(mainActivityFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(istream));
			String line = null;
			
			while ((line = in.readLine()) != null) {
				
				if (line.contains("super.onDestroy()")) {
					onDestroy = true;
				}
				instructions.add(line + "\n");
			}
			in.close();
			istream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		mainActivityFile.delete();
		UpdateMainActivity(instructions, onDestroy , MainActivityPath);
	}
	private static void UpdateMainActivity(List<String> instructions, boolean onDestroy, String MainActivityPath) {
		
		File mainActivityFile = new File(MainActivityPath);
		OutputStream outStream = null;
		String destroyAppStatements = "BroadcastReceiver receiver = new BroadcastReceiver() {\n" + 
				                      "            @Override\n" + 
				                      "            public void onReceive(Context ctx, Intent intent) {\n" + 
				                      "                finish();\n" + 
				                      "            }\n" + 
				                      "        };\n" + 
				                      "\n" + 
				                      "        registerReceiver(receiver, new IntentFilter(\"foo.intent.action.SHUTDOWN\"));\n";
		try {
			
			outStream = new FileOutputStream(mainActivityFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream));
			
			for (int i=0; i < instructions.size(); i++) {
				
				if (instructions.get(i).startsWith("package")) {
					out.write(instructions.get(i++));
					out.write("import android.os.Debug;\n");
					out.write("import android.content.BroadcastReceiver;\n" + 
							  "import android.content.Context;\n" +
							  "import android.content.Intent;\n" +
							  "import android.content.IntentFilter;");
				}
				
				if (instructions.get(i).contains("super.onCreate(savedInstanceState)")) {
					out.write(instructions.get(i++));
					
					while (!instructions.get(i).contains("setContentView")) {
						out.write(instructions.get(i++));
					}
					out.write(instructions.get(i++));
					out.write("\t\tDebug.startMethodTracing(\"TraceLogFile\");\n");
					out.write(destroyAppStatements);
				}
				
				if (instructions.get(i).contains("@Override") && !onDestroy) {
					if (instructions.get(i+1).contains("public void onCreate") || instructions.get(i+1).contains("protected void onCreate")) 
					{
						out.write("\t@Override\n" + "\tprotected void onDestroy() {\n\n" + "\t\tsuper.onDestroy();\n" +
						"\t\tDebug.stopMethodTracing();\n\n\t}\n");
						onDestroy = true ;
					}
				}
				
				if (onDestroy && instructions.get(i).contains("super.onDestroy()")) {
					out.write(instructions.get(i++));
					out.write("\t\tDebug.stopMethodTracing();\n");
				}
				out.write(instructions.get(i));
			}
			out.close();
			outStream.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String UpdateManifest(List <String> instructions, boolean writePerm, boolean readPerm, String manifestPath , String MainActivityPath) {
		
		File manifestFile = new File(manifestPath);
		OutputStream outputStream = null;
		
		try {
			outputStream = new FileOutputStream(manifestFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
			
			boolean launcherFound =false;
			
			for (int i=0; i<instructions.size(); i++) {
				
				if (instructions.get(i).contains("<application")) {
					
					if (!writePerm) {
						out.write("\t<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" /> \n");
					}
					if (!readPerm) {
						out.write("\t<uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" /> \n");
					}
				}
				
				if (instructions.get(i).contains("android.intent.action.MAIN") && !launcherFound) {
					
					int index = i;
					do {
						index++;
						if (instructions.get(index).contains("android.intent.category.LAUNCHER")) {
							launcherFound = true;
							break;
						}
						if (instructions.get(index).contains("intent-filter")) {
							break;
						}
					}while(!launcherFound);

					if (launcherFound) 
					{
						//System.out.println("Init index: " + index);
						index = i-1;
						String nameline = null;
						while (!(nameline = instructions.get(index)).contains("<activity")) {
							//System.out.println(nameline);
							//System.out.println("Ins: "+instructions.get(index));
							index--;
						}
						if (nameline.contains("<activity-alias")) {
							while (!(nameline = instructions.get(index)).contains("android:targetActivity")) {
								index++;
							}
						}
						else {
							while (!(nameline = instructions.get(index)).contains("android:name")) {
								index++;
							}
						}
						
						
						//System.out.println(nameline);
						
						String activityName = nameline.substring(nameline.indexOf("=")+2,nameline.indexOf("\"", nameline.indexOf("=")+2));
						
						
						activityName = activityName.replace(".", "/");
						
						//System.out.println("Activity Name: " + activityName);
	
						
						if (activityName.contains(MainActivityPath)) {
	
							MainActivityPath = activityName + ".java";
							
							//System.out.println("MPath: " + MainActivityPath);
						}
						else {
							MainActivityPath = MainActivityPath + "/" + activityName + ".java";
						}
						
					}//System.out.println("MPath: " + MainActivityPath);
				}
				if (instructions.get(i).contains("android:minSdkVersion")) {
					
					String temp = instructions.get(i);
					temp = temp.substring(0, temp.indexOf('\"')) + "\"15\" />\n";
					instructions.set(i, temp);
				}
				
				out.write(instructions.get(i));
			}
			out.close();
			outputStream.close();
			//System.out.println("MP: " + MainActivityPath);
			return MainActivityPath ;
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null ;
		
	}
	
	// extra Code..........
	
	private static void UpdatePackageGradleFile(List<String> instructions , String filePath) {
		
		File myGradleFile = new File(filePath);
		OutputStream outStream = null ;
		
		try {
			
			outStream = new FileOutputStream(myGradleFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream));
			
			for (int i = 0 ; i < instructions.size() ; i++) {
				
				if (instructions.get(i).contains("dependencies")) {
					out.write(instructions.get(i));
				}
				else {
					out.write(instructions.get(i));
				}
			}
			out.close();
			outStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void RemovePackageGradleFile(String filePath) {
		
		File myGradleFile = new File(filePath);
		
		InputStream inStream = null;
		List<String> instructions = new ArrayList<String>();
		
		try {
			
			inStream = new FileInputStream(myGradleFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = null ;
			String updatedLine = null ;
			boolean isChange = false ;
			int index = 0 ;
			
			while((line = in.readLine()) != null) {
				
				if (line.contains("compileSdkVersion")) {
					
					updatedLine = line.substring(0 , (line.length() - 3)) + " " + EnergyProfilerContract.compileSDKVersion;   // Give your SDK version.....
					isChange = true ;
				}
				else if (line.contains("buildToolsVersion")) {
					
					updatedLine = line.substring(0 , (line.length() - 8)) + "\"" + EnergyProfilerContract.buildToolsVersion + "\"";   // Give your BuildTools version.....
					isChange = true ;
				}
				else if (line.contains("minSdkVersion")) {
					
					//updatedLine = line.substring(0 , (line.length() - 3)) + " " + EnergyProfilerContract.minSDKVersion;   // Give your MinSDK version.....
					isChange = true ;
				}
				else if (line.contains("targetSdkVersion")) {
					
					updatedLine = line.substring(0 , (line.length() - 3)) + " " + EnergyProfilerContract.targetSDKVersion;   // Give your Target SDK version.....
					isChange = true ;
				}
				else if (line.contains("com.android.support:appcompat")) {
					
					index = line.indexOf('-');
					updatedLine = line.substring(0 , index) + "-v7:" + EnergyProfilerContract.compileSDKVersion + ".+'";   // Give your Support Activity version.....
					isChange = true ;
				}
				
				if (isChange) {
					
					//System.out.println(updatedLine);
					instructions.add(updatedLine + "\n");
					isChange = false ;
				}
				else {
					
				//	System.out.println(line);
					instructions.add(line + "\n");
				}
				
			} // End While Loop......
		}  // End Try block.... 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		myGradleFile.delete();
		
		UpdatePackageGradleFile(instructions , filePath);
	}
}
