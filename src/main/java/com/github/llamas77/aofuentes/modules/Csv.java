package com.github.llamas77.aofuentes.modules;

public class Csv {
    public int imageWidth;
    public int imageHeight;
    public int cellWidth;
    public int cellHeight;
    public int startChar;
    public String name;
    public int[] baseWidth;
    public int globalWOffset;

    public Csv() {
        baseWidth = new int[256];
    }
}
