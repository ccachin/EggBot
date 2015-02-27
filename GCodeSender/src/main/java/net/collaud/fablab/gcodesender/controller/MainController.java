package net.collaud.fablab.gcodesender.controller;

import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import net.collaud.fablab.gcodesender.serial.SerialPort;
import net.collaud.fablab.gcodesender.serial.SerialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class MainController implements Initializable {

	@Autowired
	private SerialService serialService;
	
	@Autowired
	private GcodeService gcodeService;

	@Setter
	private Stage stage;

	@FXML
	private Label labelFile;

	@FXML
	private Button buttonPrint;

	@FXML
	private ComboBox<SerialPort> comboPort;
	
	@FXML
	private TextArea textLog;

	private Optional<SerialPort> selectedPort = Optional.empty();
	private Optional<File> selectedFile = Optional.empty();

	@FXML
	private void reloadPorts() {
		comboPort.setItems(FXCollections.observableArrayList(serialService.getListPorts()));
		selectedPort = Optional.empty();
		updateButtonPrint();
	}

	@FXML
	private void chooseGCodeFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open GCode file");
		addGCodeExtensionFilter(fileChooser);
		selectedFile = Optional.ofNullable(fileChooser.showOpenDialog(stage));
		selectedFile.ifPresent(f -> labelFile.setText(f.getAbsolutePath()));
		log.info("File selected : " + selectedFile.orElse(null));
		updateButtonPrint();
	}

	private void addGCodeExtensionFilter(FileChooser fileChooser) {
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Gcode file ", "*.gcode");
		fileChooser.getExtensionFilters().add(extFilter);
	}

	@FXML
	private void print() {
		log.info("Print file {} on port {}", selectedFile.get().getAbsolutePath(), selectedPort.get());
		gcodeService.sendFile(selectedFile.get(), selectedPort.get());
	}

	public void updateButtonPrint() {
		buttonPrint.setDisable(!selectedFile.isPresent() || !selectedPort.isPresent());
	}
	
	private StringBuilder logBuilder = new StringBuilder();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		reloadPorts();
		comboPort.setOnAction(event -> {
			selectedPort = Optional.ofNullable(comboPort.getSelectionModel().getSelectedItem());
			log.info("Port selected : " + selectedPort.orElse(null));
			updateButtonPrint();
		});
		gcodeService.addObserver((Observable o, Object arg) -> {
			logBuilder.append("\n");
			logBuilder.append(arg);
			textLog.setText(logBuilder.toString());
		});
	}
}
