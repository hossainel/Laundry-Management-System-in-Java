package com.lms.laundry.controller;

import com.lms.laundry.manager.OrderManager;
import com.lms.laundry.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class OrderCalenderController {
    @FXML private DatePicker dpMonth;
    @FXML private GridPane calendarGrid;
    @FXML private Label lblReceived;
    @FXML private Label lblWashing;
    @FXML private Label lblReady;
    @FXML private Label lblDelivered;
    @FXML private Label lblStatus;

    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dpMonth.setValue(LocalDate.now().withDayOfMonth(1));
        refreshCalendar();
    }

    @FXML
    public void refreshCalendar() {
        allOrders.setAll(OrderManager.getAll());
        renderCalendar();
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i]);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            label.getStyleClass().add("calendar-header");
            calendarGrid.add(label, i, 0);
        }

        YearMonth month = YearMonth.from(dpMonth.getValue() != null ? dpMonth.getValue() : LocalDate.now());
        LocalDate firstDay = month.atDay(1);
        int startColumn = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = month.lengthOfMonth();
        int received = 0;
        int washing = 0;
        int ready = 0;
        int delivered = 0;
        int shown = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            List<Order> dayOrders = allOrders.stream()
                    .filter(order -> date.equals(order.getDeliveryDate()))
                    .toList();
            VBox cell = new VBox(5);
            cell.setPadding(new Insets(8));
            cell.setMinHeight(92);
            cell.getStyleClass().add("calendar-cell");
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("calendar-day");
            cell.getChildren().add(dayLabel);
            for (Order order : dayOrders) {
                Label orderLabel = new Label("#" + order.getId() + " " + order.getCustomerName() + " " + order.getStatus());
                orderLabel.getStyleClass().add("calendar-order");
                orderLabel.setMaxWidth(Double.MAX_VALUE);
                cell.getChildren().add(orderLabel);
                shown++;
                switch (order.getStatus()) {
                    case RECEIVED -> received++;
                    case WASHING -> washing++;
                    case READY -> ready++;
                    case DELIVERED -> delivered++;
                }
            }
            int index = startColumn + day - 1;
            calendarGrid.add(cell, index % 7, (index / 7) + 1);
        }

        lblReceived.setText(String.valueOf(received));
        lblWashing.setText(String.valueOf(washing));
        lblReady.setText(String.valueOf(ready));
        lblDelivered.setText(String.valueOf(delivered));
        lblStatus.setText("Showing " + shown + " scheduled orders in " + month);
    }
}
