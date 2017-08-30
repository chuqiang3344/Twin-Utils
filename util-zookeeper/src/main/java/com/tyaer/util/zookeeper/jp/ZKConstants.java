package com.tyaer.util.zookeeper.jp;

public class ZKConstants {
	//爬虫任务创建zookeeper父节点
	public final static String ZK_SPIDER_TASK_DIR = "/basedata/dataway/spider/task";
	//爬虫分裂种子抢注zookeeper父节点
	public final static String ZK_SPIDER_SPLITER_DIR = "/basedata/dataway/spider/spliter";
	//zookeeper分隔符
	public final static String ZK_SEPARTOR = "/";
	
	
	
	public final static String ZK_WORKEN_PATH = "zk.worken.path";
	public final static String ZK_TEMPLATE_PATH = "zk.templates.path";
	public final static String ZK_QUEEN_PATH = "zk.queen.path";
	public final static String ZK_QUEEN_PLUGIN_PATH = "zk.queen.plugins.path";
	public final static String ZK_TASK_PATH = "zk.task.path"; 
	public final static String ZK_LOCK_PATH = "zk.lock.path";
	
	/** mysql连接数分布式信号量路径 */
	public final static String ZK_MYSQL_CONNECTIONS_SEMAPHORE_PATH = "zk.db.mysql.connections.semaphore.path";
	/** mysql连接数分布式信号量数量的信号值 */
	public final static String ZK_MYSQL_CONNECTIONS_SEMAPHORE_MAX_NUM_PATH = "zk.db.mysql.connections.semaphore.num.path";
	/** mysql连接数分布式信号量最大值 */
	public final static String ZK_MYSQL_CONNECTIONS_SEMAPHORE_MAX_NUM = "zk.db.mysql.connections.semaphore.num";
	
	
	/** sql server连接数分布式信号量路径 */
	public final static String ZK_SQLSERVER_CONNECTIONS_SEMAPHORE_PATH = "zk.db.sqlserver.connections.semaphore.path";
	/** sql server连接数分布式信号量数量的信号值 */
	public final static String ZK_SQLSERVER_CONNECTIONS_SEMAPHORE_MAX_NUM_PATH = "zk.db.sqlserver.connections.semaphore.num.path";
	/** sql server连接数分布式信号量最大值 */
	public final static String ZK_SQLSERVER_CONNECTIONS_SEMAPHORE_MAX_NUM = "zk.db.sqlserver.connections.semaphore.num";
	
	
	/** 配置文件中zk的地址 */
	public static final String ZK_LOCATION = "zk.location"; 
}
