package cn.scjfl.tcptest;

import java.util.concurrent.ConcurrentLinkedQueue;
import cn.scjfl.jsonbean.DeviceBean;
import cn.scjfl.log.Log;
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
						String deviceids=(String)jsonObject.get("deviceids");
						if(comid.equals(bean.getComid())&&(cmd.equals("2001")||cmd.equals("2002"))&&deviceids.contains(bean.getDeviceid())
								||comid.equals(bean.getComid())&&deviceid.equals(bean.getDeviceid())) {
							jsonObject.remove("metric");
							jsonObject.remove("value");
							jsonObject.remove("timestamp");
							jsonObject.remove("comid");
							if(cmd.equals("2002")) {
								JSONObject data=(JSONObject) jsonObject.get("data");
								String ids=data.getString("id");
								String []idarray=ids.split(",");
								for(int i=0;i<idarray.length;i++) {
									data.remove("id");
									jsonObject.remove("data");
									data.put("id", idarray[i]);
									jsonObject.put("data", data);
									receivefromCloudbq.offer(jsonObject.toString());
								}
							}
							else {
								receivefromCloudbq.offer(jsonObject.toString());
							}
							bean.messageflag=true;
						}
					}
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e) {
				System.out.println("fail to stop the ReceiveFromMqtt thread");
				Log.log.error("fail to stop the ReceiveFromMqtt thread: "+e);
			}
			finally {
				System.out.println("receivefrommqtt die");
				Log.log.error("receivefrommqtt die");
				mqttObj.close();
				bean.stopflag=true;
				Service.isBreak=true;
			}
	}
	
	private ConcurrentLinkedQueue<String> receivefromCloudbq=null;
	private TestMqttJavaMain mqttObj=null;
	private DeviceBean bean;
}

