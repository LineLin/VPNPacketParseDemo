package com.com.vpn.codec;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2015/10/14.
 */
public class IPPacketParase {


    public static boolean isEntireIpPacket(byte[] data){
        //note: data length not enough for totalLen field
        if(data.length < 4)
            return false;

        if(getIpPacketTotalLength(data) > data.length)
            return false;
        return true;
    }

    public static int getIpPacketTotalLength(byte[] data){
        ByteBuffer bf = ByteBuffer.allocate(2);
        bf.put(data[2]);
        bf.put(data[3]);
        bf.flip();
        return bf.getShort();
    }

    public IPPacket doParse(byte[] data){
        IPPacket packet = new IPPacket();
        packet.mOriPacket = data;
        System.out.println(data.length);
        ByteBuffer bf = ByteBuffer.wrap(data);
        int headLen = (int)(bf.get() & 0x0F);
        packet.mHeadLen = headLen;
        bf.get();
        int dataLen = bf.getShort();
        packet.mBodyLen = dataLen;
        packet.mSeq = (int)(bf.getShort() | 0);
        byte flag1 = bf.get();
        byte flag2 = bf.get();
        packet.mMF = (flag1 & 0x20) != 0 ? 1 : 0;
        flag1 &= 0x1f;
        ByteBuffer offset = ByteBuffer.allocate(2);
        offset.put(flag1); offset.put(flag2); offset.flip();
        packet.mSegmentOffset = offset.getShort();

        bf.get();
        packet.mServiceType = (int)bf.get();
        bf.getShort();
        byte[] sIp = new byte[4];
        byte[] dIp = new byte[4];
        bf.get(sIp);
        bf.get(dIp);
        packet.mSrcIp = sIp[0] + "." + sIp[1] + "." + sIp[2] + "." + sIp[3];
        packet.mDestIp = dIp[0] + "." + dIp[1] + "." + dIp[2] + "." + dIp[3];

        headLen -= 5;
        while(headLen-- > 0){
            bf.getInt();
        }

        int remainSize = bf.capacity() - bf.position();
        byte[] body = new byte[remainSize];
        bf.get(body);
        packet.mBody = body;

        return packet;
    }

    static class IPPacket{

        public int mSeq;

        public int mHeadLen;

        public int mBodyLen;

        public String mSrcIp;

        public String mDestIp;

        public byte[] mBody;

        public byte[] mOriPacket;

        public int mServiceType;

        public int mMF;

        public int mSegmentOffset;

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder(128);
            sb.append("seq:" + mSeq).append("\n");
            sb.append("sIp:" + mSrcIp).append("\n");
            sb.append("dIp:" + mDestIp).append("\n");
            sb.append("type:" + ServiceType.valueOf(mServiceType).toString()).append("\n");
            sb.append("bodyLen:" + mBody.length).append("\n");
            return sb.toString();
        }

    }

    public enum  ServiceType{

        TCP(6),UDP(17);

        private int type;

        private ServiceType(int type){
            this.type = type;
        }

        public boolean equals(int type){
            return this.type == type;
        }

        public static ServiceType valueOf(int type){
            for(ServiceType st : ServiceType.values()){
                if(st.type == type)
                    return st;
            }
            return ServiceType.TCP;
        }
    }

}
