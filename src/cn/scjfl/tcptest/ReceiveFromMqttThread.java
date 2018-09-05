package cn.scjfl.tcptest;

import java.util.concurrent.ConcurrentLinkedQueue;
import cn.scjfl.jsonbean.DeviceBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public class ReceiveFromMqttThread implements Runnable {

	public ReceiveFromMqttThread(TestMqttJavaMain mqttObj,DeviceBean bean, ConcurrentLinkedQueue<String> bq) {
		this.mqttObj=mqttObj;
		this.receivefromCloudbq=bq;
		this.bean=bean;
	}
	
	@Override
	public void run() {
		//System.out.println("Receive msg form Mqtt Thread");
			try {
				//mqttObj.mqttClient.subscribe(bean.getTopics2c(), new MqttReceiver());
				String message=null;
				while(!bean.stopflag) {
				
					if (!MqttReceiver.bq.isEmpty()) {
						message=MqttReceiver.bq.poll();
						JSONObject jsonObject=JSONObject.fromObject(message);
						String comid=(String)jsonObject.get("comid");
						String deviceid=(String)jsonObject.get("deviceid");
						String cmd=(String)jsonObject.get("cmd");
						Object device=jsonObject.get("deviceids");
						JSONArray jArray=JSONArray.fromObject(device);
						if(comid.equals(bean.getComid())&&cmd.equals("2001")&&jArray.contains(bean.getDeviceid())
								||comid.equals(bean.getComid())&&deviceid.equals(bean.getDeviceid())) {
							jsonObject.remove("metric");
							jsonObject.remove("value");
							jsonObject.remove("timestamp");
							jsonObject.remove("comid");
							receivefromCloudbq.offer(jsonObject.toString());
							bean.messageflag=true;
						}
					}
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e) {
				System.out.println("fail to stop the ReceiveFromMqtt thread");
			}
			finally {
				System.out.println("receivefrommqtt die");
				mqttObj.close();
				bean.stopflag=true;
			}
	}
	
	private ConcurrentLinkedQueue<String> receivefromCloudbq=null;
	private TestMqttJavaMain mqttObj=null;
	private DeviceBean bean;
}

