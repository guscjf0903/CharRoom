package org.client;

import org.share.*;
import org.share.clienttoserver.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientOutputThread extends Thread {
    Socket socket;
    OutputStream out = null;
    Scanner scanner = new Scanner(System.in);
    static String name;
    ClientConnectPacket connectPacket;
    public ClientOutputThread(Socket socket,ClientConnectPacket connectPacket) {
        this.socket = socket;
        this.connectPacket = connectPacket;
    }

    @Override
    public void run() {
        try {
            //connectClient();
            name = connectPacket.getName();
            out = socket.getOutputStream();
            while (true) { //채팅방 시작.
                String message;
                message = scanner.nextLine();
                if (message != null) {
                    if(message.charAt(0) == '/'){
                        ClientCommand(message);
                    }else{
                        ClientMessagePacket clientMessagePacket = new ClientMessagePacket(message, name);
                        sendPacketToByte(clientMessagePacket);
                    }
                }
                if(message == "/quit"){
                    break;
                }
            }
          } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendPacketToByte(HeaderPacket sendPacket) throws IOException { //원하는 패킷을 주면 바이트로 변환 후 서버에 전송
        byte[] headerbytedata = sendPacket.getHeaderBytes();
        byte[] bodybytedata = sendPacket.getBodyBytes();
        byte[] packetbytedata = new byte[headerbytedata.length + bodybytedata.length];

        System.arraycopy(headerbytedata, 0, packetbytedata, 0, headerbytedata.length);
        System.arraycopy(bodybytedata, 0, packetbytedata, headerbytedata.length, bodybytedata.length);
        out.write(packetbytedata);
        out.flush();
    }

    public synchronized void sendFilePacketToByte(ClientFilePacket clientFilePacket){
        byte[] headerbytedata = clientFilePacket.getHeaderBytes();
        byte[] bodybytedata = clientFilePacket.getBodyBytes();


    }


    public void ClientCommand(String message) throws IOException {
        if ("/quit".equals(message)) {
            ClientDisconnectPacket clientDisconnectPacket = new ClientDisconnectPacket(name);
            sendPacketToByte(clientDisconnectPacket);
        } else if("/namechange".equals(message)){
            System.out.print("Please enter a name to change :");
            String changename = scanner.nextLine();
            ClientChangeNamePacket clientChangeNamePacket = new ClientChangeNamePacket(name,changename);
            sendPacketToByte(clientChangeNamePacket);
        } else if("/w".equals(message)){
            System.out.print("Please enter a name to whisper :");
            String whispername = scanner.nextLine();
            System.out.print("Please enter a message to whisper :");
            String whispermessage = scanner.nextLine();
            ClientWhisperPacket clientWhisperPacket = new ClientWhisperPacket(whispermessage, whispername);
            sendPacketToByte(clientWhisperPacket);
        }else if("/f".equals(message)) {
            System.out.print("Please enter a file name to send : ");
            String filepath = scanner.nextLine();
            File file = new File(filepath);
            if (file.exists()) { // 파일이 존재하는지 확인
                ClientFilePacket clientFilePacket = new ClientFilePacket(name, file);
                sendPacketToByte(clientFilePacket);
            } else { //파일이 없을때 예외처리.
                System.out.println("File does not exist. Please provide a valid file path.");
            }
        } else{
            System.out.println("Invalid command");
        }
    }
}