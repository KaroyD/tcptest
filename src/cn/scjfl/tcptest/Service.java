package cn.scjfl.tcptest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import cn.scjfl.jsonbean.DeviceBean;
import cn.scjfl.log.Log;


public class Service {
	
	private DeviceBean deviceinit() {
		DeviceBean bean=new DeviceBean();
		try {
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document document=builder.parse(new File("config.xml"));
			Element rootElement=document.getDocumentElement();
			NodeList list;
			Element element;
			
			//deviceid
			list= rootElement.getElementsByTagName("deviceid");
			element = (Element) list.item(0);
			bean.setDeviceid(element.getChildNodes().item(0).getNodeValue());
			
			//devicename
			list = rootElement.getElementsByTagName("devicename");
			element = (Element) list.item(0);
			bean.setDevicename(element.getChildNodes().item(0).getNodeValue());
			
			//comid
			list = rootElement.getElementsByTagName("comid");
			element = (Element) list.item(0);
			bean.setComid(element.getChildNodes().item(0).getNodeValue());
			
			//topics2c
			list = rootElement.getElementsByTagName("topics2c");
			element = (Element) list.item(0);
			bean.setTopics2c(element.getChildNodes().item(0).getNodeValue());
			
			//topics2c
			list = rootElement.getElementsByTagName("topicc2s");
			element = (Element) list.item(0);
			bean.setTopicc2s(element.getChildNodes().item(0).getNodeValue());
			
			//metric
			list = rootElement.getElementsByTagName("metric");
			element = (Element) list.item(0);
			bean.setMetric(element.getChildNodes().item(0).getNodeValue());
			
			System.out.println("load config.xml");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(bean.toString());
		return bean;
	}
	
	public void init()
	{
		try(ServerSocket ss=new ServerSocket(SERVER_PORT))
		{
			System.out.println("Waiting for connectiong...");
			boolean hasNet=isReachable("119.75.217.109");
			while(true) {
				if(hasNet&&isBreak){
					Socket socket = ss.accept();	
					DeviceBean bean=deviceinit();
					TestMqttJavaMain mqttObj=new TestMqttJavaMain(bean.getTopics2c(),bean);
					mqttObj.init();
					System.out.println("Connection request from: " + socket.getInetAddress());
					System.out.println("New thread start up");
					OutputStream os = socket.getOutputStream();
					ConcurrentLinkedQueue<String> receivefromCloudbq = new ConcurrentLinkedQueue <>();
					ConcurrentLinkedQueue<String> sendtoCloudbq = new ConcurrentLinkedQueue<>();
					new Thread(new ServerReaderThread(socket, bean, sendtoCloudbq)).start();
					new Thread(new SendtoMqttThread(mqttObj, bean, sendtoCloudbq)).start();
					new Thread(new ReceiveFromMqttThread(mqttObj, bean, receivefromCloudbq)).start();
					new Thread(new ServerWriterThread(socket, os, bean, receivefromCloudbq)).start();
					isBreak=false;
				}
				
				
				/*
				 * 1.检测网络
				 * 2.如果有网，且已经与终端连接，线程休眠一段时间，然后回到1
				 * 3.如果有网，但与终端断开，此时的四个线程都是死了，且bean的stopflag为true，需要进入上面的if里面，接着回到1
				 * 4.如果没网，但仍然与终端连接，先设定stopflag为true，让4个线程死亡，但不执行上面的if，线程休眠，然后到1
				 * 5.如果没网，且没有与终端连接，线程休眠，然后到1
				 */
				hasNet=isReachable("119.75.217.109");
				System.out.println("hasNet: "+hasNet);
				System.out.println("isBreak: "+isBreak);
				Thread.sleep(10000);
				
				
			}
		}
		catch (IOException e) 
		{
			System.out.println("Server initialization failed.Please check the PORT");
			Log.log.error("Server initialization failed.Please check the PORT");
		} catch (InterruptedException e) {
			System.out.println("main thread fail to sleep");
			Log.log.error("main thread fail to sleep :"+e);
		}
		finally {
			
		}
	}
	
    private  boolean isReachable(String remoteInetAddr) {
        boolean reachable = false; 
        try {   
            InetAddress address = InetAddress.getByName(remoteInetAddr); 
            reachable = address.isReachable(5000);  
            } catch (Exception e) {  
            e.printStackTrace();  
            }  
        return reachable;
    }
	
	public static void main(String []args) {
		Service server=new Service();
		server.init();
	}
	
	private final static int SERVER_PORT=12345; 
	public static boolean isBreak=true;

}

