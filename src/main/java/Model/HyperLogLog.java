package Model;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

public class HyperLogLog {

    private final int[] register;
    private final int bits; // bits de precisao, quanto maior mais preciso porem mais memoria
    private final int tamanho; // calculado a partir do bits de precisao
    private final double alphaMM;

    public HyperLogLog(int b){
        this.bits = b;
        this.tamanho = 1 << b; // operação bitwise para fazer potencia de 2 a b;
        this.register = new int[tamanho];


        this.alphaMM = calcularAlphaMM(tamanho);
    }

    public void inserir (String dado){
        int hash = calcularHash(dado);

        int indice =  hash >>> (32 - bits); // desloca sem sinal, pois hash pode obter numero negativo;

        int zerosEsquerda = Integer.numberOfLeadingZeros(hash << bits) + 1;

        if (zerosEsquerda > register[indice]){
            register[indice] = zerosEsquerda;
        }
    }


    public long estimarCardinalidade(){
        double somaHarmonica = 0;

        for (int i = 0; i < tamanho; i++){
            somaHarmonica += Math.pow(2.0, -register[i]);
        }

        double estimativa = alphaMM / somaHarmonica;


        // se a estimativa for menor que 2.5 * m, calculamos com base nos espaços vazios
        if (estimativa <= 2.5 * tamanho) {
            int registradoresVazios = 0;
            for (int i = 0; i < tamanho; i++) {
                if (register[i] == 0) {
                    registradoresVazios++;
                }
            }

            // Só aplica se houver vazios para evitar divisão por zero
            if (registradoresVazios != 0) {
                estimativa = tamanho * Math.log((double) tamanho / registradoresVazios); // linear couting
            }
        }

        return Math.round(estimativa);
    }

    private double calcularAlphaMM(int m) {
        double alphaM;

        switch (m) {
            case 16:
                alphaM = 0.673;
                break;
            case 32:
                alphaM = 0.697;
                break;
            case 64:
                alphaM = 0.709;
                break;
            default:
                alphaM = 0.7213 / (1.0 + (1.079 / m));
                break;
        }

        // retorna a constante ja multiplicada por m^2
        return alphaM * m * m;
    }

    private int calcularHash(String dado) {
        // algoritmo murmur3 de 32 bits
        return Hashing.murmur3_32_fixed()
                .hashString(dado, StandardCharsets.UTF_8)
                .asInt();
    }

    // puxa o vetor da RAM para podermos mandar pro banco
    public int[] getRegister() {
        return this.register;
    }

    // injeta os dados que vieram do banco direto na RAM
    public void carregarEstadoDoBanco(int[] estadoSalvo) {
        if (estadoSalvo != null && estadoSalvo.length == this.tamanho) {
            System.arraycopy(estadoSalvo, 0, this.register, 0, this.tamanho);
        }
    }

    // Zera todos os registradores na memória
    public void zerar() {
        java.util.Arrays.fill(this.register, 0);
    }


}