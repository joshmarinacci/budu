package com.joshondesign.tuneserver;

import java.io.PrintWriter;

public class JSONPrinter {

    int i = 0;
    private final PrintWriter out;
    private boolean first;

    public JSONPrinter(PrintWriter out) {
        this.out = out;
        first = true;
    }

    public void indent() {
        i++;
        first = true;
    }

    public void outdent() {
        i--;
    }

    public void p(String key, String value) {
        if(value == null || "null".equals(value)) {
            _p(key,"null");
        } else {
            _p(key,"\"" + value + "\"");
        }
    }
    public void p(String key, int value) {
        _p(key,""+value);
    }
    private void _p(String key, String value) {
        tab();
        if (first) {
            out.print(" ");
        } else {
            out.print(",");
        }
        out.print("\"" + key + "\":");
        out.println(value);
        if (first) {
            first = false;
        }
    }

    public void open() {
        tab();
        out.println("{");
    }

    public void close() {
        tab();
        out.println("}");
    }

    public  void tab() {
        for (int x = 0; x < i; x++) {
            out.print("    ");
        }
    }

    void println(String string) {
        tab();
        out.println(string);
    }

}
