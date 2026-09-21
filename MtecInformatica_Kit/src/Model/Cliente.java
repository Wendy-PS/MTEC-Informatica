// Esse arquivo faz parte do pacote model
// Ele representa um Cliente do banco de dados
// Cada atributo aqui corresponde a um campo da tabela "cliente"
package Model;

public class Cliente {

    // ── Atributos que correspondem aos campos da tabela cliente ──
    private int idCliente;       // chave primária do cliente
    private String nome;         // nome do cliente
    private String cpfCnpj;      // CPF ou CNPJ do cliente
    private String telefone;     // telefone/celular do cliente
    private String endereco;     // endereço do cliente

    // ── Getters e Setters ───────────────────────────────────────
    // Métodos para acessar e modificar os atributos de fora da classe

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}
