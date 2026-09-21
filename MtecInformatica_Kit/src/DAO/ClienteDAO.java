// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importações necessárias para trabalhar com banco de dados e lista
import Model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Essa classe cuida das operações de banco da tabela cliente
// Aqui ficam os métodos de inserir, alterar, excluir e listar clientes
public class ClienteDAO {

    // Insere um novo cliente no banco de dados
    public void adicionar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, cpf_cnpj, telefone, endereco) VALUES (?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getCpfCnpj());
            ps.setString(3, cliente.getTelefone());
            ps.setString(4, cliente.getEndereco());

            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || e.getErrorCode() == 23505 || "23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe um cliente cadastrado com esse CPF/CNPJ.");
            }

            throw new RuntimeException("Erro ao inserir cliente: " + e.getMessage());
        }
    }

    // Atualiza os dados de um cliente já cadastrado
    public void alterar(Cliente cliente) {
        String sql = "UPDATE cliente SET nome = ?, cpf_cnpj = ?, telefone = ?, endereco = ? WHERE idCliente = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getCpfCnpj());
            ps.setString(3, cliente.getTelefone());
            ps.setString(4, cliente.getEndereco());
            ps.setInt(5, cliente.getIdCliente());

            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || e.getErrorCode() == 23505 || "23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe um cliente cadastrado com esse CPF/CNPJ.");
            }

            throw new RuntimeException("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    // Arquiva o cliente e os dados ligados a ele.
    public void excluir(int idCliente) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                atualizarAtivoSeColunaExistir(con, "produto", "CLIENTE_idCliente", idCliente, 0);
                atualizarAtivoSeColunaExistir(con, "relatorio_orcamento", "CLIENTE_idCliente", idCliente, 0);
                atualizarAtivoSeColunaExistir(con, "servico", "CLIENTE_idCliente", idCliente, 0);

                try (PreparedStatement ps = con.prepareStatement("UPDATE cliente SET ativo = 0 WHERE idCliente = ?")) {
                    ps.setInt(1, idCliente);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao arquivar cliente: " + e.getMessage());
        }
    }

    private void atualizarAtivoSeColunaExistir(Connection con, String tabela, String colunaCliente, int idCliente, int ativo)
            throws SQLException {
        if (!colunaExiste(con, tabela, "ativo")) return;

        String sql = "UPDATE " + tabela + " SET ativo = ? WHERE " + colunaCliente + " = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ativo);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    private boolean colunaExiste(Connection con, String tabela, String coluna) throws SQLException {
        String catalog = con.getCatalog();
        try (ResultSet rs = con.getMetaData().getColumns(catalog, null, tabela, coluna)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = con.getMetaData().getColumns(catalog, null, tabela.toUpperCase(), coluna)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = con.getMetaData().getColumns(catalog, null, tabela.toLowerCase(), coluna)) {
            return rs.next();
        }
    }

    // Lista apenas os clientes ativos (ativo = 1) para a tabela principal do sistema
    public List<Cliente> listarTudo() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT idCliente, nome, cpf_cnpj, telefone, endereco FROM cliente WHERE ativo = 1 ORDER BY idCliente DESC";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getInt("idCliente"));
                cliente.setNome(rs.getString("nome"));
                cliente.setCpfCnpj(rs.getString("cpf_cnpj"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setEndereco(rs.getString("endereco"));

                lista.add(cliente);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes: " + e.getMessage());
        }

        return lista;
    }
}
