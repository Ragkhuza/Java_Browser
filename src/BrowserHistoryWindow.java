import javafx.collections.ObservableList;
import javafx.scene.web.WebHistory;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.GridLayout;

public class BrowserHistoryWindow {

    public BrowserHistoryWindow(ObservableList<WebHistory.Entry> entries) {
        System.out.println("Histories: ");
        GridLayout gl = new GridLayout(0, 2);
        gl.setVgap(10);

        JFrame jFrame = new JFrame("History");
        for (WebHistory.Entry entry : entries) {
            System.out.println(entry);
            jFrame.add(new JLabel(entry.getUrl()));
            jFrame.add(new JLabel(entry.getLastVisitedDate().toString()));
            gl.setRows(gl.getRows()+1);
        }

        if (entries.size() <= 0)
            jFrame.add(new JLabel("No History Found", SwingConstants.CENTER));

//        jFrame.setResizable(false);
        jFrame.setLayout(gl);
        jFrame.pack();
        jFrame.setSize(400, jFrame.getHeight());
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }
}
