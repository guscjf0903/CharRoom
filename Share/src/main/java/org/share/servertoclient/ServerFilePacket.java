package org.share.servertoclient;

import lombok.Getter;
import org.share.HeaderPacket;
import org.share.PacketType;
import org.share.clienttoserver.ClientFilePacket;

import java.io.*;
import java.util.Arrays;

import static org.share.HeaderPacket.*;
@Getter
public class ServerFilePacket extends HeaderPacket {
    private final String name;
    private final String fileName;
    private final int chunkNumber;
    private final byte[] chunk;
    private final int lastChunkNumber;

    public ServerFilePacket(String name,String fileName, int chunkNumber,byte[] chunk,int lastChunkNumber) {
        super(PacketType.SERVER_FILE, 20 + name.getBytes().length + fileName.getBytes().length + chunk.length);
        this.name = name;
        this.fileName = fileName;
        this.chunkNumber = chunkNumber;
        this.chunk = chunk;
        this.lastChunkNumber = lastChunkNumber;
    }


    public byte[] getBodyBytes() {// 이름길이 + 이름 + 파일이름길이 + 파일이름 + 메세지길이 + 메세지를 바이트로 변환
        byte[] namebyte = this.name.getBytes();
        byte[] filenamebyte = this.fileName.getBytes();
        byte[] bodyBytes = new byte[bodyLength];

        System.arraycopy(intToByteArray(namebyte.length), 0, bodyBytes, 0, 4);
        System.arraycopy(namebyte, 0, bodyBytes, 4, namebyte.length);

        System.arraycopy(intToByteArray(filenamebyte.length), 0, bodyBytes, 4 + namebyte.length, 4);
        System.arraycopy(filenamebyte, 0, bodyBytes, 8 + namebyte.length, filenamebyte.length);

        System.arraycopy(intToByteArray(chunkNumber), 0, bodyBytes, 8 + namebyte.length + filenamebyte.length, 4);

        System.arraycopy(intToByteArray(chunk.length), 0, bodyBytes, 12 + namebyte.length +  filenamebyte.length, 4);
        System.arraycopy(chunk, 0, bodyBytes, 16 + namebyte.length + filenamebyte.length, chunk.length);
        System.arraycopy(intToByteArray(lastChunkNumber), 0, bodyBytes, 16 + namebyte.length + filenamebyte.length + chunk.length, 4);
        return bodyBytes;
    }

    public static ServerFilePacket byteToServerFilePacket(byte[] bodyBytes) {
        int namelength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, namelength);

        int filenamelength = byteArrayToInt(bodyBytes, 12 + namelength, 15 + namelength);
        String filename = new String(bodyBytes, 16 + namelength, filenamelength);

        int chunknumber = byteArrayToInt(bodyBytes, 16 + namelength + filenamelength, 19 + namelength + filenamelength);
        int chunklength = byteArrayToInt(bodyBytes, 20 + namelength + filenamelength, 23 + namelength + filenamelength);
        byte[] chunk = Arrays.copyOfRange(bodyBytes, 24 + namelength + filenamelength, 24 + chunklength + namelength + filenamelength);
        int lastChunknumber = byteArrayToInt(bodyBytes, 24 + namelength + filenamelength + chunklength, 27 + namelength + filenamelength + chunklength);
        return new ServerFilePacket(name, filename, chunknumber, chunk, lastChunknumber);
    }
}
