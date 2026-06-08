package uhk.grafika.gui;

/* Nastroje, ktere muze uzivatel vybrat v levem panelu. */
public enum Tool {
    SELECT("Vyber"), RECTANGLE("Obdelnik"), ELLIPSE("Elipsa"), POLYGON("Polygon"), LINE("Usecka");

    private final String label;
    Tool(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
