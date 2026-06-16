package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ModalHllController {

    @FXML private Button btnCancelar;
    @FXML private Label infoTamanho;
    @FXML private Button btnCriar;
    @FXML private TextField inputNome;
    @FXML private ComboBox<Integer> comboRegistradores;

    private boolean salvou = false;
    private String nomeFinal = "";
    private int registradoresFinal = 0;

    @FXML
    public void initialize() {
        comboRegistradores.getItems().addAll(
                16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536
        );

        comboRegistradores.setValue(16384);
        atualizarLabelMemoria(16384);

        comboRegistradores.valueProperty().addListener((observable, valorAntigo, valorNovo) -> {
            if (valorNovo != null) {
                atualizarLabelMemoria(valorNovo);
            }
        });
    }

    private void atualizarLabelMemoria(int m) {
        double tamanhoKb = m / 1024.0;
        infoTamanho.setText(String.format("Tamanho: %.2f KB", tamanhoKb));
    }

    @FXML
    void criarHLL(ActionEvent event) {
        if (inputNome.getText().trim().isEmpty()) {
            inputNome.setStyle("-fx-border-color: red;");
            return;
        }

        this.salvou = true;
        this.nomeFinal = inputNome.getText();
        this.registradoresFinal = comboRegistradores.getValue();
        fecharJanela();
    }

    @FXML
    void fecharModal(ActionEvent event) {
        this.salvou = false;
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) inputNome.getScene().getWindow();
        stage.close();
    }

    public boolean isSalvou() { return salvou; }
    public String getNomeFinal() { return nomeFinal; }
    public int getRegistradoresFinal() { return registradoresFinal; }
}