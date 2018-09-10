package cn.scjfl.tcptest;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import cn.scjfl.jsonbean.DeviceBean;
import cn.scjfl.log.Log;



public class SendtoMqttThread implements Runnable {

	public SendtoMqttThread(TestMqttJavaMain mqttObj, DeviceBean bean, ConcurrentLinkedQueue<String> bq) {
		this.mqttObj=mqttObj;
		this.sendtoCloudbq=bq;
		this.bean=bean;
	}
	@Override
	public void run() {
		//System.out.println("Send to Mqtt Thread");
		try {
			String msgfromDevice=null;
			while(!bean.stopflag) {
				if (!sendtoCloudbq.isEmpty()) {
					msgfromDevice=sendtoCloudbq.poll();
					System.out.println("send to mqtt: "+msgfromDevice);
					MqttMessage message = new MqttMessage(msgfromDevice.getBytes());
					message.setQos(1);
					mqttObj.mqttClient.publish(bean.getTopicc2s(), message);
				}
				Thread.sleep(1000);
			}
		} 
		catch (InterruptedException e) {
			System.out.println("fail to stop the SendtoMqtt thread");
			Log.log.error("fail to stop the SendtoMqtt thread :"+e);
		} 
		catch (MqttException e) {
			System.out.println("Mqtt exception in sendtomqtt");
			Log.log.error("Mqtt exception in sendtomqtt :"+e);
		}
		finally {
			bean.stopflag=true;
			Service.isBreak=true;
			System.out.println("sendtomqtt die");
			Log.log.error("sendtomqtt die");
		}
	}
	
	
	private ConcurrentLinkedQueue<String> sendtoCloudbq=null;
	private TestMqttJavaMain mqttObj=null;
	private DeviceBean bean;
}
