package com.lms.laundry;

import com.lms.laundry.manager.MyJDBC;
import com.lms.laundry.manager.P;
import com.lms.laundry.view.LaundryApplication;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        try {
            // Connect Database
            MyJDBC.getConnection();
            // Launch JavaFX Application
            Application.launch(LaundryApplication.class, args);
        } catch (Exception e) {
            P.pf("Application Startup Failed!");
        }
    }
}
