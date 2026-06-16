package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class InicialController {

    @FXML
    private Button btnEntrar;

    @FXML
    private Button btnSair;

    @FXML
    private Button btnTema;

    @FXML
    private AnchorPane rootPane;


    private Boolean isDarkMode = true;

    @FXML
    public void trocarTema(){
        rootPane.getStylesheets().clear();
        if(isDarkMode){
            String lightCss = Objects.requireNonNull(getClass().getResource("/light.css")).toExternalForm();
            rootPane.getStylesheets().add(lightCss);

            btnTema.setText("🌙 Dark Mode");
            isDarkMode = false;
        } else {

            String darkCss = Objects.requireNonNull(getClass().getResource("/dark.css")).toExternalForm();
            rootPane.getStylesheets().add(darkCss);

            btnTema.setText("☀️ Light Mode");
            isDarkMode = true;
        }
    }

    @FXML
    public void entrarDashboard() {
        try {

            Parent dashboardRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));


            Stage stage = (Stage) btnEntrar.getScene().getWindow();


            Scene dashboardScene = new Scene(dashboardRoot, 1024, 768);


            stage.setResizable(true);


            stage.setScene(dashboardScene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar o arquivo Dashboard.fxml. Verifique o caminho!");
        }
    }

    @FXML
    public void fecharAplicativo() {
        System.exit(0);
    }



}
