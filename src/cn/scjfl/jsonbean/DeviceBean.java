package cn.scjfl.jsonbean;

import net.sf.json.JSONObject;

public class DeviceBean {
    private String cmd;
    private String seq;
    private String deviceid;
    private String devicename;
    private String comid;
    private String topics2c;
    private String topicc2s;
    private String metric;
	public  boolean stopflag=false;
	public  boolean keepAlive=false;
	//public  boolean messageflag=false;
	public boolean firstconnection;
	
	public DeviceBean() {
		super();
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public String getComid() {
		return comid;
	}

	public void setComid(String comid) {
		this.comid = comid;
	}

	public String getTopics2c() {
		return topics2c;
	}

	public void setTopics2c(String topics2c) {
		this.topics2c = topics2c;
	}

	public String getTopicc2s() {
		return topicc2s;
	}

	public void setTopicc2s(String topicc2s) {
		this.topicc2s = topicc2s;
	}

	public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setSeq(String message) {
    	JSONObject jsonObject = JSONObject.fromObject(message);
    	seq=(String)jsonObject.get("seq");
	}

	@Override
	public String toString() {
		return "DeviceBean{" +
				"deviceid='" + deviceid + '\'' +
				", devicename='" + devicename + '\'' +
				", comid='" + comid + '\'' +
				", topics2c='" + topics2c + '\'' +
				", topicc2s='" + topicc2s + '\'' +
				", metric='" + metric + '\'' +
				'}';
	}

	public String heartbeats() {
        JSONObject jsonS=new JSONObject();
        jsonS.put("cmd","1001");
        jsonS.put("seq",seq);
        jsonS.put("deviceid",deviceid);
        return jsonS.toString();
	}
	
	public 	String login() {
        JSONObject jsonS=new JSONObject();
        jsonS.put("cmd","1000");
        jsonS.put("seq",seq);
        jsonS.put("deviceid",deviceid);
        jsonS.put("secretkey", "abcdefg");
        jsonS.put("validate", "1200");
        return jsonS.toString();
	}
	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}
	
}
