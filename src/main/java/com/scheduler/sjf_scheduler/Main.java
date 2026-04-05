package com.scheduler.sjf_scheduler;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main extends Application {

    // Process Data Model
    public static class Process {
        private final String pid;
        private final int burstTime;
        private int waitingTime;
        private int turnaroundTime;

        public Process(String pid, int burstTime) {
            this.pid = pid;
            this.burstTime = burstTime;
        }

        public String getPid() { return pid; }
        public int getBurstTime() { return burstTime; }
        public int getWaitingTime() { return waitingTime; }
        public void setWaitingTime(int waitingTime) { this.waitingTime = waitingTime; }
        public int getTurnaroundTime() { return turnaroundTime; }
        public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    }

    private TableView<Process> table = new TableView<>();
    private TextField pidInput = new TextField();
    private TextField burstInput = new TextField();
    private ObservableList<Process> masterList = FXCollections.observableArrayList();

    // NEW: Labels to display the averages
    private Label avgWaitLabel = new Label("Average Waiting Time: 0.00");
    private Label avgTurnaroundLabel = new Label("Average Turnaround Time: 0.00");

    @Override
    public void start(Stage stage) {
        stage.setTitle("SJF Scheduler (Non-Preemptive)");

        // Input Fields
        pidInput.setPromptText("Process ID (e.g. P1)");
        burstInput.setPromptText("Burst Time");
        Button addButton = new Button("Add Process");
        addButton.setOnAction(e -> addProcess());

        Button calculateButton = new Button("Run SJF Schedule");
        calculateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        calculateButton.setOnAction(e -> calculateSJF());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> {
            masterList.clear();
            table.refresh();
            // NEW: Reset labels when cleared
            avgWaitLabel.setText("Average Waiting Time: 0.00");
            avgTurnaroundLabel.setText("Average Turnaround Time: 0.00");
        });

        HBox inputGroup = new HBox(10, pidInput, burstInput, addButton, calculateButton, clearButton);
        inputGroup.setPadding(new Insets(10));

        // Table Columns
        TableColumn<Process, String> pidCol = new TableColumn<>("PID");
        pidCol.setCellValueFactory(new PropertyValueFactory<>("pid"));

        TableColumn<Process, Integer> burstCol = new TableColumn<>("Burst Time");
        burstCol.setCellValueFactory(new PropertyValueFactory<>("burstTime"));

        TableColumn<Process, Integer> waitCol = new TableColumn<>("Waiting Time");
        waitCol.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));

        TableColumn<Process, Integer> tatCol = new TableColumn<>("Turnaround Time");
        tatCol.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));

        table.getColumns().addAll(pidCol, burstCol, waitCol, tatCol);
        table.setItems(masterList);

        // NEW: Group the labels together
        HBox statsGroup = new HBox(20, avgWaitLabel, avgTurnaroundLabel);
        statsGroup.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // Make them stand out a bit

        // NEW: Add the statsGroup to the main layout
        VBox layout = new VBox(10, inputGroup, table, statsGroup);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 600, 450); // Increased height slightly to fit labels
        stage.setScene(scene);
        stage.show();
    }

    private void addProcess() {
        try {
            String id = pidInput.getText();
            int burst = Integer.parseInt(burstInput.getText());
            if (!id.isEmpty()) {
                masterList.add(new Process(id, burst));
                pidInput.clear();
                burstInput.clear();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid number for Burst Time.");
            alert.show();
        }
    }

    private void calculateSJF() {
        if (masterList.isEmpty()) return;

        // Convert to List for sorting
        List<Process> sortedList = new ArrayList<>(masterList);

        // SJF Logic: Sort by Burst Time, then FCFS (implied by stable sort)
        sortedList.sort(Comparator.comparingInt(Process::getBurstTime));

        int currentWaitingTime = 0;

        // NEW: Variables to track the totals
        double totalWaitTime = 0;
        double totalTurnaroundTime = 0;

        for (Process p : sortedList) {
            p.setWaitingTime(currentWaitingTime);
            p.setTurnaroundTime(currentWaitingTime + p.getBurstTime());

            // NEW: Add to our running totals
            totalWaitTime += p.getWaitingTime();
            totalTurnaroundTime += p.getTurnaroundTime();

            currentWaitingTime += p.getBurstTime();
        }

        // NEW: Calculate averages
        double avgWait = totalWaitTime / sortedList.size();
        double avgTurnaround = totalTurnaroundTime / sortedList.size();

        // NEW: Update the UI labels (formatting to 2 decimal places)
        avgWaitLabel.setText(String.format("Average Waiting Time: %.2f", avgWait));
        avgTurnaroundLabel.setText(String.format("Average Turnaround Time: %.2f", avgTurnaround));

        // Update the table with sorted/calculated data
        masterList.setAll(sortedList);
        table.refresh();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
