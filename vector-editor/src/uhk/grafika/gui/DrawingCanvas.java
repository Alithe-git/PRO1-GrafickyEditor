package uhk.grafika.gui;

import uhk.grafika.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/* Kreslici platno. Tady se resi mys, kresleni objektu, vyber a presouvani. */
public class DrawingCanvas extends JPanel {
    private final DefaultListModel<GraphObject> objectListModel;
    private Tool tool = Tool.SELECT;
    private GraphObject selectedObject;
    private Point startPoint;
    private Point lastMouse;
    private GraphObject previewObject;
    private final List<Point> polygonPoints = new ArrayList<>();
    private final java.util.List<Runnable> selectionListeners = new java.util.ArrayList<>();

    private Color strokeColor = Color.BLACK;
    private Color fillColor = Color.ORANGE;
    private boolean filled = false;
    private float strokeWidth = 2.0f;

    public DrawingCanvas(DefaultListModel<GraphObject> objectListModel) {
        this.objectListModel = objectListModel;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 650));
        MouseAdapter adapter = createMouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private MouseAdapter createMouseAdapter() {
        return new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                lastMouse = e.getPoint();

                if (tool == Tool.SELECT) {
                    selectAt(e.getPoint());
                } else if (tool == Tool.POLYGON) {
                    handlePolygonClick(e);
                }
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (tool == Tool.SELECT && selectedObject != null && lastMouse != null) {
                    int dx = e.getX() - lastMouse.x;
                    int dy = e.getY() - lastMouse.y;
                    selectedObject.move(dx, dy);
                    lastMouse = e.getPoint();
                    refreshList();
                    repaint();
                    return;
                }

                if (tool == Tool.RECTANGLE || tool == Tool.ELLIPSE || tool == Tool.LINE) {
                    previewObject = createObject(startPoint, e.getPoint());
                    repaint();
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (tool == Tool.RECTANGLE || tool == Tool.ELLIPSE || tool == Tool.LINE) {
                    GraphObject object = createObject(startPoint, e.getPoint());
                    if (isBigEnough(object)) {
                        applyCurrentStyle(object);
                        objectListModel.addElement(object);
                        setSelectedObject(object);
                    }
                    previewObject = null;
                    repaint();
                }
            }
        };
    }

    private void handlePolygonClick(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() >= 2 && polygonPoints.size() >= 3) {
                finishPolygon();
                return;
            }
            if (!polygonPoints.isEmpty() && polygonPoints.get(0).distance(e.getPoint()) < 10 && polygonPoints.size() >= 3) {
                finishPolygon();
                return;
            }
            polygonPoints.add(e.getPoint());
            repaint();
        }
    }

    private void finishPolygon() {
        try {
            PolygonObject polygon = new PolygonObject(polygonPoints);
            applyCurrentStyle(polygon);
            objectListModel.addElement(polygon);
            setSelectedObject(polygon);
            polygonPoints.clear();
            repaint();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private GraphObject createObject(Point a, Point b) {
        if (tool == Tool.RECTANGLE) return new RectangleObject(a.x, a.y, b.x - a.x, b.y - a.y);
        if (tool == Tool.ELLIPSE) return new EllipseObject(a.x, a.y, b.x - a.x, b.y - a.y);
        if (tool == Tool.LINE) return new LineObject(a.x, a.y, b.x, b.y);
        return null;
    }

    private boolean isBigEnough(GraphObject object) {
        if (object == null) return false;
        Rectangle b = object.getBounds();
        return object instanceof LineObject ? (b.width + b.height > 3) : (b.width > 3 && b.height > 3);
    }

    private void applyCurrentStyle(GraphObject object) {
        object.setStrokeColor(strokeColor);
        object.setFillColor(fillColor);
        object.setFilled(filled);
        object.setStrokeWidth(strokeWidth);
    }

    private void selectAt(Point p) {
        for (int i = objectListModel.size() - 1; i >= 0; i--) {
            GraphObject object = objectListModel.get(i);
            if (object.contains(p)) {
                setSelectedObject(object);
                return;
            }
        }
        setSelectedObject(null);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < objectListModel.size(); i++) objectListModel.get(i).draw(g2);
        if (previewObject != null) previewObject.draw(g2);
        drawTempPolygon(g2);
        drawSelection(g2);
        g2.dispose();
    }

    private void drawTempPolygon(Graphics2D g2) {
        if (polygonPoints.isEmpty()) return;
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < polygonPoints.size(); i++) {
            Point p = polygonPoints.get(i);
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);
            if (i > 0) {
                Point prev = polygonPoints.get(i - 1);
                g2.drawLine(prev.x, prev.y, p.x, p.y);
            }
        }
    }

    private void drawSelection(Graphics2D g2) {
        if (selectedObject == null) return;
        Rectangle b = selectedObject.getBounds();
        g2.setColor(Color.BLUE);
        float[] dash = {6, 4};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0));
        g2.drawRect(b.x - 4, b.y - 4, Math.max(8, b.width + 8), Math.max(8, b.height + 8));
        int[][] handles = {{b.x, b.y}, {b.x + b.width, b.y}, {b.x, b.y + b.height}, {b.x + b.width, b.y + b.height}};
        g2.setStroke(new BasicStroke(1));
        for (int[] h : handles) g2.fillRect(h[0] - 4, h[1] - 4, 8, 8);
    }

    public void setTool(Tool tool) {
        this.tool = tool;
        polygonPoints.clear();
        previewObject = null;
        repaint();
    }

    public void setSelectedObject(GraphObject selectedObject) {
        this.selectedObject = selectedObject;
        for (Runnable listener : selectionListeners) listener.run();
        repaint();
    }

    public GraphObject getSelectedObject() { return selectedObject; }
    public List<GraphObject> getObjects() { return java.util.Collections.list(objectListModel.elements()); }

    public void setObjects(List<GraphObject> objects) {
        objectListModel.clear();
        for (GraphObject object : objects) objectListModel.addElement(object);
        setSelectedObject(null);
        repaint();
    }

    public void deleteSelected() {
        if (selectedObject != null) {
            objectListModel.removeElement(selectedObject);
            setSelectedObject(null);
        }
    }

    public void clearAll() {
        objectListModel.clear();
        polygonPoints.clear();
        setSelectedObject(null);
    }

    public void bringForward() {
        moveLayer(1);
    }

    public void sendBackward() {
        moveLayer(-1);
    }

    private void moveLayer(int dir) {
        if (selectedObject == null) return;

        GraphObject movedObject = selectedObject;

        int index = objectListModel.indexOf(movedObject);
        if (index < 0) return;

        int newIndex = index + dir;
        if (newIndex < 0 || newIndex >= objectListModel.size()) return;

        objectListModel.remove(index);
        objectListModel.add(newIndex, movedObject);
        setSelectedObject(movedObject);
        repaint();
    }

    public void updateDefaultStyle(Color stroke, Color fill, boolean filled, float strokeWidth) {
        this.strokeColor = stroke;
        this.fillColor = fill;
        this.filled = filled;
        this.strokeWidth = strokeWidth;
    }

    public void refreshList() {
        int idx = selectedObject == null ? -1 : objectListModel.indexOf(selectedObject);
        objectListModel.setSize(objectListModel.size());
        if (idx >= 0) objectListModel.set(idx, selectedObject);
    }

    public void addSelectionChangedListener(Runnable listener) { selectionListeners.add(listener); }
}
