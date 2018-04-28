package it.pak.tech.com.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.service.RepositoryService;

import javafx.application.Platform;

import it.pak.tech.com.controllers.EPDriver;
import it.pak.tech.com.controllers.EPExperiment;
import it.pak.tech.com.core.utils.EnergyProfilerContract;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Downloads{
	
	private String userName ;
	private String appName ;
	
	int total_releases = 0;
	
	@FXML
	ProgressBar bar;
	
	
	@FXML
	Label done ;
	
	public Downloads() {
		super();
	}
	
	public Downloads(String uName , String appName) {
		
		this.userName = uName ;
		this.appName = appName ;
	}
	
	public void setProgressBar(ProgressBar bar , Label done) {
		
		this.bar = bar ;
		this.done = done;
	}
	
	public void Download() {
		
		System.out.println("In start Download");
		//System.out.println("Username:"+userName+"->app:"+appName);
		if (userName.equals("") | 
				appName.equals("")
				) {
				EPExperiment.printAlert("ERROR: All inout fields are required...");
				return;
			}
		
		Task<Void> task = new Task<Void>() {

			int count;
			
			@Override
			protected Void call() throws Exception {
				
				try {
					
					//System.out.println(userName + " , " + appName);
					RepositoryService repoService = new RepositoryService();
					//System.out.println(userName + " , " + appName);
					Repository repository = repoService.getRepository(userName,appName);
					
	                

		            //System.out.println(userName + " , " + appName);
		            List<RepositoryTag> list = repoService.getTags(repository);
		            //System.out.println("Start " + list.size());
		            
		            String version  = "";
		            String[] versions = new String[list.size()];
		            total_releases = list.size();

		            for (int i=0; i<list.size(); i++) {
		            	
		                String actualURL = list.get(i).getZipballUrl();
		                version = list.get(i).getName();

		            	System.out.println("Version: "+ version + " is Downloading");
		                versions[i] = version;
		                URL url = new URL(actualURL);
		                URLConnection conexion = url.openConnection();
		                if (conexion.getURL().getHost() == null) {
		                System.out.println("INVALID URL");	
		                /*Code*/}
		                //System.out.println("INVALID URL: "+conexion.getURL().getHost());
		                conexion.connect();

		                InputStream input = new BufferedInputStream(url.openStream());
		                OutputStream output = new FileOutputStream(EnergyProfilerContract.tempOutputPath + "/" + version +".zip");

		                byte data[] = new byte[1024];

		                long total = 0;

		                while ((count = input.read(data)) != -1) {
		                    total += count;
		                    output.write(data, 0, count);
		                }

		                output.flush();
		                output.close();
		                input.close();
		                updateProgress(i+1, total_releases);
		                
		            }
		            
		            //done.setVisible(true);
		            System.out.println("Downloading Completed");
		            Thread.sleep(3000L);  // Sleep for 3 sec.....
		         
		            Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							// Load root layout from fxml file.
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
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
		            
					}catch (Exception e) {
//						System.out.println("Getting Repo");
						System.out.println(e.getMessage());
						EPExperiment.printAlert("ERROR: Invalid Owner name or app-name!");
						
						return null;
						
						
					}
	//			done.setVisible(true);
				return null;
			}
			
		};
		
		bar.progressProperty().bind(task.progressProperty());
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
}
