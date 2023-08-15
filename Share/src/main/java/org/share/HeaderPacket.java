package org.share;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.share.PacketType.*;

@AllArgsConstructor
@Getter
public abstract class HeaderPacket {
    protected PacketType packetType;
    protected int bodyLength; //타입을뺀 나머지 바디의 길이


    public byte[] getHeaderBytes() { //패킷타입 + 바디length byte로 변환(0~3, 4~7)
        byte[] headerBytes = new byte[8];
        System.arraycopy(intToByteArray(packetType.getValue()), 0, headerBytes, 0, 4);
        System.arraycopy(intToByteArray(bodyLength), 0, headerBytes, 4, intToByteArray(bodyLength).length);
        return headerBytes;
    }

    abstract public byte[] getBodyBytes();

    public static PacketType byteToPackettype(byte[] headerByte) {
        int type = byteArrayToInt(headerByte, 0, 3);
        return clientFindByValue(type);
    }

    public static int byteToBodyLength(byte[] headerByte) {
        return byteArrayToInt(headerByte, 4, 7);
    }


    public static byte[] intToByteArray(int num) {
        byte[] byteArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            byteArray[i] = (byte) ((num >> (8 * i)) & 0xFF);
        }
        return byteArray;
    }

    public static int byteArrayToInt(byte[] byteArray, int startIdx, int endIdx) {
        int value = 0;
        for (int i = startIdx; i <= endIdx; i++) {
            value += ((int) byteArray[i] & 0xFF) << (8 * (i - startIdx));
        }
        return value;
    }

}


