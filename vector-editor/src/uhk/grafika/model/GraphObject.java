package uhk.grafika.model;

import java.awt.*;
import java.util.Map;

/*
 Abstraktni trida pro vsechny objekty v editoru.
 Kazdy objekt se umi vykreslit, vybrat, posunout a ulozit do JSON mapy.
*/
public abstract class GraphObject {
    protected Color strokeColor = Color.BLACK;
    protected Color fillColor = Color.WHITE;
    protected boolean filled = false;
    protected float strokeWidth = 2.0f;

    public abstract void draw(Graphics2D g);
    public abstract boolean contains(Point p);
    public abstract void move(int dx, int dy);
    public abstract Rectangle getBounds();
    public abstract Map<String, Object> toMap();
    public abstract String getTypeName();

    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }

    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }

    public boolean isFilled() { return filled; }
    public void setFilled(boolean filled) { this.filled = filled; }

    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = Math.max(1.0f, strokeWidth); }

    protected void applyStyle(Graphics2D g) {
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(strokeColor);
    }

    protected Map<String, Object> addStyle(Map<String, Object> map) {
        map.put("strokeColor", colorToHex(strokeColor));
        map.put("fillColor", colorToHex(fillColor));
        map.put("filled", filled);
        map.put("strokeWidth", strokeWidth);
        return map;
    }

    public void loadStyle(Map<String, Object> map) {
        strokeColor = hexToColor((String) map.getOrDefault("strokeColor", "#000000"));
        fillColor = hexToColor((String) map.getOrDefault("fillColor", "#ffffff"));
        filled = Boolean.TRUE.equals(map.get("filled"));
        Object sw = map.get("strokeWidth");
        if (sw instanceof Number) strokeWidth = ((Number) sw).floatValue();
    }

    public String colorToHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color hexToColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    @Override
    public String toString() {
        Rectangle b = getBounds();
        return getTypeName() + " [x=" + b.x + ", y=" + b.y + ", w=" + b.width + ", h=" + b.height + "]";
    }
}
