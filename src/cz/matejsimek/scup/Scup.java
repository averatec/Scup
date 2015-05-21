package cz.matejsimek.scup;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.imgscalr.Scalr;

/**
 * Scup - Simple screenshot & file uploader <p>Easily upload screenshot or files
 * to FTP server and copy its URL address to clipboard.
 *
 * @author Matej Simek | www.matejsimek.cz
 */
public class Scup {

    /**
     * Simple new version checking with incremental number
     */
    final static int VERSION = 5;
    //
    public static Clipboard clipboard;
    public static JXTrayIcon trayIcon;
    private static Preferences prefs;
    /**
     * 16x16 app icon
     */
    public static BufferedImage iconImage = null, iconImageUpload = null;
    /**
     * User configuration keys
     */
    public final static String KEY_FTP_SERVER = "FTP_SERVER";
    public final static String KEY_FTP_USERNAME = "FTP_USERNAME";
    public final static String KEY_FTP_PASSWORD = "FTP_PASSWORD";
    public final static String KEY_DIRECTORY = "FTP_DIRECTORY";
    public final static String KEY_URL = "URL";
    public final static String KEY_UPLOAD = "UPLOAD";
    public final static String KEY_MONITOR_ALL = "MONITOR_ALL";
    public final static String KEY_INITIAL_SETTINGS = "INITIAL_SETTINGS";
    /**
     * FTP configuration variables
     */
    private static String FTP_SERVER, FTP_USERNAME, FTP_PASSWORD, FTP_DIRECTORY, URL;
    /**
     * Flag which enable upload to FTP server
     */
    public static boolean UPLOAD;
    /**
     * Flag which enable capture images from all sources, not only printscreen
     */
    public static boolean MONITOR_ALL;
    /**
     * Flag indicates initial settings
     */
    private static boolean INITIAL_SETTINGS;
    /**
     * Tray Popup menu items
     */
    private static JMenuItem uploadEnabledCheckBox;
    private static JMenuItem monitorAllCheckBox;
    private static JMenu historySubmenu;
    private static ActionListener trayIconActionListener = null;

