package uhk.grafika.gui;

import uhk.grafika.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/* Panel pro zmenu barev, tloustky cary a rozmeru vybraneho objektu. */
public class PropertiesPanel extends JPanel {
    private final DrawingCanvas canvas;
    private Color strokeColor = Color.BLACK;
    private Color fillColor = Color.ORANGE;
    private final JCheckBox filledBox = new JCheckBox("Vypln");
    private final JTextField strokeWidthField = new JTextField("2", 5);
    private final JTextField geometryField = new JTextField(18);
    private final JLabel geometryHint = new JLabel("Geometrie:");

    public PropertiesPanel(DrawingCanvas canvas) {
        this.canvas = canvas;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Vlastnosti"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JButton strokeBtn = new JButton("Barva obrysu");
        strokeBtn.addActionListener(e -> chooseStroke());
        add(strokeBtn, gbc);

        gbc.gridy++;
        JButton fillBtn = new JButton("Barva vyplne");
        fillBtn.addActionListener(e -> chooseFill());
        add(fillBtn, gbc);

        gbc.gridy++;
        filledBox.addActionListener(e -> applyAsDefault());
        add(filledBox, gbc);

        gbc.gridy++;
        add(new JLabel("Tloustka cary:"), gbc);
        gbc.gridy++;
        add(strokeWidthField, gbc);

        gbc.gridy++;
        add(geometryHint, gbc);
        gbc.gridy++;
        add(geometryField, gbc);

        gbc.gridy++;
        JButton applyBtn = new JButton("Pouzit na objekt");
        applyBtn.addActionListener(e -> applyToSelected());
        add(applyBtn, gbc);

        gbc.gridy++;
        JButton defaultBtn = new JButton("Pouzit pro nove objekty");
        defaultBtn.addActionListener(e -> applyAsDefault());
        add(defaultBtn, gbc);

        updateForSelection();
    }

    private void chooseStroke() {
        Color c = JColorChooser.showDialog(this, "Vyber barvu obrysu", strokeColor);
        if (c != null) strokeColor = c;
        applyAsDefault();
    }

    private void chooseFill() {
        Color c = JColorChooser.showDialog(this, "Vyber barvu vyplne", fillColor);
        if (c != null) fillColor = c;
        applyAsDefault();
    }

    private void applyAsDefault() {
        try {
            canvas.updateDefaultStyle(strokeColor, fillColor, filledBox.isSelected(), parseStrokeWidth());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    public void updateForSelection() {
        GraphObject object = canvas.getSelectedObject();
        if (object == null) {
            geometryHint.setText("Geometrie:");
            geometryField.setText("");
            return;
        }
        strokeColor = object.getStrokeColor();
        fillColor = object.getFillColor();
        filledBox.setSelected(object.isFilled());
        strokeWidthField.setText(String.valueOf(object.getStrokeWidth()));
        Rectangle b = object.getBounds();
        if (object instanceof PolygonObject p) {
            geometryHint.setText("Body polygonu: x,y; x,y; x,y");
            geometryField.setText(pointsToText(p.getPointsCopy()));
        } else if (object instanceof LineObject) {
            geometryHint.setText("Usecka: x1,y1,x2,y2");
            geometryField.setText(b.x + "," + b.y + "," + (b.x + b.width) + "," + (b.y + b.height));
        } else {
            geometryHint.setText("Oblast: x,y,sirka,vyska");
            geometryField.setText(b.x + "," + b.y + "," + b.width + "," + b.height);
        }
    }

    private void applyToSelected() {
        GraphObject object = canvas.getSelectedObject();
        if (object == null) {
            showError("Nejdrive vyber objekt.");
            return;
        }
        try {
            object.setStrokeColor(strokeColor);
            object.setFillColor(fillColor);
            object.setFilled(filledBox.isSelected());
            object.setStrokeWidth(parseStrokeWidth());
            applyGeometry(object);
            canvas.refreshList();
            canvas.repaint();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void applyGeometry(GraphObject object) {
        String text = geometryField.getText().trim();
        if (text.isEmpty()) return;
        if (object instanceof PolygonObject p) {
            p.setPoints(parsePoints(text));
        } else {
            int[] n = parseInts(text, 4);
            if (object instanceof RectangleObject r) r.setGeometry(n[0], n[1], n[2], n[3]);
            else if (object instanceof EllipseObject e) e.setGeometry(n[0], n[1], n[2], n[3]);
            else if (object instanceof LineObject l) l.setGeometry(n[0], n[1], n[2], n[3]);
        }
    }

    private float parseStrokeWidth() {
        String text = strokeWidthField.getText().trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Tloustka cary nesmi byt prazdna.");
        try {
            float value = Float.parseFloat(text.replace(',', '.'));
            if (value <= 0) throw new IllegalArgumentException("Tloustka cary musi byt kladna.");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tloustka cary musi byt cislo.");
        }
    }

    private int[] parseInts(String text, int count) {
        String[] parts = text.split(",");
        if (parts.length != count) throw new IllegalArgumentException("Zadej presne " + count + " cisel oddelenych carkou.");
        int[] result = new int[count];
        try {
            for (int i = 0; i < count; i++) result[i] = Integer.parseInt(parts[i].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Geometrie obsahuje neplatne cislo.");
        }
        return result;
    }

    private List<Point> parsePoints(String text) {
        String[] pairs = text.split(";");
        List<Point> points = new ArrayList<>();
        for (String pair : pairs) {
            int[] xy = parseInts(pair.trim(), 2);
            points.add(new Point(xy[0], xy[1]));
        }
        if (points.size() < 3) throw new IllegalArgumentException("Polygon musi mit aspon 3 body.");
        return points;
    }

    private String pointsToText(List<Point> points) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) sb.append("; ");
            Point p = points.get(i);
            sb.append(p.x).append(',').append(p.y);
        }
        return sb.toString();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Chyba", JOptionPane.ERROR_MESSAGE);
    }
}
