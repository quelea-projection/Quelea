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
package org.quelea.server;

import javax.sound.midi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;


import java.util.List;

class MidiEvent
{
    private static final Logger LOGGER = LoggerUtils.getLogger();
    public boolean Enabled = true;
    public int type     = ShortMessage.NOTE_ON;
    public int channel  = 16-1;
    public int note      ;// Data 1
    public int velocity = 0 ;// Data 2
    public String callbackName;
    public String Key;

    MidiEvent(String propertyString, String callbackName_in) {
        this.stringPropertyToMidiEvent(propertyString);
        this.callbackName = callbackName_in;
        LOGGER.log(Level.INFO, "Add midi control event:["+this.propertiesToString()+"] for ["+propertyString+"] for ["+callbackName+"]");
    }

    // Get and Set properties
    public String propertiesToString() {
        return ""
                + Enabled + ","
                + type + ","
                + (channel+1) + ","
                + note ;
    }

    public void stringPropertyToMidiEvent(String propertyStrIn) {
        propertyStrIn = propertyStrIn.replaceAll("\\s","");// Remove any white space
        String[] properties = propertyStrIn.split(",");
        this.Enabled     = Boolean.valueOf(properties[0]);
        this.type        = StrToMidiTypeInd(properties[1]);
        this.channel     = (Integer.parseInt(properties[2])-1);
        this.note        = Integer.valueOf(properties[3]);
    }

    // Convert message type to string and integer
    public int StrToMidiTypeInd(@NotNull String typeStr) {
        switch(typeStr) {
            case "ACTIVE_SENSING": 			return 0xFE; // (0xFE, or 254)
            case "CHANNEL_PRESSURE": 		return 0xD0; // (0xD0, or 208)
            case "CONTINUE": 				return 0xFB; // (0xFB, or 251)
            case "CONTROL_CHANGE": 			return 0xB0; // (0xB0, or 176)
            case "END_OF_EXCLUSIVE": 		return 0xF7; // (0xF7, or 247)
            case "MIDI_TIME_CODE": 			return 0xF1; // (0xF1, or 241)
            case "NOTE_OFF": 				return 0x80; // (0x80, or 128)
            case "NOTE_ON": 				return 0x90; // (0x90, or 144)
            case "PITCH_BEND": 				return 0xE0; // (0xE0, or 224)
            case "POLY_PRESSURE": 			return 0xA0; // (0xA0, or 160)
            case "PROGRAM_CHANGE": 			return 0xC0; // (0xC0, or 192)
            case "SONG_POSITION_POINTER": 	return 0xF2; // (0xF2, or 242)
            case "SONG_SELECT": 			return 0xF3; // (0xF3, or 243)
            case "START": 					return 0xFA; // (0xFA, or 250)
            case "STOP": 					return 0xFC; // (0xFC, or 252)
            case "SYSTEM_RESET": 			return 0xFF; // (0xFF, or 255)
            case "TIMING_CLOCK": 			return 0xF8; // (0xF8, or 248)
            case "TUNE_REQUEST": 			return 0xF6; // (0xF6, or 246)
            default:
                throw new IllegalStateException("Unexpected value: " + typeStr);
        }
    }

    public String StrToMidiTypeInd(int typeInt) {
        switch (typeInt)
        {
            case 0xFE:  return  "ACTIVE_SENSING"; 			// (0xFE, or 254)
            case 0xD0:  return  "CHANNEL_PRESSURE"; 		// (0xD0, or 208)
            case 0xFB:  return  "CONTINUE"; 				// (0xFB, or 251)
            case 0xB0:  return  "CONTROL_CHANGE"; 			// (0xB0, or 176)
            case 0xF7:  return  "END_OF_EXCLUSIVE"; 		// (0xF7, or 247)
            case 0xF1:  return  "MIDI_TIME_CODE"; 			// (0xF1, or 241)
            case 0x80:  return  "NOTE_OFF"; 				// (0x80, or 128)
            case 0x90:  return  "NOTE_ON"; 					// (0x90, or 144)
            case 0xE0:  return  "PITCH_BEND"; 				// (0xE0, or 224)
            case 0xA0:  return  "POLY_PRESSURE"; 			// (0xA0, or 160)
            case 0xC0:  return  "PROGRAM_CHANGE"; 			// (0xC0, or 192)
            case 0xF2:  return  "SONG_POSITION_POINTER"; 	// (0xF2, or 242)
            case 0xF3:  return  "SONG_SELECT"; 				// (0xF3, or 243)
            case 0xFA:  return  "START"; 					// (0xFA, or 250)
            case 0xFC:  return  "STOP"; 					// (0xFC, or 252)
            case 0xFF:  return  "SYSTEM_RESET"; 			// (0xFF, or 255)
            case 0xF8:  return  "TIMING_CLOCK"; 			// (0xF8, or 248)
            case 0xF6:  return  "TUNE_REQUEST"; 			// (0xF6, or 246)
            default:
                throw new IllegalStateException("Unexpected value: " + typeInt);
        }

    }

