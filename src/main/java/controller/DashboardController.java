package controller;

import Model.HyperLogLog;
import dao.InstanciaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    @FXML
    private MenuItem menuSair;

    @FXML
    private MenuItem novoHLL;

    @FXML
    private ListView<String> listaHll;

    private final ObservableList<String> obsNomes = FXCollections.observableArrayList();

    @FXML private Button btnEstimar;
    @FXML private Label lblResultado;

    @FXML private Label lblNomePainel;
    @FXML private Label lblStatus;
    @FXML private Label lblInfo;

    @FXML private Button btnZerar;

    @FXML private TilePane gridRegistradores;
    @FXML private TextField inputDado;
    @FXML private Button btnAdicionar;
    @FXML private Button btnMassa;

    // RAM
    private Map<String, HyperLogLog> ram = new HashMap<>();

    @FXML
    public void initialize() {
        listaHll.setItems(obsNomes);
        atualizarLista();

        javafx.scene.control.ContextMenu menuDireito = new javafx.scene.control.ContextMenu();

        //opções do menu
        javafx.scene.control.MenuItem btnInicializar = new javafx.scene.control.MenuItem("Inicializar (Disco -> RAM)");
        javafx.scene.control.MenuItem btnSalvar = new javafx.scene.control.MenuItem("Sincronizar (RAM -> Disco)");
        javafx.scene.control.MenuItem btnRenomear = new javafx.scene.control.MenuItem("Renomear"); // <-- AQUI
        javafx.scene.control.MenuItem btnDeletar = new javafx.scene.control.MenuItem("Deletar");

        // Inicializar
        btnInicializar.setOnAction(event -> {
            String selecionado = listaHll.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                inicializarHllNaMemoria(selecionado);
            }
        });

        // Sincronizar (Salvar no Postgres)
        btnSalvar.setOnAction(event -> {
            String selecionado = listaHll.getSelectionModel().getSelectedItem();
            if (selecionado != null && ram.containsKey(selecionado)) {
                InstanciaDAO dao = new InstanciaDAO();
                // Puxa o vetor do HLL que está na RAM e manda pro DAO salvar
                dao.salvarEstadoNoDisco(selecionado, ram.get(selecionado).getRegister());
                new dao.LogDAO().registrarAcao("Sincronizado Instância: " + selecionado);
            }
        });

        // Deletar
        btnDeletar.setOnAction(event -> {
            String selecionado = listaHll.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                deletarHll(selecionado);
            }
        });

        // Renomear
        btnRenomear.setOnAction(event -> {
            String selecionado = listaHll.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                // janela popup para digitar o novo nome
                TextInputDialog dialog = new TextInputDialog(selecionado);
                dialog.setTitle("Renomear");
                dialog.setHeaderText("Renomear Instância: " + selecionado);
                dialog.setContentText("Digite o novo nome:");

                dialog.showAndWait().ifPresent(novoNome -> {
                    novoNome = novoNome.trim();
                    if (!novoNome.isEmpty() && !novoNome.equals(selecionado)) {

                        // dao
                        InstanciaDAO dao = new InstanciaDAO();
                        dao.renomear(selecionado, novoNome);

                        // atualiza na ram
                        if (ram.containsKey(selecionado)) {
                            Model.HyperLogLog hllVivo = ram.remove(selecionado);
                            ram.put(novoNome, hllVivo);
                        }

                        //  lista lateral para mostrar o novo nome
                        atualizarLista();
                        new dao.LogDAO().registrarAcao("Instância renomeada de '" + selecionado + "' para '" + novoNome + "'");

                        // seleciona
                        listaHll.getSelectionModel().select(novoNome);
                    }
                });
            }
        });



        //tres botoes no menu
        menuDireito.getItems().addAll(btnInicializar, btnSalvar, btnDeletar, btnRenomear);

        listaHll.setContextMenu(menuDireito);

        // RADAR DE CLIQUE
        listaHll.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado != null) {
                atualizarPainelCentral(selecionado);
            }
        });

        //  O BOTÃO DE ADICIONAR DADO NA TELA CENTRAL
        btnAdicionar.setOnAction(event -> {
            String nomeAtivo = listaHll.getSelectionModel().getSelectedItem();
            String dadoTexto = inputDado.getText();

            // Só adiciona se tiver texto, se houver alguem selecionado e se esse alguem estiver na RAM
            if (nomeAtivo != null && ram.containsKey(nomeAtivo) && !dadoTexto.isEmpty()) {

                HyperLogLog hllVivo = ram.get(nomeAtivo);
                hllVivo.inserir(dadoTexto);

                inputDado.clear();

                System.out.println("Dado '" + dadoTexto + "' inserido na RAM da instância: " + nomeAtivo);

                desenharMapaDeCalor(hllVivo);
            }
        });
    }

    //  TELA CENTRAL
    private void atualizarPainelCentral(String nomeInstancia) {
        lblResultado.setText(""); // Limpa o resultado anterior
        lblNomePainel.setText("Instância: " + nomeInstancia);

        // Verifica PRIMEIRO se esse HLL esta dentro da RASM
        if (ram != null && ram.containsKey(nomeInstancia) && ram.get(nomeInstancia) != null) {

            // SE ESTIVER NA RAM
            lblStatus.setText("Status: ATIVO NA RAM");
            lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            inputDado.setDisable(false);
            btnAdicionar.setDisable(false);

            // tamanho
            Model.HyperLogLog hllVivo = ram.get(nomeInstancia);
            int quantidadeRegistradores = hllVivo.getRegister().length;

            //  int = 4 bytes.
            double tamanhoEmBytes = quantidadeRegistradores * 4.0;
            double tamanhoKb = tamanhoEmBytes / 1024.0;
            lblInfo.setText(String.format("Tamanho: %.2f KB", tamanhoKb));

            // mapa de calor
            desenharMapaDeCalor(hllVivo);

        } else {
            // SE NÃO ESTIVER NA RAM
            lblStatus.setText("Status: DESATIVADO (Apenas no Banco)");
            lblStatus.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            lblInfo.setText("Tamanho: -");

            inputDado.setDisable(true);
            btnAdicionar.setDisable(true);
            gridRegistradores.getChildren().clear();
        }
    }


    @FXML
    void calcularEstimativa(javafx.event.ActionEvent event) {
        String nomeAtivo = listaHll.getSelectionModel().getSelectedItem();

        if (nomeAtivo != null && ram.containsKey(nomeAtivo)) {
            // puxa o objeto da RAM
            Model.HyperLogLog hllVivo = ram.get(nomeAtivo);

            // roda a matemática
            long totalUnicos = hllVivo.estimarCardinalidade();


            lblResultado.setText(String.format("Estimativa: %,d itens únicos", totalUnicos));
            System.out.println("Cardinalidade de " + nomeAtivo + " estimada em: " + totalUnicos);
        }
    }




    @FXML
    void inserirTxtEmMassa(javafx.event.ActionEvent event) {
        String nomeAtivo = listaHll.getSelectionModel().getSelectedItem();

        if (nomeAtivo != null && ram.containsKey(nomeAtivo)) {

            // chama o explorador de arquivos do SO
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Selecione o arquivo TXT com os dados");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt")
            );

            Stage stage = (Stage) btnMassa.getScene().getWindow();
            java.io.File arquivoSelecionado = fileChooser.showOpenDialog(stage);

            if (arquivoSelecionado != null) {
                Model.HyperLogLog hllVivo = ram.get(nomeAtivo);
                int linhasLidas = 0;

                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(arquivoSelecionado))) {
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        if (!linha.trim().isEmpty()) {
                            hllVivo.inserir(linha.trim());
                            linhasLidas++;
                        }
                    }

                    System.out.println(linhasLidas + " itens inseridos na instância: " + nomeAtivo);
                    new dao.LogDAO().registrarAcao("Carga em massa executada. Instância: " + nomeAtivo);
                    desenharMapaDeCalor(hllVivo);

                } catch (java.io.IOException ex) {
                    System.out.println("Erro ao ler o arquivo: " + ex.getMessage());
                }
            }
        }
    }


    private void inicializarHllNaMemoria(String nomeInstancia) {
        InstanciaDAO dao = new InstanciaDAO();

        // descobre o tamanho (m) salvo
        int m = dao.buscarQtdRegistradores(nomeInstancia);
        if (m == 0) {
            System.out.println("Erro: Não encontrou os registradores para " + nomeInstancia);
            return;
        }

        // calcula os bits (b) para o seu construtor HLL (matemática de logaritmo: b = log2(m)) m=2^b
        int b = (int) (Math.log(m) / Math.log(2));

        // coloca na ram
        HyperLogLog hll = new HyperLogLog(b);

        // puxa os dados antigos do banco
        int[] estadoSalvo = dao.buscarEstado(nomeInstancia);
        if (estadoSalvo != null) {
            hll.carregarEstadoDoBanco(estadoSalvo);
        }

        // guarda na ram
        ram.put(nomeInstancia, hll);
        System.out.println("Subiu para a RAM com sucesso: " + nomeInstancia);

        // atualiza se ja estava na tela
        if (nomeInstancia.equals(listaHll.getSelectionModel().getSelectedItem())) {
            atualizarPainelCentral(nomeInstancia);
        }
    }


    @FXML
    void zerarHll(javafx.event.ActionEvent event) {
        String nomeAtivo = listaHll.getSelectionModel().getSelectedItem();

        if (nomeAtivo != null && ram.containsKey(nomeAtivo)) {
            // puxa o HLL da RAM
            Model.HyperLogLog hllVivo = ram.get(nomeAtivo);

            // zera a matriz de bits na RAM
            hllVivo.zerar();


           // dao.InstanciaDAO dao = new dao.InstanciaDAO();
          //  dao.salvarEstadoNoDisco(nomeAtivo, hllVivo.getRegister());

            // atualiza o visual
            desenharMapaDeCalor(hllVivo);
            lblResultado.setText("");

            System.out.println("Instância '" + nomeAtivo + "' zerada com sucesso!");
           // new dao.LogDAO().registrarAcao("Memória zerada para a instância: " + nomeAtivo);
        }
    }

    private void deletarHll(String nomeInstancia) {
        InstanciaDAO dao = new InstanciaDAO();
        dao.deletar(nomeInstancia);

        new dao.LogDAO().registrarAcao("Instância "+ nomeInstancia + " Deletada");
        // TIRA DA RAM
        ram.remove(nomeInstancia);

        atualizarLista();

        // Se ele deletou o item que estava olhando agora limpa o painel central
        lblNomePainel.setText("Nenhuma instância selecionada");
        lblStatus.setText("");
        inputDado.setDisable(true);
        btnAdicionar.setDisable(true);
        gridRegistradores.getChildren().clear();
    }

    private void atualizarLista() {
        InstanciaDAO dao = new InstanciaDAO();
        obsNomes.clear();
        obsNomes.addAll(dao.buscarTodosNomes());
    }

    @FXML
    public void abrirModalNovoHll() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModalHll.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle("Criar Nova Instância");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();

            ModalHllController modalController = loader.getController();

            if (modalController.isSalvou()) {
                String nome = modalController.getNomeFinal();
                int registradores = modalController.getRegistradoresFinal();

                InstanciaDAO dao = new InstanciaDAO();
                dao.salvar(nome, registradores);

                System.out.println("Sucesso! Nome: " + modalController.getNomeFinal() );
                new dao.LogDAO().registrarAcao("Instância "+ modalController.getNomeFinal() + " Criada");

                atualizarLista();

            } else {
                System.out.println("Usuário cancelou a criação.");
            }

        } catch (IllegalArgumentException e) {

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Conflito de Nomes");
            alert.setHeaderText("Regra de Negócio Violada");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        catch (IOException e) {
            System.out.println("Erro ao abrir o FXML do Modal.");
        }
    }




    @FXML
    void abrirTelaUsuarios(javafx.event.ActionEvent event) {
        dao.UsuarioDAO daoUser = new dao.UsuarioDAO();
        java.util.List<String> usuarios = daoUser.buscarTodos();


        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gestão de Usuários");
        alert.setHeaderText("Usuários com acesso ao LincolnDB");

        // cria uma caixa de texto rolável
        TextArea textArea = new TextArea(String.join("\n", usuarios));
        textArea.setEditable(false);
        alert.getDialogPane().setContent(textArea);

        // A botão de "Cadastrar"
        ButtonType btnAdicionar = new ButtonType("Cadastrar Novo");
        alert.getButtonTypes().add(btnAdicionar);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnAdicionar) {
                // clicar em cadastrar = abre um popup pedindo os dados
                TextInputDialog dialog = new TextInputDialog("Nome, email@exemplo.com");
                dialog.setTitle("Novo Usuário");
                dialog.setHeaderText("Cadastro de Administrador");
                dialog.setContentText("Digite Nome e Email (separados por vírgula):");

                dialog.showAndWait().ifPresent(dados -> {
                    String[] partes = dados.split(",");
                    if (partes.length == 2) {
                        daoUser.salvar(partes[0].trim(), partes[1].trim());


                        new dao.LogDAO().registrarAcao("Novo usuário cadastrado: " + partes[0].trim());
                        System.out.println("Usuário salvo com sucesso!");
                    }
                });
            }
        });
    }

    @FXML
    void abrirTelaLogs(javafx.event.ActionEvent event) {
        dao.LogDAO daoLog = new dao.LogDAO();
        java.util.List<String> logs = daoLog.buscarUltimosLogs();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Auditoria do Sistema");
        alert.setHeaderText("Histórico de Operações (Últimas ações)");

        TextArea textArea = new TextArea(String.join("\n", logs));
        textArea.setEditable(false);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(400);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }





    //  MAPA DE CALOR
    private void desenharMapaDeCalor(HyperLogLog hll) {

        gridRegistradores.getChildren().clear();

        int[] registradores = hll.getRegister();

        // quadrado pequeno para caber na tela
        double tamanhoQuadrado = 4.0;

        for (int valor : registradores) {
            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(tamanhoQuadrado, tamanhoQuadrado);

            if (valor == 0) {
                // Vazio:
                rect.setFill(javafx.scene.paint.Color.web("#2b2b2b"));
            } else {

                // Hue 240 = Azul (frio/valores baixos). Hue 0 = Vermelho (quente/valores altos).
                double calcValor = Math.min(valor, 32.0);
                double hue = 240.0 - (calcValor * (240.0 / 32.0));

                rect.setFill(javafx.scene.paint.Color.hsb(hue, 1.0, 1.0));
            }


            rect.setStroke(javafx.scene.paint.Color.web("#1a1a1a"));
            rect.setStrokeWidth(0.5);

            gridRegistradores.getChildren().add(rect);
        }
    }




    @FXML
    void sairBtn(ActionEvent event) {
        System.exit(0);
    }
}