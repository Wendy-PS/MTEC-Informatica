// Esse arquivo faz parte do pacote DAO
// Essa classe conversa com o banco na parte de ordens de servico.
package DAO;

import Model.Servico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {

    // Conexão com o banco que uso em todos os métodos
    private Connection connection;

    // Quando criar um ServicoDAO, já conecto ao banco automaticamente
    public ServicoDAO() {
        this.connection = new ConnectionFactory().getConnection();
    }

    // Metodo para inserir uma nova ordem de servico no banco
    public void adicionar(Servico os) {
        // Query para inserir uma nova OS na tabela servico
        // A data de conclusão só recebe valor se a OS já entrar como concluída
        String sql = "INSERT INTO servico (data_abertura, prazo_entrega, data_conclusao, status_os, " +
                     "CLIENTE_idCliente, TECNICO_idTECNICO, PRODUTO_idPRODUTO) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            validarClienteAtivo(os.getClienteIdCliente());
            stmt.setDate(1, os.getDataAbertura());        // data de abertura
            stmt.setDate(2, os.getPrazoEntrega());        // prazo de entrega
            stmt.setDate(3, dataConclusaoParaStatus(os.getStatusOs()));
            stmt.setString(4, os.getStatusOs());          // status da OS
            stmt.setInt(5, os.getClienteIdCliente());     // ID do cliente
            stmt.setInt(6, os.getTecnicoIdTecnico());     // ID do técnico
            stmt.setInt(7, os.getProdutoIdProduto());     // ID do equipamento
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir OS: " + e.getMessage());
        }
    }

    // Metodo para listar as ordens de servico na tabela
    public List<Servico> listarTudo() {
        // Busco as OS com o nome do cliente e o equipamento vinculado na própria OS
        // Uso LEFT JOIN no produto porque as OS antigas podem estar sem equipamento ainda
        String sql =
            "SELECT s.idSERVICO, " +
            "       c.nome AS nomeCliente, " +
            "       TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)) AS equipamento, " +
            "       s.data_abertura, s.prazo_entrega, s.data_conclusao, s.status_os, " +
            "       COALESCE(r.valor_total_orcamento, 0) AS valor_servico, " +
            "       CASE WHEN r.idRELATORIO_ORCAMENTO IS NULL THEN 'Manual' ELSE 'Orcamento' END AS origem_servico, " +
            "       s.CLIENTE_idCliente, s.TECNICO_idTECNICO, s.PRODUTO_idPRODUTO " +
            "FROM servico s " +
            "JOIN cliente c ON s.CLIENTE_idCliente = c.idCliente AND c.ativo = 1 " +
            "LEFT JOIN produto p ON s.PRODUTO_idPRODUTO = p.idPRODUTO " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "ORDER BY s.idSERVICO DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Servico> servicos = new ArrayList<>();

            while (rs.next()) {
                // Crio um objeto Servico para cada linha do banco
                Servico os = new Servico();
                os.setIdSERVICO(rs.getInt("idSERVICO"));
                os.setNomeCliente(rs.getString("nomeCliente"));

                // Se a OS antiga ainda não tiver equipamento, mostro uma informação simples na tabela
                String equipamento = rs.getString("equipamento");
                if (equipamento == null || equipamento.trim().isEmpty()) {
                    equipamento = "Sem equipamento";
                }
                os.setNomeEquipamento(equipamento);

                os.setDataAbertura(rs.getDate("data_abertura"));
                os.setPrazoEntrega(rs.getDate("prazo_entrega"));
                os.setDataConclusao(rs.getDate("data_conclusao"));
                os.setStatusOs(rs.getString("status_os"));
                os.setValorServico(rs.getBigDecimal("valor_servico"));
                os.setOrigemServico(rs.getString("origem_servico"));
                os.setClienteIdCliente(rs.getInt("CLIENTE_idCliente"));
                os.setTecnicoIdTecnico(rs.getInt("TECNICO_idTECNICO"));

                // getInt retorna 0 quando o valor no banco é NULL
                // Isso serve para OS antigas que ainda não tinham produto vinculado
                os.setProdutoIdProduto(rs.getInt("PRODUTO_idPRODUTO"));

                servicos.add(os);
            }

            return servicos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar OS: " + e.getMessage());
        }
    }

    // Metodo para atualizar uma ordem de servico
    public void alterar(Servico os) {
        // Atualizo também o produto/equipamento vinculado à OS
        String sql = "UPDATE servico SET data_abertura=?, prazo_entrega=?, status_os=?, " +
                     "data_conclusao = CASE WHEN LOWER(TRIM(?)) LIKE 'conclu%' " +
                     "THEN COALESCE(data_conclusao, CURRENT_DATE) ELSE NULL END, " +
                     "TECNICO_idTECNICO=?, CLIENTE_idCliente=?, PRODUTO_idPRODUTO=? " +
                     "WHERE idSERVICO=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            validarClienteAtivo(os.getClienteIdCliente());
            stmt.setDate(1, os.getDataAbertura());        // nova data de abertura
            stmt.setDate(2, os.getPrazoEntrega());        // novo prazo
            stmt.setString(3, os.getStatusOs());          // novo status
            stmt.setString(4, os.getStatusOs());          // usado para decidir a data de conclusão
            stmt.setInt(5, os.getTecnicoIdTecnico());     // novo técnico
            stmt.setInt(6, os.getClienteIdCliente());     // novo cliente
            stmt.setInt(7, os.getProdutoIdProduto());     // novo equipamento
            stmt.setInt(8, os.getIdSERVICO());            // ID da OS a atualizar
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar OS: " + e.getMessage());
        }
    }

    // Metodo para excluir uma ordem de servico pelo ID
    public void excluir(int id) {
        String sql = "DELETE FROM servico WHERE idSERVICO=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id); // ID da OS a excluir
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir OS: " + e.getMessage());
        }
    }

    // Se o status for concluído, salvo a data de hoje como data de conclusão
    private Date dataConclusaoParaStatus(String status) {
        if (status != null && status.trim().toLowerCase().startsWith("conclu")) {
            return new Date(System.currentTimeMillis());
        }
        return null;
    }

    // Cliente arquivado nao pode receber novas ordens de servico
    private void validarClienteAtivo(int idCliente) throws SQLException {
        String sql = "SELECT ativo FROM cliente WHERE idCliente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("ativo")) {
                    throw new SQLException("Cliente inativo não pode ser usado em serviços.");
                }
            }
        }
    }
}