    // Make a qucik find key
    // --- For quick access
    @Override
    public String toString() {
        ShortMessage tmpMsg = new ShortMessage();
        try {
            tmpMsg.setMessage(this.type,this.channel,this.note,this.velocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return tmpMsg.toString();
    };

    public ShortMessage toMidiMessage() throws InvalidMidiDataException {
        //ShortMessage msg = new ShortMessage();
        //msg.setMessage(type,channel,note,velocity);
        //return  msg;
        return (new ShortMessage(type,channel,note,velocity));
    }
    public boolean match(@NotNull MidiMessage m ) throws InvalidMidiDataException {
        byte[] LB = this.toMidiMessage().getMessage();
        byte[] b = m.getMessage();
        if(b[0] == LB[0] && b[1] == LB[1])
            return true;
        else return false;
    }
}


/**
 * The MIDI  control server, responsible for handling the midi  calls and
 * changing the correct content12.
 * <p>
 *
 * @author Ben
 */
public class MidiInterfaceConnector
{
    // Get logger
    private static final Logger LOGGER = LoggerUtils.getLogger();

    // State variables
    private boolean midiInputReady = false;
    private boolean midiOutputReady = false;
    private boolean RemoteControlDeferToMidiMode = false;

    //MIDI input and output devices
    private MidiDevice QueleaMidiDev_IN = null;
    private MidiDevice QueleaMidiDev_OUT = null;

    // Midi event map
    private Map<String,MidiEvent> midiEventMap = new HashMap<>();

    /**
     * Create a MIDI interface connection with a specified midi interface.
     * <p>
     *
     * @param defaultMidiDeviceInterface
     */
    public MidiInterfaceConnector(String defaultMidiDeviceInterface) throws InvalidMidiDataException, MidiUnavailableException {
        LOGGER.log(Level.INFO, "Setup midi connection with [{0}]",defaultMidiDeviceInterface);
        updateMidiEventPropertyList();
        this.setupMidiInputConnection(defaultMidiDeviceInterface);
        //this.setupMidiOutputConnection(defaultMidiDeviceInterface);
    }

    public void updateMidiEventPropertyList() {
        List<MidiEvent> midiEventList = new ArrayList<>();
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Clear           (), "clear"          ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Black           (), "black"          ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_GoToItem        (), "goToItem"       ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Next            (), "next"           ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_NextItem        (), "nextItem"       ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Play            (), "play"           ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Prev            (), "prev"           ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_PrevItem        (), "prevItem"       ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Section         (), "section"        ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Tlogo			  (), "logo"		   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown1  (), "transposeDown1" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown2  (), "transposeDown2" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown3  (), "transposeDown3" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown4  (), "transposeDown4" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown5  (), "transposeDown5" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown6  (), "transposeDown6" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp0	  (), "transposeUp0"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp1	  (), "transposeUp1"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp2	  (), "transposeUp2"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp3	  (), "transposeUp3"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp4	  (), "transposeUp4"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp5	  (), "transposeUp5"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp6	  (), "transposeUp6"   ));

        for (MidiEvent me : midiEventList)
        {
            midiEventMap.put(me.callbackName,me);
        }
    }


