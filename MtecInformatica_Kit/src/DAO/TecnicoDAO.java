// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importações necessárias para trabalhar com banco de dados e lista
import Model.Tecnico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

// Essa classe cuida das operações de banco da tabela tecnico
// Aqui ficam os métodos de inserir, alterar, excluir e listar técnicos
public class TecnicoDAO {

    // Insere um novo técnico no banco de dados
    public void adicionar(Tecnico tecnico) {
        String sql = "INSERT INTO tecnico (nome, cpf, telefone, email, especialidade, status, observacoes, login, senha) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tecnico.getNome());
            ps.setString(2, tecnico.getCpf());
            ps.setString(3, tecnico.getTelefone());
            ps.setString(4, tecnico.getEmail());
            ps.setString(5, tecnico.getEspecialidade());
            ps.setString(6, tecnico.getStatus());
            ps.setString(7, tecnico.getObservacoes());
            ps.setString(8, tecnico.getLogin());
            ps.setString(9, tecnico.getSenha());

            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || e.getErrorCode() == 23505 || "23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe um técnico cadastrado com esse CPF ou login.");
            }

            throw new RuntimeException("Erro ao inserir técnico: " + e.getMessage());
        }
    }

    // Atualiza os dados de um técnico já cadastrado
    public void alterar(Tecnico tecnico) {
        String senha = tecnico.getSenha();

        // Se a senha vier vazia na edição, mantenho a senha antiga no banco
        if (senha == null || senha.trim().isEmpty()) {
            alterarSemSenha(tecnico);
        } else {
            alterarComSenha(tecnico);
        }
    }

    // Atualiza o técnico sem trocar a senha
    private void alterarSemSenha(Tecnico tecnico) {
        String sql = "UPDATE tecnico SET nome = ?, cpf = ?, telefone = ?, email = ?, " +
                     "especialidade = ?, status = ?, observacoes = ?, login = ? WHERE idTECNICO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tecnico.getNome());
            ps.setString(2, tecnico.getCpf());
            ps.setString(3, tecnico.getTelefone());
            ps.setString(4, tecnico.getEmail());
            ps.setString(5, tecnico.getEspecialidade());
            ps.setString(6, tecnico.getStatus());
            ps.setString(7, tecnico.getObservacoes());
            ps.setString(8, tecnico.getLogin());
            ps.setInt(9, tecnico.getIdTECNICO());

            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || e.getErrorCode() == 23505 || "23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe um técnico cadastrado com esse CPF ou login.");
            }

            throw new RuntimeException("Erro ao atualizar técnico: " + e.getMessage());
        }
    }

    // Atualiza o técnico trocando também a senha
    private void alterarComSenha(Tecnico tecnico) {
        String sql = "UPDATE tecnico SET nome = ?, cpf = ?, telefone = ?, email = ?, " +
                     "especialidade = ?, status = ?, observacoes = ?, login = ?, senha = ? WHERE idTECNICO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tecnico.getNome());
            ps.setString(2, tecnico.getCpf());
            ps.setString(3, tecnico.getTelefone());
            ps.setString(4, tecnico.getEmail());
            ps.setString(5, tecnico.getEspecialidade());
            ps.setString(6, tecnico.getStatus());
            ps.setString(7, tecnico.getObservacoes());
            ps.setString(8, tecnico.getLogin());
            ps.setString(9, tecnico.getSenha());
            ps.setInt(10, tecnico.getIdTECNICO());

            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || e.getErrorCode() == 23505 || "23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe um técnico cadastrado com esse CPF ou login.");
            }

            throw new RuntimeException("Erro ao atualizar técnico: " + e.getMessage());
        }
    }

    // Exclui um técnico pelo ID
    public void excluir(int idTecnico) {
        String sql = "DELETE FROM tecnico WHERE idTECNICO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTecnico);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir técnico: " + e.getMessage());
        }
    }

    // Lista todos os técnicos cadastrados no banco
    public List<Tecnico> listarTudo() {
        List<Tecnico> lista = new ArrayList<>();

        String sql = "SELECT idTECNICO, nome, cpf, telefone, email, especialidade, status, observacoes, login, senha " +
                     "FROM tecnico ORDER BY idTECNICO DESC";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tecnico tecnico = new Tecnico();
                tecnico.setIdTECNICO(rs.getInt("idTECNICO"));
                tecnico.setNome(rs.getString("nome"));
                tecnico.setCpf(rs.getString("cpf"));
                tecnico.setTelefone(rs.getString("telefone"));
                tecnico.setEmail(rs.getString("email"));
                tecnico.setEspecialidade(rs.getString("especialidade"));
                tecnico.setStatus(rs.getString("status"));
                tecnico.setObservacoes(rs.getString("observacoes"));
                tecnico.setLogin(rs.getString("login"));
                tecnico.setSenha(rs.getString("senha"));

                lista.add(tecnico);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar técnicos: " + e.getMessage());
        }

        return lista;
    }

    // Busca a quantidade de técnicos por especialidade para usar no Dashboard
    public Map<String, Integer> buscarEstatisticasPorEspecialidade() {
        Map<String, Integer> dados = new LinkedHashMap<>();

        String sql =
            "SELECT COALESCE(NULLIF(TRIM(especialidade), ''), 'Não informada') AS especialidade, " +
            "COUNT(*) AS quantidade " +
            "FROM tecnico " +
            "GROUP BY COALESCE(NULLIF(TRIM(especialidade), ''), 'Não informada') " +
            "ORDER BY quantidade DESC";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dados.put(rs.getString("especialidade"), rs.getInt("quantidade"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar estatísticas por especialidade: " + e.getMessage());
        }

        return dados;
    }

    // Busca um técnico pelo ID, usado principalmente na edição
    public Tecnico buscarPorId(int id) {
        String sql = "SELECT idTECNICO, nome, cpf, telefone, email, especialidade, status, observacoes, login, senha " +
                     "FROM tecnico WHERE idTECNICO = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Tecnico tecnico = new Tecnico();

                    tecnico.setIdTECNICO(rs.getInt("idTECNICO"));
                    tecnico.setNome(rs.getString("nome"));
                    tecnico.setCpf(rs.getString("cpf"));
                    tecnico.setTelefone(rs.getString("telefone"));
                    tecnico.setEmail(rs.getString("email"));
                    tecnico.setEspecialidade(rs.getString("especialidade"));
                    tecnico.setStatus(rs.getString("status"));
                    tecnico.setObservacoes(rs.getString("observacoes"));
                    tecnico.setLogin(rs.getString("login"));
                    tecnico.setSenha(rs.getString("senha"));

                    return tecnico;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar técnico: " + e.getMessage());
        }

        return null;
    }
}
