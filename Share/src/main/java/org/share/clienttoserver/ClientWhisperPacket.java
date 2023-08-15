package org.share.clienttoserver;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

import static org.share.HeaderPacket.*;
@Getter
public class ClientWhisperPacket extends HeaderPacket {
    private final String message;
    private final String whispername;

    public ClientWhisperPacket(String message, String whispername) {
        super(PacketType.CLIENT_WHISPERMESSAGE, 8 + whispername.getBytes().length + message.getBytes().length);
        this.message = message;
        this.whispername = whispername;
    }

    public byte[] getBodyBytes() {// 이름길이 + 이름 + 메세지길이 + 메세지를 바이트로 변환
        byte[] whispernameBytes = whispername.getBytes();
        byte[] messageBytes = message.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(whispernameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(whispernameBytes, 0, bodyBytes, 4, whispernameBytes.length);
        System.arraycopy(intToByteArray(messageBytes.length), 0, bodyBytes, 4 + whispernameBytes.length, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 8 + whispernameBytes.length, messageBytes.length);
        return bodyBytes;
    }

    public static ClientWhisperPacket byteToClientWhisperPacket(byte[] bodyBytes) {
        int whispernameLength = byteArrayToInt(bodyBytes, 8, 11);
        String whispername = new String(bodyBytes, 12, whispernameLength); //인덱스 12부터 nameLength만큼 문자열로 변환

        int messageLength = byteArrayToInt(bodyBytes, 12 + whispernameLength, 15 + whispernameLength);
        String message = new String(bodyBytes, 16 + whispernameLength, messageLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new ClientWhisperPacket(message, whispername);
    }


}
