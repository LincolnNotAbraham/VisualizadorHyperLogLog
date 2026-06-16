import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Carrega a fonte fisicamente da pasta resources/fonts direto para a memória do Java
        Font.loadFont(getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"), 14);

        // 2. Carrega o seu arquivo FXML (Atenção ao nome exato que está na sua pasta)
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Inicial.fxml")));

        // 3. Monta a cena e define o tamanho da janela
        Scene scene = new Scene(root, 800, 600);

        // 4. Configurações da Janela
        primaryStage.setTitle("Visualizador HLL");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Impede o usuário de quebrar o layout esticando a tela
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}