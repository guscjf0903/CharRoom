package org.server;

import org.share.*;
import org.share.clienttoserver.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static org.server.SeverMessageHandler.*;
import static org.share.HeaderPacket.*;
import static org.share.PacketType.*;
import static org.share.clienttoserver.ClientChangeNamePacket.*;
import static org.share.clienttoserver.ClientConnectPacket.*;
import static org.share.clienttoserver.ClientDisconnectPacket.*;
import static org.share.clienttoserver.ClientFilePacket.*;
import static org.share.clienttoserver.ClientMessagePacket.*;
import static org.share.clienttoserver.ClientWhisperPacket.*;

public class ServerThread extends Thread {
    static final int MAXBUFFERSIZE = 5000;

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
                PacketType clientpackettype = byteToPackettype(clientbytedata); //헤더부분 타입추출
                int clientpacketlength = byteToBodyLength(clientbytedata);// 헤더부분 길이추출
                boolean disconnectcheck = true;
                if (clientbytelength >= 0) {
                    HeaderPacket packet = makeClientPacket(clientbytedata, clientpackettype);
                    disconnectcheck = packetCastingAndSend(packet, clientpackettype);
                }
                if(!disconnectcheck){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[" + clientName + "Disconnected]");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private synchronized void connectClient(ClientConnectPacket connectPacket) throws IOException { // 커넥트 요청 들어올시 동작
        if (clientMap.containsValue(connectPacket.getName())) {
            exceptionMessage(out, "Duplicate name. Please enter another name");
            return;
        }
        clientName = connectPacket.getName();
        clientMap.put(out, clientName);
        sendAllNotify(clientName + " is Connected");
        System.out.println("[" + clientName + " Connected]"); //서버에 띄우는 메세지.
    }

    private HeaderPacket makeClientPacket(byte[] bytedata, PacketType clienttype) throws IOException {
        if (clienttype == CLIENT_MESSAGE) {
            return byteToClientMessagePacket(bytedata);
        } else if (clienttype == CLIENT_CONNECT) {
            return byteToClientConnectPacket(bytedata);
        } else if (clienttype == CLIENT_DISCONNECT) {
            return byteToClientDisconnectPacket(bytedata);
        } else if (clienttype == CLIENT_CHANGENAME) {
            return byteToClientChangeNamePacket(bytedata);
        } else if (clienttype == CLIENT_WHISPERMESSAGE) {
            return byteToClientWhisperPacket(bytedata);
        } else if (clienttype == CLIENT_FILE) {
            return byteToClientFilePacket(bytedata);
        } else return null;
    }

    public synchronized boolean packetCastingAndSend(HeaderPacket packet, PacketType clientpackettype) throws IOException {
        if (packet != null) {
            if (clientpackettype == PacketType.CLIENT_CONNECT) {
                ClientConnectPacket connectPacket = (ClientConnectPacket) packet;
                connectClient(connectPacket);
            } else if (clientpackettype == PacketType.CLIENT_MESSAGE) {
                ClientMessagePacket messagePacket = (ClientMessagePacket) packet;
                sendAllMessage(messagePacket);
            } else if (clientpackettype == CLIENT_CHANGENAME) {
                ClientChangeNamePacket changeNamePacket = (ClientChangeNamePacket) packet;
                boolean containsValue = clientMap.containsValue(changeNamePacket.getChangename());
                if (containsValue) {
                    exceptionMessage(out, "Duplicate name. Please enter another name");
                } else {
                    clientChangeName(changeNamePacket);
                    clientName = changeNamePacket.getChangename();
                    exceptionMessage(out, "Your name has been changed to " + changeNamePacket.getChangename());
                }
            } else if (clientpackettype == CLIENT_WHISPERMESSAGE) {
                ClientWhisperPacket whisperPacket = (ClientWhisperPacket) packet;
                sendWhisperMessage(whisperPacket, clientName);
            }
            else if (clientpackettype == CLIENT_FILE) {
                ClientFilePacket filePacket = (ClientFilePacket) packet;
                System.out.println("packetCasting chunk :" + filePacket.getChunk().length);
                sendFile(filePacket, clientName);
                return true;
            }
            else if (clientpackettype == PacketType.CLIENT_DISCONNECT) {
                ClientDisconnectPacket disconnectPacket = (ClientDisconnectPacket) packet;
                disconnectClient(disconnectPacket);
                if (disconnectPacket.getName().equals(clientName)) {
                    clientMap.remove(out);
                    return false;
                }
            }
        }
        return true;
    }
}



