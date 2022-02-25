package fr.calitro.tpjc;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class applet extends Applet {
	
	private static final byte CAGNOTTE_CLA = (byte) 0xB0;
	private static final short MAX_CAGNOTTE= (short) 32000;
	
	private short pinCode;
	private short cagnotte;

	private applet() {
		cagnotte = 0;
		pinCode = 1234;
	}

	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		new applet().register();
	}

	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();
		
		if(buffer[ISO7816.OFFSET_CLA] != CAGNOTTE_CLA) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}
		
		byte ins = buffer[ISO7816.OFFSET_INS];
		
		switch(ins) {
	
			// APDU Get cagnotte
			// 0xB0 0x01 0x00 0x00 0x00 0x02;
			case 0x01:
				APDUUtils.shortToByteArray(cagnotte, buffer, (short) 0);
				apdu.setOutgoingAndSend((short) 0, (short) 2);
				break;
				
			// APDU Increase cagnotte
			// Example 31999
			// 0xB0 0x02 0x7C 0xFF 0x00 0x00;
			case 0x02:
				if(cagnotte >= MAX_CAGNOTTE) {
					ISOException.throwIt(ISO7816.SW_WARNING_STATE_UNCHANGED);
				}
				short amountToAdd = APDUUtils.byteArrayToShort(buffer, ISO7816.OFFSET_P1);
				if (amountToAdd >= (short)(MAX_CAGNOTTE - cagnotte)) {
					cagnotte = MAX_CAGNOTTE;
				} else {
					cagnotte = (short) (cagnotte + amountToAdd);
				}
				break;
				
			// APDU Decrease cagnotte
			// Example 9 with right PIN 1234
			// 0xB0 0x03 0x00 0x09 0x02 0x04 0xD2 0x00;
			
			// Example 31999 with right PIN 1234
			// 0xB0 0x03 0x7C 0xff 0x02 0x04 0xD2 0x00;
			
			// Example 9 with wrong PIN 1235
			// 0xB0 0x03 0x00 0x09 0x02 0x04 0xD3 0x00;
				
			// Example 31999 with wrong PIN 1235
			// 0xB0 0x03 0x7C 0xff 0x02 0x04 0xD3 0x00;
			case 0x03:
				short pinCodeInput = -1;
				pinCodeInput = APDUUtils.byteArrayToShort(buffer, ISO7816.OFFSET_CDATA);
				
				if(pinCodeInput < (short) 0 || pinCodeInput != pinCode) {
					ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
				}
				
				short amountToRemove = APDUUtils.byteArrayToShort(buffer, ISO7816.OFFSET_P1);
				
				
				if(amountToRemove > cagnotte) {
					ISOException.throwIt(ISO7816.SW_WARNING_STATE_UNCHANGED);
				}
				
				cagnotte = (short) (cagnotte - amountToRemove);
				break;
				
			default: 
				break;
		}

	}

}
