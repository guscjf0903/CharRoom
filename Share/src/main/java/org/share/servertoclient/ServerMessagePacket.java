package org.share.servertoclient;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;
@Getter
public class ServerMessagePacket extends HeaderPacket {
    private final String message;
    private final String name;

    public ServerMessagePacket(String message, String name) { //헤더내용 삽입
        super(PacketType.SERVER_MESSAGE, 8 + name.getBytes().length + message.getBytes().length);
        this.message = message;
        this.name = name;
    }

    public byte[] getBodyBytes() {
        byte[] nameBytes = name.getBytes();
        byte[] messageBytes = message.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);
        System.arraycopy(intToByteArray(messageBytes.length), 0, bodyBytes, 4 + nameBytes.length, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 8 + nameBytes.length, messageBytes.length);
        return bodyBytes;
    }

    public static ServerMessagePacket byteToServerMessagePacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환

        int messageLength = byteArrayToInt(bodyBytes, 12 + nameLength, 15 + nameLength);
        String message = new String(bodyBytes, 16 + nameLength, messageLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new ServerMessagePacket(message, name);
    }
}
