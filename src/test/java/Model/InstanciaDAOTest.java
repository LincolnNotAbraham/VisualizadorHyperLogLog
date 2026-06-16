package Model;

import Model.HyperLogLog;
import dao.InstanciaDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class InstanciaDAOTest {

    @Test
    public void testIntegridadeSimples() {
        InstanciaDAO dao = new InstanciaDAO();
        String nome = "teste_simples_dao";

        //  instância na RAM e coloca dados
        HyperLogLog hllOriginal = new HyperLogLog(10); // 1024 registradores
        hllOriginal.inserir("Dado_A");
        hllOriginal.inserir("Dado_B");

        // RAM -> Disco
        dao.salvar(nome, 1024);
        dao.salvarEstadoNoDisco(nome, hllOriginal.getRegister());

        // Disco -> RAM
        int[] vetorDoBanco = dao.buscarEstado(nome);

        // os dois vetores tem que ser igual
        assertArrayEquals(hllOriginal.getRegister(), vetorDoBanco, "O vetor do banco veio diferente do original!");


        dao.deletar(nome);
    }
}