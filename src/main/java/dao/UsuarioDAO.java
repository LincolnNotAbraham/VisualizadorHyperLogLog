package dao;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void salvar(String nome, String email) {
        String sql = "INSERT INTO usuarios (nome, email) VALUES (?, ?)";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            System.out.println("DAO: Usuário cadastrado!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> buscarTodos() {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT nome, email FROM usuarios";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(rs.getString("nome") + " - " + rs.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}