//package org.share;
//
////헤더 12바이트 : 클라이언트 패킷 4바이트 + 메세지길이 4바이트 + 이름 길이 4바이트 구성.
////바디 13~1024바이트 : 처음에 헤더를 보내고 헤더 내용으로 길이와 타입을 예측한다음 바디를 받는다.
//
//import java.io.ByteArrayOutputStream;
//public class ByteConversion {
//    public static byte[] headerByteConversion(HeaderPacket packet) { // 헤더는 무조건 정수 3개의 정보 12바이트를 넣는다.(타입 + 메세지 + 이름)
//        ByteArrayOutputStream headerBytes = new ByteArrayOutputStream(12);
//        byte[] typeByte;
//        byte[] messageLengthByte;
//        byte[] nameLengthByte;
//        typeByte = intToByteArray(packet.getIntPacketType());
//        messageLengthByte = intToByteArray(packet.getMessageLength());
//        nameLengthByte = intToByteArray(packet.getNameLength());
//
//
//        headerBytes.write(typeByte, 0, 4);
//        headerBytes.write(messageLengthByte, 0, 4);
//        headerBytes.write(nameLengthByte, 0, 4);
//
//        return headerBytes.toByteArray();
//    }
//
//    public static byte[] bodyByteConversion(HeaderPacket packet) { //헤더에서 해석한 3개의 정수로 타입을 해석 메세지+이름으로 바디를 해석
//        ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
//        byte[] messageByte;
//        byte[] nameByte;
//
//        nameByte = packet.getName().getBytes();
//        bodyBytes.write(nameByte,0, packet.getNameLength());
//        messageByte = packet.getMessage().getBytes();
//        bodyBytes.write(messageByte,0,packet.getMessageLength());
//
//        return bodyBytes.toByteArray();
//    }
//    static byte[] intToByteArray (int number){
//        byte[] byteArray = new byte[4];
//        for (int i = 0; i < 4; i++) {
//            byteArray[i] = (byte) ((number >> (8 * i)) & 0xFF);
//        }
//        return byteArray;
//    }
//}
//
