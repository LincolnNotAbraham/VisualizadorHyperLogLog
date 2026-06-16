package dao;

import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    public void registrarAcao(String acao) {
        String sql = "INSERT INTO logs_operacao (acao) VALUES (?)";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, acao);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> buscarUltimosLogs() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT acao, data_hora FROM logs_operacao ORDER BY id DESC LIMIT 10";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                logs.add(rs.getString("data_hora") + " | " + rs.getString("acao"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }
}