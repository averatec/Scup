package cz.matejsimek.scup.DrawTool;

import java.awt.*;

/**
 * Created by kamil on 20.05.15.
 */
public class EllipseDrawTool extends AbstractDrawTool {
    @Override
    public void draw(Point point, Dimension dimension, Point pointOrg, Dimension dimensionOrg) {
        graphics.drawOval(
                (int) point.getX(),
                (int) point.getY(),
                (int) dimension.getWidth(),
                (int) dimension.getHeight()
        );

    }
}
