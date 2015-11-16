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

package org.quelea.windows.main.actionhandlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.multimedia.RecordingEncoder;

/**
 * Class to handle the recordings.
 * 
 * @author Arvid
 */
public class RecordingsHandler {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Dialog noDevicesDialog;
    private boolean running;
    private boolean paused;
    private File wavFile;
    private String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(Calendar.getInstance().getTime());
    private String path = QueleaProperties.get().getRecordingsPath(); // Get path from settings
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE; // format of audio file
    private TargetDataLine line; // the line from which audio data is captured
    private ByteArrayOutputStream out; 
    private AudioFormat format;
    private long startTime;
    private Thread captureThread;
    private long tempTime;
    private ArrayList<String> recordingPaths = new ArrayList<>();
    //Sound level variables
    private float level;
    private final static float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
    private final static float MAX_8_BITS_UNSIGNED = 0xff;
    private final static float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
    private final static float MAX_16_BITS_UNSIGNED = 0xffff;
    private boolean isRecording;
    private boolean finishedSaving;

    /**
     * Defines an audio format
     * @return 
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    /**
     * Initialize a new recording. Captures the sound and saves it to a WAV file
     * 
     * @param pb
     * @param textField
     * @param tb 
     */
    void start(ProgressBar pb, TextField textField, ToggleButton tb) {
        try {
            isRecording = true;
            String fileName = timeStamp;
            wavFile = new File(path + "\\" + fileName + ".wav");
            Platform.runLater(() -> {
                textField.setText(fileName);
            });
            startTime = System.currentTimeMillis();
            tempTime = 0;
            format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (AudioSystem.isLineSupported(info)) {
                LOGGER.log(Level.INFO, "Capturing audio");
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();   // start capturing
                startBuffering(pb, tb);
            }
            else {
                LOGGER.log(Level.INFO, "No recording device found");
                Platform.runLater(() -> {
                    Dialog.Builder setRecordingWarningBuilder = new Dialog.Builder()
                            .create()
                            .setTitle(LabelGrabber.INSTANCE.getLabel("recording.no.devices.title"))
                            .setMessage(LabelGrabber.INSTANCE.getLabel("recording.no.devices.message"))
                            .addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), (ActionEvent t) -> {
                                noDevicesDialog.hide();
                                noDevicesDialog = null;
                            });
                    noDevicesDialog = setRecordingWarningBuilder.setWarningIcon().build();
                    noDevicesDialog.show();
                });
            }
        } catch (LineUnavailableException ex) {
            LOGGER.log(Level.WARNING, "Line unavailable", ex);
        }
    }

    /**
     * Pause recording
     * 
     * @param tb 
     */
    void pause(ToggleButton tb) {
        running = false;
        paused = true;
        tempTime = System.currentTimeMillis() - startTime + tempTime;
        setTime(tempTime, tb);
        writeToTempFile();
    }

    /**
     * Resume recording
     * 
     * @param pb
     * @param tb 
     */
    void resume(ProgressBar pb, ToggleButton tb) {
        startTime = System.currentTimeMillis();
        paused = false;
        line.start();   // start capturing
        startBuffering(pb, tb);
    }

    /**
     * Closes the target data line to finish capturing and recording
     * 
     * @param textField
     * @param tb
     * @throws UnsupportedAudioFileException 
     */
    void finish(TextField textField, ToggleButton tb) throws UnsupportedAudioFileException {
        try {
            finishedSaving = false;
            writeToTempFile();
            // Stop recording
            running = false;
            line.stop();
            line.close();
            out.close();
            captureThread.interrupt();
            // Save the recording to a file
            saveToFile(recordingPaths);
            // Rename file with name entered by user
            String newFileName = path + "\\" + textField.getText().replaceAll("[^\\p{L}0-9.-]", "_") + ".wav";
            wavFile.renameTo(new File(newFileName));

            // Convert to MP3 if setting is checked
            if (QueleaProperties.get().getConvertRecordings()) {
                String[] options = {":sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100}:std{access=file{no-overwrite},mux=mp3,dst='" + newFileName.replace(".wav", ".mp3") + "'} vlc://quit"};
                new RecordingEncoder(newFileName, options).run();
            }
        } catch (IOException ex) {
            finishedSaving = true;
            Logger.getLogger(RecordingsHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            isRecording = false;
            finishedSaving = true;
            LOGGER.log(Level.INFO, "Saved");
        }
    }
    
    /**
     * Method to start capturing sound.
     * 
     * @param pb ProgressBar to display sound input level
     * @param tb ToggleButton to display time
     */
    private void startBuffering(ProgressBar pb, ToggleButton tb) {
        Runnable runner = new Runnable() {
            int bufferSize = (int) format.getSampleRate()
                    * format.getFrameSize();
            byte buffer[] = new byte[bufferSize];

            @Override
            public void run() {
                out = new ByteArrayOutputStream();
                running = true;
                while (running) {
                    int count
                            = line.read(buffer, 0, buffer.length);
                    calculateLevel(buffer, 0, 0);
                    Platform.runLater(() -> {
                        pb.setProgress(level);
                    });
                    long elapsedTimeMillis;
                    elapsedTimeMillis = System.currentTimeMillis() - startTime + tempTime;
                    if (!paused) {
                        setTime(elapsedTimeMillis, tb);
                    }
                    
                    // Change the color of the ProgressBar depending on level
                    // Proper limits should be checked
                    Platform.runLater(() -> {
                        if (level > 0.9f) {
                            pb.setStyle("-fx-accent: red;");
                        } else if (level > 0.7) {
                            pb.setStyle("-fx-accent: orange;");
                        } else {
                            pb.setStyle("-fx-accent: green;");
                        }
                    });
                    if (count > 0) {
                        out.write(buffer, 0, count);
                    }
                    // Store a temp recording if more than 10 seconds are recorded
                    if (out.size() > (176400 * 10)) {
                        writeToTempFile();
                        out.reset();
                    }
                }
                line.stop();
                if (!paused) {
                    Platform.runLater(() -> {
                        tb.setText("");
                    });
                }
            }

        };
        captureThread = new Thread(runner);
        captureThread.start();
    }

    /**
     *  Method to store recording to a temporary file
     */
    private void writeToTempFile() {
        byte audio[] = out.toByteArray();
        InputStream input = new ByteArrayInputStream(audio);
        AudioInputStream ais = new AudioInputStream(input,
                format, audio.length / format.getFrameSize());
        try {
            File tempFile = File.createTempFile("quelea.recording", ".wav");
            tempFile.deleteOnExit();
            recordingPaths.add(tempFile.getPath());
            AudioSystem.write(ais, fileType, tempFile);
            input.close();
            ais.close();
        } catch (IOException ex) {
            Logger.getLogger(RecordingsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to combine temporary recordings.
     * 
     * @param sourceFilesList
     * @param destinationFileName
     * @return
     * @throws Exception 
     */
    public Boolean concatenateFiles(List<String> sourceFilesList, String destinationFileName) throws Exception {
        Boolean result = false;

        AudioInputStream audioInputStream = null;
        List<AudioInputStream> audioInputStreamList = null;
        AudioFormat audioFormat = null;
        Long frameLength = null;

        try {
            // loop through our files first and load them up
            for (String sourceFile : sourceFilesList) {
                audioInputStream = AudioSystem.getAudioInputStream(new File(sourceFile));

                // get the format of first file
                if (audioFormat == null) {
                    audioFormat = audioInputStream.getFormat();
                }

                // add it to our stream list
                if (audioInputStreamList == null) {
                    audioInputStreamList = new ArrayList<>();
                }
                audioInputStreamList.add(audioInputStream);

                // keep calculating frame length
                if (frameLength == null) {
                    frameLength = audioInputStream.getFrameLength();
                } else {
                    frameLength += audioInputStream.getFrameLength();
                }
            }

            // now write our concatenated file
            AudioSystem.write(new AudioInputStream(new SequenceInputStream(Collections.enumeration(audioInputStreamList)), audioFormat, frameLength), Type.WAVE, new File(destinationFileName));

            // if all is good, return true
            result = true;
        } catch (UnsupportedAudioFileException | IOException e) {
            throw e;
        } finally {
            if (audioInputStream != null) {
                audioInputStream.close();
            }
            if (audioInputStreamList != null) {
                audioInputStreamList = null;
            }
        }
        return result;
    }

    /**
     * Method to calculate input level.
     *
     * @param buffer
     * @param readPoint
     * @param leftOver
     */
    private void calculateLevel(byte[] buffer,
            int readPoint,
            int leftOver) {
        int max = 0;
        boolean use16Bit = (format.getSampleSizeInBits() == 16);
        boolean signed = (format.getEncoding()
                == AudioFormat.Encoding.PCM_SIGNED);
        boolean bigEndian = (format.isBigEndian());
        if (use16Bit) {
            for (int i = readPoint; i < buffer.length - leftOver; i += 2) {
                int value = 0;
                // deal with endianness
                int hiByte = (bigEndian ? buffer[i] : buffer[i + 1]);
                int loByte = (bigEndian ? buffer[i + 1] : buffer[i]);
                if (signed) {
                    short shortVal = (short) hiByte;
                    shortVal = (short) ((shortVal << 8) | (byte) loByte);
                    value = shortVal;
                } else {
                    value = (hiByte << 8) | loByte;
                }
                max = Math.max(max, value);
            } // for
        } else {
            // 8 bit - no endianness issues, just sign
            for (int i = readPoint; i < buffer.length - leftOver; i++) {
                int value = 0;
                if (signed) {
                    value = buffer[i];
                } else {
                    short shortVal = 0;
                    shortVal = (short) (shortVal | buffer[i]);
                    value = shortVal;
                }
                max = Math.max(max, value);
            } // for
        } // 8 bit
        // express max as float of 0.0 to 1.0 of max value
        // of 8 or 16 bits (signed or unsigned)
        if (signed) {
            if (use16Bit) {
                level = (float) max / MAX_16_BITS_SIGNED;
            } else {
                level = (float) max / MAX_8_BITS_SIGNED;
            }
        } else {
            if (use16Bit) {
                level = (float) max / MAX_16_BITS_UNSIGNED;
            } else {
                level = (float) max / MAX_8_BITS_UNSIGNED;
            }
        }
    } // calculateLevel

    
    /**
     * Method to set elapsed time on ToggleButton
     * @param elapsedTimeMillis Time elapsed recording last was started
     * @param tb ToggleButton to set time
     */
    private void setTime(long elapsedTimeMillis, ToggleButton tb) {
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        int hours = (int) elapsedTimeSec / 3600;
        int minutes = (int) (elapsedTimeSec % 3600) / 60;
        int seconds = (int) elapsedTimeSec % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        Platform.runLater(() -> {
            tb.setText(time);
        });
    }

    public void saveToFile(ArrayList<String> recordingPaths) throws IOException {
        AudioInputStream clip1 = null;
            try {
                // Merge temp recordings (if any exist) with the most recent recording
                for (String path : recordingPaths) {
                    if (clip1 == null) {
                        clip1 = AudioSystem.getAudioInputStream(new File(path));
                        continue;
                    }
                    AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(path));
                    AudioInputStream appendedFiles = new AudioInputStream(
                            new SequenceInputStream(clip1, clip2),
                            clip1.getFormat(),
                            clip1.getFrameLength() + clip2.getFrameLength());
                    clip1 = appendedFiles;
                }
                
                // If temp files are being stored
                if (wavFile == null) {
                    wavFile = new File(path + "\\Restored recording" + timeStamp + ".wav");
                }

                // Write recording to file
                AudioSystem.write(clip1, fileType, wavFile);
            } catch (UnsupportedAudioFileException | IOException ex) {
                Logger.getLogger(RecordingsHandler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (clip1 != null) {
                    clip1.close();
                    System.gc();
                }
            }
    }
    
    public boolean getIsRecording() {
        return isRecording;
    }
    
    public boolean getFinishedSaving() {
        return finishedSaving;
    }
}
