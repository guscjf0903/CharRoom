package org.share.servertoclient;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;
@Getter
public class ServerDisconnectPacket extends HeaderPacket {
    private final String name;

    public ServerDisconnectPacket(String name) { //헤더내용 삽입
        super(PacketType.SERVER_DISCONNECT, 4 + name.getBytes().length);
        this.name = name;
    }

    public byte[] getBodyBytes() {
        byte[] nameBytes = name.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);
        return bodyBytes;
    }

    public static ServerDisconnectPacket byteToServerDisconnectPacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환
        return new ServerDisconnectPacket(name);
    }
}
