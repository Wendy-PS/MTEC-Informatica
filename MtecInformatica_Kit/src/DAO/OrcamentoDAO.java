// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importacoes necessarias para trabalhar com banco de dados e lista
import Model.Orcamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Essa classe cuida das operacoes de banco da tabela relatorio_orcamento
// Aqui ficam os metodos de inserir, alterar, excluir e listar orcamentos
public class OrcamentoDAO {

    // Status usados para ligar o fluxo de orcamento com ordem de servico
    private static final String STATUS_ORCAMENTO_APROVADO = "Aprovado";
    private static final String STATUS_OS_INICIAL = "Em Andamento";

    // Insere um novo orcamento no banco de dados
    public void adicionar(Orcamento orcamento) {
        String sql =
            "INSERT INTO relatorio_orcamento " +
            "(defeito_relatado, diagnostico_tecnico, pecas_orcadas, mao_obra_orcada, valor_total_orcamento, " +
            "abertura, validade, status_orcamento, CLIENTE_idCliente, SERVICO_idSERVICO, PRODUTO_idPRODUTO, TECNICO_idTECNICO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                validarClienteAtivo(con, orcamento.getClienteIdCliente());
                garantirServicoSeAprovado(con, orcamento);
                preencherOrcamentoStatement(ps, orcamento);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orcamento.setIdRelatorioOrcamento(rs.getInt(1));
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir orçamento: " + e.getMessage());
        }
    }

    // Atualiza um orcamento ja cadastrado
    public void alterar(Orcamento orcamento) {
        String sql =
            "UPDATE relatorio_orcamento SET " +
            "defeito_relatado = ?, diagnostico_tecnico = ?, pecas_orcadas = ?, mao_obra_orcada = ?, " +
            "valor_total_orcamento = ?, abertura = ?, validade = ?, status_orcamento = ?, CLIENTE_idCliente = ?, " +
            "SERVICO_idSERVICO = ?, PRODUTO_idPRODUTO = ?, TECNICO_idTECNICO = ? " +
            "WHERE idRELATORIO_ORCAMENTO = ?";

        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                validarClienteAtivo(con, orcamento.getClienteIdCliente());
                garantirServicoSeAprovado(con, orcamento);
                preencherOrcamentoStatement(ps, orcamento);
                ps.setInt(13, orcamento.getIdRelatorioOrcamento());
                ps.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar orçamento: " + e.getMessage());
        }
    }

    // Preenche os campos do INSERT e do UPDATE do orcamento
    private void preencherOrcamentoStatement(PreparedStatement ps, Orcamento orcamento) throws SQLException {
        ps.setString(1, orcamento.getDefeitoRelatado());
        ps.setString(2, orcamento.getDiagnosticoTecnico());
        ps.setBigDecimal(3, orcamento.getPecasOrcadas());
        ps.setBigDecimal(4, orcamento.getMaoObraOrcada());
        ps.setBigDecimal(5, orcamento.getValorTotalOrcamento());
        ps.setDate(6, orcamento.getAbertura());
        ps.setDate(7, orcamento.getValidade());
        ps.setString(8, orcamento.getStatusOrcamento());
        ps.setInt(9, orcamento.getClienteIdCliente());

        // O orcamento pode existir antes de virar OS, entao o servico pode ficar NULL
        if (orcamento.getServicoIdServico() <= 0) {
            ps.setNull(10, Types.INTEGER);
        } else {
            ps.setInt(10, orcamento.getServicoIdServico());
        }

        ps.setInt(11, orcamento.getProdutoIdProduto());
        ps.setInt(12, orcamento.getTecnicoIdTecnico());
    }

    // Cria ou atualiza a OS quando o orcamento estiver aprovado
    private void garantirServicoSeAprovado(Connection con, Orcamento orcamento) throws SQLException {
        if (!orcamentoAprovado(orcamento)) {
            return;
        }

        if (orcamento.getServicoIdServico() <= 0) {
            int idServico = criarServicoDoOrcamento(con, orcamento);
            orcamento.setServicoIdServico(idServico);
        } else if (!atualizarServicoDoOrcamento(con, orcamento)) {
            int idServico = criarServicoDoOrcamento(con, orcamento);
            orcamento.setServicoIdServico(idServico);
        }
    }

    // Confere o texto do status sem ligar para maiuscula/minuscula
    private boolean orcamentoAprovado(Orcamento orcamento) {
        return orcamento != null
            && orcamento.getStatusOrcamento() != null
            && STATUS_ORCAMENTO_APROVADO.equalsIgnoreCase(orcamento.getStatusOrcamento().trim());
    }

    // Cria uma nova OS com os mesmos vinculos do orcamento
    private int criarServicoDoOrcamento(Connection con, Orcamento orcamento) throws SQLException {
        String sql =
            "INSERT INTO servico (data_abertura, prazo_entrega, status_os, " +
            "CLIENTE_idCliente, TECNICO_idTECNICO, PRODUTO_idPRODUTO) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherServicoStatement(ps, orcamento);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID da OS criada.");
    }

    // Se o orcamento ja tinha OS, so mantenho os dados dela atualizados
    private boolean atualizarServicoDoOrcamento(Connection con, Orcamento orcamento) throws SQLException {
        String sql =
            "UPDATE servico SET data_abertura = ?, prazo_entrega = ?, status_os = ?, " +
            "CLIENTE_idCliente = ?, TECNICO_idTECNICO = ?, PRODUTO_idPRODUTO = ? " +
            "WHERE idSERVICO = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            preencherServicoStatement(ps, orcamento);
            ps.setInt(7, orcamento.getServicoIdServico());
            return ps.executeUpdate() > 0;
        }
    }

    // A validade do orcamento entra como prazo de entrega inicial da OS
    private void preencherServicoStatement(PreparedStatement ps, Orcamento orcamento) throws SQLException {
        ps.setDate(1, orcamento.getAbertura());
        ps.setDate(2, orcamento.getValidade());
        ps.setString(3, STATUS_OS_INICIAL);
        ps.setInt(4, orcamento.getClienteIdCliente());
        ps.setInt(5, orcamento.getTecnicoIdTecnico());
        ps.setInt(6, orcamento.getProdutoIdProduto());
    }

    // Cliente arquivado nao pode ser usado em novas movimentacoes
    private void validarClienteAtivo(Connection con, int idCliente) throws SQLException {
        String sql = "SELECT ativo FROM cliente WHERE idCliente = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("ativo")) {
                    throw new SQLException("Cliente inativo não pode ser usado em orçamentos ou serviços.");
                }
            }
        }
    }

    // Exclui um orcamento pelo ID
    public void excluir(int idOrcamento) {
        String sql = "DELETE FROM relatorio_orcamento WHERE idRELATORIO_ORCAMENTO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idOrcamento);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir orçamento: " + e.getMessage());
        }
    }

    // Lista todos os orcamentos cadastrados com cliente, produto e tecnico
    public List<Orcamento> listarTudo() {
        List<Orcamento> lista = new ArrayList<>();

        String sql =
            "SELECT r.idRELATORIO_ORCAMENTO, r.defeito_relatado, r.diagnostico_tecnico, " +
            "r.pecas_orcadas, r.mao_obra_orcada, r.valor_total_orcamento, r.abertura, r.validade, r.status_orcamento, " +
            "r.CLIENTE_idCliente, r.SERVICO_idSERVICO, r.PRODUTO_idPRODUTO, r.TECNICO_idTECNICO, " +
            "c.nome AS nomeCliente, " +
            "TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)) AS nomeEquipamento, " +
            "t.nome AS nomeTecnico " +
            "FROM relatorio_orcamento r " +
            "INNER JOIN cliente c ON c.idCliente = r.CLIENTE_idCliente AND c.ativo = 1 " +
            "LEFT JOIN produto p ON p.idPRODUTO = r.PRODUTO_idPRODUTO " +
            "LEFT JOIN tecnico t ON t.idTECNICO = r.TECNICO_idTECNICO " +
            "ORDER BY r.idRELATORIO_ORCAMENTO DESC";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Orcamento orcamento = new Orcamento();

                orcamento.setIdRelatorioOrcamento(rs.getInt("idRELATORIO_ORCAMENTO"));
                orcamento.setDefeitoRelatado(rs.getString("defeito_relatado"));
                orcamento.setDiagnosticoTecnico(rs.getString("diagnostico_tecnico"));
                orcamento.setPecasOrcadas(rs.getBigDecimal("pecas_orcadas"));
                orcamento.setMaoObraOrcada(rs.getBigDecimal("mao_obra_orcada"));
                orcamento.setValorTotalOrcamento(rs.getBigDecimal("valor_total_orcamento"));
                orcamento.setAbertura(rs.getDate("abertura"));
                orcamento.setValidade(rs.getDate("validade"));
                orcamento.setStatusOrcamento(rs.getString("status_orcamento"));
                orcamento.setClienteIdCliente(rs.getInt("CLIENTE_idCliente"));
                orcamento.setServicoIdServico(rs.getInt("SERVICO_idSERVICO"));
                orcamento.setProdutoIdProduto(rs.getInt("PRODUTO_idPRODUTO"));
                orcamento.setTecnicoIdTecnico(rs.getInt("TECNICO_idTECNICO"));
                orcamento.setNomeCliente(rs.getString("nomeCliente"));

                String equipamento = rs.getString("nomeEquipamento");
                if (equipamento == null || equipamento.trim().isEmpty()) {
                    equipamento = "Sem equipamento";
                }

                orcamento.setNomeEquipamento(equipamento);
                orcamento.setNomeTecnico(rs.getString("nomeTecnico"));

                lista.add(orcamento);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar orçamentos: " + e.getMessage());
        }

        return lista;
    }
}
