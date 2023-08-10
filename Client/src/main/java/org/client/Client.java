package org.client;

import org.share.HeaderPacket;
import org.share.PacketType;
import org.share.clienttoserver.ClientConnectPacket;
import org.share.servertoclient.ServerExceptionPacket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static org.share.HeaderPacket.byteToBodyLength;
import static org.share.HeaderPacket.byteToPacket;
import static org.share.servertoclient.ServerExceptionPacket.byteToServerExceptionPacket;

public class Client {
    private static final int SERVER_PORT = 8888;
    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {

        Socket socket;
        ClientConnectPacket connectpacket;
        try {
            //socket 연결 후 클라이언트 인풋,아웃풋 생성
            socket = new Socket("localhost", SERVER_PORT);

            connectpacket = duplicateNameCheck(socket);

            ClientOutputThread clientinputThread = new ClientOutputThread(socket,connectpacket);
            ClientInputThread clientoutputThread = new ClientInputThread(socket);
            clientoutputThread.start();
            clientinputThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientConnectPacket duplicateNameCheck(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        Scanner scanner = new Scanner(System.in);
        ClientConnectPacket connectpacket = null;
        while(true){
            System.out.print("please enter your name :");
            String name = scanner.nextLine(); // 중복확인 기능 추가해야함.
            connectpacket = new ClientConnectPacket(name);

            byte[] headerbytedata = connectpacket.getHeaderBytes();
            byte[] bodybytedata = connectpacket.getBodyBytes();
            byte[] packetbytedata = new byte[headerbytedata.length + bodybytedata.length];

            System.arraycopy(headerbytedata, 0, packetbytedata, 0, headerbytedata.length);
            System.arraycopy(bodybytedata, 0, packetbytedata, headerbytedata.length, bodybytedata.length);
            out.write(packetbytedata);

            byte[] serverbytedata = new byte[1024];
            int serverbytelength = in.read(serverbytedata);
            PacketType serverpackettype = byteToPacket(serverbytedata);//서버 헤더부분 타입추출
            if(serverpackettype == PacketType.SERVER_EXCEPTION){
                ServerExceptionPacket serverExceptionPacket = byteToServerExceptionPacket(serverbytedata);
                System.out.println("[SERVER] " + serverExceptionPacket.getMessage());
            }else{
               break;
            }
        }
        return connectpacket;
    }

}

