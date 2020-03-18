import javax.swing.JTextField;
import java.net.URL;

final public class UrlHelper {
    public static String sanitizeUrl(String url) {
        String u = url;
        u = u.trim();
        u = u.replace("https", "");
        u = u.replace("http", "");
        u = u.replace("://", "");
        u = u.replace("www.", "");

        return u;
    }

    public static URL verifyURL(String url, JTextField URLTextField) {
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
}
