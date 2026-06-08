package uhk.grafika;

import uhk.grafika.gui.MainWindow;

import javax.swing.*;

/* Start aplikace. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
