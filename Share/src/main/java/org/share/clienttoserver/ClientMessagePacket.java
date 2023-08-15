package org.share.clienttoserver;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

@Getter
public class ClientMessagePacket extends HeaderPacket {
    private final String message;
    private final String name;

    public ClientMessagePacket(String message, String name) {
        super(PacketType.CLIENT_MESSAGE, 8 + name.getBytes().length + message.getBytes().length);
        this.message = message;
        this.name = name;
    }


    public byte[] getBodyBytes() {// 이름길이 + 이름 + 메세지길이 + 메세지를 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] messageBytes = message.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);
        System.arraycopy(intToByteArray(messageBytes.length), 0, bodyBytes, 4 + nameBytes.length, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 8 + nameBytes.length, messageBytes.length);
        return bodyBytes;
    }

    public static ClientMessagePacket byteToClientMessagePacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환

        int messageLength = byteArrayToInt(bodyBytes, 12 + nameLength, 15 + nameLength);
        String message = new String(bodyBytes, 16 + nameLength, messageLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new ClientMessagePacket(message, name);
    }


}
