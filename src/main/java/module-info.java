module com.lms.laundry {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires mysql.connector.j;
    requires com.zaxxer.hikari;
    requires java.prefs;

    exports com.lms.laundry;
    opens com.lms.laundry to javafx.fxml;
    exports com.lms.laundry.controller;
    opens com.lms.laundry.controller to javafx.fxml;
    exports com.lms.laundry.manager;
    opens com.lms.laundry.manager to javafx.fxml;
    exports com.lms.laundry.model;
    opens com.lms.laundry.model to javafx.fxml;
    exports com.lms.laundry.view;
    opens com.lms.laundry.view to javafx.fxml;
}