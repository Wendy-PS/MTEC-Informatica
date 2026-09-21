package DAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Fábrica de conexões do sistema MTEC.
 *
 * MODO PADRÃO (embutido): usa o banco H2 gravado em arquivo, dentro do próprio computador
 * do usuário - não precisa instalar MySQL nem configurar nada. Na primeira abertura o banco
 * é criado sozinho a partir de /bancoDeDados/mtec_h2.sql (estrutura + dados iniciais).
 *
 * Onde ficam os dados:  %APPDATA%\MtecInformatica\dados   (Windows)
 *                       ~/.MtecInformatica/dados          (outros sistemas)
 *
 * MODO OPCIONAL (mysql): se existir o arquivo "conexao.properties" na pasta de dados com
 * modo=mysql, o sistema volta a usar um servidor MySQL, como na versão original.
 */
public class ConnectionFactory {

    private static final String NOME_BANCO = "mtec_db";
    private static final String SCRIPT_INICIAL = "/bancoDeDados/mtec_h2.sql";

    // Parâmetros do H2: modo de compatibilidade com MySQL e nomes de tabela/coluna em minúsculas
    // (igual ao MySQL). DB_CLOSE_DELAY=-1 mantém o banco aberto enquanto o programa roda.
    private static final String PARAMS_H2 =
        ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1";

    private static boolean iniciado = false;
    private static boolean modoMysql = false;
    private static String url;
    private static String usuario;
    private static String senha;

    public static Connection getConnection() {
        try {
            iniciarSeNecessario();
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            throw new RuntimeException(montarMensagemErro(e));
        }
    }

    /** Pasta onde ficam o banco de dados e o arquivo de configuração. */
    public static File getPastaDados() {
        String forcada = System.getProperty("mtec.dados");
        if (forcada != null && !forcada.trim().isEmpty()) {
            return new File(forcada);
        }
        String appData = System.getenv("APPDATA");
        File base = (appData != null && !appData.trim().isEmpty())
                ? new File(appData, "MtecInformatica")
                : new File(System.getProperty("user.home"), ".MtecInformatica");
        return new File(base, "dados");
    }

    // ------------------------------------------------------------------------------------

    private static synchronized void iniciarSeNecessario() throws SQLException {
        if (iniciado) return;

        File pasta = getPastaDados();
        if (!pasta.exists() && !pasta.mkdirs()) {
            throw new SQLException("Não foi possível criar a pasta de dados: " + pasta.getAbsolutePath());
        }

        Properties cfg = lerConfiguracao(pasta);
        modoMysql = "mysql".equalsIgnoreCase(cfg.getProperty("modo", "embutido").trim());

        if (modoMysql) {
            url = cfg.getProperty("mysql.url", "jdbc:mysql://localhost:3306/mtec_db");
            usuario = cfg.getProperty("mysql.usuario", "root");
            senha = cfg.getProperty("mysql.senha", "root");
        } else {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver do banco embutido (H2) não encontrado no programa.");
            }
            String caminho = new File(pasta, NOME_BANCO).getAbsolutePath().replace('\\', '/');
            url = "jdbc:h2:file:" + caminho + PARAMS_H2;
            usuario = "sa";
            senha = "";
            criarBancoSeForNovo();
        }
        iniciado = true;
    }

    /** Lê conexao.properties; se não existir, cria um modelo comentado com o modo embutido. */
    private static Properties lerConfiguracao(File pasta) {
        Properties p = new Properties();
        File arq = new File(pasta, "conexao.properties");
        try {
            if (!arq.exists()) {
                String modelo =
                    "# Configuração de conexão do sistema MTEC\n" +
                    "# modo=embutido  -> banco H2 dentro do próprio computador (padrão, não precisa de instalação)\n" +
                    "# modo=mysql     -> usa um servidor MySQL (preencha as linhas abaixo)\n" +
                    "modo=embutido\n" +
                    "#mysql.url=jdbc:mysql://localhost:3306/mtec_db\n" +
                    "#mysql.usuario=root\n" +
                    "#mysql.senha=root\n";
                try (OutputStream os = Files.newOutputStream(arq.toPath())) {
                    os.write(modelo.getBytes(StandardCharsets.UTF_8));
                }
            }
            try (InputStream is = Files.newInputStream(arq.toPath());
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                p.load(br);
            }
        } catch (IOException e) {
            // sem arquivo de configuração legível: segue com o padrão (embutido)
        }
        return p;
    }

    /** Se o banco ainda não tem as tabelas, cria tudo a partir do script embutido no programa. */
    private static void criarBancoSeForNovo() throws SQLException {
        try (Connection con = DriverManager.getConnection(url, usuario, senha)) {
            boolean existe;
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) = 'tecnico'")) {
                rs.next();
                existe = rs.getInt(1) > 0;
            }
            if (existe) return;

            InputStream is = ConnectionFactory.class.getResourceAsStream(SCRIPT_INICIAL);
            if (is == null) {
                throw new SQLException("Script inicial do banco não encontrado: " + SCRIPT_INICIAL);
            }
            con.setAutoCommit(false);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement st = con.createStatement()) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    String comando = linha.trim();
                    if (comando.isEmpty() || comando.startsWith("--")) continue;
                    if (comando.endsWith(";")) comando = comando.substring(0, comando.length() - 1);
                    st.execute(comando);
                }
                con.commit();
            } catch (IOException | SQLException e) {
                con.rollback();
                throw (e instanceof SQLException) ? (SQLException) e : new SQLException(e.getMessage(), e);
            }
        }
    }

    private static String montarMensagemErro(SQLException e) {
        String msg = String.valueOf(e.getMessage());
        // 90020 = banco H2 já aberto por outro processo (sistema aberto duas vezes)
        if (e.getErrorCode() == 90020 || msg.contains("already in use") || msg.contains("Locked by another process")) {
            return "O sistema MTEC já está aberto em outra janela. Feche-o e tente de novo.";
        }
        return "Erro na conexão: " + e;
    }
}
