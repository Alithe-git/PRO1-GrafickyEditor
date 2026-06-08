package uhk.grafika.gui;

import uhk.grafika.model.GraphObject;

import javax.swing.*;
import java.awt.*;

/* Pravy panel se seznamem objektu a tlacitky pro vrstvy/mazani. */
public class ObjectListPanel extends JPanel {
    public ObjectListPanel(DrawingCanvas canvas, DefaultListModel<GraphObject> model) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Objekty"));
        JList<GraphObject> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) canvas.setSelectedObject(list.getSelectedValue());
        });
        canvas.addSelectionChangedListener(() -> list.setSelectedValue(canvas.getSelectedObject(), true));
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 4, 4));
        JButton delete = new JButton("Smazat vybrany");
        delete.addActionListener(e -> canvas.deleteSelected());
        JButton clear = new JButton("Vymazat platno");
        clear.addActionListener(e -> canvas.clearAll());
        JButton forward = new JButton("Dopredu");
        forward.addActionListener(e -> canvas.bringForward());
        JButton backward = new JButton("Dozadu");
        backward.addActionListener(e -> canvas.sendBackward());
        buttons.add(forward); buttons.add(backward); buttons.add(delete); buttons.add(clear);
        add(buttons, BorderLayout.SOUTH);
    }
}
