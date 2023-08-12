package org.server;

import org.share.*;
import org.share.servertoclient.ServerDisconnectPacket;
import org.share.servertoclient.ServerExceptionPacket;
import org.share.servertoclient.ServerMessagePacket;
import org.share.servertoclient.ServerNotifyPacket;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static org.server.SeverMessageHandler.*;
import static org.share.HeaderPacket.*;
import static org.share.PacketType.*;
import static org.share.clienttoserver.ClientChangeNamePacket.*;
import static org.share.clienttoserver.ClientConnectPacket.*;
import static org.share.clienttoserver.ClientDisconnectPacket.*;
import static org.share.clienttoserver.ClientMessagePacket.*;
import static org.share.clienttoserver.ClientWhisperPacket.*;

public class ServerThread extends Thread {
    static final int MAXBUFFERSIZE = 1024;

    public static Map<OutputStream, String> clientMap = Collections.synchronizedMap(new HashMap<OutputStream, String>());
    public String clientName;

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public ServerThread(Socket socket) {
        this.socket = socket;
        try {
            out = socket.getOutputStream(); // 클라에게 보내는 메세지
            in = socket.getInputStream(); //클라에서 오는 메세지
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] clientbytedata = new byte[MAXBUFFERSIZE];
                int clientbytelength = in.read(clientbytedata);
                PacketType clientpackettype = byteToPacket(clientbytedata); //헤더부분 타입추출
                int clientpacketlength = byteToBodyLength(clientbytedata);// 헤더부분 길이추출
                HeaderPacket packet;

                if (clientbytelength >= 0) {
                    packet = makeClientPacket(clientbytedata, clientpackettype);
                    if (packet.getPacketType() == PacketType.CLIENT_CONNECT) {
                        connectClient(packet);
                    } else if (packet.getPacketType() == PacketType.CLIENT_MESSAGE) {
                        sendAllMessage(packet, clientName);
                    } else if (packet.getPacketType() == CLIENT_CHANGENAME) {
                        boolean containsValue = clientMap.containsValue(packet.getData());
                        if (containsValue) {
                            duplicateName(out);
                        }else{
                            clientChangeName(packet);
                            clientName = packet.getData();
                            ClientnameChange(out, packet.getName(),packet.getData());
                        }
                    } else if(packet.getPacketType() == CLIENT_WHISPERMESSAGE){
                        sendWhisperMessage(packet,clientName);
                    }else if (packet.getPacketType() == PacketType.CLIENT_DISCONNECT) {
                        disconnectClient(packet);
                        if (packet.getName().equals(clientName)) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[" + clientName + "Disconnected]");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private synchronized void connectClient(HeaderPacket packet) throws IOException { // 커넥트 요청 들어올시 동작
        if (clientMap.containsValue(packet.getName())) {
            duplicateName(out);
            return;
        }
        clientName = packet.getName();
        clientMap.put(out, clientName);
        sendAllNotify(clientName + " is Connected");
        System.out.println("[" + clientName + " Connected]"); //서버에 띄우는 메세지.
    }

    private HeaderPacket makeClientPacket(byte[] bytedata, PacketType clienttype) {
        if (clienttype == CLIENT_MESSAGE) {
            return byteToClientMessagePacket(bytedata);
        } else if (clienttype == CLIENT_CONNECT) {
            return byteToClientConnectPacket(bytedata);
        } else if (clienttype == CLIENT_DISCONNECT) {
            return byteToClientDisconnectPacket(bytedata);
        } else if (clienttype == CLIENT_CHANGENAME) {
            return byteToClientChangeNamePacket(bytedata);
        } else if(clienttype == CLIENT_WHISPERMESSAGE){
            return byteToClientWhisperPacket(bytedata);
        } else return null;
    }


}



