module com.scheduler.sjf_scheduler {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.scheduler.sjf_scheduler to javafx.fxml;
    exports com.scheduler.sjf_scheduler;
}