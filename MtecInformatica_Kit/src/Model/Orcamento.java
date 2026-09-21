// Esse arquivo faz parte do pacote model
// Ele representa um Orçamento/Relatório de Orçamento do banco de dados
// Cada atributo aqui corresponde a um campo da tabela "relatorio_orcamento"
package Model;

import java.math.BigDecimal;
import java.sql.Date;

public class Orcamento {

    // ── Atributos que correspondem aos campos da tabela relatorio_orcamento ──
    private int idRelatorioOrcamento;       // chave primária do orçamento
    private String defeitoRelatado;         // descrição/defeito informado pelo cliente
    private String diagnosticoTecnico;      // diagnóstico do técnico
    private BigDecimal pecasOrcadas;        // valor das peças
    private BigDecimal maoObraOrcada;       // valor bruto salvo na coluna antiga do banco
    private BigDecimal valorTotalOrcamento; // valor total do orçamento
    private Date abertura;                  // data de abertura do orçamento
    private Date validade;                  // data de validade do orçamento
    private String statusOrcamento;         // status atual do orçamento
    private int clienteIdCliente;           // cliente vinculado ao orçamento
    private int servicoIdServico;           // OS vinculada ao orçamento, pode ficar 0/null
    private int produtoIdProduto;           // equipamento vinculado
    private int tecnicoIdTecnico;           // técnico vinculado

    // ── Atributos extras para exibição na tabela ──────────────────
    // Esses campos não estão na tabela relatorio_orcamento, vêm dos JOINs
    private String nomeCliente;
    private String nomeEquipamento;
    private String nomeTecnico;

    // ── Getters e Setters ───────────────────────────────────────

    public int getIdRelatorioOrcamento() { return idRelatorioOrcamento; }
    public void setIdRelatorioOrcamento(int idRelatorioOrcamento) { this.idRelatorioOrcamento = idRelatorioOrcamento; }

    public String getDefeitoRelatado() { return defeitoRelatado; }
    public void setDefeitoRelatado(String defeitoRelatado) { this.defeitoRelatado = defeitoRelatado; }

    public String getDiagnosticoTecnico() { return diagnosticoTecnico; }
    public void setDiagnosticoTecnico(String diagnosticoTecnico) { this.diagnosticoTecnico = diagnosticoTecnico; }

    public BigDecimal getPecasOrcadas() { return pecasOrcadas; }
    public void setPecasOrcadas(BigDecimal pecasOrcadas) { this.pecasOrcadas = pecasOrcadas; }

    public BigDecimal getMaoObraOrcada() { return maoObraOrcada; }
    public void setMaoObraOrcada(BigDecimal maoObraOrcada) { this.maoObraOrcada = maoObraOrcada; }

    public BigDecimal getValorTotalOrcamento() { return valorTotalOrcamento; }
    public void setValorTotalOrcamento(BigDecimal valorTotalOrcamento) { this.valorTotalOrcamento = valorTotalOrcamento; }

    public Date getAbertura() { return abertura; }
    public void setAbertura(Date abertura) { this.abertura = abertura; }

    public Date getValidade() { return validade; }
    public void setValidade(Date validade) { this.validade = validade; }

    public String getStatusOrcamento() { return statusOrcamento; }
    public void setStatusOrcamento(String statusOrcamento) { this.statusOrcamento = statusOrcamento; }

    public int getClienteIdCliente() { return clienteIdCliente; }
    public void setClienteIdCliente(int clienteIdCliente) { this.clienteIdCliente = clienteIdCliente; }

    public int getServicoIdServico() { return servicoIdServico; }
    public void setServicoIdServico(int servicoIdServico) { this.servicoIdServico = servicoIdServico; }

    public int getProdutoIdProduto() { return produtoIdProduto; }
    public void setProdutoIdProduto(int produtoIdProduto) { this.produtoIdProduto = produtoIdProduto; }

    public int getTecnicoIdTecnico() { return tecnicoIdTecnico; }
    public void setTecnicoIdTecnico(int tecnicoIdTecnico) { this.tecnicoIdTecnico = tecnicoIdTecnico; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getNomeEquipamento() { return nomeEquipamento; }
    public void setNomeEquipamento(String nomeEquipamento) { this.nomeEquipamento = nomeEquipamento; }

    public String getNomeTecnico() { return nomeTecnico; }
    public void setNomeTecnico(String nomeTecnico) { this.nomeTecnico = nomeTecnico; }
}
