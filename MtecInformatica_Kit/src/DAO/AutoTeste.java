package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Autoteste do banco embutido. NÃO faz parte do uso normal do sistema: é executado pelo
 * script de build (CONSTRUIR_EXE.bat) numa pasta temporária, para confirmar que o banco
 * H2 aceita as consultas usadas pelo sistema antes de entregar o executável.
 *
 * Uso: java -Dmtec.dados=PASTA_TEMPORARIA -cp ... DAO.AutoTeste
 * Código de saída: 0 = tudo certo; 1 = alguma verificação falhou.
 */
public class AutoTeste {

    private static int falhas = 0;

    private static void ok(String nome) { System.out.println("  [OK]    " + nome); }
    private static void falha(String nome, String detalhe) {
        falhas++;
        System.out.println("  [FALHOU] " + nome + " -> " + detalhe);
    }
    private static void confere(String nome, Object esperado, Object obtido) {
        if (String.valueOf(esperado).equals(String.valueOf(obtido))) ok(nome + " = " + obtido);
        else falha(nome, "esperado " + esperado + " mas veio " + obtido);
    }

    private static long contar(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Autoteste do banco embutido MTEC ===");
        try (Connection con = ConnectionFactory.getConnection()) {

            // 1) dados iniciais
            confere("clientes", 30, contar(con, "SELECT COUNT(*) FROM cliente"));
            confere("tecnicos", 6, contar(con, "SELECT COUNT(*) FROM tecnico"));
            confere("produtos", 45, contar(con, "SELECT COUNT(*) FROM produto"));
            confere("servicos", 24, contar(con, "SELECT COUNT(*) FROM servico"));
            confere("orcamentos", 36, contar(con, "SELECT COUNT(*) FROM relatorio_orcamento"));

            // 2) login (mesmo SQL da tela de login; usuario em maiusculas prova que ignora caixa como o MySQL)
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT idTECNICO FROM tecnico WHERE login = ? AND senha = ? AND status = 'Ativo' LIMIT 1")) {
                ps.setString(1, "ADMIN"); ps.setString(2, "123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) ok("login admin/123"); else falha("login admin/123", "nenhuma linha");
                }
            }

            // 3) nome do equipamento (CONCAT_WS + TRIM)
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                    "SELECT TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)) AS equipamento " +
                    "FROM servico s LEFT JOIN produto p ON p.idPRODUTO = s.PRODUTO_idPRODUTO WHERE s.idSERVICO = 1")) {
                rs.next();
                confere("equipamento da OS 1", "Desktop HP ProDesk", rs.getString("equipamento"));
            }

            // 4) consultas do dashboard/relatorios (YEAR, MONTH, CURRENT_DATE, COALESCE/NULLIF)
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT MONTH(data_abertura) AS mes, COUNT(*) AS total FROM servico " +
                    "WHERE data_abertura IS NOT NULL AND YEAR(data_abertura) = ? GROUP BY MONTH(data_abertura) ORDER BY mes")) {
                ps.setInt(1, 2026);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    confere("OS abertas em junho/2026 (mes=" + rs.getInt("mes") + ")", 24, rs.getInt("total"));
                }
            }
            contar(con, "SELECT COUNT(*) FROM servico WHERE LOWER(TRIM(status_os)) NOT LIKE 'conclu%' " +
                        "AND prazo_entrega IS NOT NULL AND prazo_entrega < CURRENT_DATE");
            ok("consulta de servicos vencidos (CURRENT_DATE)");
            contar(con, "SELECT COUNT(*) FROM servico WHERE 1 = 1 AND data_abertura IS NOT NULL " +
                        "AND YEAR(data_abertura) = YEAR(CURRENT_DATE) AND MONTH(data_abertura) = MONTH(CURRENT_DATE)");
            ok("filtro do dashboard 'mes atual'");
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                    "SELECT c.nome AS rotulo, COUNT(p.idPRODUTO) AS total FROM cliente c " +
                    "LEFT JOIN produto p ON p.CLIENTE_idCliente = c.idCliente " +
                    "GROUP BY c.idCliente, c.nome ORDER BY total DESC, c.nome LIMIT 6")) {
                int n = 0; while (rs.next()) n++;
                confere("ranking clientes x produtos (linhas)", 6, n);
            }
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                    "SELECT COALESCE(SUM(COALESCE(r.valor_total_orcamento, 0)), 0) AS total " +
                    "FROM servico s LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
                    "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%'")) {
                rs.next();
                BigDecimal v = rs.getBigDecimal("total");
                if (v.compareTo(new BigDecimal("1310.00")) == 0) ok("faturamento das OS concluidas = " + v);
                else falha("faturamento das OS concluidas", "esperado 1310.00 mas veio " + v);
            }

            // 5) escritas (tudo dentro de transacao e desfeito no fim)
            con.setAutoCommit(false);
            try {
                // 5a) novo cliente recebe o proximo id (31) e ativo=1 por padrao
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO cliente (nome, cpf_cnpj, telefone, endereco) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Cliente Teste"); ps.setString(2, "000.000.000-00");
                    ps.setString(3, "(27) 90000-0000"); ps.setString(4, "Rua Teste");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        confere("id gerado para novo cliente", 31, rs.getInt(1));
                    }
                }
                confere("ativo padrao do novo cliente", 1,
                        contar(con, "SELECT ativo FROM cliente WHERE cpf_cnpj = '000.000.000-00'"));

                // 5b) CPF/CNPJ duplicado tem de gerar o codigo que o sistema reconhece
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO cliente (nome, cpf_cnpj, telefone, endereco) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, "Duplicado"); ps.setString(2, "000.000.000-00");
                    ps.setString(3, "(27) 90000-0001"); ps.setString(4, "x");
                    ps.executeUpdate();
                    falha("CPF duplicado", "o banco aceitou o duplicado");
                } catch (SQLException e) {
                    boolean reconhecido = e.getErrorCode() == 1062 || e.getErrorCode() == 23505 || "23505".equals(e.getSQLState());
                    if (reconhecido) ok("CPF duplicado reconhecido (codigo " + e.getErrorCode() + ")");
                    else falha("CPF duplicado", "codigo inesperado " + e.getErrorCode() + " / " + e.getSQLState());
                }

                // 5c) login duplicado ignorando maiusculas/minusculas ('ADMIN' x 'admin')
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO tecnico (nome, cpf, login, senha) VALUES ('Teste', '111.111.111-11', 'ADMIN', 'x')")) {
                    ps.executeUpdate();
                    falha("login duplicado (ADMIN x admin)", "o banco aceitou");
                } catch (SQLException e) {
                    ok("login duplicado (ADMIN x admin) bloqueado (codigo " + e.getErrorCode() + ")");
                }

                // 5d) orcamento novo: getGeneratedKeys funcionando (proximo id = 37)
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO relatorio_orcamento (defeito_relatado, valor_total_orcamento, status_orcamento, abertura, validade, CLIENTE_idCliente) " +
                        "VALUES ('teste', 10.50, 'Aguardando', CURRENT_DATE, CURRENT_DATE, 1)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        confere("id gerado para novo orcamento", 37, rs.getInt(1));
                    }
                }

                // 5e) a "limpeza total" de clientes arquivados (DELETE com subconsulta) precisa ser aceita
                try (Statement st = con.createStatement()) {
                    st.executeUpdate("UPDATE cliente SET ativo = 0 WHERE idCliente = 30");
                    st.executeUpdate("DELETE FROM relatorio_orcamento WHERE CLIENTE_idCliente IN (SELECT idCliente FROM cliente WHERE ativo = 0)");
                    st.executeUpdate("DELETE FROM servico WHERE CLIENTE_idCliente IN (SELECT idCliente FROM cliente WHERE ativo = 0)");
                    st.executeUpdate("DELETE FROM produto WHERE CLIENTE_idCliente IN (SELECT idCliente FROM cliente WHERE ativo = 0)");
                    int removidos = st.executeUpdate("DELETE FROM cliente WHERE ativo = 0");
                    confere("limpeza total de arquivados (clientes removidos)", 1, removidos);
                }
            } finally {
                con.rollback();
                con.setAutoCommit(true);
            }

            confere("clientes apos desfazer o teste", 30, contar(con, "SELECT COUNT(*) FROM cliente"));

        } catch (Throwable t) {
            falha("execucao", t.toString());
            t.printStackTrace(System.out);
        }

        if (falhas == 0) {
            System.out.println("=== AUTOTESTE OK: o banco embutido esta funcionando ===");
            System.exit(0);
        } else {
            System.out.println("=== AUTOTESTE COM " + falhas + " FALHA(S) ===");
            System.exit(1);
        }
    }
}
