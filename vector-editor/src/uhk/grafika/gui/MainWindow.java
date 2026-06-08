package uhk.grafika.gui;

import uhk.grafika.io.SceneIO;
import uhk.grafika.model.GraphObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/* Hlavni okno cele aplikace. Sklada dohromady platno, panely a menu. */
public class MainWindow extends JFrame {
    private final DefaultListModel<GraphObject> listModel = new DefaultListModel<>();
    private final DrawingCanvas canvas = new DrawingCanvas(listModel);
    private final PropertiesPanel propertiesPanel = new PropertiesPanel(canvas);

    public MainWindow() {
        super("Jednoduchy vektorovy editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));
        setJMenuBar(createMenuBar());

        add(new ToolPanel(canvas), BorderLayout.WEST);
        add(new JScrollPane(canvas), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.add(new ObjectListPanel(canvas, listModel), BorderLayout.CENTER);
        right.add(propertiesPanel, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        canvas.addSelectionChangedListener(() -> propertiesPanel.updateForSelection());

        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Soubor");

        JMenuItem save = new JMenuItem("Ulozit JSON");
        save.addActionListener(e -> saveJson());
        JMenuItem load = new JMenuItem("Nacist JSON");
        load.addActionListener(e -> loadJson());
        JMenuItem export = new JMenuItem("Export PNG");
        export.addActionListener(e -> exportPng());
        JMenuItem exit = new JMenuItem("Konec");
        exit.addActionListener(e -> dispose());

        file.add(save); file.add(load); file.add(export); file.addSeparator(); file.add(exit);
        bar.add(file);
        return bar;
    }

    private void saveJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Ulozit scenu jako JSON");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".json");
            try {
                SceneIO.saveJson(file, canvas.getObjects());
                JOptionPane.showMessageDialog(this, "Scena byla ulozena.");
            } catch (Exception ex) {
                showError("Nepodarilo se ulozit soubor: " + ex.getMessage());
            }
        }
    }

    private void loadJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Nacist scenu z JSON");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                canvas.setObjects(SceneIO.loadJson(chooser.getSelectedFile()));
                JOptionPane.showMessageDialog(this, "Scena byla nactena.");
            } catch (Exception ex) {
                showError("Nepodarilo se nacist soubor: " + ex.getMessage());
            }
        }
    }

    private void exportPng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportovat jako PNG");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), ".png");
            try {
                SceneIO.exportPng(file, canvas, canvas.getObjects());
                JOptionPane.showMessageDialog(this, "Obrazek byl exportovan.");
            } catch (Exception ex) {
                showError("Nepodarilo se exportovat PNG: " + ex.getMessage());
            }
        }
    }

    private File ensureExtension(File file, String ext) {
        if (file.getName().toLowerCase().endsWith(ext)) return file;
        return new File(file.getParentFile(), file.getName() + ext);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Chyba", JOptionPane.ERROR_MESSAGE);
    }
}
