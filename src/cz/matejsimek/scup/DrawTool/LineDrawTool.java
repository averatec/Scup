package cz.matejsimek.scup.DrawTool;

import java.awt.*;

/**
 * Created by kamil on 20.05.15.
 */
public class LineDrawTool extends AbstractDrawTool {
    @Override
    public void draw(Point point, Dimension dimension, Point pointOrg, Dimension dimensionOrg) {
        graphics.drawLine(
                (int) pointOrg.getX(),
                (int) pointOrg.getY(),
                (int) (pointOrg.getX() + dimensionOrg.getWidth()),
                (int) (pointOrg.getY() + dimensionOrg.getHeight())
        );

    }
}
