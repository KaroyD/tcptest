package cn.scjfl.tcptest;

import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class MqttReceiver implements IMqttMessageListener {
	static ConcurrentLinkedQueue<String> bq=new ConcurrentLinkedQueue<>();
	private String str;
	private boolean firstconnection=true;
    @Override
    public void messageArrived(String topic, MqttMessage message) {
    	str=message.toString();
    	if(str.contains("\"cmd\":\"1000\"")) {
    		bq.offer(str);
    		System.out.println("login successfully");
    		firstconnection=false;
    		return;
    	}
    	if(str.contains("{")&&str.contains("}")&&!firstconnection) {
	        bq.offer(str);
	        System.out.println("MqttReceiver: "+str);
    	}
    }

}

class TestMqttJavaMain {
	final String broker       = "tcp://facedemo1.mqtt.iot.gz.baidubce.com:1883";
    final String clientId     = "hxy_mqtt_java_" + UUID.randomUUID().toString();
    final String username     = "facedemo1/face_com0000_device0001";
    final String password     = "qCn43wuGTdNEjXCuief/6bUipMICOA7B3lmGnlJWQMg=";

        
    public MqttConnectOptions connOpts = null;
    public MqttClient mqttClient=null;
    private String subtopic;
    public TestMqttJavaMain(String subtopic) {
		this.subtopic=subtopic;
	}
    public void init() {
    	try {
            connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            connOpts.setCleanSession(true);
            //connOpts.setAutomaticReconnect(true);
            //connOpts.setConnectionTimeout(10);
            connOpts.setKeepAliveInterval(20);
            System.out.println("clean session?: "+connOpts.isCleanSession());
            System.out.println("Connecting to broker: " + broker);
            mqttClient = new MqttClient(broker, clientId);
            System.out.println("Connected. Client id is " + clientId);
            mqttClient.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
					SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
					long now=System.currentTimeMillis();
					System.out.println("new message arrived "+aDate.format(now));
				}		
				@Override
				public void deliveryComplete(IMqttDeliveryToken arg0) {
					SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
					long now=System.currentTimeMillis();
					System.out.println("message delivery complete "+aDate.format(now));
				}	
				@Override
				public void connectionLost(Throwable arg0) {
					SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
					long now=System.currentTimeMillis();
					System.out.println("exception: "+arg0);
					System.out.println("connection to the server is lost "+aDate.format(now));
					System.out.println("try to reconnect");
					reconnectIfNecessary();
				}
			});
            connect();
        	}
        	catch (Exception e) {
				System.out.println("fail to initialize mqtt");
			}
        } 
    
   public synchronized void reconnectIfNecessary() { 
	   if(mqttClient==null|| !mqttClient.isConnected()) {
		   System.out.println("reconnectIfNecessary");
		   connect();
	   }
   }
   
   private void connect() {
		try {
		   if(!mqttClient.isConnected()) {
	           mqttClient.connect(connOpts);
	           System.out.println("connected");
	           mqttClient.subscribe(subtopic, new MqttReceiver());
	       }else {
	    	   mqttClient.disconnect();
	    	   mqttClient.connect(connOpts);
	    	   System.out.println("reconnected");
	       }
	   }
	   catch (MqttException e) {
		   	System.out.println("mqtt exception");
		   	System.out.println("caouse: "+e.getCause());
		   	System.out.println("message: "+e.getMessage());
		   	System.out.println("code: "+e.getReasonCode());
		   	e.printStackTrace();
		}
	   catch (Exception e) {
	           e.printStackTrace();
	       }
}
   

    public void close() {
    	try {
			mqttClient.disconnect(5000);
			mqttClient.close();
		} catch (MqttException e) {
			System.out.println("fail to close mqttclient");
		}
    }
}
