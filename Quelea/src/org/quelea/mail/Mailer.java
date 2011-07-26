package org.quelea.mail;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;
import org.quelea.Application;
import org.quelea.Schedule;

/**
 *
 * @author Michael
 */
public class Mailer {

    private static volatile Mailer instance;

    private Mailer() {
        //Internal only
    }

    public static Mailer getInstance() {
        if (instance == null) {
            synchronized (Mailer.class) {
                if (instance == null) {
                    instance = new Mailer();
                }
            }
        }
        return instance;
    }

    public void sendSchedule(Schedule schedule, String body) {

        if (schedule == null || !schedule.iterator().hasNext()) {
            throw new RuntimeException("Can't send empty schedule: " + schedule);
        }

        try {

            MimeMessage msg = new MimeMessage(Session.getInstance(System.getProperties()));
            msg.setSubject("Quelea schedule");

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(body);

            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            File originalFile = schedule.getFile();
            File tempFile = File.createTempFile("queleaschedule", "tmp");
            tempFile.deleteOnExit();
            schedule.setFile(tempFile);
            try {
                schedule.writeToFile();
            }
            finally {
                schedule.setFile(originalFile);
            }
            FileDataSource fds = new FileDataSource(tempFile);
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName("schedule.qsch");

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            // add the Multipart to the message
            msg.setContent(mp);

            // set the Date: header
            msg.setSentDate(new Date());

            File temp = File.createTempFile("quelea", ".eml");
            temp.deleteOnExit();
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(temp);
                msg.writeTo(stream);
            }
            finally {
                stream.close();
            }

            try {
                Desktop.getDesktop().open(temp);
            }
            catch(Throwable ex) {
                JOptionPane.showMessageDialog(Application.get().getMainWindow(),
                        "There was an error opening your email client. "
                        + "Make sure you have an email client installed and "
                        + "registered to handle eml files. Otherwise, you'll "
                        + "have to send the email manually.",
                        "Error sending email", JOptionPane.WARNING_MESSAGE);
            }

        }
        catch (MessagingException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
