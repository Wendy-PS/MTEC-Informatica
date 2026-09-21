// Esse arquivo faz parte do pacote model
// Ele representa um Produto/Equipamento do cliente no banco de dados
// Cada atributo aqui corresponde a um campo da tabela "produto"
package Model;

public class Produto {

    // ── Atributos que correspondem aos campos da tabela produto ──
    private int idPRODUTO;          // chave primária do produto/equipamento
    private String tipo;            // tipo do equipamento ou peça
    private String marca;           // marca do equipamento ou peça
    private String modelo;          // modelo do equipamento ou peça
    private String especificacoes;  // informações extras do equipamento
    private int clienteIdCliente;   // ID do cliente dono do equipamento

    // ── Atributo extra para exibição na tabela ───────────────────
    // Esse campo não está na tabela produto, vem do JOIN com cliente
    private String nomeCliente;     // nome do cliente dono do equipamento

    // ── Getters e Setters ───────────────────────────────────────
    // Métodos para acessar e modificar os atributos de fora da classe

    public int getIdPRODUTO() { return idPRODUTO; }
    public void setIdPRODUTO(int idPRODUTO) { this.idPRODUTO = idPRODUTO; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getEspecificacoes() { return especificacoes; }
    public void setEspecificacoes(String especificacoes) { this.especificacoes = especificacoes; }

    public int getClienteIdCliente() { return clienteIdCliente; }
    public void setClienteIdCliente(int clienteIdCliente) { this.clienteIdCliente = clienteIdCliente; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
}
