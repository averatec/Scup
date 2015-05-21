package cz.matejsimek.scup;

/**
 * Created by kamil on 20.05.15.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {

    private BufferedImage image = null;

    public ImagePanel() {

    }

    public ImagePanel(BufferedImage image) {
        setImage(image);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawRect(0, 0, 10, 10);
        if (image != null) {
            g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
        }
    }

}
