package org.share.clienttoserver;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

//0~3 타입 + 4~7 타입을 뺀 바디길이 + 8~11 이름길이 + 12~이름
@Getter
public class ClientConnectPacket extends HeaderPacket {
    private final String name;

    public ClientConnectPacket(String name) { //디스커넥트는 message가 필요없음
        super(PacketType.CLIENT_CONNECT, 4 + name.getBytes().length);
        this.name = name;
    }

    public byte[] getBodyBytes() { //이름길이 + 이름을 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);
        return bodyBytes;
    }

    public static ClientConnectPacket byteToClientConnectPacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환
        return new ClientConnectPacket(name);
    }



}