    public void setupMidiInputConnection(String InputDevice) throws MidiUnavailableException {
        LOGGER.log(Level.INFO, "Get midi device list");
        // Get midi information
        MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        // Midi device placeholder
        MidiDevice device;
        // Loop over the midi devices
        for (int d = 0; d < midiInfo.length; d++)
        {
            try {
                // Get the midi device
                device = MidiSystem.getMidiDevice(midiInfo[d]);

                //Get list of receivers for this device
                List<Receiver> deviceReceivers = device.getReceivers();

                //Get list of transmitters for this device
                List<Transmitter> deviceTransmitters = device.getTransmitters();

                // Get the correct midi INPUT device
                if (InputDevice.equals(midiInfo[d].toString()) && device.getMaxTransmitters() != 0)  {
                    LOGGER.log(Level.INFO, "Quelea connector MIDI device INPUT Located [" + midiInfo[d] + "]");
                    QueleaMidiDev_IN = device;

                    //Info for the midi device
                    LOGGER.log(Level.INFO, "Deivce[" + String.valueOf(d) + "] [" + midiInfo[d] + "]"+"\n"+
                            "Transmitter list size for the device is ["+ deviceTransmitters.size()+"]"+
                            "  max transmitters: " + device.getMaxTransmitters()+"\n"+
                            "Receiver list size for the device is ["+ deviceReceivers.size()+"]"+
                            "  max receivers: " + device.getMaxReceivers());
                    break;
                }
/*
                // Get the correct midi OUTPUT device
                if (InputDevice.equals(midiInfo[d].toString()) && device.getMaxReceivers() != 0)  {
                    LOGGER.log(Level.INFO, "Quelea connector MIDI device OUTPUT Located [" + midiInfo[d] + "]");
                    QueleaMidiDev_OUT = device;

                    //Info for the midi device
                    LOGGER.log(Level.INFO, "Deivce[" + String.valueOf(d) + "] [" + midiInfo[d] + "]"+"\n"+
                            "Transmitter list size for the device is ["+ deviceTransmitters.size()+"]"+
                            "  max transmitters: " + device.getMaxTransmitters()+"\n"+
                            "Receiver list size for the device is ["+ deviceReceivers.size()+"]"+
                            "  max receivers: " + device.getMaxReceivers());
                    break;
                }
*/
            } catch (MidiUnavailableException e) {   // if anything goes wrong disable midi control
                QueleaProperties.get().setUseMidiControl(false);
                LOGGER.log(Level.WARNING, "MIDI device listing failed! Midi control disabled!");
                return;
            }
        }

        // If the device is located
        if (QueleaMidiDev_IN == null)
        {
            LOGGER.log(Level.INFO, "Quelea connector MIDI INPUT device NOT located!");
        }
        else
        {
            // If the device is located
            if (!(QueleaMidiDev_IN.isOpen()))
            {
                try
                {
                    QueleaMidiDev_IN.open();
                    LOGGER.log(Level.INFO, "MIDI INPUT device successfully opened.");
                }
                catch (MidiUnavailableException e)
                {   // if anything goes wrong disable midi control
                    QueleaProperties.get().setUseMidiControl(false);
                    LOGGER.log(Level.WARNING, "MIDI INPUT device listing failed! Midi control disabled!");
                    return;
                }
            }


            //Get list of transmitters for this device
            Transmitter externalTransmitter = QueleaMidiDev_IN.getTransmitter();;
            LOGGER.log(Level.INFO, "Default transmitter ["+ externalTransmitter.toString()+"]");
            externalTransmitter.setReceiver( new QueleaInputMidiReceiver(QueleaMidiDev_IN.getDeviceInfo().toString()) );
        }
    }

    public void setupMidiOutputConnection(String OutputDevice) throws MidiUnavailableException {
        LOGGER.log(Level.INFO, "Get midi device list");
        // Get midi information
        MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        // Midi device placeholder
        MidiDevice device;
        // Loop over the midi devices
        for (int d = 0; d < midiInfo.length; d++)
        {
            try {
                // Get the midi device
                device = MidiSystem.getMidiDevice(midiInfo[d]);

                //Get list of receivers for this device
                List<Receiver> deviceReceivers = device.getReceivers();

                //Get list of transmitters for this device
                List<Transmitter> deviceTransmitters = device.getTransmitters();

                //Info for the midi device
                LOGGER.log(Level.INFO, "Deivce[" + String.valueOf(d) + "] [" + midiInfo[d] + "]"+"\n"+
                        "Transmitter list size for the device is ["+ deviceTransmitters.size()+"]"+
                        "  max transmitters: " + device.getMaxTransmitters()+"\n"+
                        "Receiver list size for the device is ["+ deviceReceivers.size()+"]"+
                        "  max receivers: " + device.getMaxReceivers());

                if (OutputDevice.equals(midiInfo[d].toString()) && device.getMaxTransmitters() != 0)  {
                    LOGGER.log(Level.INFO, "Quelea connector MIDI device INPUT Located [" + midiInfo[d] + "]");
                    QueleaMidiDev_IN = device;
                    // break;
                }

                if (OutputDevice.equals(midiInfo[d].toString()) && device.getMaxReceivers() != 0)  {
                    LOGGER.log(Level.INFO, "Quelea connector MIDI device OUTPUT Located [" + midiInfo[d] + "]");
                    QueleaMidiDev_OUT = device;
                    // break;
                }

            } catch (MidiUnavailableException e) {   // if anything goes wrong disable midi control
                QueleaProperties.get().setUseMidiControl(false);
                LOGGER.log(Level.WARNING, "MIDI device listing failed! Midi control disabled!");
                return;
            }
        }

        // If the device is located
        if (QueleaMidiDev_IN == null)
        {
            LOGGER.log(Level.INFO, "Quelea connector MIDI device NOT located!");
        }
        else
        {
            // If the device is located
            if (!(QueleaMidiDev_IN.isOpen()))
            {
                try
                {
                    QueleaMidiDev_IN.open();
                    LOGGER.log(Level.INFO, "MIDI device successfully opened.");
                }
                catch (MidiUnavailableException e)
                {   // if anything goes wrong disable midi control
                    QueleaProperties.get().setUseMidiControl(false);
                    LOGGER.log(Level.WARNING, "MIDI device listing failed! Midi control disabled!");
                    return;
                }
            }


            //Get list of transmitters for this device
            Transmitter defautlTransmitter = MidiSystem.getTransmitter();
            LOGGER.log(Level.INFO, "Default transmitter ["+defautlTransmitter.toString()+"]");
            defautlTransmitter.setReceiver( new QueleaInputMidiReceiver(QueleaMidiDev_IN.getDeviceInfo().toString()) );
        }
    }

