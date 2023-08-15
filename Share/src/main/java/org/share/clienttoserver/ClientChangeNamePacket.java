package org.share.clienttoserver;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

@Getter
public class ClientChangeNamePacket extends HeaderPacket {
    private final String name;
    private final String changename;

    public ClientChangeNamePacket(String name, String changename){
        super(PacketType.CLIENT_CHANGENAME, 8 + name.getBytes().length + changename.getBytes().length);
        this.name = name;
        this.changename = changename;
    }
    public byte[] getBodyBytes(){//원래이름길이 + 이름 + 바뀐이름길이 + 바뀐이름 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] changenameBytes = changename.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length),0,bodyBytes,0,4);
        System.arraycopy(nameBytes,0,bodyBytes,4,nameBytes.length);
        System.arraycopy(intToByteArray(changenameBytes.length),0,bodyBytes,4 + nameBytes.length,4);
        System.arraycopy(changenameBytes,0,bodyBytes,8+nameBytes.length,changenameBytes.length);
        return bodyBytes;
    }
    public static ClientChangeNamePacket byteToClientChangeNamePacket(byte[] bodyBytes){
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength);

        int changenameLength = byteArrayToInt(bodyBytes,12+nameLength,15 + nameLength);
        String changename = new String(bodyBytes, 16 + nameLength, changenameLength);

        return new ClientChangeNamePacket(name, changename);
    }


}
