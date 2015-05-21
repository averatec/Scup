package cz.matejsimek.scup.DrawTool;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by kamil on 20.05.15.
 */
abstract public class AbstractDrawTool {
    protected Graphics2D graphics;
    protected BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    abstract public void draw(Point point, Dimension dimension, Point pointOrg, Dimension dimensionOrg);
}
