package cz.matejsimek.scup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by kamil on 20.05.15.
 */
public class ColorChooser extends javax.swing.JFrame {
    private JPanel rootPanel;
    private JColorChooser color;
    private JButton ok;

    public ColorChooser() {
        super("Scup");
        setContentPane(rootPanel);
        setResizable(false);
        setLocationByPlatform(true);
        pack();

        color.setColor(Color.RED);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });
    }

    public JColorChooser getColor() {
        return color;
    }
}
