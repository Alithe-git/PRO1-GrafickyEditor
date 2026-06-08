package uhk.grafika.gui;

import javax.swing.*;
import java.awt.*;

/* Levy panel s vyberem nastroje. */
public class ToolPanel extends JPanel {
    public ToolPanel(DrawingCanvas canvas) {
        setLayout(new GridLayout(0, 1, 4, 4));
        setBorder(BorderFactory.createTitledBorder("Nastroje"));
        ButtonGroup group = new ButtonGroup();
        for (Tool tool : Tool.values()) {
            JToggleButton btn = new JToggleButton(tool.toString());
            if (tool == Tool.SELECT) btn.setSelected(true);
            btn.addActionListener(e -> canvas.setTool(tool));
            group.add(btn);
            add(btn);
        }
    }
}
