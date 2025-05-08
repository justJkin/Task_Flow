module com.mycompany.taskflow {
    requires javafx.controls;  // JavaFX
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires java.desktop;
    requires jbcrypt;
    requires javafx.graphics;
    exports com.mycompany.taskflow;
    exports com.mycompany.taskflow.controller;
    exports com.mycompany.taskflow.model;
    opens com.mycompany.taskflow.controller to javafx.fxml;
    exports com.mycompany.taskflow.controller.Admin;
    opens com.mycompany.taskflow.controller.Admin to javafx.fxml;
    exports com.mycompany.taskflow.model.user;
    exports com.mycompany.taskflow.model.Manager;
    exports com.mycompany.taskflow.model.Admin;
    exports com.mycompany.taskflow.controller.Manager;

    opens com.mycompany.taskflow.controller.Manager to javafx.fxml;

    opens com.mycompany.taskflow to javafx.fxml;
    opens com.mycompany.taskflow.controller.user to javafx.fxml;
    exports com.mycompany.taskflow.controller.user;

    opens com.mycompany.taskflow.model to javafx.base;
    opens com.mycompany.taskflow.model.Manager to javafx.base;


}
