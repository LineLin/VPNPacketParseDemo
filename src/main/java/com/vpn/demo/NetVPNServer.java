package com.vpn.demo;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import com.com.vpn.codec.IPPacketParase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2015/10/14.
 */
public class NetVPNServer extends VpnService{

    private ParcelFileDescriptor mTunInterface;

    private InputStream mVPNInputStream;

    private OutputStream mVPNOutputStream;

    private IPPacketParase mIpParse;

    private int mTunSocketFd;

    private Thread mVpnReadThread;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Create");
        mVpnReadThread = new VPNReadThread();
        mIpParse = new IPPacketParase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if(mTunInterface != null){
            try{
                mTunInterface.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        mVpnReadThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        try{
            mTunInterface.close();
            mVpnReadThread.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mTunInterface.close();
            mVpnReadThread.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class VPNReadThread extends Thread{
        @Override
        public void run() {
            mTunInterface = new VpnService.Builder()
                    .setSession("VPNServer Demo")
                    .addAddress("10.0.0.8",0x20)
                    .addRoute("0.0.0.0",0)
                    .establish();
            mTunSocketFd = mTunInterface.getFd();
            mVPNInputStream = new FileInputStream(mTunInterface.getFileDescriptor());
            mVPNOutputStream = new FileOutputStream(mTunInterface.getFileDescriptor());
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buff = new byte[65535];
            while(true){
//                System.out.println("run!");
                int read = 0;
                boolean isEntire = false;
                try {
                    if((read = mVPNInputStream.read(buff)) > 0) {
                        out.write(buff,0,read);
                        buff = out.toByteArray();
                        out.reset();
                        System.out.println(mIpParse.doParse(buff));
                    }
                }catch(IOException e){

                }
            }
        }
    }
}
