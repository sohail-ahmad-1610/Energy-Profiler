package it.pak.tech.com.controllers;

import java.io.IOException;

import it.pak.tech.com.core.ShowStats;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ShowStatsController {

	@FXML
	private TableView<ShowStats> statsTable;
	@FXML
	private TableColumn<ShowStats, String> appVerisonCol;
//	@FXML
//	private TableColumn<ShowStats, String> testcaseCol;
	@FXML
	private TableColumn<ShowStats, Integer> noOfMethodsCol;
	@FXML
	private TableColumn<ShowStats, Double> totalEnergyCol;
	@FXML
	private TableColumn<ShowStats,Integer> maxRunCol;
	
	private Stage stage;
	
	@FXML
	private void initialize() {
		statsTable.setEditable(false);
		appVerisonCol.setCellValueFactory(cellData -> cellData.getValue().appVersionProperty());
		noOfMethodsCol.setCellValueFactory(cellData -> cellData.getValue().noOfMethodsProperty().asObject());
		totalEnergyCol.setCellValueFactory(cellData -> cellData.getValue().totalEnergyProperty().asObject());
		maxRunCol.setCellValueFactory(cellData -> cellData.getValue().maxRunProperty().asObject());
		
	}
	
	public void setTableItems() {
		statsTable.setItems(EPExperiment.statsData);
		statsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onClick());
	}

	private void onClick() {
		if (statsTable.getSelectionModel().getSelectedItem() != null) {
	        ShowStats selected = statsTable.getSelectionModel().getSelectedItem();
	        System.out.println("Methods: "+selected.getNoOfMethods());
	        
 	        // LOAD new Scene showing all Methods Details.
	        showStatsAction(selected);
		
		}else {
	    	System.out.println("Item not selected....\n\n");
	    }
		
	}
	
	private void showStatsAction(ShowStats selected) {
		
		System.out.println("Show Methods Stats");
		// Load root layout from fxml file.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(EPDriver.class.getResource("/it/pak/tech/com/ui/MethodsDetails.fxml"));
        AnchorPane rootLayout;
		try {
			rootLayout = (AnchorPane) loader.load();
			// Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
           
            stage = new Stage();
            stage.setTitle("Methods Details");
            stage.setScene(scene);        
            stage.show();
            stage.setOnCloseRequest(event -> {
    		    System.out.println("Stage is closing");
    		    //statsTable.setEditable(true);
    		    statsTable.getSelectionModel().clearSelection();
    		});
            MethodsDetailsController methodsDetailsController= loader.getController();
            methodsDetailsController.setTableItems(selected.getMaxRun(),selected.getAppVerison()); 
		} 
		catch (IOException e) {
			System.out.println("Cannot Load MethodsDetailsController");
			e.printStackTrace();
		}
		
	}
	@FXML
	public void exitApplication(ActionEvent event) {
	   Platform.exit();
	   
	}	
}
