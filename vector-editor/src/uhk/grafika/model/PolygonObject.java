package uhk.grafika.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* Polygon slozeny z vice bodu. Body se daji ulozit i upravit v panelu vlastnosti. */
public class PolygonObject extends GraphObject {
    private final List<Point> points = new ArrayList<>();

    public PolygonObject(List<Point> points) {
        if (points.size() < 3) throw new IllegalArgumentException("Polygon musi mit aspon 3 body.");
        for (Point p : points) this.points.add(new Point(p));
    }

    @Override
    public void draw(Graphics2D g) {
        Polygon polygon = toAwtPolygon();
        if (filled) {
            g.setColor(fillColor);
            g.fillPolygon(polygon);
        }
        applyStyle(g);
        g.drawPolygon(polygon);
    }

    @Override
    public boolean contains(Point p) {
        Polygon polygon = toAwtPolygon();
        if (polygon.contains(p)) return true;
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            if (java.awt.geom.Line2D.ptSegDist(a.x, a.y, b.x, b.y, p.x, p.y) <= Math.max(5, strokeWidth + 3)) return true;
        }
        return false;
    }

    @Override
    public void move(int dx, int dy) {
        for (Point p : points) p.translate(dx, dy);
    }

    @Override
    public Rectangle getBounds() { return toAwtPolygon().getBounds(); }

    public List<Point> getPointsCopy() {
        List<Point> copy = new ArrayList<>();
        for (Point p : points) copy.add(new Point(p));
        return copy;
    }

    public void setPoints(List<Point> newPoints) {
        if (newPoints.size() < 3) throw new IllegalArgumentException("Polygon musi mit aspon 3 body.");
        points.clear();
        for (Point p : newPoints) points.add(new Point(p));
    }

    private Polygon toAwtPolygon() {
        Polygon polygon = new Polygon();
        for (Point p : points) polygon.addPoint(p.x, p.y);
        return polygon;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = addStyle(new LinkedHashMap<>());
        map.put("type", "polygon");
        List<Object> arr = new ArrayList<>();
        for (Point p : points) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("x", p.x); pm.put("y", p.y);
            arr.add(pm);
        }
        map.put("points", arr);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static PolygonObject fromMap(Map<String, Object> map) {
        List<Point> pts = new ArrayList<>();
        List<Object> arr = (List<Object>) map.get("points");
        for (Object o : arr) {
            Map<String, Object> pm = (Map<String, Object>) o;
            pts.add(new Point(((Number) pm.get("x")).intValue(), ((Number) pm.get("y")).intValue()));
        }
        PolygonObject po = new PolygonObject(pts);
        po.loadStyle(map);
        return po;
    }

    @Override public String getTypeName() { return "Polygon"; }
}
