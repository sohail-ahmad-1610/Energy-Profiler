package it.pak.tech.com.controllers;


import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class EPDriver extends Application {
	
	AnchorPane rootLayout;
	public static Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) {
		try {
	        
			this.primaryStage = primaryStage;
	        
	        initRootLayout();
	        
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
        	String jarDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
        	// Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            // System.out.println(DownloadManager.class.getResource("../ui/startDownload.fxml"));
            EPExperiment.isConfigScene = false ;
            loader.setLocation(EPDriver.class.getResource("../ui/startDownload.fxml"));
            rootLayout = (AnchorPane) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);     
            primaryStage.setTitle("E Profiler");
            primaryStage.show();
        }   
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	public static Stage getPrimaryStage() {
		
		return primaryStage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
