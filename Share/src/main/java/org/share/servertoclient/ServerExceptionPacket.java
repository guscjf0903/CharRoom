package org.share.servertoclient;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;
@Getter
public class ServerExceptionPacket extends HeaderPacket {
    private final String message;

    public ServerExceptionPacket(String message) { //헤더내용 삽입
        super(PacketType.SERVER_EXCEPTION, 4 + message.getBytes().length);
        this.message = message;
    }

    public byte[] getBodyBytes() {
        byte[] messageBytes = message.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(messageBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 4, messageBytes.length);
        return bodyBytes;
    }

    public static ServerExceptionPacket byteToServerExceptionPacket(byte[] bodyBytes) {
        int messageLength = byteArrayToInt(bodyBytes, 8, 11);
        String message = new String(bodyBytes, 12, messageLength); //인덱스 12부터 messageLength만큼 문자열로 변환
        return new ServerExceptionPacket(message);
    }

}
