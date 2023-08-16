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
    private String name;
    private File file;

    private String filename;


    public ServerFilePacket(String filename, File file,String name) {
        super(PacketType.SERVER_FILE, 8 + name.getBytes().length + (int) file.length() + filename.getBytes().length);
        this.name = name;
        this.file = file;
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }
    public String getFilename() {
        return filename;
    }



    public byte[] getBodyBytes() {// 이름길이 + 이름 + 메세지길이 + 메세지를 바이트로 변환
        byte[] nameBytes = name.getBytes();
        byte[] fileBytes = readFileBytes(file);
        byte[] filenameBytes = filename.getBytes();
        byte[] bodyBytes = new byte[4096];
        System.arraycopy(intToByteArray(nameBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(nameBytes, 0, bodyBytes, 4, nameBytes.length);

        System.arraycopy(intToByteArray(filenameBytes.length), 0, bodyBytes, 4 + nameBytes.length, 4);
        System.arraycopy(filenameBytes, 0, bodyBytes, 8 + nameBytes.length, filenameBytes.length);

        System.arraycopy(intToByteArray(fileBytes.length), 0, bodyBytes, 8 + nameBytes.length + filenameBytes.length , 4);
        System.arraycopy(fileBytes, 0, bodyBytes, 12 + nameBytes.length + filenameBytes.length, fileBytes.length);

        return bodyBytes;
    }

    public static ServerFilePacket byteToServerFilePacket(byte[] bodyBytes) {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환

        int filenamelength = byteArrayToInt(bodyBytes, 12 + nameLength, 15 + nameLength);
        String filename = new String(bodyBytes, 16 + nameLength, 16 + nameLength + filenamelength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        int filelength = byteArrayToInt(bodyBytes, 16 + nameLength + filenamelength, 19 + nameLength + filenamelength);
        byte[] fileData = Arrays.copyOfRange(bodyBytes, 20 + nameLength + filenamelength, 20 + nameLength + filenamelength + filelength);


        File tempFile = createTempFileFromByteArray(fileData);

        return new ServerFilePacket(filename, tempFile,name);
    }

    private static File createTempFileFromByteArray(byte[] fileData) {
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
            byte[] buffer = new byte[2048];
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
