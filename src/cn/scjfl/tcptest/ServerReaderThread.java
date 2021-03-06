package cn.scjfl.tcptest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import cn.scjfl.jsonbean.DeviceBean;
import cn.scjfl.log.Log;
import net.sf.json.JSONObject;




class ServerReaderThread implements Runnable {

    public ServerReaderThread(Socket socket, DeviceBean bean, ConcurrentLinkedQueue<String> bq) {
        this.socket = socket;
        this.sendtoCloudbq = bq;
        this.bean = bean;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            int num;
            char ch;
            int count = 0;
            String message = "";
            while (!bean.stopflag) {
                while ((num = isr.read()) != -1) {
                    ch = (char) num;
                    message += ch;
                    if (ch == '{') {
                        count++;
                    } else if (ch == '}') {
                        count--;
                        if (count == 0) {
                            //System.out.println(message);
                            //sendtoCloudbq.put(message);
                            //String containstr = "\"cmd\" : \"1001\"";
                        	if (message.contains("\"cmd\" : \"1000\"")){
                        		bean.firstconnection=true;
                        	}
                        	else if (message.contains("\"cmd\" : \"1001\"")) {
//                                System.out.println("xintiao jinru ");
                            	bean.keepAlive = true;
                                message.replace(" ", "");
                                bean.setSeq(message);
                            } 
                        	else if(!message.contains("\"cmd\" : \"1000\"")){
                                //分解传上来的字符串，添加三个新的头之后，放进队列
                                JSONObject jsonObject = JSONObject.fromObject(message);
                                JSONObject jsObj = new JSONObject();

                                jsObj.put("metric", bean.getMetric());
                                jsObj.put("value", jsonObject.get("cmd"));
                                jsObj.put("timestamp", System.currentTimeMillis());
//                                System.out.println(jsonObject.toString() + "  " + jsObj.toString());
                                @SuppressWarnings("unchecked")
                                Iterator<String> it = jsonObject.keys();

                                while (it.hasNext()) {
//                                    System.out.println(" a:  "+it.next().toString()+"  b:  "+jsonObject.get(it.next()));
                                    String flagIt=it.next();
//                                    System.out.println( " zheshi "+ flagIt);
//                                    System.out.println("   get flageit value "+jsonObject.get(flagIt));
                                    jsObj.put(flagIt, jsonObject.get(flagIt));
                                }
                                
                                System.out.println("put in to sendtobq: "+jsObj.toString());
                                sendtoCloudbq.offer(jsObj.toString());
                            }
                            message = "";
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } 
        catch (InterruptedException e) {
        	System.out.println("Server Reader fail to sleep");
        	Log.log.error("Server Reader fail to sleep :"+e);
        }
        catch (Exception e) {
        	System.out.println("Server Reader exception");
        	Log.log.error("Server Reader exception :"+e);
        } 
        finally {
        	bean.stopflag=true;
        	Service.isBreak=true;
            System.out.println("service reader die");
        	Log.log.error("service reader die");
            try {
                if (socket != null)
                    socket.close();
            } 
            catch (IOException ex) {
                System.out.println("Failed to release resource");
                Log.log.error("service reader die");
            }
        }
    }

    private Socket socket = null;
    private DeviceBean bean = null;
    private ConcurrentLinkedQueue<String> sendtoCloudbq = null;
}