package cz.matejsimek.scup;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Clipboard listener which decide what to do with clipboard content
 *
 * @author Matej Simek | www.matejsimek.cz
 */
public class ClipboardChangeListener extends Thread {

    /**
     * Data source
     */
    private Clipboard clipboard;

    @Override
    public void run() {
        Scup.setClipboard("");
        System.out.println("Starting clipboard listener...");

        BufferedImage oldImage = null;

        while (true) {

            try {
                // Text in clipboard idicates free way to clear old references
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    if (oldImage != null) {
                        oldImage.flush();
                    }
                    oldImage = null;
                } // Compare old image with new one from clipboard if its available
                else if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
                    BufferedImage newImage = (BufferedImage) clipboard.getData(DataFlavor.imageFlavor);
                    clipboard.setContents(new StringSelection(""), null);

                    if (!newImage.equals(oldImage)) {
                        System.out.println("New image detected in clipboard");
                        if (oldImage != null) {
                            oldImage.flush();
                        }
                        oldImage = newImage;
                        processImageContent(newImage);
                    } else {
                        newImage.flush();
                        newImage = null;
                    }

                }
            }catch(IOException e){
                try {
                    clipboard.setContents(new StringSelection(""), null);
                    Runtime.getRuntime().exec(new String[]{"/usr/bin/notify-send", "Scup", "Image in clipboard is too large to processing.", "--icon=dialog-information"});
                } catch (IOException ex) {
                    System.out.println(e.getLocalizedMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            synchronized (this) {
                try {
                    this.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * @param clipboard  Data source
     */
    public ClipboardChangeListener(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * Basic image content handling - determinates image source and
     *
     * @param image
     */
    public void processImageContent(BufferedImage image) {
        new Paint(image).setVisible(true);
    }
}
