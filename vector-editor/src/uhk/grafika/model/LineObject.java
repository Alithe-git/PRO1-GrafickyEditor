package uhk.grafika.model;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.LinkedHashMap;
import java.util.Map;

/* Objekt usecky. Vypln se u nej nepouziva. */
public class LineObject extends GraphObject {
    private int x1, y1, x2, y2;

    public LineObject(int x1, int y1, int x2, int y2) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    }

    @Override
    public void draw(Graphics2D g) {
        applyStyle(g);
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public boolean contains(Point p) {
        return Line2D.ptSegDist(x1, y1, x2, y2, p.x, p.y) <= Math.max(5, strokeWidth + 3);
    }

    @Override
    public void move(int dx, int dy) { x1 += dx; y1 += dy; x2 += dx; y2 += dy; }

    @Override
    public Rectangle getBounds() {
        int x = Math.min(x1, x2), y = Math.min(y1, y2);
        return new Rectangle(x, y, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public void setGeometry(int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2) throw new IllegalArgumentException("Usecka musi mit delku vetsi nez 0.");
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = addStyle(new LinkedHashMap<>());
        map.put("type", "line");
        map.put("x1", x1); map.put("y1", y1); map.put("x2", x2); map.put("y2", y2);
        return map;
    }

    public static LineObject fromMap(Map<String, Object> map) {
        LineObject o = new LineObject(num(map,"x1"), num(map,"y1"), num(map,"x2"), num(map,"y2"));
        o.loadStyle(map);
        return o;
    }

    private static int num(Map<String, Object> map, String key) { return ((Number) map.get(key)).intValue(); }
    @Override public String getTypeName() { return "Usecka"; }
}
