/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import DAO.frmSplash;


/**
 *
 * @author maril
 */
public class Main {
        public static void main(String[] args) {

        // Prepara o banco embutido em segundo plano enquanto a splash aparece
        // (na 1a abertura ele e criado aqui; se der erro, a mensagem aparece no login).
        Thread aquecerBanco = new Thread(() -> {
            try { ConnectionFactory.getConnection().close(); } catch (Exception e) { /* tratado no login */ }
        }, "mtec-banco");
        aquecerBanco.setDaemon(true);
        aquecerBanco.start();

          // chamar a tela splash para iniciar nosso programa
        frmSplash frms = new frmSplash();
        frms.setVisible(true);
        
        
    }
    
}


