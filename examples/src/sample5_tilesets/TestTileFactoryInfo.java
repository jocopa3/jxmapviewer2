/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample5_tilesets;

import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 *
 * @author Matt
 */
public class TestTileFactoryInfo extends TileFactoryInfo {

    private final static int TOP_ZOOM_LEVEL = 19;

    private final static int MAX_ZOOM_LEVEL = 17;

    private final static int MIN_ZOOM_LEVEL = 2;

    private final static int TILE_SIZE = 256;

    public TestTileFactoryInfo() {
        super("Testing", MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL, TOP_ZOOM_LEVEL, TILE_SIZE, false, false, "", "", "", "");
    }

    @Override
    public String getTileUrl(final int x, final int y, final int zoom) {
        return "";
    }

}
