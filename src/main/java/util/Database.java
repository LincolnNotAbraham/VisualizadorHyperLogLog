package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class Database {


    private static final Dotenv dotenv = Dotenv.load();


    private static final String URL = dotenv.get("DATABASE_URL");


    private static final String USER = dotenv.get("USERDB");
    private static final String PASSWORD = dotenv.get("PASSWORDDB");


    private Database() {}

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco de dados: " + e.getMessage());
        }
    }


    public static void inicializar() {
        String sql = "CREATE TABLE IF NOT EXISTS instancias (" +
                "id SERIAL PRIMARY KEY, " +
                "nome VARCHAR(255) NOT NULL, " +
                "registradores INTEGER NOT NULL" +
                ");";

        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id SERIAL PRIMARY KEY, " +
                "nome VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL" +
                ");";

        // Tabela 3: Auditoria do Sistema (CRUD exigido)
        String sqlLogs = "CREATE TABLE IF NOT EXISTS logs_operacao (" +
                "id SERIAL PRIMARY KEY, " +
                "acao VARCHAR(255) NOT NULL, " +
                "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";


        try (java.sql.Connection conn = conectar();
             java.sql.Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlLogs);

            System.out.println("Banco inicializado: Tabela 'instancias' pronta.");

        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao inicializar o banco de dados: " + e.getMessage());
        }
    }

}