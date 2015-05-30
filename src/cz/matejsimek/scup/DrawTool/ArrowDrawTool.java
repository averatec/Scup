package cz.matejsimek.scup.DrawTool;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by kamil on 20.05.15.
 */
public class ArrowDrawTool extends AbstractDrawTool {
    @Override
    public void draw(Point point, Dimension dimension, Point pointOrg, Dimension dimensionOrg) {
//        graphics.drawLine(
//                (int) pointOrg.getX(),
//                (int) pointOrg.getY(),
//                (int) (pointOrg.getX() + dimensionOrg.getWidth()),
//                (int) (pointOrg.getY() + dimensionOrg.getHeight())
//        );
        drawArrow(
                (int) pointOrg.getX(),
                (int) pointOrg.getY(),
                (int) (pointOrg.getX() + dimensionOrg.getWidth()),
                (int) (pointOrg.getY() + dimensionOrg.getHeight())
        );
    }

    void drawArrow(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        graphics.transform(at);


        Stroke stroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(1));

        int size = len / 6;
        // Draw horizontal arrow starting in (0, 0)
        graphics.drawLine(0, 0, len, 0);
        graphics.fillPolygon(new int[] {0, len, len, len},
                new int[] {0,size/3, 0, -(size/3)}, 4);
        graphics.fillPolygon(new int[] {len-1, len-1, len-1+size, len-1},
                new int[] {0,size, 0, -size}, 4);
        graphics.setStroke(stroke);
    }

}
