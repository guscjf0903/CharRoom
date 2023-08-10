//package org.share;
//
//import java.io.Serializable;
//
//public class PacketHeader implements Serializable {
//    private ClientPacketType clientPacketType;
//    private int messageLength;
//    private int nameLength;
//
//    public PacketHeader(ClientPacketType clientPacketType, int messageLength,int nameLength){
//        this.clientPacketType = clientPacketType;
//        this.messageLength = messageLength;
//        this.nameLength = nameLength;
//    }
//
//    public ClientPacketType getType() {
//        return clientPacketType;
//    }
//    public int getMessageLength() {
//        return messageLength;
//    }
//    public int getNameLength() {
//        return nameLength;
//    }
//}