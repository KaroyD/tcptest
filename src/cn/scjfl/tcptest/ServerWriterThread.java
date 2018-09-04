package cn.scjfl.tcptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import cn.scjfl.jsonbean.DeviceBean;




class ServerWriterThread implements Runnable {

    public ServerWriterThread(Socket socket, OutputStream os, DeviceBean bean, ConcurrentLinkedQueue<String> bq) {
        this.socket = socket;
        this.os = os;
        this.receivefromCloudbq = bq;
        this.bean=bean;
    }

    @Override
    public void run() {
        String content = null;
        try {
                while (!bean.stopflag) {
//                    System.out.println("xintiao flase and trun :" +Service.keepAlive);
                    if (bean.keepAlive == true) {
                        content = bean.heartbeats();
//                        System.out.println("heartbeat");
                        os.write(content.getBytes());
                        os.flush();
                        bean.keepAlive = false;
                    }
                   // while ((content = receivefromCloudbq.take()) != null) {
                     if(bean.messageflag==true) {
                         content = receivefromCloudbq.poll();
                         System.out.println("send to terminal: " + content);
                         System.out.println("length+: " + content.length());
                         os.write(content.getBytes());
                         os.flush();
                         bean.messageflag=false;
                     }
                   // }
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //		catch (IOException e) {
            //			e.printStackTrace();
            //		}
        catch (IOException e) {
            e.printStackTrace();
        } finally {
        	bean.stopflag=true;
            System.out.println("service writer die");
                try {
                    if (socket != null)
                        socket.close();
                    if (os != null)
                        os.close();
                    if (bufferkeyboard != null)
                        bufferkeyboard.close();
                } catch (IOException ex) {
                    System.out.println("Failed to release resource");
                }
            }
    }

    private Socket socket = null;
    private OutputStream os = null;
    private BufferedReader bufferkeyboard = null;
    private DeviceBean bean=null;
    private ConcurrentLinkedQueue<String> receivefromCloudbq = null;

}
