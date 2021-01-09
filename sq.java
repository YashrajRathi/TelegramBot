import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class sq{

    public static void main(String[] args) throws AWTException {
        if (SystemTray.isSupported()) {
            sq td = new sq();
            displayTray("Njs23.txt","File has been received.");
        } else {
            System.err.println("System tray not supported!");
        }

		System.out.println("First this line is shown");
		return ;
    }

    public static void displayTray(String title,String info) throws AWTException {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);

        trayIcon.displayMessage(title, info , MessageType.INFO);
    }
}