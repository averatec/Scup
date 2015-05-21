package cz.matejsimek.scup;

import cz.matejsimek.scup.DrawTool.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created by kamil on 20.05.15.
 */
public class Paint extends javax.swing.JFrame {

    private Preferences prefs;

    private JPanel paintPanel;
    private JButton arrow;
    private JButton rectangle;
    private JButton ellipse;
    private JButton line;
    private JButton text;
    private JButton undo;
    private JButton redo;
    private JButton upload;
    private JButton save;
    private ImagePanel imgPanel;
    private JSlider size;
    private JButton color;
    private JButton blur;
    private AbstractDrawTool drawTool;
    private Point start;
    private BufferedImage buffImage;
    private ArrayList<BufferedImage> imgHistory;
    private int imgHistoryIterator;
    private ColorChooser colorChooser;

    public Paint(BufferedImage image) {
        super("Scup");
        init(image);
        registerListeners();

    }

    public static BufferedImage bufferedImageClone(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private void init(BufferedImage image) {
        imgHistory = new ArrayList<BufferedImage>();
        imgHistoryIterator = 0;
        colorChooser = new ColorChooser();
        prefs = Preferences.userNodeForPackage(cz.matejsimek.scup.Scup.class);

        setContentPane(paintPanel);

        imgHistory.add(bufferedImageClone(image));
        imgPanel.setImage(image);
        imgPanel.setSize(image.getWidth(), image.getHeight());
        imgPanel.setMinimumSize((new Dimension(image.getWidth(), image.getHeight())));
        imgPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setSize((new Dimension(getWidth() + image.getWidth(), getHeight() + image.getHeight())));
        setResizable(false);
        setLocationByPlatform(true);
        pack();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void performRedo() {
        try {
            BufferedImage image = imgHistory.get(imgHistory.size() - 1 + (++imgHistoryIterator));
            imgPanel.setImage(image);
            imgPanel.repaint();
        } catch (IndexOutOfBoundsException ignored) {
            imgHistoryIterator--;
        }
    }

    private void performUndo() {
        try {
            BufferedImage image = imgHistory.get(imgHistory.size() - 1 + (--imgHistoryIterator));
            imgPanel.setImage(bufferedImageClone(image));
            imgPanel.repaint();
        } catch (IndexOutOfBoundsException ignored) {
            imgHistoryIterator++;
        }
    }

    private void registerListeners() {
        paintPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
                "doUndo");
        paintPanel.getActionMap().put("doUndo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                performUndo();
            }
        });
        paintPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
                "doRedo");
        paintPanel.getActionMap().put("doRedo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                performRedo();
            }
        });
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
                prefs.putBoolean(Scup.KEY_UPLOAD, false);
                Scup.reloadConfiguration();
                Scup.processImage(imgPanel.getImage());
                prefs.putBoolean(Scup.KEY_UPLOAD, true);
                Scup.reloadConfiguration();
            }
        });
        upload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
                Scup.processImage(imgPanel.getImage());
            }
        });
        undo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                performUndo();
            }
        });
        redo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                performRedo();
            }
        });

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getSource() == rectangle) {
                    drawTool = new RectangleDrawTool();
                }
                if (actionEvent.getSource() == line) {
                    drawTool = new LineDrawTool();
                }
                if (actionEvent.getSource() == ellipse) {
                    drawTool = new EllipseDrawTool();
                }
                if (actionEvent.getSource() == arrow) {
                    drawTool = new ArrowDrawTool();
                }
                if (actionEvent.getSource() == blur) {
                    drawTool = new BlurDrawTool();
                }
                if (actionEvent.getSource() == text) {
                    //@todo
                }
            }
        };

        imgPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                BufferedImage image = bufferedImageClone(buffImage);
                imgPanel.setImage(image);
                draw(e);
            }
        });

        imgPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                start = e.getPoint();
                buffImage = imgPanel.getImage();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                BufferedImage image = imgPanel.getImage();
                draw(e);
                imgHistory.add(bufferedImageClone(image));
                imgHistoryIterator = 0;
            }
        });

        color.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                colorChooser.setVisible(true);
            }
        });

        arrow.addActionListener(listener);
        rectangle.addActionListener(listener);
        ellipse.addActionListener(listener);
        line.addActionListener(listener);
        text.addActionListener(listener);
        blur.addActionListener(listener);
    }

    private Graphics2D getGraphics2D(BufferedImage image) {
        Graphics g = image.createGraphics();
        g.setColor(Color.BLACK);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int thickness = size.getValue();
        g2.setStroke(new BasicStroke(thickness));
        g2.setColor(colorChooser.getColor().getColor());

        if (drawTool instanceof ArrowDrawTool) {
            ((ArrowDrawTool) drawTool).setSize(thickness * 2); //brzydkoooo
        }
        if (drawTool instanceof AbstractDrawTool) {
            drawTool.setImage(image);
            drawTool.setGraphics(g2);
        }

        return g2;
    }

    private void $$$setupUI$$$() {
        createUIComponents();
    }

    private void createUIComponents() {
        imgPanel = new ImagePanel();
    }

    private Point getRelStart(Point end) {
        Rectangle rect = imgPanel.getVisibleRect();
        Point point = new Point((int) (start.getX() - rect.getX()), (int) (start.getY() - rect.getY()));
        Dimension dimension = getRelDimension(end);

        return new Point(
                (int) Math.max(0, Math.min(point.getX(), point.getX() + dimension.getWidth())),
                (int) Math.max(0, Math.min(point.getY(), point.getY() + dimension.getHeight()))
        );
    }

    private Point getRelStartOrg() {
        Rectangle rect = imgPanel.getVisibleRect();
        return new Point((int) (start.getX() - rect.getX()), (int) (start.getY() - rect.getY()));
    }

    private Dimension getRelDimension(Point end) {
        Rectangle rect = imgPanel.getVisibleRect();
        return new Dimension(
                (int) Math.min(end.getX() - start.getX(), (rect.getX() + rect.getWidth()) - start.getX()),
                (int) Math.min(end.getY() - start.getY(), (rect.getY() + rect.getHeight()) - start.getY())
        );
    }

    private Dimension getRelDimensionAbs(Point end) {
        Rectangle rect = imgPanel.getVisibleRect();
        return new Dimension(
                (int) Math.abs(Math.min(end.getX() - start.getX(), (rect.getX() + rect.getWidth()) - start.getX())),
                (int) Math.abs(Math.min(end.getY() - start.getY(), (rect.getY() + rect.getHeight()) - start.getY()))
        );
    }

    private void draw(MouseEvent e) {
        Graphics2D g2 = getGraphics2D(imgPanel.getImage());

        Point relStart = getRelStart(e.getPoint());
        Dimension relDim = getRelDimensionAbs(e.getPoint());

        Point relStartOrg = getRelStartOrg();
        Dimension relDimOrg = getRelDimension(e.getPoint());

        if (drawTool != null) {
            drawTool.draw(relStart, relDim, relStartOrg, relDimOrg);
        }

        g2.dispose();
        imgPanel.repaint();
    }
}
