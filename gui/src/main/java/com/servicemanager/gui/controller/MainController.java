package com.servicemanager.gui.controller;

import com.servicemanager.gui.exception.ConfigLoadException;
import com.servicemanager.gui.exception.DuplicateServiceException;
import com.servicemanager.gui.model.Service;
import com.servicemanager.gui.service.ServiceManager;
import com.servicemanager.gui.service.ServiceObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class MainController implements ServiceObserver {

    @FXML private TableView<Service> serviceTable;
    @FXML private TableColumn<Service, String> nameColumn;
    @FXML private TableColumn<Service, String> statusColumn;
    @FXML private TableColumn<Service, String> commandColumn;
    @FXML private TextField nameField;
    @FXML private TextField commandField;
    @FXML private TextField windowsCommandField;
    @FXML private TextField workingDirField;
    @FXML private TextArea outputArea;
    @FXML private VBox mainView;
    @FXML private VBox docPane;
    @FXML private WebView docWebView;

    private ServiceManager serviceManager;
    private final ObservableList<Service> serviceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        commandColumn.setCellValueFactory(new PropertyValueFactory<>("command"));

        try {
            serviceManager = new ServiceManager();
            serviceManager.addObserver(this);
            refreshTable();
            appendOutput("Service Manager ready. Loaded " + serviceList.size() + " services.");
        } catch (ConfigLoadException e) {
            appendOutput("ERROR: " + e.getMessage());
        }

        serviceTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                nameField.setText(sel.getName());
                commandField.setText(sel.getCommand());
                windowsCommandField.setText(sel.getWindowsCommand());
                workingDirField.setText(sel.getWorkingDir());
            }
        });
    }

    @Override
    public void onServiceEvent(String eventType, String serviceName, String message) {
        Platform.runLater(() -> {
            appendOutput("[" + eventType + "] " + serviceName + " - " + message);
            refreshTable();
        });
    }

    @FXML
    private void handleStart() {
        Service selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendOutput("Select a service first.");
            return;
        }
        String result = callManager(() -> serviceManager.startService(selected.getName()));
        appendOutput(result);
        refreshTable();
    }

    @FXML
    private void handleStop() {
        Service selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendOutput("Select a service first.");
            return;
        }
        String result = callManager(() -> serviceManager.stopService(selected.getName()));
        appendOutput(result);
        refreshTable();
    }

    @FXML
    private void handleRestart() {
        Service selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendOutput("Select a service first.");
            return;
        }
        String result = callManager(() -> serviceManager.restartService(selected.getName()));
        appendOutput(result);
        refreshTable();
    }

    @FXML
    private void handleStatus() {
        Service selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendOutput("Select a service first.");
            return;
        }
        String result = callManager(() -> serviceManager.getServiceStatus(selected.getName()));
        appendOutput(result);
    }

    @FXML
    private void handleLogs() {
        Service selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendOutput("Select a service first.");
            return;
        }
        String result = callManager(() -> serviceManager.getServiceLogs(selected.getName(), 50));
        appendOutput(result);
        refreshTable();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleVideoTutorial() {
        try {
            Desktop.getDesktop().browse(
                    URI.create("https://www.youtube.com/watch?v=PLACEHOLDER"));
        } catch (Exception e) {
            appendOutput("Could not open browser: " + e.getMessage());
        }
    }

    @FXML
    private void handleDocumentation() {
        try {
            File readme = new File("../README.md");
            if (!readme.exists()) {
                readme = new File("README.md");
            }
            String md = new String(java.nio.file.Files.readAllBytes(readme.toPath()));
            docWebView.getEngine().loadContent(markdownToHtml(md));
        } catch (Exception e) {
            docWebView.getEngine().loadContent(
                    "<html><body style='font-family:Segoe UI;padding:20;color:#333;'>" +
                            "<h2>Could not load README.md</h2><p>" + e.getMessage() + "</p></body></html>");
        }
        docPane.setVisible(true);
        docPane.setManaged(true);
        mainView.setVisible(false);
        mainView.setManaged(false);
    }

    private String markdownToHtml(String md) {
        StringBuilder html = new StringBuilder();
        html.append("""
            <html>
            <head>
            <meta charset="utf-8">
            <style>
                body { font-family: 'Segoe UI', sans-serif; font-size: 14px;
                       color: #333; padding: 30px; max-width: 800px; margin: 0 auto;
                       background: #f4f6f8; }
                h1 { color: #2563eb; border-bottom: 2px solid #2563eb;
                     padding-bottom: 8px; margin-top: 0; }
                h2 { color: #1d4ed8; margin-top: 28px; }
                h3 { color: #1e40af; margin-top: 20px; }
                code { background: #e2e8f0; padding: 2px 6px; border-radius: 4px;
                       font-family: 'Consolas', monospace; font-size: 13px; }
                pre { background: #e2e8f0; padding: 14px; border-radius: 8px;
                      overflow-x: auto; }
                pre code { background: transparent; padding: 0; }
                a { color: #2563eb; text-decoration: none; }
                a:hover { text-decoration: underline; }
                hr { border: none; border-top: 1px solid #d1d5db; margin: 20px 0; }
                ul { padding-left: 24px; }
                li { margin-bottom: 4px; }
                p { line-height: 1.6; }
                table { border-collapse: collapse; width: 100%; margin: 12px 0; }
                th, td { border: 1px solid #d1d5db; padding: 8px 12px; text-align: left; }
                th { background: #2563eb; color: white; }
            </style>
            </head>
            <body>
            """);

        String[] lines = md.split("\n", -1);
        boolean inCodeBlock = false;
        StringBuilder codeBlock = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    html.append("<pre><code>")
                            .append(escapeHtml(codeBlock.toString()))
                            .append("</code></pre>\n");
                    codeBlock.setLength(0);
                    inCodeBlock = false;
                } else {
                    inCodeBlock = true;
                }
                continue;
            }

            if (inCodeBlock) {
                if (codeBlock.length() > 0) codeBlock.append("\n");
                codeBlock.append(line);
                continue;
            }

            if (inList && !line.trim().startsWith("- ") && !line.trim().startsWith("* ")
                    && !line.trim().isEmpty()) {
                html.append("</ul>\n");
                inList = false;
            }

            if (line.trim().isEmpty()) {
                if (inList) {
                    html.append("</ul>\n");
                    inList = false;
                }
                continue;
            }

            String trimmed = line.trim();

            if (trimmed.startsWith("# ")) {
                html.append("<h1>").append(renderInline(trimmed.substring(2).trim())).append("</h1>\n");
            } else if (trimmed.startsWith("## ")) {
                html.append("<h2>").append(renderInline(trimmed.substring(3).trim())).append("</h2>\n");
            } else if (trimmed.startsWith("### ")) {
                html.append("<h3>").append(renderInline(trimmed.substring(4).trim())).append("</h3>\n");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                if (!inList) {
                    html.append("<ul>\n");
                    inList = true;
                }
                html.append("<li>").append(renderInline(trimmed.substring(2).trim())).append("</li>\n");
            } else if (trimmed.startsWith("---") || trimmed.startsWith("***")) {
                html.append("<hr>\n");
            } else if (trimmed.matches("^\\d+\\.\\s.*")) {
                String content = trimmed.replaceFirst("^\\d+\\.\\s+", "");
                html.append("<p><b>").append(renderInline(content)).append("</b></p>\n");
            } else {
                html.append("<p>").append(renderInline(line.trim())).append("</p>\n");
            }
        }

        if (inCodeBlock) {
            html.append("<pre><code>")
                    .append(escapeHtml(codeBlock.toString()))
                    .append("</code></pre>\n");
        }
        if (inList) {
            html.append("</ul>\n");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String renderInline(String text) {
        text = escapeHtml(text);
        text = text.replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "<a href=\"$2\">$1</a>");
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
        text = text.replaceAll("`([^`]+)`", "<code>$1</code>");
        return text;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    @FXML
    private void handleBackToMain() {
        docPane.setVisible(false);
        docPane.setManaged(false);
        mainView.setVisible(true);
        mainView.setManaged(true);
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
        appendOutput("Service list refreshed.");
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();
        String command = commandField.getText().trim();
        String windowsCommand = windowsCommandField.getText() == null
                ? ""
                : windowsCommandField.getText().trim();
        String workingDir = workingDirField.getText().trim();

        if (name.isEmpty() || command.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please enter both a service name and command.");
            return;
        }

        try {
            serviceManager.createCustomService(name, command,
                    windowsCommand.isEmpty() ? null : windowsCommand, workingDir);
            appendOutput("Service '" + name + "' created.");
            showAlert(Alert.AlertType.INFORMATION, "Service Created",
                    "The service '" + name + "' has been added.");
            nameField.clear();
            commandField.clear();
            windowsCommandField.clear();
            workingDirField.clear();
            refreshTable();
        } catch (DuplicateServiceException e) {
            appendOutput("ERROR: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Duplicate Service", e.getMessage());
        } catch (IllegalArgumentException e) {
            appendOutput("ERROR: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        } catch (Exception e) {
            appendOutput("ERROR: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Create Failed", e.getMessage());
        }
    }

    @FXML
    private void handleSaveChanges() {
        Service selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No Service Selected",
                    "Please select a service before saving changes.");
            return;
        }

        String name = selected.getName();
        String command = commandField.getText().trim();
        String windowsCommand = windowsCommandField.getText() == null
                ? ""
                : windowsCommandField.getText().trim();
        String workingDir = workingDirField.getText().trim();

        if (command.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Command cannot be empty.");
            return;
        }

        try {
            serviceManager.updateService(name, command,
                    windowsCommand.isEmpty() ? null : windowsCommand, workingDir);
            appendOutput("Service '" + name + "' updated.");
            showAlert(Alert.AlertType.INFORMATION, "Service Updated",
                    "The service '" + name + "' has been updated.");
            refreshTable();
        } catch (Exception e) {
            appendOutput("ERROR: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage());
        }
    }

    private void refreshTable() {
        if (serviceManager == null) return;
        serviceList.setAll(serviceManager.getServices());
        serviceTable.setItems(serviceList);
    }

    void setServiceManagerForTest(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    private void appendOutput(String text) {
        if (text == null || text.isEmpty()) return;
        outputArea.appendText(text + "\n");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String callManager(ManagerCall call) {
        try {
            return call.execute();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @FunctionalInterface
    private interface ManagerCall {
        String execute() throws Exception;
    }
}