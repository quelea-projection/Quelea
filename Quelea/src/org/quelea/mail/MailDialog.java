package org.quelea.mail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.validator.EmailValidator;
import org.quelea.Application;
import org.quelea.Schedule;

/**
 *
 * @author Michael
 */
public class MailDialog extends JDialog {

    private JTextField toField = new JTextField();
    private JTextArea emailBody = new JTextArea();
    private JButton sendButton = new JButton();
    private Schedule schedule;

    public MailDialog() {
        super(Application.get().getMainWindow(), "Email Schedule", true);
        emailBody.setLineWrap(true);
        emailBody.setWrapStyleWord(true);
        emailBody.setPreferredSize(new Dimension(400, 200));
        toField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkAddress();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkAddress();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkAddress();
            }
            
            private void checkAddress() {
                sendButton.setEnabled(EmailValidator.getInstance().isValid(toField.getText().trim()));
            }
        });
        reset();
        sendButton.setText("Send email");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Mailer.getInstance().sendSchedule(schedule, toField.getText(), emailBody.getText());
                setVisible(false);
            }
        });
        setLayout(new BorderLayout());
        add(toField, BorderLayout.NORTH);
        JScrollPane scrollPanel = new JScrollPane(emailBody, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPanel, BorderLayout.CENTER);
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.X_AXIS));
        sendPanel.add(sendButton);
        add(sendPanel, BorderLayout.SOUTH);
        pack();
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public final void reset() {
        toField.setText("<Email address of recipient>");
        emailBody.setText(
                "Hi,\n"
                + "Attached is a Quelea schedule you've been sent. Simply "
                + "open it with Quelea and all the items should appear correctly.\n\n"
                + "Thanks,\n"
                + "Quelea Team\n\n\n"
                + "-----\n"
                + "Please note this is an automated email, do not reply to this "
                + "address. If you wish to reply please be sure to change the "
                + "address to the correct person.");
    }

    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }
}
