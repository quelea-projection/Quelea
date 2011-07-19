package org.quelea.deprecatedvideo;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Video {

    private SourceDataLine mLine;
    private long mSystemVideoClockStartTime;
    private long mFirstVideoTimestampInStream;
    private String filename;
    private IContainer container;
    private int videoStreamId;
    private int audioStreamId;
    private IStreamCoder audioCoder;
    private IStreamCoder videoCoder;
    private IVideoResampler resampler;
    private ExecutorService service;
    private boolean closed;
    private long position;
    private long targetPosition;
    private long oldPosition;
    private boolean before;
    private List<FrameChangeListener> frameChangeListeners;

    public Video(String filename) {
        this.filename = filename;
        service = Executors.newSingleThreadExecutor();
        closed = true;
        targetPosition = -1;
        oldPosition = -1;
        frameChangeListeners = new ArrayList<FrameChangeListener>();
    }

    public long getPosition() {
        return position;
    }

    public synchronized void setPosition(long position) {
        targetPosition = position;
    }

    public void addFrameChangeListener(FrameChangeListener listener) {
        frameChangeListeners.add(listener);
    }

    public void open() {
        service.execute(new Runnable() {

            @Override
            public void run() {
                container = IContainer.make();

                if (container.open(filename, IContainer.Type.READ, null) < 0) {
                    throw new IllegalArgumentException("could not open file: " + filename);
                }

                int numStreams = container.getNumStreams();

                videoStreamId = -1;
                audioStreamId = -1;
                for (int i = 0; i < numStreams; i++) {
                    IStream stream = container.getStream(i);
                    IStreamCoder coder = stream.getStreamCoder();
                    if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                        videoStreamId = i;
                        videoCoder = coder;
                    } else if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                        audioStreamId = i;
                        audioCoder = coder;
                    }
                }
                if (videoStreamId == -1 && audioStreamId == -1) {
                    throw new RuntimeException("could not find audio or video stream in container: " + filename);
                }

                if (videoCoder != null) {
                    if (videoCoder.open() < 0) {
                        throw new RuntimeException("could not open audio decoder for container: " + filename);
                    }

                    if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
                        // if this stream is not in BGR24, we're going to need to
                        // convert it.  The VideoResampler does that for us.
                        resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
                                videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                        if (resampler == null) {
                            throw new RuntimeException("could not create color space resampler for: " + filename);
                        }
                    }
                }

                if (audioCoder != null) {
                    if (audioCoder.open() < 0) {
                        throw new RuntimeException("could not open audio decoder for container: " + filename);
                    }

                    /*
                     * And once we have that, we ask the Java Sound System to get itself ready.
                     */
                    try {
                        openJavaSound(audioCoder);
                    } catch (LineUnavailableException ex) {
                        throw new RuntimeException("unable to open sound device on your system when playing back container: " + filename);
                    }
                }
                closed = false;
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void play() {

        service.execute(new Runnable() {

            @Override
            public void run() {

                /*
                 * Now, we start walking through the container looking at each packet.
                 */
                IPacket packet = IPacket.make();
                mFirstVideoTimestampInStream = Global.NO_PTS;
                mSystemVideoClockStartTime = 0;

                while (container.readNextPacket(packet) >= 0) {
                    if (closed) {
                        if (videoCoder != null) {
                            videoCoder.close();
                            videoCoder = null;
                        }
                        if (audioCoder != null) {
                            audioCoder.close();
                            audioCoder = null;
                        }
                        if (container != null) {
                            container.close();
                            container = null;
                        }
                        closeJavaSound();
                        return;
                    }
                    position = (long) (packet.getPts() / packet.getTimeBase().getDouble());
                    /*
                     * Now we have a packet, let's see if it belongs to our video stream
                     */
                    if (packet.getStreamIndex() == videoStreamId) {
                        /*
                         * We allocate a new picture to get the data out of Xuggler
                         */
                        IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
                                videoCoder.getWidth(), videoCoder.getHeight());

                        /*
                         * Now, we decode the video, checking for any errors.
                         *
                         */
                        int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
                        if (bytesDecoded < 0) {
                            throw new RuntimeException("got error decoding audio in: " + filename);
                        }

                        /*
                         * Some decoders will consume data in a packet, but will not be able to construct
                         * a full video picture yet.  Therefore you should always check if you
                         * got a complete picture from the decoder
                         */
                        if (picture.isComplete()) {
                            IVideoPicture newPic = picture;
                            if (oldPosition != -1 && before && picture.getTimeStamp() > oldPosition / packet.getTimeBase().getDouble()) {
                                continue;
                            }
                            System.out.println(picture.getTimeStamp() + " :: " + (oldPosition / packet.getTimeBase().getDouble()));
                            if (oldPosition != -1 && !before && picture.getTimeStamp() < oldPosition / packet.getTimeBase().getDouble()) {
                                continue;
                            }

                            /*
                             * If the resampler is not null, that means we didn't get the video in BGR24 format and
                             * need to convert it into BGR24 format.
                             */
                            if (resampler != null) {
                                // we must resample
                                newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
                                if (resampler.resample(newPic, picture) < 0) {
                                    throw new RuntimeException("could not resample video from: " + filename);
                                }
                            }
                            if (newPic.getPixelType() != IPixelFormat.Type.BGR24) {
                                throw new RuntimeException("could not decode video as BGR 24 bit data in: " + filename);
                            }

                            long delay = millisecondsUntilTimeToDisplay(newPic);
                            // if there is no audio stream; go ahead and hold up the main thread.  We'll end
                            // up caching fewer video pictures in memory that way.
                            try {
                                if (delay > 0) {
                                    Thread.sleep(delay);
                                }
                            } catch (InterruptedException e) {
                                return;
                            }
                            // And finally, convert the picture to an image and display it
                            final BufferedImage newImage = Utils.videoPictureToImage(newPic);

                            for (FrameChangeListener listener : frameChangeListeners) {
                                listener.frameChanged(newImage);
                            }
                        }
                    } else if (packet.getStreamIndex() == audioStreamId) {
                        /*
                         * We allocate a set of samples with the same number of channels as the
                         * coder tells us is in this buffer.
                         *
                         * We also pass in a buffer size (1024 in our example), although Xuggler
                         * will probably allocate more space than just the 1024 (it's not important why).
                         */
                        IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());

                        /*
                         * A packet can actually contain multiple sets of samples (or frames of samples
                         * in audio-decoding speak).  So, we may need to call decode audio multiple
                         * times at different offsets in the packet's data.  We capture that here.
                         */
                        int offset = 0;

                        /*
                         * Keep going until we've processed all data
                         */
                        while (offset < packet.getSize()) {
                            int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                            if (bytesDecoded < 0) {
                                throw new RuntimeException("got error decoding audio in: " + filename);
                            }
                            offset += bytesDecoded;
                            /*
                             * Some decoder will consume data in a packet, but will not be able to construct
                             * a full set of samples yet.  Therefore you should always check if you
                             * got a complete set of samples from the decoder
                             */
                            if (samples.isComplete()) {
                                // note: this call will block if Java's sound buffers fill up, and we're
                                // okay with that.  That's why we have the video "sleeping" occur
                                // on another thread.
                                playJavaSound(samples);
                            }
                        }
                    }
                    synchronized (Video.this) {
                        if (targetPosition >= 0) {
                            oldPosition = packet.getTimeStamp();
                            container.seekKeyFrame(-1, targetPosition, 0);
                            before = targetPosition < packet.getTimeStamp() / packet.getTimeBase().getDouble();
                            if(!before) {
                                container.readNextPacket(packet);
                                oldPosition = packet.getTimeStamp();
                            }
                            mFirstVideoTimestampInStream = Global.NO_PTS;
                            targetPosition = -1;
                        }
                    }
                }
            }
        });
    }

    public void close() {
        closed = true;
    }

    private long millisecondsUntilTimeToDisplay(IVideoPicture picture) {
        oldPosition = -1;
        long millisecondsToSleep = 0;
        if (mFirstVideoTimestampInStream == Global.NO_PTS) {
            // This is our first time through
            mFirstVideoTimestampInStream = picture.getTimeStamp();
            // get the starting clock time so we can hold up frames
            // until the right time.
            mSystemVideoClockStartTime = System.currentTimeMillis();
            millisecondsToSleep = 0;
        } else {

            long millisecondsClockTimeSinceStartofVideo = System.currentTimeMillis() - mSystemVideoClockStartTime;
            // compute how long for this frame since the first frame in the stream.
            // remember that IVideoPicture and IAudioSamples timestamps are always in MICROSECONDS,
            // so we divide by 1000 to get milliseconds.
            long millisecondsStreamTimeSinceStartOfVideo = (picture.getTimeStamp() - mFirstVideoTimestampInStream) / 1000;
            final long millisecondsTolerance = 0; // and we give ourselfs 50 ms of tolerance
            millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo
                    - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
        }
        System.out.println("MS: " + millisecondsToSleep);
        return millisecondsToSleep;
    }

    private void openJavaSound(IStreamCoder aAudioCoder) throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
                (int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
                aAudioCoder.getChannels(),
                true, /* xuggler defaults to signed 16 bit samples */
                false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        mLine = (SourceDataLine) AudioSystem.getLine(info);
        /**
         * if that succeeded, try opening the line.
         */
        mLine.open(audioFormat);
        /**
         * And if that succeed, start the line.
         */
        mLine.start();


    }

    private void playJavaSound(IAudioSamples aSamples) {
        /**
         * We're just going to dump all the samples into the line.
         */
        byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
        mLine.write(rawBytes, 0, aSamples.getSize());
    }

    private void closeJavaSound() {
        if (mLine != null) {
            /*
             * Wait for the line to finish playing
             */
            mLine.drain();
            /*
             * Close the line.
             */
            mLine.close();
            mLine = null;
        }
    }
}
