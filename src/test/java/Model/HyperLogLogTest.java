package Model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HyperLogLogTest {

    @Test
    public void testEstimativaBaixaCardinalidade() {
        // Testa o "Linear Counting" (correção para poucos dados)
        HyperLogLog hll = new HyperLogLog(14); // 16.384 registradores

        for (int i = 0; i < 100; i++) {
            hll.inserir("item_teste_" + i);
        }

        long estimativa = hll.estimarCardinalidade();

        // Para 100 itens, a estimativa tem que ser praticamente exata
        assertTrue(estimativa >= 98 && estimativa <= 102,
                "Falha na estimativa baixa. Esperado: ~100. Resultado: " + estimativa);
    }

    @Test
    public void testEstimativaAltaCardinalidade() {
        // Testa a matemática pesada (Média Harmônica)
        HyperLogLog hll = new HyperLogLog(14);
        int totalInserido = 100000;

        for (int i = 0; i < totalInserido; i++) {
            hll.inserir(java.util.UUID.randomUUID().toString());
        }

        long estimativa = hll.estimarCardinalidade();

        // margem de erro de 1.5%
        long margem = (long) (totalInserido * 0.015);

        assertTrue(estimativa >= (totalInserido - margem) && estimativa <= (totalInserido + margem),
                "Estimativa fora da margem aceitável. Esperado: ~100000. Resultado: " + estimativa);
    }

    @Test
    public void testZerarInstancia() {

        HyperLogLog hll = new HyperLogLog(10); // 1024 registradores

        hll.inserir("dado_aleatorio_1");
        hll.inserir("dado_aleatorio_2");

        // Limpa a estrutura
        hll.zerar();

        // Verifica se todos os índices do array realmente voltaram para 0
        int[] registradores = hll.getRegister();
        for (int valor : registradores) {
            assertEquals(0, valor, "A matriz não foi zerada corretamente. Encontrado valor: " + valor);
        }

        // A estimativa de um HLL vazio deve ser obrigatoriamente 0
        assertEquals(0, hll.estimarCardinalidade(), "A estimativa de um HLL zerado deveria ser 0");
    }



}