import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

public class Browser {
    private Thread showPageThread = new Thread();
    private JButton backButton = new JButton("<"), forwardButton = new JButton(">");
    private JTextField URLTextField = new JTextField(100);
//    private JEditorPane displayEditorPane = new JEditorPane();
    private ArrayList<String> pageList = new ArrayList<>();
    private static JFrame jFrame;
    private static JFXPanel jfxPanel;
    private static WebView webView;
    private static WebEngine webEngine;
    private static WebHistory webHistory;

    public Browser() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        jFrame = new JFrame("Doggo's Browser");
        jfxPanel = new JFXPanel();
        JPanel menuJPanel = new JPanel();

        menuJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        menuJPanel.setBackground(Color.getHSBColor(0.5f, 0.5f, 0.7f));

        jFrame.setSize(screenWidth, screenHeight);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        backButton.addActionListener(e -> actionBack());
        backButton.setEnabled(true);
        menuJPanel.add(backButton);

        forwardButton.addActionListener(e -> actionForward());
        forwardButton.setEnabled(true);
        menuJPanel.add(forwardButton);

        URLTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionGo();
                }
            }
        });

        menuJPanel.add(URLTextField);

        JButton goBtn = new JButton("GO");
        goBtn.addActionListener(e -> actionGo());
        menuJPanel.add(goBtn);

        JButton historyBtn = new JButton("History");
        historyBtn.addActionListener(e -> {});
        menuJPanel.add(historyBtn);

        JButton blockedBtn = new JButton("Filtered Websites");
        blockedBtn.addActionListener(e -> {});
        menuJPanel.add(blockedBtn);
        /*displayEditorPane.setContentType("text/html");
        displayEditorPane.setEditable(false);
        displayEditorPane.addHyperlinkListener(jFrame);*/

        jFrame.getContentPane().setLayout(new BorderLayout());
        jFrame.getContentPane().add(menuJPanel, BorderLayout.NORTH);
        jFrame.getContentPane().add(jfxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();
            webHistory = webEngine.getHistory();
            jfxPanel.setScene(new Scene(webView));
        });

        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private void actionBack() {
        Platform.runLater(() -> {
            try {
                webHistory.go(-1);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("No more backward history found.");
            } catch (Exception e) {
                System.out.println("error in actionBack: " + e.getMessage());
            }
        });

//        URL currentUrl = displayEditorPane.getPage();
        /*String currentUrl = "google.com";
        int pageIndex = pageList.indexOf(currentUrl);
        try {
            showPage(new URL((String) pageList.get(pageIndex - 1)), false);
        } catch (Exception e) {
        }*/
    }

    private void actionForward() {
        Platform.runLater(() -> {
            try {
                webHistory.go(1);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("No more forward history found.");
            } catch (Exception e) {
                System.out.println("error in actionForward: " + e.getMessage());
            }
        });
//        URL currentUrl = displayEditorPane.getPage();
        /*String currentUrl = "google.com";
        int pageIndex = pageList.indexOf(currentUrl.toString());
        try {
            showPage(new URL((String) pageList.get(pageIndex + 1)), false);
        } catch (Exception e) {
        }*/
    }

    private void actionGo() {
        if (!URLTextField.getText().toLowerCase().startsWith("https://")) {
            System.out.println(URLTextField.getText());
            URLTextField.setText("https://" + URLTextField.getText());
        }
        URL verifiedUrl = verifyUrl(URLTextField.getText());
        if (verifiedUrl != null) {

            if (showPageThread.isAlive())
                showPageThread.interrupt();

            showPageThread = new Thread(() -> showPage(verifiedUrl, true));

            showPageThread.start();
        } else {
            System.out.println("Invalid URL");
        }
    }

    private URL verifyUrl(String url) {
        if (!url.toLowerCase().startsWith("https://"))
            return null;

        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }

        return verifiedUrl;
    }

    private void showPage(URL pageUrl, boolean addToList) {
        System.out.println("showing page: " + pageUrl);
        try {
            String currentUrl = "google.com";
//            URL currentUrl = displayEditorPane.getPage();
//            displayEditorPane.setPage(pageUrl);
//            URL newUrl = displayEditorPane.getPage();
            Platform.runLater(() -> {
                webView.getEngine().load(pageUrl.toString());
            });

            if (addToList) {
                int listSize = pageList.size();
                if (listSize <= 0) {
                    return;
                }
                int pageIndex = pageList.indexOf(currentUrl.toString());
                if (pageIndex >= listSize - 1) {
                    return;
                }
                for (int i = listSize - 1; i > pageIndex; i--) {
                    pageList.remove(i);
                }
                pageList.add(currentUrl);
            }
            URLTextField.setText(currentUrl);
            updateButtons();
        } catch (Exception e) {
            System.out.println("Unable to load page");
        }
    }

    private void updateButtons() {
        /*if (pageList.size() < 2) {
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);
        } else {
            String currentUrl = "gggg";
            int pageIndex = pageList.indexOf(currentUrl.toString());
            backButton.setEnabled(pageIndex > 0);
            forwardButton.setEnabled(pageIndex < (pageList.size() - 1));
        }*/
    }

    public static void main(String[] args) {
        new Browser();
    }
}

   
    