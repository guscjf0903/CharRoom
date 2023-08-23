package org.share.clienttoserver;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;

import java.io.*;
import java.util.*;
@Getter
public class ClientFilePacket extends HeaderPacket {
    private final String filename;
    private File file;
    byte[] chunk;
    int chunknumber;
    int lastChunknumber;

    public ClientFilePacket(String filename, int chunknumber,byte[] chunk, int lastChunknumber) throws IOException {
        super(PacketType.CLIENT_FILE, 16 + filename.getBytes().length + chunk.length);
        this.filename = filename;
        this.chunknumber = chunknumber;
        this.chunk = chunk;
        this.lastChunknumber = lastChunknumber;
    }


    public byte[] getBodyBytes(){// 파일이름길이 + 파일이름 + 청크넘버 + 청크를 바이트로 변환
        byte[] filenamebyte = this.filename.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(filenamebyte.length), 0, bodyBytes, 0, 4);
        System.arraycopy(filenamebyte, 0, bodyBytes, 4, filenamebyte.length);
        System.arraycopy(intToByteArray(chunknumber), 0, bodyBytes, 4 + filenamebyte.length, 4);
        System.arraycopy(intToByteArray(chunk.length), 0, bodyBytes, 8 + filenamebyte.length, 4);
        System.arraycopy(chunk, 0, bodyBytes, 12 + filenamebyte.length, chunk.length);
        System.arraycopy(intToByteArray(lastChunknumber), 0, bodyBytes, 12 + filenamebyte.length + chunk.length, 4);

        return bodyBytes;
    }


    public static ClientFilePacket byteToClientFilePacket(byte[] bodyBytes) throws IOException { //바이트를 파일로 변환
        int filenamelength = byteArrayToInt(bodyBytes, 8, 11);
        String filename = new String(bodyBytes, 12, filenamelength);
        int chunknumber = byteArrayToInt(bodyBytes, 12 + filenamelength, 15 + filenamelength);
        int chunklength = byteArrayToInt(bodyBytes, 16 + filenamelength, 19 + filenamelength);
        byte[] chunk = Arrays.copyOfRange(bodyBytes, 20 + filenamelength, 20 + filenamelength + chunklength);
        int lastChunknumber = byteArrayToInt(bodyBytes, 20 + filenamelength + chunklength, 23 + filenamelength + chunklength);
        return new ClientFilePacket(filename, chunknumber,chunk, lastChunknumber);
    }

}
