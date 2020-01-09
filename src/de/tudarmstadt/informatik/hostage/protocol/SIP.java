package de.tudarmstadt.informatik.hostage.protocol;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.hostage.wrapper.Packet;

/**
 * SIP protocol. Implementation of RFC document 3261 It can handle the
 * following requests: REGISTER, INVITE, ACK, BYE. For all other requests
 * '400 Bad Request' will be replied.
 * @author Wulf Pfeiffer
 */
public class SIP implements Protocol {
	
	private enum STATE {
		NONE, CLOSED
	}
	
	private STATE state = STATE.NONE;
		
	private static final String VERSION = "SIP/2.0";
	private static final String REGISTER = "REGISTER";
	private static final String INVITE = "INVITE";
	private static final String ACK= "ACK";
	private static final String BYE = "BYE";
	private static final String STATUS_CODE_200 = "200 OK";
	private static final String STATUS_CODE_400 = "400 Bad Request";
	private static final String STATUS_CODE_505 = "505 Version Not Supported";
	
	private String header;
	private String sdpPayload;

	@Override
	public int getPort() {
		return 5060;
	}

	@Override
	public boolean isClosed() {
		return (state == STATE.CLOSED);
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public List<Packet> processMessage(Packet requestPacket) {
		String request = null;
		if (requestPacket != null) {
			request = requestPacket.toString();
		}
		List<Packet> responsePackets = new ArrayList<Packet>();
		String[] lines = request.split("\r\n");
		extractLines(lines);
		
		if(!lines[0].contains(VERSION)) {
			responsePackets.add(getVersionNotSupportedResponse());
			return responsePackets;
		} else if(lines[0].contains(REGISTER)) {
			responsePackets.add(getOkResponse());
		} else if(lines[0].contains(INVITE)) {
			responsePackets.add(getOkResponseWithSDP());
		} else if(lines[0].contains(BYE)) {
			responsePackets.add(getOkResponse());
			state = STATE.CLOSED;
		} else if(lines[0].contains(ACK)) {
			//nothing to do here
		} else {
			responsePackets.add(getBadRequestResponse());
		}
		
		return responsePackets;
	}

	@Override
	public TALK_FIRST whoTalksFirst() {
		return TALK_FIRST.CLIENT;
	}
	
	@Override
	public String toString() {
		return "SIP";
	}
	
	private void extractLines(String[] lines) {
		header = "";
		sdpPayload = "";
		StringBuffer sbHeader = new StringBuffer();
		StringBuffer sbSdp = new StringBuffer();
		boolean recordHeader = false;
		boolean recordSdp = false;
		for (String line : lines) {
			if (line.startsWith("Via:")) {
				recordHeader = true;
			} else if (line.startsWith("Max-Forwards")) {
				recordHeader = false;
				header = sbHeader.toString();
			} else if(line.startsWith("v=")) {
				recordSdp = true;
			} else if(line.startsWith("a=")) {
				sbSdp.append(line + "\r\n");
				sdpPayload = sbSdp.toString();
				break;
			}
			if(recordHeader) {
				sbHeader.append(line + "\r\n");
			} else if(recordSdp) {
				sbSdp.append(line + "\r\n");
			}
		}
	}
	
	private Packet getOkResponseWithSDP() {
		StringBuffer sb = new StringBuffer();
		sb.append(VERSION + " " + STATUS_CODE_200 + "\r\n");
		sb.append(header);
		sb.append("Content-Type: application/sdp\r\n");
		sb.append("Content-Length:   " + sdpPayload.length() + "\r\n");
		sb.append("\r\n");
		sb.append(sdpPayload);

		return new Packet(sb.toString(), toString());
	}
	
	private Packet getOkResponse() {
		StringBuffer sb = new StringBuffer();
		sb.append(VERSION + " " + STATUS_CODE_200 + "\r\n");
		sb.append(header);
		sb.append("Content-Length:   0\r\n");
		
		return new Packet(sb.toString(), toString());
	}
	
	private Packet getBadRequestResponse() {
		StringBuffer sb = new StringBuffer();
		sb.append(VERSION + " " + STATUS_CODE_400 + "\r\n");
		sb.append(header);
		sb.append("Content-Length:   0\r\n");
		
		return new Packet(sb.toString(), toString());
	}
	
	private Packet getVersionNotSupportedResponse() {
		StringBuffer sb = new StringBuffer();
		sb.append(VERSION + " " + STATUS_CODE_505 + "\r\n");
		sb.append(header);
		sb.append("Content-Length:   0\r\n");
		
		return new Packet(sb.toString(), toString());
	}

}
