package org.share.clienttoserver;

import org.share.HeaderPacket;
import org.share.PacketType;

import java.io.*;
import java.util.*;

public class ClientFilePacket extends HeaderPacket {
    private String name;
    private File file;


    public ClientFilePacket(String name, File file) {
        super(PacketType.CLIENT_FILE, 8 + name.getBytes().length + (int) file.length());
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public byte[] getBodyBytes() {// 이름길이 + 이름 + 메세지길이 + 메세지를 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] fileBytes = readFileBytes(file);
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);
        System.arraycopy(intToByteArray(fileBytes.length), 0, bodyBytes, 4 + nameBytes.length, 4);
        System.arraycopy(fileBytes, 0, bodyBytes, 8 + nameBytes.length, fileBytes.length);
        return bodyBytes;
    }

    public static ClientFilePacket byteToClientFilePacket(byte[] bodyBytes) { //바이트를 파일로 변환
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환

        int filelength = byteArrayToInt(bodyBytes, 12 + nameLength, 15 + nameLength);
        byte[] fileData = Arrays.copyOfRange(bodyBytes, 16 + nameLength, 16 + nameLength + filelength);

        File tempFile = createTempFileFromByteArray(fileData);

        return new ClientFilePacket(name, tempFile);
    }

    private static File createTempFileFromByteArray(byte[] fileData) { //파일을 재조합해서 다시 패킷을 만들어줄때 쓰는 메서드
        try {
            File tempFile = File.createTempFile("received", ".tmp"); // 임시 파일 생성
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                fileOutputStream.write(fileData); // 바이트 데이터를 파일에 쓰기
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readFileBytes(File file){  //file을 바이트로 바꿔줌
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteOutput.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            return byteOutput.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
