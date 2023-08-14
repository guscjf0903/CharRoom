package org.server;

import org.share.HeaderPacket;
import org.share.servertoclient.*;

import java.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static java.lang.System.out;
import static org.server.ServerThread.clientMap;

public class SeverMessageHandler {
    public static void sendAllMessage(HeaderPacket packet,String sendName) throws IOException { //모두에게 전송하는 메세지 (lock 걸어야함)
        byte[] sendAllbyte = null;
        ServerMessagePacket serversendpacket = new ServerMessagePacket(packet.getMessage(), packet.getName());
        sendAllbyte = packetToByte(serversendpacket);
        try {
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) {
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                if (sendName == receiverName) {
                    continue;
                }
                try {
                    clientStream.write(sendAllbyte);
                    clientStream.flush();
                } catch (IOException e) {
                    // 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                    clientMap.remove(receiverName);
                    out.println("[" + receiverName + " Disconnected]");
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public static void sendWhisperMessage(HeaderPacket packet, String sendName) throws IOException {
        byte[] sendAllbyte = null;
        ServerMessagePacket serversendpacket = new ServerMessagePacket(packet.getMessage(), sendName);
        sendAllbyte = packetToByte(serversendpacket);
        try {
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) {
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                if (receiverName.equals(packet.getName())) {
                    try {
                        clientStream.write(sendAllbyte);
                        clientStream.flush();
                    } catch (IOException e) {// 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                        clientMap.remove(receiverName);
                        out.println("[" + receiverName + " Disconnected]");
                    }
                    return;
                }
            }
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) { //만약 전송되지 않았을때 예외처리
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                ServerExceptionPacket exceptionpacket = new ServerExceptionPacket("There is no user with that name.");
                byte[] exceptionpacketbyte = packetToByte(exceptionpacket);
                if(receiverName.equals(sendName)){
                    try {
                        clientStream.write(exceptionpacketbyte);
                        clientStream.flush();
                    } catch (IOException e) {// 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                        clientMap.remove(receiverName);
                        out.println("[" + receiverName + " Disconnected]");
                    }
                    return;
                }

            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(File file , String fileName,String name){
        byte[] sendAllbyte = null;
        ServerFilePacket serverFilePacket = new ServerFilePacket(fileName, file, name);
        sendAllbyte = packetToByte(serverFilePacket);
        try {
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) {
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                if (name.equals(receiverName)) {
                    continue;
                }
                try {
                    clientStream.write(sendAllbyte);
                    clientStream.flush();
                } catch (IOException e) {
                    // 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                    clientMap.remove(receiverName);
                    out.println("[" + receiverName + " Disconnected]");
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }


    public static void clientChangeName(HeaderPacket packet) throws IOException {
        for (Map.Entry<OutputStream,String> entry : clientMap.entrySet()) {
            String receiverName = entry.getValue();
            OutputStream clientStream = entry.getKey();
            if(packet.getName().equals(receiverName)){
                clientMap.put(clientStream,packet.getData());
                break;
            }
        }
    }


    public static void sendAllNotify(String message) throws IOException { //서버 공지 (lock 걸어야함)
        ServerNotifyPacket packet = new ServerNotifyPacket(message);
        byte[] sendNotifybyte = packetToByte(packet);

        try {
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) {
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                try {
                    clientStream.write(sendNotifybyte);
                    clientStream.flush();
                } catch (IOException e) {
                    // 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                    clientMap.remove(receiverName);
                    out.println("[" + receiverName + " Disconnected]");
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void disconnectClient(HeaderPacket packet) throws IOException {
        ServerDisconnectPacket disconnectpacket = new ServerDisconnectPacket(packet.getName());
        byte[] disconnectpacketbyte = packetToByte(disconnectpacket);
        try {
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) {
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                try {
                    clientStream.write(disconnectpacketbyte);
                    clientStream.flush();
                } catch (IOException e) {
                    // 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                    clientMap.remove(receiverName);
                    out.println("[" + receiverName + " Disconnected]");
                }
            }
            clientMap.remove(packet.getName());
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
        out.println("[" + packet.getName() + " Disconnected]"); //서버에 띄우는 메세지.
    }

    public static synchronized void duplicateName(OutputStream out) throws IOException {
        ServerExceptionPacket exceptionpacket = new ServerExceptionPacket("Duplicate name. Please enter another name");
        byte[] exceptionpacketbyte = packetToByte(exceptionpacket);
        out.write(exceptionpacketbyte);
        out.flush();
    }

    public static synchronized void ClientnameChange(OutputStream out,String name,String changename) throws IOException { //한명에게만 서버 공지전송
        ServerNameChangePacket serverNameChangePacket = new ServerNameChangePacket(name,changename);
        byte[] namechangePacketbyte = packetToByte(serverNameChangePacket);
        out.write(namechangePacketbyte);
        out.flush();
    }

    private static byte[] packetToByte(HeaderPacket sendpacket){
        byte[] headerbytedata = sendpacket.getHeaderBytes();
        byte[] bodybytedata = sendpacket.getBodyBytes();
        byte[] packetbytedata = new byte[headerbytedata.length + bodybytedata.length];

        System.arraycopy(headerbytedata, 0, packetbytedata, 0, headerbytedata.length);
        System.arraycopy(bodybytedata, 0, packetbytedata, headerbytedata.length, bodybytedata.length);
        return packetbytedata;
    }


}
