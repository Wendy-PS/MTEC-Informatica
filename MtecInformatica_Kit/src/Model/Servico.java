// Esse arquivo faz parte do pacote model
// Essa classe representa uma ordem de servico do banco de dados
// Cada atributo aqui corresponde a um campo da tabela "servico"
package Model;

import java.math.BigDecimal;
import java.sql.Date;

public class Servico {

    // -- Atributos que correspondem aos campos da tabela servico --
    private int    idSERVICO;          // chave primária da OS
    private Date   dataAbertura;       // data em que a OS foi aberta
    private Date   prazoEntrega;       // prazo para entregar o serviço
    private Date   dataConclusao;      // data em que o serviço foi concluído
    private String statusOs;           // status atual da OS
    private BigDecimal valorServico;   // valor final vindo do orcamento aprovado
    private String origemServico;      // Manual ou Orcamento
    private int    clienteIdCliente;   // ID do cliente dono da OS
    private int    tecnicoIdTecnico;   // ID do técnico responsável
    private int    produtoIdProduto;   // ID do equipamento vinculado à OS

    // -- Atributos extras para exibição na tabela ------------------
    // Esses não estão na tabela servico, mas vêm do JOIN com cliente e produto
    private String nomeCliente;     // nome do cliente (vem da tabela cliente)
    private String nomeEquipamento; // equipamento (vem da tabela produto)

    // -- Getters e Setters -----------------------------------------
    // Métodos para acessar e modificar os atributos de fora da classe

    public int getIdSERVICO() { return idSERVICO; }
    public void setIdSERVICO(int idSERVICO) { this.idSERVICO = idSERVICO; }

    public Date getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(Date dataAbertura) { this.dataAbertura = dataAbertura; }

    public Date getPrazoEntrega() { return prazoEntrega; }
    public void setPrazoEntrega(Date prazoEntrega) { this.prazoEntrega = prazoEntrega; }

    public Date getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(Date dataConclusao) { this.dataConclusao = dataConclusao; }

    public String getStatusOs() { return statusOs; }
    public void setStatusOs(String statusOs) { this.statusOs = statusOs; }

    public BigDecimal getValorServico() { return valorServico; }
    public void setValorServico(BigDecimal valorServico) { this.valorServico = valorServico; }

    public String getOrigemServico() { return origemServico; }
    public void setOrigemServico(String origemServico) { this.origemServico = origemServico; }

    public int getClienteIdCliente() { return clienteIdCliente; }
    public void setClienteIdCliente(int clienteIdCliente) { this.clienteIdCliente = clienteIdCliente; }

    public int getTecnicoIdTecnico() { return tecnicoIdTecnico; }
    public void setTecnicoIdTecnico(int tecnicoIdTecnico) { this.tecnicoIdTecnico = tecnicoIdTecnico; }

    public int getProdutoIdProduto() { return produtoIdProduto; }
    public void setProdutoIdProduto(int produtoIdProduto) { this.produtoIdProduto = produtoIdProduto; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getNomeEquipamento() { return nomeEquipamento; }
    public void setNomeEquipamento(String nomeEquipamento) { this.nomeEquipamento = nomeEquipamento; }
}