    /**
     * Startup initialization, then endless Thread sleep
     *
     * @param args not used yet
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        // Set system windows theme and load default icon
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            iconImage = ImageIO.read(Scup.class.getResource("resources/icon.png"));
            iconImageUpload = ImageIO.read(Scup.class.getResource("resources/iconupload.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Read configuration
        prefs = Preferences.userNodeForPackage(cz.matejsimek.scup.Scup.class);
        readConfiguration();
        // Init tray icon
        initTray();
        // Get system clipboard and asign event handler to it
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        ClipboardChangeListener cl = new ClipboardChangeListener(clipboard);
        cl.start();

        // Show configuration form on startup until first save
        if (INITIAL_SETTINGS) {
            new Setting().setVisible(true);
        }

        // Endless program run, events are handled in EDT thread
        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    /**
     * Open default system browser on given URI
     *
     * @param uri
     * @return true if operation was successful
     */
    static public boolean openBrowserOn(URI uri) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(uri);
                return true;
            } catch (Exception ex) {
                System.err.println("Error while opening browser");
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Open given file with default associated program
     *
     * @param filepath
     * @return
     */
    static public boolean openOnFile(String filepath) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(new File(filepath));
                return true;
            } catch (Exception ex) {
                System.err.println("Error while opening file with associated program");
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Sets tray icon tooltip text, sets default if text is empty
     *
     * @param text
     */
    static void setTrayTooltip(String text) {
        trayIcon.setToolTip(null);
    }

    /**
     * Place app icon into system tray, build popupmenu and attach event handlers
     * to items
     */
    static private void initTray() {
        if (SystemTray.isSupported()) {
            final SystemTray tray = SystemTray.getSystemTray();
            // Different trayicon sizes, prefer downscalling
            String icoVersion;
            int icoWidth = tray.getTrayIconSize().width;
            if (icoWidth <= 16) {
                icoVersion = "";
            } else if (icoWidth <= 24) {
                icoVersion = "24";
            } else if (icoWidth <= 32) {
                icoVersion = "32";
            } else if (icoWidth <= 48) {
                icoVersion = "48";
            } else if (icoWidth <= 64) {
                icoVersion = "64";
            } else if (icoWidth <= 96) {
                icoVersion = "96";
            } else if (icoWidth <= 128) {
                icoVersion = "128";
            } else if (icoWidth <= 256) {
                icoVersion = "256";
            } else {
                icoVersion = "512";
            }
            // Load tray icon
            try {
                trayIcon = new JXTrayIcon(ImageIO.read(Scup.class.getResource("resources/icon" + icoVersion + ".png")));
                setTrayTooltip("");
                trayIcon.setImageAutoSize(true);
            } catch (IOException ex) {
                System.err.println("IOException: TrayIcon could not be added.");
                System.exit(1);
            }
            // Add tray icon to system tray
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("AWTException: TrayIcon could not be added.");
                System.exit(1);
            }
            // Build popup menu showed on trayicon right click (on Windows)
            final JPopupMenu jpopup = new JPopupMenu();

            JMenuItem settingsItem = new JMenuItem("Settings...");
            uploadEnabledCheckBox = new JMenuItem("Upload to server");
            monitorAllCheckBox = new JMenuItem("Monitor all");
            historySubmenu = new JMenu("History");
            JMenuItem exitItem = new JMenuItem("Exit");

            jpopup.add(settingsItem);
            jpopup.add(uploadEnabledCheckBox);
            jpopup.add(monitorAllCheckBox);
            jpopup.addSeparator();
            jpopup.add(historySubmenu);
            jpopup.addSeparator();
            jpopup.add(exitItem);
            // Add popup to tray
            trayIcon.setJPopupMenu(jpopup);
            // Set default flags
            configureCheckboxes();
            historySubmenu.setEnabled(false);

            // Add listener to settingsItem.
            settingsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new Setting().setVisible(true);
                }
            });

            // Add listener to uploadEnabledCheckBox
            uploadEnabledCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    UPLOAD = !UPLOAD;
                    prefs.putBoolean(KEY_UPLOAD, UPLOAD);
                    configureUploadCheckbox();
                }
            });

            // Add listener to monitorAllCheckBox
            monitorAllCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MONITOR_ALL = !MONITOR_ALL;
                    prefs.putBoolean(KEY_MONITOR_ALL, MONITOR_ALL);
                    configureMonitorCheckbox();
                }
            });

            // Add listener to exitItem.
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tray.remove(trayIcon);
                    System.exit(0);
                }
            });
            try {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/notify-send", "Scup", "I am here to serve", "--icon=dialog-information"});
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }

        } else {
            System.err.println("SystemTray is not supported");
        }
    }

    protected static void configureCheckboxes() {
        configureMonitorCheckbox();
        configureUploadCheckbox();
    }

    protected static void configureMonitorCheckbox() {
        if (MONITOR_ALL) {
            if (!"\u2713".equals(Character.toString(monitorAllCheckBox.getText().charAt(0)))) {
                monitorAllCheckBox.setText("\u2713" + monitorAllCheckBox.getText());
            }
        } else {
            if ("\u2713".equals(Character.toString(monitorAllCheckBox.getText().charAt(0)))) {
                monitorAllCheckBox.setText(monitorAllCheckBox.getText().substring(1));
            } else {
                monitorAllCheckBox.setText(monitorAllCheckBox.getText());
            }
        }
    }

    protected static void configureUploadCheckbox() {
        if (UPLOAD) {
            if (!"\u2713".equals(Character.toString(uploadEnabledCheckBox.getText().charAt(0)))) {
                uploadEnabledCheckBox.setText("\u2713" + uploadEnabledCheckBox.getText());
            }
        } else {
            if ("\u2713".equals(Character.toString(uploadEnabledCheckBox.getText().charAt(0)))) {
                uploadEnabledCheckBox.setText(uploadEnabledCheckBox.getText().substring(1));
            } else {
                uploadEnabledCheckBox.setText(uploadEnabledCheckBox.getText());
            }
        }
    }

    /**
     * Fills class varibles:
     * <code>FTP_SERVER, FTP_USERNAME, FTP_PASSWORD,
     * FTP_DIRECTORY, URL, UPLOAD, MONITOR_ALL</code>
     */
    static private void readConfiguration(/*String filename*/) {
        // Load config
        FTP_SERVER = prefs.get(KEY_FTP_SERVER, "localhost");
        FTP_USERNAME = prefs.get(KEY_FTP_USERNAME, "anonymous");
        FTP_PASSWORD = prefs.get(KEY_FTP_PASSWORD, "");
        FTP_DIRECTORY = prefs.get(KEY_DIRECTORY, "");
        URL = prefs.get(KEY_URL, "http://localhost");
        UPLOAD = prefs.getBoolean(KEY_UPLOAD, false);
        MONITOR_ALL = prefs.getBoolean(KEY_MONITOR_ALL, true);
        INITIAL_SETTINGS = prefs.getBoolean(KEY_INITIAL_SETTINGS, true);
    }

    public static void reloadConfiguration() {
        readConfiguration();
        configureCheckboxes();
    }

    /**
     * Handle uploading of any given file to chosen service (FTP, DROPBOX)
     *
     * @param file           file to upload
     * @param remoteFilename remote filename
     * @return URL of uploaded file
     */
    static String uploadFile(File file, String remoteFilename) {
        // Change tray icon image
        Image oldIcon = trayIcon.getImage();
        trayIcon.setImage(iconImageUpload);
        // Inform about upload
        String url = null;

        FileUpload fileupload = new FileUpload(FTP_SERVER, FTP_USERNAME, FTP_PASSWORD, FTP_DIRECTORY);
        boolean status = fileupload.uploadFile(file, remoteFilename);
        if (status) {
            url = (URL.endsWith("/") ? URL : URL + "/") + remoteFilename;
        }
        // Revert back tray changes
        trayIcon.setImage(oldIcon);
        oldIcon.flush();
        setTrayTooltip("");
        // Return URL or null
        return url;
    }

    static boolean testConnection() {
        return (new FileUpload(FTP_SERVER, FTP_USERNAME, FTP_PASSWORD, FTP_DIRECTORY)).testConnection();
    }

    /**
     * Whole image handling process - display, crop, save on disk, transfer to
     * FTP, copy its URL to clipboard
     *
     * @param image     to process
     */
    static void processImage(BufferedImage image) {
        System.out.println("Processing image...");
        System.out.println("Image: " + image.getWidth() + "x" + image.getHeight());

        if (image == null) {
            System.out.println("Image is empty, canceling");
            return;
        }

        File imageFile = saveImageToFile(image);
        final String imageUrl;

        if (UPLOAD) {
            // Calculate image hash
            String hash = generateHashForFile(imageFile);
            String newFilename = hash.substring(0, 10) + ".png";
            // Rename file after its hash
            File renamedFile = new File(newFilename);
            imageFile = imageFile.renameTo(renamedFile) ? renamedFile : imageFile;
            // Transer image to FTP
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            imageUrl = uploadFile(imageFile, dateFormat.format(date) + "_" + imageFile.getName());
            if (imageUrl == null) {
                // Upload failed, it happens
                try {
                    Runtime.getRuntime().exec(new String[]{"/usr/bin/notify-send", "Upload failed", "I can not serve, sorry", "--icon=dialog-error"});
                    System.err.println("Upload failed");
                } catch (IOException e) {
                    System.out.println(e.getLocalizedMessage());
                }
                return;
            } else {
                // Don't keep copy of already uploaded image
                imageFile.delete();
            }

        } // Copy local absolute path only when upload is disabled
        else {
            imageUrl = imageFile.getAbsolutePath();
        }
        // Copy URL to clipboard
        setClipboard(imageUrl);
        // Notify user about it

        try {
            Runtime.getRuntime().exec(new String[]{"/usr/bin/notify-send", "Image " + (UPLOAD ? "uploaded" : "saved"), imageUrl, "--icon=dialog-information"});
            System.out.println("Image " + (UPLOAD ? "uploaded " : "saved ") + imageUrl);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        // Display last uploaded image
        switchTrayIconActionListenerTo(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setClipboard(imageUrl);
                try {
                    Runtime.getRuntime().exec(new String[]{"/usr/bin/notify-send", "Last image " + (UPLOAD ? "URL" : "path"), imageUrl, "--icon=dialog-information"});
                    System.err.println("Upload failed");
                } catch (IOException excetpion) {
                    System.out.println(excetpion.getLocalizedMessage());
                }
                System.out.println("Last image " + (UPLOAD ? "URL" : "path") + imageUrl);
            }
        });

        // Save it to history
        addImageToHistory(image, imageUrl, !UPLOAD);
    }

    /**
     * Adds image into history submenu
     *
     * @param image
     * @param path
     */
    static private void addImageToHistory(BufferedImage image, final String path, boolean isLocalFile) {
        BufferedImage scaled;
        // Resize image to usable dimensions
        if (image.getWidth() > 140 || image.getHeight() > 80) {
            scaled = Scalr.resize(image, 140, 80);
        } else {
            scaled = image;
        }

        // Rape JMenuItem with big image
        JMenuItem item = new JMenuItem(path, new ImageIcon(scaled));
        // Copy path on click
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Scup.setClipboard(path);
            }
        });

        // Open browser/program on CTRL+click
        item.addMouseListener(new OpenFileMouseAdapter(path, isLocalFile));
        // Finally add item to submenu
        historySubmenu.add(item);
        historySubmenu.setEnabled(true);
        // Clean old items
        if (historySubmenu.getItemCount() > 5) {
            historySubmenu.remove(0);
        }
        scaled.flush();
    }

    /**
     * Copy given String to system clipboard (could fail)
     *
     * @param str string to copy
     */
    static void setClipboard(String str) {
        try {
            clipboard.setContents(new StringSelection(str), null);
        } catch (IllegalStateException ex) {
            System.err.println("Can't set clipboard, trying again!");
            ex.printStackTrace();
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }
            setClipboard(str);
        }
    }

    /**
     * Assure there is only one action listener on tratIcon
     *
     * @param newListener to switch
     */
    static void switchTrayIconActionListenerTo(ActionListener newListener) {
        if (trayIconActionListener != null) {
            trayIcon.removeActionListener(trayIconActionListener);
        }
        trayIconActionListener = newListener;
        trayIcon.addActionListener(trayIconActionListener);
    }


    /**
     * Save image to PNG file named by its content hash into current directory
     *
     * @param img
     * @return
     */
    static File saveImageToFile(BufferedImage img) {
        try {
            // Generate default image name
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            File outputFile = new File(dateFormat.format(Calendar.getInstance().getTime()) + ".png");
            // Write image data
            ImageIO.write(img, "png", outputFile);

            return outputFile;

        } catch (IOException ex) {
            System.err.println("Can't write image to file!");
        }
        return null;
    }

    /**
     * Generate SHA hash for given file
     *
     * @param file to calculate
     * @return Hash in hexadecimal format
     */
    static String generateHashForFile(File file) {
        FileInputStream fis = null;

        try {
            byte[] buf = new byte[1024];

            MessageDigest md = MessageDigest.getInstance("SHA");

            fis = new FileInputStream(file);
            int len;
            while ((len = fis.read(buf)) > 0) {
                md.update(buf, 0, len);
            }

            Formatter formatter = new Formatter();
            for (byte b : md.digest()) {
                formatter.format("%02x", b);
            }

            fis.close();

            return formatter.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
}