/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.services.mail;

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
import org.javafx.dialog.Dialog;
import org.quelea.data.Schedule;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;

/**
 * A singleton class used for sending off a schedule using the default email
 * program.
 * <p/>
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
     * <p/>
     * @return the singleton instance of this class.
     */
    public static Mailer getInstance() {
        if(instance == null) {
            synchronized(Mailer.class) {
                if(instance == null) {
                    instance = new Mailer();
                }
            }
        }
        return instance;
    }

    /**
     * Send the given schedule via e-mail.
     * <p/>
     * @param schedule the schedule to send as an attachment.
     * @param body the body of the email.
     */
    public void sendSchedule(Schedule schedule, String body) {

        if(schedule == null || !schedule.iterator().hasNext()) {
            LOGGER.log(Level.WARNING, "Empty schedule passed to email, aborting");
        }

        try {
            MimeMessage msg = new MimeMessage(Session.getInstance(System.getProperties()));
            msg.setSubject(LabelGrabber.INSTANCE.getLabel("quelea.schedule.text"));

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

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            msg.setContent(mp);

            msg.setSentDate(new Date());

            File temp = File.createTempFile("quelea", ".eml");
            temp.deleteOnExit();
            try(FileOutputStream stream = new FileOutputStream(temp)) {
                msg.writeTo(stream);
            }
            try {
                Desktop.getDesktop().open(temp);
            }
            catch(Throwable ex) {
                LOGGER.log(Level.WARNING, "Error with mailer", ex);
                Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("email.error.title"), LabelGrabber.INSTANCE.getLabel("email.error.text"));
            }

        }
        catch(MessagingException | IOException ex) {
            LOGGER.log(Level.WARNING, "Error sending mail", ex);
        }
    }
}
