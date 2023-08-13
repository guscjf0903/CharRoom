package org.share.clienttoserver;

import org.share.*;
import org.share.PacketType;

import java.io.*;
import java.util.Arrays;

public class newClientFilePacket extends HeaderPacket {
    private File file;


    public newClientFilePacket(File file) { //타입 + 파일길이로 헤더 구성
        super(PacketType.CLIENT_FILE, 4  + (int) file.length());
        this.file = file;
    }

    public File getFile() {
        return file;
    }
    public static int getfileLength(byte[] bytedata) { //파일길이 구하기
        return  byteArrayToInt(bytedata, 4 , 7);
    }
    public byte[] getBodyBytes() {// body를 파일로 그냥 바로 사용하기때문에 getFile메서드 사용
        return null;
    }

}
