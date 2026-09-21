// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importações necessárias para trabalhar com banco de dados e lista
import Model.Produto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Essa classe cuida das operações de banco da tabela produto
// Aqui ficam os métodos de inserir, alterar, excluir e listar equipamentos/peças
public class ProdutoDAO {

    // Insere um novo produto/equipamento no banco de dados
    public void adicionar(Produto produto) {
        String sql = "INSERT INTO produto (tipo, marca, modelo, especificacoes, CLIENTE_idCliente) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            validarClienteAtivo(con, produto.getClienteIdCliente());
            ps.setString(1, produto.getTipo());
            ps.setString(2, produto.getMarca());
            ps.setString(3, produto.getModelo());
            ps.setString(4, produto.getEspecificacoes());
            ps.setInt(5, produto.getClienteIdCliente());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto/equipamento: " + e.getMessage());
        }
    }

    // Atualiza os dados de um produto/equipamento já cadastrado
    public void alterar(Produto produto) {
        String sql = "UPDATE produto SET tipo = ?, marca = ?, modelo = ?, especificacoes = ?, CLIENTE_idCliente = ? " +
                     "WHERE idPRODUTO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            validarClienteAtivo(con, produto.getClienteIdCliente());
            ps.setString(1, produto.getTipo());
            ps.setString(2, produto.getMarca());
            ps.setString(3, produto.getModelo());
            ps.setString(4, produto.getEspecificacoes());
            ps.setInt(5, produto.getClienteIdCliente());
            ps.setInt(6, produto.getIdPRODUTO());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto/equipamento: " + e.getMessage());
        }
    }

    // Exclui um produto/equipamento pelo ID
    public void excluir(int idProduto) {
        String sql = "DELETE FROM produto WHERE idPRODUTO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProduto);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto/equipamento: " + e.getMessage());
        }
    }

    // Lista todos os produtos/equipamentos cadastrados no banco
    public List<Produto> listarTudo() {
        List<Produto> lista = new ArrayList<>();

        String sql =
            "SELECT p.idPRODUTO, p.tipo, p.marca, p.modelo, p.especificacoes, " +
            "p.CLIENTE_idCliente, c.nome AS nomeCliente " +
            "FROM produto p " +
            "INNER JOIN cliente c ON c.idCliente = p.CLIENTE_idCliente " +
            "WHERE c.ativo = 1 " +
            "ORDER BY p.idPRODUTO DESC";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto();
                produto.setIdPRODUTO(rs.getInt("idPRODUTO"));
                produto.setTipo(rs.getString("tipo"));
                produto.setMarca(rs.getString("marca"));
                produto.setModelo(rs.getString("modelo"));
                produto.setEspecificacoes(rs.getString("especificacoes"));
                produto.setClienteIdCliente(rs.getInt("CLIENTE_idCliente"));
                produto.setNomeCliente(rs.getString("nomeCliente"));

                lista.add(produto);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos/equipamentos: " + e.getMessage());
        }

        return lista;
    }

    // Cliente arquivado nao pode receber novos equipamentos
    private void validarClienteAtivo(Connection con, int idCliente) throws SQLException {
        String sql = "SELECT ativo FROM cliente WHERE idCliente = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("ativo")) {
                    throw new SQLException("Cliente inativo não pode ser usado em equipamentos.");
                }
            }
        }
    }
}
