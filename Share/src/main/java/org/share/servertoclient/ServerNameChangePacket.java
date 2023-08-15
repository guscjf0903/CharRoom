package org.share.servertoclient;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

@Getter
public class ServerNameChangePacket extends HeaderPacket {
    private final String changename;
    private final String name;

    public ServerNameChangePacket(String name, String changename) {
        super(PacketType.SERVER_CHANGENAME, 8 + name.getBytes().length + changename.getBytes().length);
        this.name = name;
        this.changename = changename;
    }

    public byte[] getBodyBytes() {// 이름길이 + 이름 + 메세지길이 + 메세지를 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] changenameBytes = changename.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);
        System.arraycopy(intToByteArray(changenameBytes.length), 0, bodyBytes, 4 + nameBytes.length, 4);
        System.arraycopy(changenameBytes, 0, bodyBytes, 8 + nameBytes.length, changenameBytes.length);
        return bodyBytes;
    }

    public static ServerNameChangePacket byteToServerNameChangePacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환

        int changenameLength = byteArrayToInt(bodyBytes, 12 + nameLength, 15 + nameLength);
        String changename = new String(bodyBytes, 16 + nameLength, changenameLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new ServerNameChangePacket(name, changename);
    }


}