    public void testTone() throws InvalidMidiDataException, MidiUnavailableException
    {

        //------------------------- FOR DEBUG
        ShortMessage myMsg = new ShortMessage();
        // Start playing the note Middle C (60),
        // moderately loud (velocity = 93).
        myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
        long timeStamp = -1;
        Receiver       rcvr = MidiSystem.getReceiver();
        rcvr.send(myMsg, timeStamp);

        //------------------------------- FOR DEBUG


    }

    @Override
    public void finalize() throws Throwable {
        //super.finalize();
        // If the device is located
        if (!(QueleaMidiDev_IN.isOpen()))
        {
            QueleaMidiDev_IN.close();
            LOGGER.log(Level.INFO, "MIDI device successfully is now closed.");
        }
    }


    public class QueleaOutputMidiTransmitter implements Transmitter
    {

        @Override
        public void setReceiver(Receiver receiver) {

        }

        @Override
        public Receiver getReceiver() {
            return null;
        }

        @Override
        public void close() {

        }
    }

    public class QueleaInputMidiReceiver implements Receiver
    {
        private String name;
        public QueleaInputMidiReceiver(String toString) {
            this.name = name;
        }

        @Override
        public void send(MidiMessage message, long timeStamp)
        {
            byte[] aMsg = message.getMessage();
            int velocity = (int)aMsg[2];
            // take the MidiMessage msg and store it in a byte array
            LOGGER.log(Level.INFO, "Midi message received ["+aMsg.toString()+"]");
            try {
                if (false){}
                else if (midiEventMap.get("clear"          ).match(message)) { RCHandler.clear(); }
                else if (midiEventMap.get("black"          ).match(message)) { RCHandler.black(); }
                else if (midiEventMap.get("goToItem"       ).match(message)) { RCHandler.gotoItem( "gotoitem"+velocity  ); }
                else if (midiEventMap.get("next"           ).match(message)) { RCHandler.next() ; }
                else if (midiEventMap.get("nextItem"       ).match(message)) { RCHandler.nextItem(); }
                else if (midiEventMap.get("play"           ).match(message)) { RCHandler.play(); }
                else if (midiEventMap.get("prev"           ).match(message)) { RCHandler.prev(); }
                else if (midiEventMap.get("prevItem"       ).match(message)) { RCHandler.prevItem(); }
                else if (midiEventMap.get("section"        ).match(message)) { RCHandler.setLyrics("section"+velocity); }
                else if (midiEventMap.get("logo"		   	  ).match(message)) { RCHandler.logo(); }
                else if (midiEventMap.get("transposeDown1" ).match(message)) { RCHandler.transposeSong(1); }
                else if (midiEventMap.get("transposeDown2" ).match(message)) { RCHandler.transposeSong(2); }
                else if (midiEventMap.get("transposeDown3" ).match(message)) { RCHandler.transposeSong(3); }
                else if (midiEventMap.get("transposeDown4" ).match(message)) { RCHandler.transposeSong(4); }
                else if (midiEventMap.get("transposeDown5" ).match(message)) { RCHandler.transposeSong(5); }
                else if (midiEventMap.get("transposeDown6" ).match(message)) { RCHandler.transposeSong(6); }
                else if (midiEventMap.get("transposeUp0"   ).match(message)) { RCHandler.transposeSong(0); }
                else if (midiEventMap.get("transposeUp1"   ).match(message)) { RCHandler.transposeSong(-1); }
                else if (midiEventMap.get("transposeUp2"   ).match(message)) { RCHandler.transposeSong(-2); }
                else if (midiEventMap.get("transposeUp3"   ).match(message)) { RCHandler.transposeSong(-3); }
                else if (midiEventMap.get("transposeUp4"   ).match(message)) { RCHandler.transposeSong(-4); }
                else if (midiEventMap.get("transposeUp5"   ).match(message)) { RCHandler.transposeSong(-5); }
                else if (midiEventMap.get("transposeUp6"   ).match(message)) { RCHandler.transposeSong(-6); }

            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() {

        }
    }
}