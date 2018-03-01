package it.pak.tech.com.controllers;

import it.pak.tech.com.core.ShowStats;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ShowStatsController {

	@FXML
	private TableView<ShowStats> statsTable;
	@FXML
	private TableColumn<ShowStats, String> appVerisonCol;
	@FXML
	private TableColumn<ShowStats, String> testcaseCol;
	@FXML
	private TableColumn<ShowStats, Integer> noOfMethodsCol;
	@FXML
	private TableColumn<ShowStats, Double> totalEnergyCol;
	@FXML
	private TableColumn<ShowStats,Integer> maxRunCol;
	
	@FXML
	private void initialize() {
		
		appVerisonCol.setCellValueFactory(cellData -> cellData.getValue().appVersionProperty());
		testcaseCol.setCellValueFactory(cellData -> cellData.getValue().testcaseProperty());
		noOfMethodsCol.setCellValueFactory(cellData -> cellData.getValue().noOfMethodsProperty().asObject());
		totalEnergyCol.setCellValueFactory(cellData -> cellData.getValue().totalEnergyProperty().asObject());
		maxRunCol.setCellValueFactory(cellData -> cellData.getValue().maxRunProperty().asObject());
		
	}
	
	public void setTableItems() {
		statsTable.setItems(EPExperiment.statsData);
	}
}
