package uhk.grafika.io;

import uhk.grafika.model.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/* Ukladani sceny do JSON a export platna do PNG. */
public class SceneIO {
    public static void saveJson(File file, List<GraphObject> objects) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", 1);
        List<Object> arr = new ArrayList<>();
        for (GraphObject object : objects) arr.add(object.toMap());
        root.put("objects", arr);
        Files.writeString(file.toPath(), MiniJson.stringify(root), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public static List<GraphObject> loadJson(File file) throws IOException {
        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        Object parsed = MiniJson.parse(text);
        if (!(parsed instanceof Map<?, ?>)) throw new IOException("Soubor nema spravny JSON format.");
        Map<String, Object> root = (Map<String, Object>) parsed;
        Object rawObjects = root.get("objects");
        if (!(rawObjects instanceof List<?>)) throw new IOException("V JSON souboru chybi pole objects.");

        List<GraphObject> result = new ArrayList<>();
        for (Object item : (List<Object>) rawObjects) {
            Map<String, Object> map = (Map<String, Object>) item;
            String type = (String) map.get("type");
            switch (type) {
                case "rectangle" -> result.add(RectangleObject.fromMap(map));
                case "ellipse" -> result.add(EllipseObject.fromMap(map));
                case "line" -> result.add(LineObject.fromMap(map));
                case "polygon" -> result.add(PolygonObject.fromMap(map));
                default -> throw new IOException("Neznamy typ objektu: " + type);
            }
        }
        return result;
    }

    public static void exportPng(File file, Component canvas, java.util.List<GraphObject> objects) throws IOException {
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (GraphObject object : objects) object.draw(g);
        g.dispose();
        ImageIO.write(image, "png", file);
    }
}
