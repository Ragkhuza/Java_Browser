import javax.swing.JOptionPane;

final class Notification {

    // display JOptionpane for Message (no title)
    static void toastMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    // display JOptionpane for Message
    static void toastMessage(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // display JOptionpane for Success
    static void toastSuccess(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Success!", JOptionPane.INFORMATION_MESSAGE);
    }

    // display JOptionpane for Warning
    static void toastWarning(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Warning!", JOptionPane.WARNING_MESSAGE);
    }

    // display JOptionpane for Error
    static void toastError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }

    // display JOptionpane for Error
    static void toastError(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    // return 0(yes) - 1(cancel)
    // display confirmation
    static int toastQuestion(String title, String msg) {
        return JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

    }
}
