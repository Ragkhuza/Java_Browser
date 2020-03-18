import javax.swing.*;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Date;

public class BrowserBlacklistWindow {
    final static String timeFormat = "HH:mm:ss";

    ArrayList<String> fw;

    BrowserBlacklistWindow(ArrayList<String> fw) {
        this.fw = fw;
    }

    public void show() { 
        // @TODO @DOGGO maybe we need to make this static, what if we open too many of them?
        JFrame jFrame = new JFrame("blocked websites");

        JPanel jPanel = new JPanel();

        int blockListSize = fw.size();

        JSpinner[] timeFrom = new JSpinner[blockListSize];
        JSpinner[] timeTo = new JSpinner[blockListSize];
        JTextField[] webSiteField = new JTextField[blockListSize];

        GridLayout gl = new GridLayout(blockListSize + 2, 3);

        addPrimaryBtns(jPanel, gl, jFrame);
        addLabels(jPanel);

        for (int i = 0; i < blockListSize; i++) {
            String[] splittedUrl = fw.get(i).split("`");

            // Website url text field
            webSiteField[i] = new JTextField(splittedUrl[0]);

            // TIME FROM
            timeFrom[i] = new JSpinner( new SpinnerDateModel() );
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeFrom[i], timeFormat);
            timeFrom[i].setEditor(timeEditor);
            timeFrom[i].setValue(new Date(0)); // start from 1970 since we don't care about the day

            // TIME TO
            timeTo[i] = new JSpinner( new SpinnerDateModel() );
            JSpinner.DateEditor timeEditor2 = new JSpinner.DateEditor(timeTo[i], timeFormat);
            timeTo[i].setEditor(timeEditor2);
            timeTo[i].setValue(new Date(0)); // start from 1970 since we don't care about the day

            jPanel.add(webSiteField[i]);
            jPanel.add(timeFrom[i]);
            jPanel.add(timeTo[i]);
        }

        jPanel.setLayout(gl);
        jFrame.add(jPanel);
        jFrame.pack();

        jFrame.setResizable(false);
        jFrame.setSize(600, jFrame.getHeight() + (blockListSize * 20));
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private void addPrimaryBtns(JPanel jpanel, GridLayout gl, JFrame jframe) {
        JButton btnApply = new JButton("Apply");
        JButton btnAdd = new JButton("Add More");
        JButton btnCancel = new JButton("Cancel");

        btnAdd.addActionListener(e -> {
            JSpinner ts = new JSpinner( new SpinnerDateModel() );
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(ts, timeFormat);
            ts.setEditor(timeEditor);
            ts.setValue(new Date(0)); // start from 1970 since we don't care about the day

            JSpinner ts2 = new JSpinner( new SpinnerDateModel() );
            JSpinner.DateEditor timeEditor2 = new JSpinner.DateEditor(ts2, timeFormat);
            ts2.setEditor(timeEditor2);
            ts2.setValue(new Date(0)); // start from 1970 since we don't care about the day

            jpanel.add(new JTextField(""));
            jpanel.add(ts);
            jpanel.add(ts2);

            gl.setRows(gl.getRows() + 1);
            jpanel.revalidate();
            jframe.setSize(600, jframe.getHeight() + 20);
            jframe.validate();
        });

        btnApply.addActionListener(e -> onBtnApplyClick(jpanel));
        btnCancel.addActionListener(e -> onBtnCancelClick(jframe));

        jpanel.add(btnApply);
        jpanel.add(btnAdd);
        jpanel.add(btnCancel);
    }

    private void onBtnApplyClick(JPanel jpanel) {
        JOptionPane.showMessageDialog(null, "Applied");
        Component[] components = jpanel.getComponents();

        Browser.filteredWebsites = new ArrayList<String>();

        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof  JTextField) {
                JTextField c = (JTextField) components[i];
                JSpinner timeFrom = (JSpinner) components[i+1];
                JSpinner timeTo = (JSpinner) components[i+2];

                Date dfr = (Date) timeFrom.getValue();
                Date dto = (Date) timeTo.getValue();

                String res = c.getText() + " ` " + dfr.getTime() + " ` " + dto.getTime();
                System.out.println("Result: " + res);
                updateBlackList(res);
            }
        }
    }

    private void onBtnCancelClick(JFrame jframe) {
        JOptionPane.showMessageDialog(null, "Cancelled");
        jframe.dispose();
    }

    private void addLabels(JPanel jPanel) {
        jPanel.add(new JLabel("URL"));
        jPanel.add(new JLabel("FROM"));
        jPanel.add(new JLabel("TO"));
    }

    /*private void updateBlackList(JTextArea jTextArea) {
        Browser.filteredWebsites = new ArrayList<String>();
        String text = jTextArea.getText();
        String[] words=text.split("\\n");

        Browser.filteredWebsites.addAll(Arrays.asList(words));
    }*/

    private void updateBlackList(String blacklistWithTime) {
        Browser.filteredWebsites.add(blacklistWithTime);
    }

    public void addToBlackList(String url) {
        Browser.filteredWebsites.add(url.toLowerCase());
    }
}
