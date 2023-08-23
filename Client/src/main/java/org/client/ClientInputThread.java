package org.client;

import org.share.HeaderPacket;
import org.share.PacketType;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

import org.share.servertoclient.*;

import static org.share.HeaderPacket.*;
import static org.share.servertoclient.ServerDisconnectPacket.*;
import static org.share.servertoclient.ServerExceptionPacket.*;
import static org.share.servertoclient.ServerFilePacket.*;
import static org.share.servertoclient.ServerMessagePacket.*;
import static org.share.servertoclient.ServerNameChangePacket.*;
import static org.share.servertoclient.ServerNotifyPacket.*;

public class ClientInputThread extends Thread {
    static final int MAXBUFFERSIZE = 5000;
    Socket socket;
    InputStream in = null;
    ClientOutputThread clientOutputThread;
    HashMap<String, RandomAccessFile> fileMap = new HashMap<>();

    public ClientInputThread(Socket socket, ClientOutputThread clientOutputThread) {
        this.socket = socket;
        this.clientOutputThread = clientOutputThread;
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();

            while (true) {
                byte[] serverbytedata = new byte[MAXBUFFERSIZE];
                int serverbytelength = in.read(serverbytedata);
                PacketType serverpackettype = byteToPackettype(serverbytedata);//서버 헤더부분 타입추출
                int serverpacketlength = byteToBodyLength(serverbytedata); //서버 헤더부분 길이추출
                boolean disconnectcheck;
                if (serverbytelength >= 0) {
                    HeaderPacket packet = makeServerPacket(serverbytedata, serverpackettype);
                    disconnectcheck = packetCastingAndPrint(packet, serverpackettype);
                    if(!disconnectcheck){
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[IOException]");
        } finally {
            try {
                System.out.println("Disconnected from server.");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean packetCastingAndPrint(HeaderPacket packet, PacketType packetType) throws IOException {
        if (packetType == PacketType.SERVER_NOTIFY) {
            ServerNotifyPacket notifyPacket = (ServerNotifyPacket) packet;
            System.out.println("[SERVER] " + notifyPacket.getMessage()); //서버의 Notify 메세지 출력
            return true;
        } else if (packetType == PacketType.SERVER_EXCEPTION) {
            ServerExceptionPacket exceptionPacket = (ServerExceptionPacket) packet;
            System.out.println("[SERVER] " + exceptionPacket.getMessage()); //서버의 Exception 메세지 출력
            return true;
        } else if (packetType == PacketType.SERVER_MESSAGE) {
            ServerMessagePacket messagePacket = (ServerMessagePacket) packet;
            System.out.println("[" + messagePacket.getName() + "] : " + messagePacket.getMessage());//클라이언트가 보낸 메세지
            return true;
        } else if (packetType == PacketType.SERVER_DISCONNECT) {
            ServerDisconnectPacket disconnectPacket = (ServerDisconnectPacket) packet;
            if (disconnectPacket.getName().equals(clientOutputThread.getClientname())) { //본인이 나가기 된 경우.
                return false;
            }
            System.out.println("[SERVER] " + disconnectPacket.getName() + " left the server.");
        } else if(packetType == PacketType.SERVER_CHANGENAME){
            ServerNameChangePacket nameChangePacket = (ServerNameChangePacket) packet;
            System.out.println("[SERVER] " + nameChangePacket.getName() + "->" + nameChangePacket.getChangename());
            if(clientOutputThread.getClientname().equals(nameChangePacket.getName())){
                clientOutputThread.setClientname(nameChangePacket.getChangename());
            }
            return true;
        } else if(packetType == PacketType.SERVER_FILE){
            ServerFilePacket serverFilePacket = (ServerFilePacket) packet;
            saveFile(serverFilePacket);
            return true;
        }
        return true;
    }


    private HeaderPacket makeServerPacket(byte[] bytedata, PacketType servertype) {
        if (servertype == PacketType.SERVER_NOTIFY) {
            return byteToServerNotifyPacket(bytedata);
        } else if (servertype == PacketType.SERVER_EXCEPTION) {
            return byteToServerExceptionPacket(bytedata);
        } else if (servertype == PacketType.SERVER_MESSAGE) {
            return byteToServerMessagePacket(bytedata);
        } else if (servertype == PacketType.SERVER_DISCONNECT) {
            return byteToServerDisconnectPacket(bytedata);
        } else if(servertype == PacketType.SERVER_CHANGENAME){
            return byteToServerNameChangePacket(bytedata);
        } else if(servertype == PacketType.SERVER_FILE){
            return byteToServerFilePacket(bytedata);
        }
        else return null;
    }

    public void saveFile(ServerFilePacket packet) throws IOException {
        try{
            String filename = packet.getFileName();
            int chunknumber = packet.getChunkNumber();
            byte[] chunk = packet.getChunk();
            if(!fileMap.containsKey(filename)){
                File file = new File("/Users/hyunchuljung/Desktop/ClientFolder/" + filename);
                RandomAccessFile rfile = new RandomAccessFile(file,"rw");
                fileMap.put(filename,rfile);
            }
            RandomAccessFile rfile = fileMap.get(filename);
            rfile.seek(chunknumber * 4096L);
            rfile.write(chunk);
        }catch (IOException e){
            fileMap.remove(packet.getFileName());
            e.printStackTrace();
        }
        if(packet.getChunkNumber() == packet.getLastChunkNumber()){
            fileMap.remove(packet.getFileName());
            System.out.println("File Download Complete");
        } else if(packet.getChunkNumber() == 0){
            System.out.println("File Download Start");
        }
    }


}