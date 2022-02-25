package fr.calitro.tpjc;

public class APDUUtils {

    public static void shortToByteArray(short x, byte[] buffer, short offset) {
        buffer[(short) (offset+1)] = (byte)(x & 0xff);
        buffer[(short) offset] = (byte)((x >> 8) & 0xff);
    }
    
    
    public static short byteArrayToShort(byte[] buffer, short offset) {
        return (short)( ((buffer[(short) offset] & 0xFF) << 8) | (buffer[(short) (offset+1)] & 0xFF) );
    }
    
}