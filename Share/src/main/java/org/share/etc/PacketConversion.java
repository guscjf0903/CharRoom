//package org.share;
//// 헤더 : 0 ~ 20바이트 - 0~9 패킷의 타입, 10 ~ 13 메세지의 길이
//// 바디 : 14 ~ 39 닉네임 - 40 ~ 1024 메세지의 내용
//
//public class PacketConversion {
//    public static int[] headerPacketConversion(byte[] headerbytes){ //배열 정수 3개는 타입 + 길이 + 닉네임
//        int[] result = new int[3];
//        int type = byteArrayToInt(headerbytes,0,3);
//        int messagelength = byteArrayToInt(headerbytes,4,7);
//        int namelength = byteArrayToInt(headerbytes,8,11);
//
//        result[0] = type;
//        result[1] = messagelength;
//        result[2] = namelength;
//
//        return result;
//    }
//
//    public static String[] bodyPacketConversion(int messagelength,int namelength, byte[] bodybytes){
//        String message = new String(bodybytes,12,messagelength); //12부터 바디의 시작.
//        String name = new String(bodybytes,messagelength,namelength);
//        String[] bodypacket = new String[2];
//        bodypacket[0] = message;
//        bodypacket[1] = name;  ,
//        return bodypacket;
//    }
//
//
//
//    static int byteArrayToInt(byte[] byteArray,int startIdx, int endIdx){
//        int value = 0;
//        for (int i = startIdx; i <= endIdx; i++) {
//            value += ((int) byteArray[i] & 0xFF) << (8 * (i - startIdx));
//        }
//        return value;
//    }
//
//
//}
