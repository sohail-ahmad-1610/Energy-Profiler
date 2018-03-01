package it.pak.tech.com.controllers;


import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
	
	/**b
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
           // System.out.println(DownloadManager.class.getResource("../ui/startDownload.fxml"));
            EPExperiment.isConfigScene = true ;
            loader.setLocation(EPDriver.class.getResource("../ui/MainConfigurationScene.fxml"));
            rootLayout = (AnchorPane) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	public static Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return primaryStage;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);

	}

}
