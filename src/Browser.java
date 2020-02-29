import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Browser {
    private Thread showPageThread = new Thread();
    private JButton backBtn = new JButton("<");
    private JButton forwardBtn = new JButton(">");
    private JTextField URLTextField = new JTextField(100);

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

        JFrame jFrame = new JFrame("Doggo's Browser (Alpha Version 0.0.3)");
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
                    String urlOld = oldLoc;
                    String urlNew = newLoc;
                    System.out.println("[WebEngine] Worker blocked the website!");

                    try {
                        urlOld = new URL(oldLoc).getHost();
                        urlNew = new URL(newLoc).getHost();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Cant convert the url to URL data type");
                    }

                    System.out.println("Avoid infinite dialog loop by comparing " + urlOld + " : " + urlNew);
                    if (urlOld.equals(urlNew))
                        return;

                    Notification.toastError("Blacklisted", "You can't view the "+ newLoc +" website kid!");
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
                    Notification.toastError("Opps", "Failed to load Webpage!");
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
        Notification.toastMessage("Check the console!");
        Platform.runLater(() -> {
            System.out.println("Histories: ");
            for (WebHistory.Entry entry : webHistory.getEntries())
                System.out.println(entry);
        });
    }

    // filtered website button
    private void onBtnBlockedClick() {
        // @TODO @DOGGO maybe we need to make this static, what if we open too many of them?
        JFrame jFrame = new JFrame("blocked websites");

        JTextArea jTextArea = new JTextArea(20, 20);

        jTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBlackList(jTextArea);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBlackList(jTextArea);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBlackList(jTextArea);
            }
        });
        JScrollPane jScrollPane = new JScrollPane(jTextArea);

        for (String url : filteredWebsites)
            jTextArea.append(url + "\n");

        jFrame.add(jScrollPane);
        jFrame.setSize(300, 400);
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
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
            Notification.toastError("Blacklisted", "You can't view the " + currentUrl + " website kid!");
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

    private void updateBlackList(JTextArea jTextArea) {
        filteredWebsites = new ArrayList<String>();
        String text = jTextArea.getText();
        String[] words=text.split("\\n");

        filteredWebsites.addAll(Arrays.asList(words));
    }
    // still needs improvement
    // for simplicity black if the url contains the word
    private boolean isBlackListed(String urlToFind) {
        // removes https, http, ://, www.
        urlToFind = sanitizeUrl(urlToFind);
        System.out.println("Checking " + urlToFind + " in blacklists.");

        for (String url : filteredWebsites)
            // matches google.com, quora.com
            // doesn't match www.google.com, https://www.google.com, http://google.com
            if (url.length() > 0)
                if (urlToFind.toLowerCase().matches( (url + "(.*)") )) {
                    System.out.println("URL: " + urlToFind + " have been blocked ||  matched: " + url);
                    return true;
                }

        return false;
    }

    private String sanitizeUrl(String url) {
        String u = url;
        u = u.trim();
        u = u.replace("https", "");
        u = u.replace("http", "");
        u = u.replace("://", "");
        u = u.replace("www.", "");

        return u;
    }

    public static void main(String[] args) {
        new Browser();
    }
}

   
    