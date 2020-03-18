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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;

final public class Browser {
    private Thread showPageThread = new Thread();
    private JButton backBtn = new JButton("<");
    private JButton forwardBtn = new JButton(">");
    private JTextField URLTextField = new JTextField(100);
    private JFXPanel jfxPanel;
    private WebView webView;
    private WebEngine webEngine;

    private static WebHistory webHistory;
    public static ArrayList<String> filteredWebsites = new ArrayList<>();

    public Browser() {
        new BrowserBlacklistWindow(filteredWebsites).addToBlackList("stackoverflow.com  ` 1584532553619 ` 1584532553623");
//        BrowserBlacklistWindow.addToBlackList("stackoverflow.com");
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

        JButton blockedBtn = new JButton("Block Websites");
        blockedBtn.addActionListener(e -> onBtnBlockedClick());
        menuJPanel.add(blockedBtn);

        return menuJPanel;
    }

    private void onBtnHistoryClick() {
//        Notification.toastMessage("Check the console!");
        Platform.runLater(() -> {
            new BrowserHistoryWindow(webHistory.getEntries());
        });
    }

    // filtered website button
    private void onBtnBlockedClick() {
        BrowserBlacklistWindow bbw = new BrowserBlacklistWindow(filteredWebsites);
        bbw.show();
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

        URL verifiedUrl = UrlHelper.verifyURL(currentUrl, URLTextField);

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

    // still needs improvement
    // for simplicity black if the url contains the word
    private boolean isBlackListed(String urlToFind) {
        // removes https, http, ://, www.
        urlToFind = UrlHelper.sanitizeUrl(urlToFind);
        System.out.println("Checking " + urlToFind + " in blacklists.");

        for (String urlWithTime : filteredWebsites) {
            System.out.println("Unparsed URL: " + urlWithTime);
            String[] splittedUrl = urlWithTime.split("`");
            String url = splittedUrl[0].trim();

            // compare date
            DateFormat formatter = new SimpleDateFormat("MM/dd/yy");

            Date timeFrom = null;
            Date timeTo = null;

            try {
                System.out.println("Parsing: " + splittedUrl[1].trim());
                System.out.println("Parsing: " + splittedUrl[2].trim());

                timeFrom = new Date(Long.parseLong(splittedUrl[1].trim()));
                timeTo = new Date(Long.parseLong(splittedUrl[2].trim()));


                Date d = new Date();
                Date dateToday = new Date(70,0,1, d.getHours(),d.getMinutes(),d.getSeconds());
                System.out.println("Date From:  " + timeFrom.toString());
                System.out.println("Date Today: " + dateToday.toString());
                System.out.println("Date To:    " + timeTo. toString());

                System.out.println("Date after : " + dateToday.after(timeFrom));
                System.out.println("Date before: " + dateToday.before(timeTo));

                if(dateToday.after(timeFrom) && dateToday.before(timeTo)) {
                    System.out.println("THE WEBSITE IS BETWEEN THE BLOCKING DATES");
                    // matches google.com, quora.com
                    // doesn't match www.google.com, https://www.google.com, http://google.com
                    if (url.length() > 0)
                        if (urlToFind.toLowerCase().matches( (url + "(.*)") )) {
                            System.out.println("URL: " + urlToFind + " have been blocked ||  matched: " + url);
                            return true;
                        }
                }

            } catch (Exception e) {
                System.out.println("[Browser.java] Error parsing date");
                e.printStackTrace();
            }
        }

        return false;
    }

    public static void main(String[] args) {
        new Browser();
    }
}