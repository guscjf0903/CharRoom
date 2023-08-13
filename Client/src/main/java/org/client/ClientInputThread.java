package org.client;

import org.share.HeaderPacket;
import org.share.PacketType;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static org.share.HeaderPacket.*;
import static org.share.servertoclient.ServerDisconnectPacket.*;
import static org.share.servertoclient.ServerExceptionPacket.*;
import static org.share.servertoclient.ServerFilePacket.*;
import static org.share.servertoclient.ServerMessagePacket.*;
import static org.share.servertoclient.ServerNameChangePacket.*;
import static org.share.servertoclient.ServerNotifyPacket.*;

public class ClientInputThread extends Thread {
    static final int MAXBUFFERSIZE = 1024;
    Socket socket;
    InputStream in = null;

    public ClientInputThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();

            while (true) {
                byte[] serverbytedata = new byte[MAXBUFFERSIZE];
                int serverbytelength = in.read(serverbytedata);
                PacketType serverpackettype = byteToPacket(serverbytedata);//서버 헤더부분 타입추출
                int serverpacketlength = byteToBodyLength(serverbytedata); //서버 헤더부분 길이추출
                HeaderPacket packet;
                if (serverbytelength >= 0) {
                    packet = makeServerPacket(serverbytedata, serverpackettype);
                    if (packet.getPacketType() == PacketType.SERVER_NOTIFY) {
                        System.out.println("[SERVER] " + packet.getMessage()); //서버의 Notify 메세지 출력
                    } else if (packet.getPacketType() == PacketType.SERVER_EXCEPTION) {
                        System.out.println("[SERVER] " + packet.getMessage()); //서버의 Exception 메세지 출력
                    } else if (packet.getPacketType() == PacketType.SERVER_MESSAGE) {
                        System.out.println("[" + packet.getName() + "] : " + packet.getMessage());//클라이언트가 보낸 메세지
                    } else if (packet.getPacketType() == PacketType.SERVER_DISCONNECT) {
                        if (packet.getName().equals(ClientOutputThread.name)) { //본인이 나가기 된 경우.
                            break;
                        }
                        System.out.println("[SERVER] " + packet.getName() + " left the server.");
                    } else if(packet.getPacketType() == PacketType.SERVER_CHANGENAME){
                        System.out.println("[SERVER] " + packet.getName() + "->" + packet.getData());
                        ClientOutputThread.name = packet.getData();
                    } else if(packet.getPacketType() == PacketType.SERVER_FILE){
                        System.out.println("[SERVER] " + packet.getName() + " send file : " + packet.getFile());
                    }
                }
            }
        } catch (IOException e) {
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
        } else return null;
    }


}