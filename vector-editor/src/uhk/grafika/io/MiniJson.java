package uhk.grafika.io;

import java.util.*;

/*
 Jednoduchy JSON zapisovac a parser bez externich knihoven.
 Staci pro strukturu tohoto projektu: mapy, seznamy, cisla, boolean a stringy.
*/
public class MiniJson {
    public static String stringify(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escape((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(',');
                sb.append('\n').append(stringify(String.valueOf(e.getKey()))).append(':').append(stringify(e.getValue()));
                first = false;
            }
            if (!first) sb.append('\n');
            return sb.append('}').toString();
        }
        if (value instanceof Iterable<?> it) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object o : it) {
                if (!first) sb.append(',');
                sb.append('\n').append(stringify(o));
                first = false;
            }
            if (!first) sb.append('\n');
            return sb.append(']').toString();
        }
        return stringify(String.valueOf(value));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static Object parse(String text) {
        return new Parser(text).parseValue();
    }

    private static class Parser {
        private final String s;
        private int i = 0;

        Parser(String s) { this.s = s; }

        Object parseValue() {
            skipWs();
            if (i >= s.length()) throw err("Neocekavany konec JSON souboru");
            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') { i += 4; return null; }
            return parseNumber();
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{'); skipWs();
            if (peek('}')) { i++; return map; }
            while (true) {
                String key = parseString();
                skipWs(); expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWs();
                if (peek('}')) { i++; break; }
                expect(',');
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('['); skipWs();
            if (peek(']')) { i++; return list; }
            while (true) {
                list.add(parseValue());
                skipWs();
                if (peek(']')) { i++; break; }
                expect(',');
            }
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (i >= s.length()) throw err("Spatny escape ve stringu");
                    char e = s.charAt(i++);
                    if (e == 'n') sb.append('\n');
                    else sb.append(e);
                } else sb.append(c);
            }
            throw err("Neukonceny string");
        }

        Boolean parseBoolean() {
            if (s.startsWith("true", i)) { i += 4; return true; }
            if (s.startsWith("false", i)) { i += 5; return false; }
            throw err("Spatna boolean hodnota");
        }

        Number parseNumber() {
            int start = i;
            while (i < s.length() && "-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String n = s.substring(start, i);
            try {
                if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
                return Integer.parseInt(n);
            } catch (NumberFormatException e) {
                throw err("Spatne cislo: " + n);
            }
        }

        void skipWs() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        boolean peek(char c) { skipWs(); return i < s.length() && s.charAt(i) == c; }
        void expect(char c) { skipWs(); if (i >= s.length() || s.charAt(i++) != c) throw err("Ocekavam znak " + c); }
        RuntimeException err(String msg) { return new RuntimeException(msg + " na pozici " + i); }
    }
}
