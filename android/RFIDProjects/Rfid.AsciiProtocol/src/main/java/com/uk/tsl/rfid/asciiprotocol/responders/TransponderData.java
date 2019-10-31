package com.uk.tsl.rfid.asciiprotocol.responders;

import java.util.Date;

import com.uk.tsl.rfid.asciiprotocol.enumerations.TransponderAccessErrorCode;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TransponderBackscatterErrorCode;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Represents a transponder response from an Inventory, Read, Write, Lock or Kill command
*/
public class TransponderData
{
    /**
     The value of WordsWritten when not a write response

    */
    public static final int NO_WORDS_WRITTEN = -1;

    /**
     Initializes a new instance of the TransponderData class

     @param crc The Crc value
     @param epc The Epc value
     @param index The Index value
     @param didKill - true if the transponder is the result of a successful Kill command
     @param didLock - true if the transponder is the result of a successful Lock command
     @param pc The Pc value
     @param readData The ReadData value
     @param rssi The Rssi value
     @param timestamp the timestamp of the transponder (based on the response's timestamp
     @param accessErrorCode the access error code value
     @param backscatterErrorCode the backscatter error code value
     @param tidData The TID data value
     @param wordsWritten The WordsWritten value
    */
    public TransponderData(
            Integer crc,
            String epc,
            Integer index,
            boolean didKill,
            boolean didLock,
            Integer pc,
            byte[] readData,
            Integer rssi,
            Date timestamp,
            TransponderAccessErrorCode accessErrorCode,
            TransponderBackscatterErrorCode backscatterErrorCode,
            byte[] tidData,
            int wordsWritten
            )
    {
        this(crc, epc, index, didKill, didLock, pc, null, readData, rssi, timestamp, accessErrorCode, backscatterErrorCode, tidData, wordsWritten);
    }

    /**
     Initializes a new instance of the TransponderData class

     @param crc The Crc value
     @param epc The Epc value
     @param index The Index value
     @param didKill - true if the transponder is the result of a successful Kill command
     @param didLock - true if the transponder is the result of a successful Lock command
     @param pc The Pc value
     @param qt The QT value
     @param readData The ReadData value
     @param rssi The Rssi value
     @param timestamp the timestamp of the transponder (based on the response's timestamp
     @param accessErrorCode the access error code value
     @param backscatterErrorCode the backscatter error code value
     @param tidData The TID data value
     @param wordsWritten The WordsWritten value
     */
    public TransponderData(
            Integer crc,
            String epc,
            Integer index,
            boolean didKill,
            boolean didLock,
            Integer pc,
            Integer qt,
            byte[] readData,
            Integer rssi,
            Date timestamp,
            TransponderAccessErrorCode accessErrorCode,
            TransponderBackscatterErrorCode backscatterErrorCode,
            byte[] tidData,
            int wordsWritten
    )
    {
        setCrc(crc);
        setEpc(epc);
        setIndex(index);
        setKill(didKill);
        setLock(didLock);
        setPc(pc);
        setQt(qt);
        setReadData(readData);
        setRssi(rssi);
        setTimestamp(timestamp);
        setAccessErrorCode(accessErrorCode);
        setBackscatterErrorCode(backscatterErrorCode);
        setTidData(tidData);
        setWordsWritten(wordsWritten);
    }

    public TransponderData()
    {

    }


    /**
     Gets the CRC part of an inventory response from a transponder
     or null if CRC output is not enabled
    */
    private Integer privateCrc;
    public final Integer getCrc()
    {
        return privateCrc;
    }
    private void setCrc(Integer value)
    {
        privateCrc = value;
    }

    /**
     Gets the EPC part of an inventory response from a transponder
    */
    private String privateEpc;
    public final String getEpc()
    {
        return privateEpc;
    }
    private void setEpc(String value)
    {
        privateEpc = value;
    }

    /**
     Gets the Index of the transponder or null if index output ("IX:") is not enabled
    */
    private Integer privateIndex;
    public final Integer getIndex()
    {
        return privateIndex;
    }
    private void setIndex(Integer value)
    {
        privateIndex = value;
    }

    /**
     * @return true if the transponder was successfully killed
     */
    public final boolean didKill() { return privateDidKill; }
    private boolean privateDidKill;
    private void setKill(boolean value) { privateDidKill = value; }

    /**
     * @return true if the transponder was successfully locked
     */
    public final boolean didLock() { return privateDidLock; }
    private boolean privateDidLock;
    private void setLock(boolean value) { privateDidLock = value; }

    /**
     Gets the PC part of an inventory response from a transponder
     or null if PC output is not enabled
     */
    private Integer privatePc;
    public final Integer getPc()
    {
        return privatePc;
    }
    private void setPc(Integer value)
    {
        privatePc = value;
    }

    /**
     Gets the QT part of an inventory response from a transponder
     or null if QT output is not enabled
     */
    private Integer privateQt;
    public final Integer getQt()
    {
        return privateQt;
    }
    private void setQt(Integer value)
    {
        privateQt = value;
    }

    /**
     Gets the data read from the transponder (only applicable when raise from read commands)
    */
    private byte[] privateReadData;
    public final byte[] getReadData()
    {
        return privateReadData;
    }
    private void setReadData(byte[] readData)
    {
        privateReadData = readData;
    }

    /**
     Gets the RSSI of a transponder in an inventory response
     or null if RSSI output is not enabled
    */
    private Integer privateRssi;
    public final Integer getRssi()
    {
        return privateRssi;
    }
    private void setRssi(Integer value)
    {
        privateRssi = value;
    }

    /**
     * Gets the timestamp for this transponder
     */
    public final Date getTimestamp() { return privateTimestamp; }
    private Date privateTimestamp;
    private void setTimestamp(Date value) { privateTimestamp = value; }

    /**
     * Gets the access error code for this transponder
     */
    public final TransponderAccessErrorCode getAccessErrorCode() { return privateAccessErrorCode; }
    private TransponderAccessErrorCode privateAccessErrorCode;
    private void setAccessErrorCode(TransponderAccessErrorCode value) { privateAccessErrorCode = value; }

    /**
     * Gets the backscatter error for this transponder
     */
    public final TransponderBackscatterErrorCode getBackscatterErrorCode() { return privateBackscatterErrorCode; }
    private TransponderBackscatterErrorCode privateBackscatterErrorCode;
    private void setBackscatterErrorCode(TransponderBackscatterErrorCode value) { privateBackscatterErrorCode = value; }


    /**
     Gets the TID data read from the transponder (only applicable when Impinj Monza Fast Id extension used)
    */
    private byte[] privateTidData;
    public final byte[] getTidData()
    {
        return privateTidData;
    }
    private void setTidData(byte[] idData)
    {
        privateTidData = idData;
    }

    /**
     Gets the number of words written to the transponder (only applicable when raised from write commands)
    */
    private int privateWordsWritten;
    public final int getWordsWritten()
    {
        return privateWordsWritten;
    }
    private void setWordsWritten(int value)
    {
        privateWordsWritten = value;
    }
}