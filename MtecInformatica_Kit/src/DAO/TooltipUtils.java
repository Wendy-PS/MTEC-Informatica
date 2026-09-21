package DAO;

import javax.swing.AbstractButton;

public final class TooltipUtils {
    private TooltipUtils() {}

    public static void aplicarTooltipPadrao(AbstractButton botao) {
        if (botao == null) return;
        String atual = botao.getToolTipText();
        if (atual != null && !atual.trim().isEmpty()) return;
        String dica = tooltipBotao(botao.getText());
        if (dica != null && !dica.isEmpty()) {
            botao.setToolTipText(dica);
        }
    }

    public static String tooltipBotao(String texto) {
        if (texto == null) return "";
        String txt = texto.trim();
        String upper = txt.toUpperCase();

        switch (upper) {
            case "INSERIR":
                return "Cadastrar um novo registro.";
            case "ATUALIZAR":
                return "Salvar as alterações do registro em edição.";
            case "APAGAR":
            case "CANCELAR":
                return "Limpar os campos e cancelar a edição.";
            case "ARQUIVOS":
                return "Abrir a área de arquivos ou registros inativos.";
            case "STATUS":
                return "Abrir a área de status ou registros inativos.";
            case "LIMPAR":
                return "Limpar o campo de pesquisa.";
            case "EDITAR":
                return "Carregar este registro para edição.";
            case "EXCLUIR":
                return "Excluir este registro.";
            case "INATIVAR":
                return "Inativar este registro.";
            case "ATIVAR":
                return "Ativar este registro.";
            case "GERAR PDF":
                return "Gerar um PDF com os dados do orçamento.";
            case "IMPRIMIR":
                return "Imprimir o relatório exibido na tela.";
            case "ATUALIZAR DASHBOARD":
                return "Atualizar os dados exibidos no dashboard.";
            case "CONFIRMAR":
                return "Confirmar a seleção.";
            case "HOJE":
                return "Selecionar a data de hoje.";
            default:
                if (txt.contains("📅")) return "Escolher data no calendário";
                return "";
        }
    }
}
