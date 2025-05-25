/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pagina;

import back_end.Classes.Usuario;
import back_end.Excepciones.PersistenciaException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Login_1 extends javax.swing.JFrame {

    FondoPanel fondo = new FondoPanel();
    private boolean intentandoLogin = false;

    public Login_1() {
        this.setContentPane(fondo);
        initComponents();
        setSize(1201, 718);
    }
    
    private void configurarEventos() {
        // Permitir login con Enter en el campo de contraseña
        password.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Permitir pasar al siguiente campo con Enter
        username.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    password.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }
    
    private void realizarLogin() {
        // Evitar múltiples intentos simultáneos
        if (intentandoLogin) {
            return;
        }

        intentandoLogin = true;
        login.setEnabled(false);
        login.setText("Verificando...");

        // Usar SwingWorker para no bloquear la UI
        SwingUtilities.invokeLater(() -> {
            try {
                String correo = username.getText().trim();
                String contraseña = new String(password.getPassword());

                // Validar campos vacíos
                if (correo.isEmpty()) {
                    mostrarError("Por favor, ingrese su correo electrónico", "Campo vacío");
                    username.requestFocus();
                    return;
                }

                if (contraseña.isEmpty()) {
                    mostrarError("Por favor, ingrese su contraseña", "Campo vacío");
                    password.requestFocus();
                    return;
                }

                // Validar formato de correo básico
                if (!validarFormatoCorreo(correo)) {
                    mostrarError("El formato del correo electrónico no es válido", "Correo inválido");
                    username.requestFocus();
                    return;
                }

                // Intentar iniciar sesión
                Usuario usuarioLogueado = Usuario.iniciarSesion(correo, contraseña);

                if (usuarioLogueado != null) {
                    
                    // Abrir ventana principal
                    abrirVentanaPrincipal(usuarioLogueado);
                } else {
                    // Credenciales incorrectas
                    mostrarError("Correo o contraseña incorrectos.\nVerifique sus datos e intente nuevamente.", "Error de autenticación");
                    limpiarCampos();
                    username.requestFocus();
                }

            } catch (PersistenciaException e) {
                mostrarError("Error al conectar con la base de datos:\n" + e.getMessage(), "Error de Base de Datos");
                System.err.println("Error de persistencia: " + e.getMessage());
            } catch (Exception e) {
                mostrarError("Error inesperado del sistema:\n" + e.getMessage(), "Error del Sistema");
                e.printStackTrace();
            } finally {
                // Restaurar estado del botón
                restaurarBotonLogin();
            }
        });
    }
    
    private boolean validarFormatoCorreo(String correo) {
        String patronCorreo = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return correo.matches(patronCorreo);
    }
    
    private void abrirVentanaPrincipal(Usuario usuario) {
        try {
            Lugares_inicio ventana = new Lugares_inicio(usuario);
            ventana.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            mostrarError("Error al abrir la ventana principal:\n" + e.getMessage(), "Error de Sistema");
            restaurarBotonLogin();
        }
    }
    
    private void limpiarCampos() {
        password.setText("");
        // No limpiar el username para que el usuario no tenga que escribirlo de nuevo
    }

    private void restaurarBotonLogin() {
        login.setEnabled(true);
        login.setText("Log in");
        intentandoLogin = false;
    }
    
    private void mostrarError(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        login = new javax.swing.JButton();
        registarte = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255, 70));

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 0, 24)); // NOI18N
        jLabel1.setText("Log in");

        jLabel2.setFont(new java.awt.Font("Segoe UI Light", 1, 14)); // NOI18N
        jLabel2.setText("Correo:");

        jLabel3.setFont(new java.awt.Font("Segoe UI Light", 1, 14)); // NOI18N
        jLabel3.setText("Password:");

        login.setBackground(new java.awt.Color(0, 0, 0));
        login.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        login.setForeground(new java.awt.Color(255, 255, 255));
        login.setText("Log in");
        login.setAutoscrolls(true);
        login.setBorderPainted(false);
        login.setFocusPainted(false);
        login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loginMouseClicked(evt);
            }
        });
        login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginActionPerformed(evt);
            }
        });

        registarte.setFont(new java.awt.Font("Segoe UI Light", 1, 14)); // NOI18N
        registarte.setText("¿No tienes cuenta? Regístrate");
        registarte.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                registarteMouseClicked(evt);
            }
        });

        password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                            .addComponent(password)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(registarte))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(246, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(198, 198, 198))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(login)
                        .addGap(181, 181, 181))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addComponent(jLabel1)
                .addGap(64, 64, 64)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(registarte)
                .addGap(64, 64, 64)
                .addComponent(login)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/apuestas-online.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(106, 106, 106))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(246, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(113, 113, 113))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void loginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginMouseClicked
        realizarLogin();
    }//GEN-LAST:event_loginMouseClicked

    private void loginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginActionPerformed
        realizarLogin();
    }//GEN-LAST:event_loginActionPerformed

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        realizarLogin();
    }//GEN-LAST:event_passwordActionPerformed

    private void registarteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registarteMouseClicked
        try {
            Registrarse ventana = new Registrarse();
            ventana.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            mostrarError("Error al abrir la ventana de registro:\n" + e.getMessage(), "Error del Sistema");
        }
    }//GEN-LAST:event_registarteMouseClicked

    private void registarteMouseEntered(java.awt.event.MouseEvent evt) {
        
        registarte.setForeground(new java.awt.Color(0, 51, 153)); 
        
    }
    
    private void registarteMouseExited(java.awt.event.MouseEvent evt) {
        
        registarte.setForeground(new java.awt.Color(0, 102, 204));
        
    }

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Login_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Login_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Login_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Login_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Login_1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton login;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel registarte;
    private javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables
    class FondoPanel extends JPanel {

        private Image imagen;

        @Override
        public void paint(Graphics g) {
            imagen = new ImageIcon(getClass().getResource("/icons/fondo_login.png")).getImage();
            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            setOpaque(false);
            super.paint(g);
        }
    }
}
