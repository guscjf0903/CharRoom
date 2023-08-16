package org.client;

import lombok.Getter;
import lombok.Setter;
import org.share.*;
import org.share.clienttoserver.*;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
@Getter
@Setter
public class ClientOutputThread extends Thread {
    Socket socket;
    OutputStream out = null;
    Scanner scanner = new Scanner(System.in);
    String clientname;
    public ClientOutputThread(Socket socket,String clientname) {
        this.socket = socket;
        this.clientname = clientname;
    }


    @Override
    public void run() {
        try {
            out = socket.getOutputStream();
            startChat();
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

    public void startChat(){
        try{
            while(true){
                String message;
                message = scanner.nextLine();
                if(message == null){
                    continue;
                }
                if(message.startsWith("/")){
                    ClientCommand(message);
                }else{
                    ClientMessagePacket clientMessagePacket = new ClientMessagePacket(message, clientname);
                    sendPacketToByte(clientMessagePacket);
                }
                if(message.equals("/quit")){
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public synchronized void sendFilePacketToByte(File file){
        byte[] headerbytedata = clientFilePacket.getHeaderBytes();
        try{
            //헤더 전송
            out.write(headerbytedata);
            out.flush();

            InputStream fileInputStream = new FileInputStream(clientFilePacket.getFile());
            byte[] chunk = new byte[1024];
            int byteRead;
            while((byteRead = fileInputStream.read(chunk)) != -1){
                out.write(chunk,0,byteRead);
                out.flush();
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void ClientCommand(String message) throws IOException {
        if ("/quit".equals(message)) {
            ClientDisconnectPacket clientDisconnectPacket = new ClientDisconnectPacket(clientname);
            sendPacketToByte(clientDisconnectPacket);
        } else if("/namechange".equals(message)){
            System.out.print("Please enter a name to change :");
            String changename = scanner.nextLine();
            ClientChangeNamePacket clientChangeNamePacket = new ClientChangeNamePacket(clientname,changename);
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
                sendFilePacketToByte(file);
            } else { //파일이 없을때 예외처리.
                System.out.println("File does not exist. Please provide a valid file path.");
            }
        } else{
            System.out.println("Invalid command");
        }
    }
}