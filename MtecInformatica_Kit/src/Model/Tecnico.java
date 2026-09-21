// Esse arquivo faz parte do pacote model
// Ele representa um Técnico do banco de dados
// Cada atributo aqui corresponde a um campo da tabela "tecnico"
package Model;

public class Tecnico {

    // ── Atributos que correspondem aos campos da tabela tecnico ──
    private int idTECNICO;           // chave primária do técnico
    private String nome;             // nome do técnico
    private String cpf;              // CPF do técnico
    private String telefone;         // telefone/celular do técnico
    private String email;            // e-mail do técnico
    private String especialidade;    // área de especialidade do técnico
    private String status;           // status do técnico: Ativo ou Inativo
    private String observacoes;      // observações extras sobre o técnico
    private String login;            // login usado para acessar o sistema
    private String senha;            // senha usada para acessar o sistema

    // ── Getters e Setters ───────────────────────────────────────
    // Métodos para acessar e modificar os atributos de fora da classe

    public int getIdTECNICO() { return idTECNICO; }
    public void setIdTECNICO(int idTECNICO) { this.idTECNICO = idTECNICO; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
