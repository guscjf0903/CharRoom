package org.server;

import org.share.HeaderPacket;
import org.share.servertoclient.ServerDisconnectPacket;
import org.share.servertoclient.ServerExceptionPacket;
import org.share.servertoclient.ServerMessagePacket;
import org.share.servertoclient.ServerNotifyPacket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static org.server.ServerThread.clientMap;

public class SeverMessageHandler {
    public static void sendAllMessage(HeaderPacket packet,String clientName) throws IOException { //모두에게 전송하는 메세지 (lock 걸어야함)
        byte[] sendAllbyte = null;
        ServerMessagePacket serversendpacket = new ServerMessagePacket(packet.getMessage(), packet.getName());
        sendAllbyte = packetToByte(serversendpacket);
        try {
            for (Map.Entry<OutputStream, String> entry : clientMap.entrySet()) {
                String receiverName = entry.getValue();
                OutputStream clientStream = entry.getKey();
                if (clientName == receiverName) {
                    continue;
                }
                try {
                    clientStream.write(sendAllbyte);
                    clientStream.flush();
                } catch (IOException e) {
                    // 클라이언트와의 연결이 끊어진 경우, 해당 클라이언트를 제거합니다.
                    clientMap.remove(receiverName);
                    System.out.println("[" + receiverName + " Disconnected]");
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public static void clientChangeName(HeaderPacket packet) throws IOException {
        if (Map.Entry<OutputStream,String> entry : clientMap.entrySet()) {
            clientMap
        }
//-------------------------------------------------//
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
                    System.out.println("[" + receiverName + " Disconnected]");
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
                    System.out.println("[" + receiverName + " Disconnected]");
                }
            }
            clientMap.remove(packet.getName());
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
        System.out.println("[" + packet.getName() + " Disconnected]"); //서버에 띄우는 메세지.
    }

    public static synchronized void duplicateName(OutputStream out) throws IOException {
        ServerExceptionPacket exceptionpacket = new ServerExceptionPacket("Duplicate name. Please enter another name");
        byte[] exceptionpacketbyte = packetToByte(exceptionpacket);
        out.write(exceptionpacketbyte);
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
