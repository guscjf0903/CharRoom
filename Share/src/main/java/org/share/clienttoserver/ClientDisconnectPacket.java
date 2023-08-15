package org.share.clienttoserver;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

@Getter
public class ClientDisconnectPacket extends HeaderPacket {
    private final String name;

    public ClientDisconnectPacket(String name) { //디스커넥트는 message가 필요없음
        super(PacketType.CLIENT_DISCONNECT, 4 + name.getBytes().length);
        this.name = name;
    }

    public byte[] getBodyBytes() { //이름길이 + 이름을 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4); //이름길이
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length); //이름
        return bodyBytes;
    }

    public static ClientDisconnectPacket byteToClientDisconnectPacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환
        return new ClientDisconnectPacket(name);
    }
}
