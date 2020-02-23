import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
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
    private JButton backBtn = new JButton("<");
    private JButton forwardBtn = new JButton(">");
    private JTextField URLTextField = new JTextField(100);
    private ArrayList<String> pageList = new ArrayList<>();

    private static JFXPanel jfxPanel;
    private static WebView webView;
    private static WebEngine webEngine;
    private static WebHistory webHistory;
    private static ArrayList<String> filteredWebsites = new ArrayList<>();

    public Browser() {
        addToBlackList("stackoverflow.com");
        // @Doggo fullscreen 'cause my recorder isn't working if not
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        JFrame jFrame = new JFrame("Doggo's Browser");
        jfxPanel = new JFXPanel();

        jFrame.setSize(screenWidth, screenHeight);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jFrame.getContentPane().setLayout(new BorderLayout());
        jFrame.getContentPane().add(buildMenuPanel(), BorderLayout.NORTH);
        jFrame.getContentPane().add(jfxPanel, BorderLayout.CENTER);

        initializeWebView();

        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private void initializeWebView() {
        Platform.runLater(() -> {
            webView = new WebView();

            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            // set manually 'cause the default is outdated
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/73.0");
//            System.out.println("User Agent: " + webEngine.getUserAgent());

            webEngine.locationProperty().addListener((observable, oldLoc, newLoc) -> {
                if (isBlackListed(newLoc)) {
                    System.out.println("[WebEngine] Worker blocked the website!");
                    JOptionPane.showMessageDialog(null, "You can't view the website kid!", "Blacklisted", JOptionPane.ERROR_MESSAGE);
                    Platform.runLater(() -> webEngine.load(oldLoc));
                }
            });
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                String loc = webEngine.getLocation();

                System.out.println("[WebEngine] oldState: " + oldState);
                System.out.println("[WebEngine] newState: " + newState);
                System.out.println("[WebEngine] location: " + loc);

                URLTextField.setText(loc); // update URL in text field

                if (newState == Worker.State.SUCCEEDED)
                    updateButtons();

                if (newState == Worker.State.FAILED)
                    JOptionPane.showMessageDialog(null, "Failed to load Webpage!", "Opps", JOptionPane.ERROR_MESSAGE);
            });

            webHistory = webEngine.getHistory();
            jfxPanel.setScene(new Scene(webView));
        });
    }

    private JPanel buildMenuPanel() {
        JPanel menuJPanel = new JPanel();

        menuJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        menuJPanel.setBackground(Color.getHSBColor(0.5f, 0.5f, 0.7f));

        backBtn.addActionListener(e -> onBtnBackClick());
        backBtn.setEnabled(false);
        menuJPanel.add(backBtn);

        forwardBtn.addActionListener(e -> onBtnForwardClick());
        forwardBtn.setEnabled(false);
        menuJPanel.add(forwardBtn);

        URLTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onBtnGoClick();
                }
            }
        });

        menuJPanel.add(URLTextField);

        JButton goBtn = new JButton("GO");
        goBtn.addActionListener(e -> onBtnGoClick());
        menuJPanel.add(goBtn);

        JButton historyBtn = new JButton("History");
        historyBtn.addActionListener(e -> onBtnHistoryClick());
        menuJPanel.add(historyBtn);

        JButton blockedBtn = new JButton("Filtered Websites");
        blockedBtn.addActionListener(e -> onBtnBlockedClick());
        menuJPanel.add(blockedBtn);

        return menuJPanel;
    }

    private void onBtnHistoryClick() {
        Platform.runLater(() -> {
            System.out.println("Histories: ");
            for (WebHistory.Entry entry : webHistory.getEntries())
                System.out.println(entry);
        });
    }

    private void onBtnBlockedClick() {

    }

    private void onBtnBackClick() {
        updateButtons();
        // performance wise
        if (webHistory.getCurrentIndex() > 0)
            Platform.runLater(() -> {
                try {
                    webHistory.go(-1);
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("No more backward history found.");
                } catch (Exception e) {
                    System.out.println("error in actionBack: " + e.getMessage());
                }
            });
    }

    private void onBtnForwardClick() {
        updateButtons();
        // performance wise
        if (webHistory.getCurrentIndex() < webHistory.getEntries().size())
            Platform.runLater(() -> {
                try {
                    webHistory.go(1);
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("No more forward history found.");
                } catch (Exception e) {
                    System.out.println("error in actionForward: " + e.getMessage());
                }
            });
    }

    private void onBtnGoClick() {
        String currentUrl = URLTextField.getText();
        if (isBlackListed(currentUrl)) {
            JOptionPane.showMessageDialog(null, "You can't view the website kid!", "Blacklisted", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateButtons();

        if (currentUrl.contains(".")) {
            System.out.println("URL contains \".\"");
            if (!currentUrl.toLowerCase().startsWith("https://")) {
                System.out.println("Adding https:// to " + currentUrl);
                URLTextField.setText("https://" + currentUrl);
                currentUrl = "https://" + currentUrl;
            }

        }

        URL verifiedUrl = verifyURL(currentUrl);

        System.out.println("VERIFIED URL: " + verifiedUrl);
        if (verifiedUrl != null) {

            if (showPageThread.isAlive())
                showPageThread.interrupt();

            showPageThread = new Thread(() -> showPage(verifiedUrl));

            showPageThread.start();
        } else {
            System.out.println("Invalid URL");
        }
    }

    private URL verifyURL(String url) {
        System.out.println("Verifying Url: " + url);
        if (!url.toLowerCase().startsWith("https://") && URLTextField.getText().contains("."))
            return null;

        URL verifiedUrl = null;

        try {
            // do a google search if "." is not found
            if (!URLTextField.getText().contains(".")) {
                System.out.println("Doing google search instead");
                verifiedUrl = new URL(("https://www.google.com/search?q=" + URLTextField.getText()));
            } else {
                // go to website
                verifiedUrl = new URL(url);
            }


        } catch (Exception e) {
            return null;
        }

        return verifiedUrl;
    }

    private void showPage(URL pageUrl) {
        System.out.println("showing page: " + pageUrl);
        Platform.runLater(() -> {
            webView.getEngine().load(pageUrl.toString());
        });

        URLTextField.setText(pageUrl.toString());
        updateButtons(); // @TODO @DOGGO could cause problems if web page dont load
    }

    private void updateButtons() {
        new Thread(() -> {
            System.out.println("Updating Buttons: " + webHistory.getCurrentIndex());

            // update back button
            if (webHistory.getCurrentIndex() > 0) {
                System.out.println("back button enabled");
                backBtn.setEnabled(true);
            } else {
                System.out.println("back button disabled");
                backBtn.setEnabled(false);
            }

            // update forward button
            if (webHistory.getCurrentIndex() < webHistory.getEntries().size() - 1) {
                System.out.println("forward button enabled");
                forwardBtn.setEnabled(true);
            } else {
                System.out.println("forward button enabled");
                forwardBtn.setEnabled(false);
            }
        }).start();
    }

    private void addToBlackList(String url) {
        filteredWebsites.add(url.toLowerCase());
    }

    // still needs improvement
    // for simplicity black if the url contains the word
    private boolean isBlackListed(String urlToFind) {
        System.out.println("Checking " + urlToFind + " in blacklists.");
        for (String url : filteredWebsites)
            if (urlToFind.toLowerCase().matches( ("(.*)" + url + "(.*)") )) {
                System.out.println("URL: " + urlToFind + " have been blocked");
                return true;
            }

        return false;
    }
    public static void main(String[] args) {
        new Browser();
    }
}

   
    