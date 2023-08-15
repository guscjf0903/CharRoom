package org.client;

import org.share.HeaderPacket;
import org.share.PacketType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import org.client.ClientOutputThread;
import org.share.servertoclient.*;

import static org.share.HeaderPacket.*;
import static org.share.servertoclient.ServerDisconnectPacket.*;
import static org.share.servertoclient.ServerExceptionPacket.*;
import static org.share.servertoclient.ServerMessagePacket.*;
import static org.share.servertoclient.ServerNameChangePacket.*;
import static org.share.servertoclient.ServerNotifyPacket.*;

public class ClientInputThread extends Thread {
    static final int MAXBUFFERSIZE = 2048;
    Socket socket;
    InputStream in = null;
    ClientOutputThread clientOutputThread;

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
                boolean disconnectcheck = true;
                if (serverbytelength >= 0) {
                    HeaderPacket packet = makeServerPacket(serverbytedata, serverpackettype);
                    disconnectcheck = packetCastingAndPrint(packet, serverpackettype);

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

    public boolean packetCastingAndPrint(HeaderPacket packet, PacketType packetType){
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
            if (disconnectPacket.getName().equals(clientOutputThread.getName())) { //본인이 나가기 된 경우.
                return false;
            }
            System.out.println("[SERVER] " + disconnectPacket.getName() + " left the server.");
        } else if(packetType == PacketType.SERVER_CHANGENAME){
            ServerNameChangePacket nameChangePacket = (ServerNameChangePacket) packet;
            System.out.println("[SERVER] " + nameChangePacket.getName() + "->" + nameChangePacket.getChangename());
            clientOutputThread.setName(nameChangePacket.getChangename());
            return true;
        }
        //파일은 미구현
        else if(packetType == PacketType.SERVER_FILE){
            //saveFile(serverbytedata);
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
        }
        else return null;
    }

    public void saveFile(byte[] bodyBytes) throws IOException {
        int nameLength = byteArrayToInt(bodyBytes, 8, 11);
        String name = new String(bodyBytes, 12, nameLength); //인덱스 12부터 nameLength만큼 문자열로 변환
        int filenamelength = byteArrayToInt(bodyBytes, 12 + nameLength, 15 + nameLength);
        String filename = new String(bodyBytes, 16 + nameLength, filenamelength);
        int filelength = byteArrayToInt(bodyBytes, 16 + nameLength + filenamelength, 19 + nameLength + filenamelength);


        File receivedFile = new File("/Users/hyunchuljung/Desktop/ClientFolder/" + filename);
        FileOutputStream fos = new FileOutputStream(receivedFile);
        byte[] fileData = Arrays.copyOfRange(bodyBytes, 20 + nameLength + filenamelength, 20 + nameLength + filenamelength + filelength);

        System.out.println("[SERVER] " + name + " send file : " + filename);

        fos.write(fileData);
        fos.close();
    }


}