package com.tyaer.router;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import sun.nio.ch.IOUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Twin on 2017/7/27.
 */
public class AutomaticDialing {
    private static final Logger logger = Logger.getLogger(AutomaticDialing.class);

    public static void main(String[] args) {

    }

    /**
     * 方法功能描述:结束采集进程
     *
     * @param
     * @return void
     * @author duanbj
     * @date 2015-10-16 上午10:26:34
     */
    public static void againDial() {
        String systemName = System.getProperties().getProperty("os.name");
        if (!systemName.contains("Windows")) {
            logger.warn("结束旧的采集线程：");
            String[] cmd = {"/bin/sh", "-c", "ps -ef | grep java"};
            try {
                Runtime runtime = Runtime.getRuntime();
                Process p = runtime.exec(cmd);// 调用系统命令
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String string = IOUtils.toString(reader);
                System.out.println(string);
                p.destroy();
                runtime.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {

            }
        }
    }

    private static ArrayList<String> CLOSELIST = new ArrayList<>();
    /**
     * 方法功能描述:结束采集进程
     *
     * @param
     * @return void
     * @author duanbj
     * @date 2015-10-16 上午10:26:34
     */
    public static void killProcess() {
        String systemName = System.getProperties().getProperty("os.name");
        if (!systemName.contains("Windows")) {
            logger.warn("结束旧的采集线程：" + CLOSELIST);
            String[] cmd = {"/bin/sh", "-c", "ps -ef | grep java"};
            try {
                Runtime runtime = Runtime.getRuntime();
                Process p = runtime.exec(cmd);// 调用系统命令
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while (null != (line = reader.readLine())) {
                    //从第二行开始
                    //pi        3488  3357  0 10:25 pts/1    00:00:00 /bin/sh ./Sina_Start.sh
                    String[] args = line.split("\\s{1,}");
                    int pid = Integer.parseInt(args[1]);//进程ID
                    String name = args[args.length - 1];
                    if (CLOSELIST.contains(name)) {
                        String kill = "sudo kill -9 " + pid;
                        runtime.exec(kill);
                        logger.warn("杀死进程" + name);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
