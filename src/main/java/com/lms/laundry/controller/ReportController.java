package com.lms.laundry.controller;

import com.lms.laundry.manager.ReportManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class ReportController {
    @FXML private Label lblReportTitle;
    @FXML private Label lblReportType;
    @FXML private Label lblStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TableView<ReportManager.ReportRow> tblReport;
    @FXML private TableColumn<ReportManager.ReportRow, String> colLabel;
    @FXML private TableColumn<ReportManager.ReportRow, Integer> colCount;
    @FXML private TableColumn<ReportManager.ReportRow, Double> colAmount;

    @FXML
    public void initialize() {
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpTo.setValue(LocalDate.now());
        colLabel.setCellValueFactory(new PropertyValueFactory<>("label"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colCount.getStyleClass().add("col-center");
        colAmount.getStyleClass().add("col-right");
        refreshReport();
    }

    @FXML
    public void refreshReport() {
        LocalDate from = dpFrom.getValue() != null ? dpFrom.getValue() : LocalDate.now();
        LocalDate to = dpTo.getValue() != null ? dpTo.getValue() : LocalDate.now();
        String type = lblReportType != null ? lblReportType.getText() : "daily";
        ObservableList<ReportManager.ReportRow> rows = switch (type) {
            case "trial" -> ReportManager.getTrialBalance(from, to);
            case "orders" -> ReportManager.getOrderStatusSummary(from, to);
            case "sales" -> ReportManager.getSalesSummary(from, to);
            case "tax" -> ReportManager.getTaxSummary(from, to);
            default -> ReportManager.getDailySummary(from, to);
        };
        tblReport.setItems(rows);
        if (lblStatus != null) lblStatus.setText("Showing " + rows.size() + " rows");
    }
}
