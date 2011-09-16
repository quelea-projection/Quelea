/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.mail;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.quelea.utils.LoggerUtils;

/**
 * A singleton class used for sending off a schedule using the default email
 * program.
 * @author Michael
 */
public class Mailer {

    private static volatile Mailer instance;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    private Mailer() {
        //Internal only
    }

    /**
     * Get the singleton instance of this class.
     * @return the singleton instance of this class.
     */
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

    /**
     * Send the given schedule via e-mail.
     * @param schedule the schedule to send as an attachment.
     * @param body the body of the email.
     */
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
            try (FileOutputStream stream = new FileOutputStream(temp)) {
                msg.writeTo(stream);
            }
            try {
                Desktop.getDesktop().open(temp);
            }
            catch (Throwable ex) {
                JOptionPane.showMessageDialog(Application.get().getMainWindow(),
                        "There was an error opening your email client. "
                        + "Make sure you have an email client installed and "
                        + "registered to handle eml files. Otherwise, you'll "
                        + "have to send the email manually.",
                        "Error sending email", JOptionPane.WARNING_MESSAGE);
            }

        }
        catch (MessagingException | IOException ex) {
            LOGGER.log(Level.WARNING, "Error sending mail", ex);
        }
    }
}
