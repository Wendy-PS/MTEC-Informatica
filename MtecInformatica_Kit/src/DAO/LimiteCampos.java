package DAO;

import java.awt.Toolkit;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

// Filtro simples para evitar textos enormes nos campos.
// A ideia é copiar o limite do banco para a tela, assim o usuário não consegue
// digitar mais caracteres do que o sistema consegue salvar com segurança.
public final class LimiteCampos {

    private LimiteCampos() {
    }

    public static void aplicar(JTextComponent campo, int limite) {
        if (campo == null || limite <= 0) {
            return;
        }

        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroLimite(limite));
    }

    private static class FiltroLimite extends DocumentFilter {
        private final int limite;

        FiltroLimite(int limite) {
            this.limite = limite;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, texto, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String texto, AttributeSet attr)
                throws BadLocationException {
            if (texto == null) {
                return;
            }

            int tamanhoAtual = fb.getDocument().getLength();
            int espacoLivre = limite - (tamanhoAtual - length);

            if (espacoLivre <= 0) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            String textoPermitido = texto;
            if (texto.length() > espacoLivre) {
                textoPermitido = texto.substring(0, espacoLivre);
                Toolkit.getDefaultToolkit().beep();
            }

            super.replace(fb, offset, length, textoPermitido, attr);
        }
    }
}
