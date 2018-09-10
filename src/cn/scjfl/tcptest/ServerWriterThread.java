package cn.scjfl.tcptest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import cn.scjfl.jsonbean.DeviceBean;
import cn.scjfl.log.Log;




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
            	System.out.println("Server Writer fail to sleep");
            	Log.log.error("Server Writer fail to sleep :"+e);
            }
            //		catch (IOException e) {
            //			e.printStackTrace();
            //		}
        catch (IOException e) {
        	System.out.println("Server Writer IOException");
        	Log.log.error("Server Writer IOException :"+e);
        } finally {
        	bean.stopflag=true;
        	Service.isBreak=true;
            System.out.println("service writer die");
            Log.log.error("Server Writer die");
                try {
                    if (socket != null)
                        socket.close();
                    if (os != null)
                        os.close();
//                    if (bufferkeyboard != null)
//                        bufferkeyboard.close();
                } catch (IOException ex) {
                    System.out.println("Failed to release resource");
                	Log.log.error("Server Writer fail release resource :"+ex);
                }
            }
    }

    private Socket socket = null;
    private OutputStream os = null;
    //private BufferedReader bufferkeyboard = null;
    private DeviceBean bean=null;
    private ConcurrentLinkedQueue<String> receivefromCloudbq = null;

}
