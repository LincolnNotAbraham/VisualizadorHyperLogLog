package dao;

import util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InstanciaDAO {

    // metodo auxiliar para checar duplicidade
    public boolean existe(String nome) {
        String sql = "SELECT 1 FROM instancias WHERE nome = ?";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Se tiver resultado (next == true), o nome já existe no banco
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void salvar(String nome, int registradores) throws IllegalArgumentException {

        // Regra de Negocio
        if (existe(nome)) {
            throw new IllegalArgumentException("Operação negada: Já existe um HLL com o nome '" + nome + "'.");
        }

        String sql = "insert into instancias (nome, registradores) values (?,?)";

        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, nome);
            stmt.setInt(2, registradores);
            stmt.executeUpdate();

            System.out.println("DAO: Instância '" + nome + "' salva com sucesso!");

        } catch (java.sql.SQLException e){
            System.err.println("DAO: Erro ao salvar no banco: "+ e.getMessage());
        }
    }

    public List<String> buscarTodosNomes(){
        List<String> nomes = new ArrayList<>();
        String sql = "SELECT nome FROM instancias ORDER BY nome ASC";

        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                nomes.add(rs.getString("nome"));
            }

        } catch (java.sql.SQLException e) {
            System.err.println("DAO: Erro ao buscar nomes das instâncias." + e.getMessage());

        }
        return nomes;
    }


    public void deletar(String nome) {
        String sql = "DELETE FROM instancias WHERE nome = ?";

        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.executeUpdate();

            System.out.println("DAO: Instância " + nome + " deletada do Postgres!");

        } catch (java.sql.SQLException e) {
            System.err.println("DAO: Erro ao deletar a instância.");
            e.printStackTrace();
        }
    }


    // busca os dados reais para colocar na RAM
    public int[] buscarEstado(String nome) {
        String sql = "SELECT estado FROM instancias WHERE nome = ?";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                java.sql.Array sqlArray = rs.getArray("estado");
                if (sqlArray != null) {
                    // O JDBC devolve Integer[], precisamos converter para o seu int[] primitivo
                    Integer[] arrayInteiro = (Integer[]) sqlArray.getArray();
                    int[] primitivo = new int[arrayInteiro.length];
                    for (int i = 0; i < arrayInteiro.length; i++) {
                        primitivo[i] = arrayInteiro[i] != null ? arrayInteiro[i] : 0;
                    }
                    return primitivo;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar estado da instância: " + nome);
            e.printStackTrace();
        }
        return null;
    }

    // salva a RAM de volta no disco
    public void salvarEstadoNoDisco(String nome, int[] registradores) {
        String sql = "UPDATE instancias SET estado = ? WHERE nome = ?";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Converte int[] primitivo para Integer[] pro JDBC não chorar
            Integer[] arrayInteiro = java.util.Arrays.stream(registradores).boxed().toArray(Integer[]::new);
            java.sql.Array sqlArray = conn.createArrayOf("integer", arrayInteiro);

            pstmt.setArray(1, sqlArray);
            pstmt.setString(2, nome);
            pstmt.executeUpdate();

            System.out.println("DAO: Estado da instância " + nome + " salvo no Postgres!");
        } catch (Exception e) {
            System.err.println("Erro ao salvar estado no disco.");
            e.printStackTrace();
        }
    }


    public int buscarQtdRegistradores(String nome) {
        String sql = "SELECT registradores FROM instancias WHERE nome = ?";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("registradores");
        } catch (Exception e) {}
        return 0;
    }



    public void renomear(String nomeAntigo, String nomeNovo) {
        String sql = "UPDATE instancias SET nome = ? WHERE nome = ?";
        try (java.sql.Connection conn = util.Database.conectar();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomeNovo);
            pstmt.setString(2, nomeAntigo);
            pstmt.executeUpdate();

            System.out.println("DAO: Instância renomeada de '" + nomeAntigo + "' para '" + nomeNovo + "'");
        } catch (Exception e) {
            System.err.println("Erro ao renomear instância." + e.getMessage());

        }
    }



}
