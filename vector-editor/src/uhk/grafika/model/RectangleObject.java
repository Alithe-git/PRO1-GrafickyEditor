package uhk.grafika.model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/* Objekt obdelniku. Uklada levy horni roh a rozmery. */
public class RectangleObject extends GraphObject {
    private int x, y, width, height;

    public RectangleObject(int x, int y, int width, int height) {
        this.x = Math.min(x, x + width);
        this.y = Math.min(y, y + height);
        this.width = Math.abs(width);
        this.height = Math.abs(height);
    }

    @Override
    public void draw(Graphics2D g) {
        if (filled) {
            g.setColor(fillColor);
            g.fillRect(x, y, width, height);
        }
        applyStyle(g);
        g.drawRect(x, y, width, height);
    }

    @Override
    public boolean contains(Point p) {
        return new Rectangle(x, y, width, height).contains(p);
    }

    @Override
    public void move(int dx, int dy) { x += dx; y += dy; }

    @Override
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void setGeometry(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Sirka a vyska musi byt kladne cislo.");
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = addStyle(new LinkedHashMap<>());
        map.put("type", "rectangle");
        map.put("x", x); map.put("y", y); map.put("width", width); map.put("height", height);
        return map;
    }

    public static RectangleObject fromMap(Map<String, Object> map) {
        RectangleObject o = new RectangleObject(num(map,"x"), num(map,"y"), num(map,"width"), num(map,"height"));
        o.loadStyle(map);
        return o;
    }

    private static int num(Map<String, Object> map, String key) { return ((Number) map.get(key)).intValue(); }
    @Override public String getTypeName() { return "Obdelnik"; }
}
