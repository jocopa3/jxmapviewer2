/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample5_tilesets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import org.jxmapviewer.viewer.Tile;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 *
 * @author Matt
 */
public class TestTileFactory extends TileFactory {

    public TestTileFactory(TileFactoryInfo info) {
        super(info);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        System.out.println("getTile:"+x+", "+y+", "+zoom);
        Tile tile = new Tile(x, y, zoom);
        startLoading(tile);
        return tile;
    }

    @Override
    public void dispose() {

    }
    public static final int TILESIZE = 256, HALF_WORLDSIZE = 1 << 20;

    public static int worldSizeInBlocks = 2 * HALF_WORLDSIZE,
            viewSizeW = worldSizeInBlocks * TILESIZE / Dimension.OVERWORLD.chunkW,
            viewSizeL = worldSizeInBlocks * TILESIZE / Dimension.OVERWORLD.chunkL;

    @Override
    protected void startLoading(Tile tile) {
        System.out.println("startLoading");
        final BufferedImage img = tile.getImage() == null ? new BufferedImage(TILESIZE, TILESIZE, BufferedImage.TYPE_USHORT_565_RGB) : tile.getImage();
        
        Dimension dimension = Dimension.OVERWORLD;
        // 1 chunk per tile on scale 1.0
        int pixelsPerBlockW_unscaled = TILESIZE / dimension.chunkW;
        int pixelsPerBlockL_unscaled = TILESIZE / dimension.chunkL;
        MapRenderer renderer = new ChessPatternRenderer(0xFF2B0000, 0xFF580000);
        float scale = tile.getZoom();

        // this will be the amount of chunks in the width of one tile
        int invScale = Math.round(1f / scale);

        //scale the amount of pixels, less pixels per block if zoomed out
        int pixelsPerBlockW = Math.round(pixelsPerBlockW_unscaled * scale);
        int pixelsPerBlockL = Math.round(pixelsPerBlockL_unscaled * scale);

        // for translating to origin
        // HALF_WORLDSIZE and TILESIZE must be a power of two
        int tilesInHalfWorldW = (HALF_WORLDSIZE * pixelsPerBlockW) / TILESIZE;
        int tilesInHalfWorldL = (HALF_WORLDSIZE * pixelsPerBlockL) / TILESIZE;

        // translate tile coord to origin, multiply origin-relative-tile-coordinate with the chunks per tile
        int minChunkX = (tile.getX() - tilesInHalfWorldW) * invScale;
        int minChunkZ = (tile.getY() - tilesInHalfWorldL) * invScale;
        int maxChunkX = minChunkX + invScale;
        int maxChunkZ = minChunkZ + invScale;

        //scale pixels to dimension scale (Nether 1 : 8 Overworld)
        pixelsPerBlockW *= dimension.dimensionScale;
        pixelsPerBlockL *= dimension.dimensionScale;

        int x, z, pX, pY;
        String tileTxt;

        //check if the tile is not aligned with its inner chunks
        //hacky: it must be a single chunk that is to big for the tile, render just the visible part, easy.
        int alignment = invScale % dimension.dimensionScale;
        if (alignment > 0) {

            int chunkX = minChunkX / dimension.dimensionScale;
            if (minChunkX % dimension.dimensionScale < 0) {
                chunkX -= 1;
            }
            int chunkZ = minChunkZ / dimension.dimensionScale;
            if (minChunkZ % dimension.dimensionScale < 0) {
                chunkZ -= 1;
            }

            int stepX = dimension.chunkW / dimension.dimensionScale;
            int stepZ = dimension.chunkL / dimension.dimensionScale;
            int minX = (minChunkX % dimension.dimensionScale) * stepX;
            if (minX < 0) {
                minX += dimension.chunkW;
            }
            int minZ = (minChunkZ % dimension.dimensionScale) * stepZ;
            if (minZ < 0) {
                minZ += dimension.chunkL;
            }
            int maxX = (maxChunkX % dimension.dimensionScale) * stepX;
            if (maxX <= 0) {
                maxX += dimension.chunkW;
            }
            int maxZ = (maxChunkZ % dimension.dimensionScale) * stepZ;
            if (maxZ <= 0) {
                maxZ += dimension.chunkL;
            }

            tileTxt = chunkX + ";" + chunkZ + " (" + ((chunkX * dimension.chunkW) + minX) + "; " + ((chunkZ * dimension.chunkL) + minZ) + ")";

            renderer.renderToBitmap(img, dimension,
                    chunkX, chunkZ,
                    minX, minZ,
                    maxX, maxZ,
                    0, 0,
                    pixelsPerBlockW, pixelsPerBlockL);

        } else {

            minChunkX /= dimension.dimensionScale;
            minChunkZ /= dimension.dimensionScale;
            maxChunkX /= dimension.dimensionScale;
            maxChunkZ /= dimension.dimensionScale;

            tileTxt = "(" + (minChunkX * dimension.chunkW) + "; " + (minChunkZ * dimension.chunkL) + ")";

            int pixelsPerChunkW = pixelsPerBlockW * dimension.chunkW;
            int pixelsPerChunkL = pixelsPerBlockL * dimension.chunkL;

            for (z = minChunkZ, pY = 0; z < maxChunkZ; z++, pY += pixelsPerChunkL) {

                for (x = minChunkX, pX = 0; x < maxChunkX; x++, pX += pixelsPerChunkW) {

                    try {
                        renderer.renderToBitmap(img, dimension,
                                x, z,
                                0, 0,
                                dimension.chunkW/2, dimension.chunkL/2,
                                pX, pY,
                                pixelsPerBlockW, pixelsPerBlockL);
                    } catch (Exception e) {
                        
                        e.printStackTrace();
                    }

                }
            }
        }

        //draw tile-edges white
        for (int i = 0; i < TILESIZE; i++) {

            //horizontal edges
            img.setRGB(i, 0, Color.WHITE.getRGB());
            img.setRGB(i, TILESIZE - 1, Color.WHITE.getRGB());

            //vertical edges
            img.setRGB(0, i, Color.WHITE.getRGB());
            img.setRGB(TILESIZE - 1, i, Color.WHITE.getRGB());
        }
        
        drawText(tileTxt, img, Color.WHITE.getRGB(), 0);
        
        tile.image = new SoftReference<BufferedImage>(img);
        tile.setLoaded(true);
        fireTileLoadedEvent(tile);
    }

    // Not yet implemented
    public static BufferedImage drawText(String text, BufferedImage b, int textColor, int bgColor) {
        System.out.println(textColor);
        /* Get text dimensions
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setTextSize(b.getHeight() / 16f);
        StaticLayout mTextLayout = new StaticLayout(text, textPaint, b.getWidth() / 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        // Create bitmap and canvas to draw to
        Canvas c = new Canvas(b);

        if(bgColor != 0){
            // Draw background
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(bgColor);
            c.drawPaint(paint);
        }

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();
        */
        Graphics graphics = b.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial Black", Font.PLAIN, b.getHeight() / 16));
        graphics.drawString(text, 2, b.getHeight()-5);
        return b;
    }
    
}
