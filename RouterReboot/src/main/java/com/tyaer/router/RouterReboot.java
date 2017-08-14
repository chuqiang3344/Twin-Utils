package com.tyaer.router;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * 
 * 功能描述：路由器重启
 * @ClassName: RouterReboot 	
 * @author duanbj
 * @date 2016-4-23 上午10:17:47  
 * @Company 中泓在线软件有限公司
 */
public class RouterReboot {
	private static final Logger logger = Logger.getLogger(RouterReboot.class);

	public static void reboot(){
		String ip = IpGetter.getLocalIP();
		reboot(ip);
	}


	public static void reboot(String ip){
		logger.info("IP:"+ip);
		if(StringUtils.isNotBlank(ip)){
//			ip = getRouterIp(ip);
			String url = "http://"+ip+":9090/userRpm/SysRebootRpm.htm?Reboot=%D6%D8%C6%F4%C2%B7%D3%C9%C6%F7";
			logger.info(url);
			String host = ip + ":9090";
			String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36";
			String pwd="Basic%20YWRtaW46Y24taG9uXzE0MDA%3D";
			String cookie = "Authorization="+pwd;
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			// 方法头
			httpGet.addHeader("User-Agent", userAgent);
			httpGet.addHeader("Cookie",cookie);

			httpGet.addHeader("Host",host);
			httpGet.addHeader("Connection","keep-alive");
			httpGet.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpGet.addHeader("Upgrade-Insecure-Requests","1");
			httpGet.addHeader("Referer","http://"+host+"/userRpm/SysRebootRpm.htm");
			httpGet.addHeader("Accept-Encoding","gzip, deflate, sdch");
			httpGet.addHeader("Accept-Language","zh-CN,zh;q=0.8");
			try {
				HttpResponse response = client.execute(httpGet);
				System.out.println(response.getStatusLine().getStatusCode());
				String string = EntityUtils.toString(response.getEntity(),"gb2312");
				logger.info(string);
				String cookie2 = "Authorization=Basic%20YWRtaW46emhvbmdAaG9uZysx";
				httpGet.addHeader("Cookie",cookie2);
				HttpResponse response2 = client.execute(httpGet);
				System.out.println(response2.getStatusLine().getStatusCode());
				String string2 = EntityUtils.toString(response2.getEntity(),"gb2312");
				logger.info(string2);
				logger.info("重启路由器！");
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				httpGet.releaseConnection();
			}
		}
	}

	public static String getRouterIp(String ip){
		int indexOf = ip.lastIndexOf(".");
		ip = ip.substring(0, indexOf);
		return ip + ".1";
	}

	public static void main(String[] args) {
//		System.out.println(getRouterIp("192.168.2.101"));
//		reboot();
		reboot("183.15.250.249");
	}
}
