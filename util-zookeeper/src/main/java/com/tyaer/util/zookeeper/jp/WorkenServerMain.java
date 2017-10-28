//package com.tyaer.util.zookeeper.jp;
//
//import com.alibaba.fastjson.JSON;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.math.NumberUtils;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.recipes.locks.InterProcessMutex;
//import org.apache.zookeeper.CreateMode;
//import org.apache.zookeeper.data.Stat;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.management.*;
//import java.lang.management.ManagementFactory;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//
//public class WorkenServerMain {
//    /**
//     * 用于保存node节点的信息，包括IP、进程号等
//     */
//    public static final String WORKEN_NODE_KEY = "node-info";
//    private final static String workThreadNumKey = "webants.work.threads";
//    private static CountDownLatch latch = null;
//    private static Logger logger = LoggerFactory.getLogger(WorkenServerMain.class);
//    private InterProcessMutex lock;
//    private Map<String, String> configs;
//    /**
//     * 爬虫线程池中线程的数量，默认为30，可以在配置文件中更改
//     */
//    private int workThreadNum = 30;
//
//    public static void main(String[] args) {
//        if (null != args && args.length > 0) {
//            String cfgName = "";
//            cfgName = args[0];
//            // 初始化context
//            ApplicationContext context = ApplicationContext.getInstance();
//            // 初始化配置文件
//            if (StringUtils.isNotEmpty(cfgName)) {
//                context.readPropertyFromFile(cfgName);
//            }
//        }
//        WorkenServerMain workenServer = new WorkenServerMain();
//        workenServer.init();
//        int templateCount = workenServer.getTemplateCountFromZK();
//        latch = new CountDownLatch(templateCount);
//        workenServer.registerWorkenNode();
//        workenServer.initTemplateListener();
//        workenServer.initPluginListener();
//        workenServer.initLogListener();
//        /*while(true){
//			List<SiteTemplate> tempaltes = SiteTemplateHolder.getAllTemplates();
//			if(null!=tempaltes && tempaltes.size()>0){
//				break;
//			}
//			try {
//				Thread.sleep(5*1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}*/
//        // 使用CountDownLatch来替换while轮询
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            logger.error("error while await for CountDownLatch!", e);
//        }
//        workenServer.initSpider();
//    }
//
//    public static void notifySpiderWhenTemplateAdded() {
//        if (latch != null) {
//            logger.info("notify worken node!");
//            latch.countDown();
//        }
//    }
//
//    public void init() {
//		/*String connectionString = "localhost:2183,localhost:2182,localhost:2181";
//		zkClient = ZookeeperFactoryBean.createWithOptions(connectionString,
//				new ExponentialBackoffRetry(1000, 3), 1000, 1000);*/
//        initCfg();
//        initLock();
//    }
//
//    public Map<String, String> initCfg() {
//        // 有问题，获取到的是当前启动项目的classpath路径，如果打包成jar包之后由其他程序启动，则此处获取到的是其他程序的classpath路径
//        // String path =
//        // Thread.currentThread().getContextClassLoader().getResource("").getPath();
//		/*String path = this.getClass().getClassLoader().getResource("zk/zk-config.properties")
//				.getPath();*/
//        //String tempalteFilePath = path + File.separator + "zk-config.properties";
//		/*String tempalteFilePath = path;
//		configs = PropertiesUtil.parsePropertiesFromFile(tempalteFilePath);*/
//
//        // getResourceAsStream 可以读取classpath路径下的资源文件，可以直接读取 java -cp命令添加的文件
//        configs = PropertiesUtil.parsePropertiesFromInputStream(
//                this.getClass().getClassLoader().getResourceAsStream("zk/zk-config.properties"));
//        //把global信息添加到configs中
//        configs.putAll(ApplicationContext.getInstance().getGlobalProps());
//
//        // 设置线程的数量，如果global.properties中有配置的话
//        String threadNumTxt = configs.get(workThreadNumKey);
//        int configThreadNum = NumberUtils.toInt(threadNumTxt, -1);
//        if (configThreadNum > -1) {
//            workThreadNum = configThreadNum;
//        }
//
//        return configs;
//    }
//
//    public void initLock() {
//        String lockPath = this.configs.get(ZKConstants.ZK_LOCK_PATH);
//        CuratorFramework zkClient = SimpleZKClient.getZKClient();
//        lock = new InterProcessMutex(zkClient, lockPath);
//    }
//
//    public void registerWorkenNode() {
//        String workenNodePath = this.configs.get(ZKConstants.ZK_WORKEN_PATH);
//        if (StringUtils.isNotEmpty(workenNodePath)) {
//            // 将worken节点注册到ZK
//            NodeDefinition worken = new NodeDefinition();
//            worken.setName("worken-1");
//            worken.setDesc("worken-1-desc");
//            ApplicationContext.getInstance().addContextObject(WORKEN_NODE_KEY, worken);
//            try {
//                String workenNodeTruePath = workenNodePath + "/"
//                        + worken.getNodeId();
//                CuratorFramework zkClient = SimpleZKClient.getZKClient();
//                if (null != zkClient) {
//                    byte[] nodeData = JSON.toJSONString(worken).getBytes();
//                    zkClient.create()
//                            .creatingParentsIfNeeded()
//                            .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
//                            .forPath(workenNodeTruePath, nodeData);
//                    // 增加一个链接监听器，在于zk失去连接，并且在重新之后会重新注册webant节点信息
//                    WebantNodeRecreateWhenLostConnectionListener wnrwlcListener =
//                            new WebantNodeRecreateWhenLostConnectionListener(workenNodeTruePath, nodeData,
//                                    CreateMode.EPHEMERAL_SEQUENTIAL);
//                    zkClient.getConnectionStateListenable().addListener(wnrwlcListener);
//                    logger.info("register worken node success.");
//                } else {
//                    logger.warn("get zk client fail.");
//                }
//            } catch (Exception e) {
//                logger.warn("register worken node faild.", e);
//            }
//        }
//    }
//
//    /**
//     * 初始化worken的监听器，包括template路径监听器，
//     */
//    private void initTemplateListener() {
//        TemplateNodeListener nodeListener = new TemplateNodeListener();
//        if (null != latch) {
//            nodeListener.setNotify(true);
//        }
//        ZkPathCacheBuilder cacheBuilder = new ZkPathCacheBuilder();
//        String templateNodePath = this.configs.get(ZKConstants.ZK_TEMPLATE_PATH);
//        try {
//            CuratorFramework zkClient = SimpleZKClient.getZKClient();
//            if (null != zkClient) {
//                cacheBuilder.startBuild(zkClient, templateNodePath, true)
//                        .addListener(nodeListener).endBuild();
//            } else {
//                logger.warn("get zk client fail.");
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            // e.printStackTrace();
//            logger.warn("add template listener fail.", e);
//        }
//    }
//
//    /**
//     * 初始化worken的监听器，包括plugin路径监听器，
//     */
//    private void initPluginListener() {
//        PluginNodeListener nodeListener = new PluginNodeListener();
//        ZkPathCacheBuilder cacheBuilder = new ZkPathCacheBuilder();
//        String pluginNodePath = this.configs.get(ZKConstants.ZK_QUEEN_PLUGIN_PATH);
//        try {
//            CuratorFramework zkClient = SimpleZKClient.getZKClient();
//            if (null != zkClient) {
//                cacheBuilder.startBuild(zkClient, pluginNodePath, true)
//                        .addListener(nodeListener).endBuild();
//            } else {
//                logger.warn("get zk client fail.");
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            // e.printStackTrace();
//            logger.warn("add plugin listener fail.", e);
//        }
//    }
//
//    /**
//     * 初始化log的监听器
//     */
//    private void initLogListener() {
//        LogConfigNodeListener nodeListener = new LogConfigNodeListener();
//        NodeCacheBuilder cacheBuilder = new NodeCacheBuilder();
//        String pluginNodePath = this.configs.get(LogConfigNodeListener.ZK_LOG_PATH);
//        try {
//            CuratorFramework zkClient = SimpleZKClient.getZKClient();
//            if (null != zkClient) {
//				/*NodeCache cache = cacheBuilder.startBuild(zkClient, pluginNodePath)
//						.addListener(nodeListener).endBuild();
//				nodeListener.setCache(cache);*/
//                // NodeCacheListener重新封装
//                cacheBuilder.startBuild(zkClient, pluginNodePath)
//                        .addCustomListener(nodeListener).endBuild();
//            } else {
//                logger.warn("get zk client fail.");
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            // e.printStackTrace();
//            logger.warn("add log listener fail.", e);
//        }
//    }
//
//    private int getTemplateCountFromZK() {
//        int count = -1;
//        String templateNodeParentPath = this.configs.get(ZKConstants.ZK_TEMPLATE_PATH);
//        CuratorFramework zkClient = SimpleZKClient.getZKClient();
//        if (null != zkClient) {
//            try {
//                Stat stat = zkClient.checkExists().forPath(templateNodeParentPath);
//                if (null != stat) {
//                    count = stat.getNumChildren();
//                }
//            } catch (Exception e) {
//                logger.error("check state of node {} fail!", templateNodeParentPath);
//                logger.error("exception is ", e);
//            }
//        }
//        return count;
//    }
//
//
//    //private CuratorFramework zkClient;
//
//    private void initSpider() {
//        List<SiteTemplate> templates = SiteTemplateHolder.getAllTemplates();
//        if (null != templates && templates.size() <= 0) {
//            logger.warn("could not find any templates...");
//        } else {
//            // 初始化context
//            ApplicationContext context = ApplicationContext.getInstance();
//			/*// 初始化配置文件
//			if(StringUtils.isNotEmpty(configFileName)){
//				context.readPropertyFromFile(configFileName);
//			}*/
//            ZKPluginManager pluginManager = ZKPluginManager.instance();
//            context.registerPluginManager(pluginManager);
//
//            // 刷新urlclassloader
//            List<String> jarpaths = SiteTemplateHolder.getAllJarFilePath();
//            logger.info("all jar path is:" + jarpaths);
//            pluginManager.updateUrlClassLoader(jarpaths);
//
//            //初始化并启动代理汇报队列
//            BadHttpProxyHolder.init();
//            BadHttpProxyHolder.startReport();
//
//            SiteTemplateHelper helper = new SiteTemplateHelper();
//            SiteTemplate template = templates.get(0);
//            logger.info("templates size is:{}", templates.size());
//            logger.info("template id is:{}", template.getTemplateId());
//            Site site = helper.createSite(template);
//            TemplateSpider spider = TemplateSpider.create(site)
//                    .enablePluginWatch();
//
//            spider.setDownloader(new DownloaderRouter());
////			spider.setDownloader(new HttpClientUseProxyDownloader());
//            //spider.setDownloader(new HttpClientDownloader());
//            spider.setScheduler(new MongodbScheduler());
//            spider.addSpiderProcessListener(new MongoDBProcessListener());//与MongodbScheduler配合使用
////			spider.addPipeline(new ConsolePipeline());
//            // 保存到mysql数据库
//			/*spider.addPipeline(new MysqlPipeline());*/
//            // 保存到mongodb
//            spider.addPipeline(new HtmlBlankRemovedPipeline());//去除html标签
//            spider.addPipeline(new RemoveRemarkedInfoPipeline());//去除"暂无数据"等空白标签
//            spider.addPipeline(new WebantLogInfoPipeline());
//            StatisticContextListener statisticContextListener = StatisticContextListener.getInstance(new CuratorLock(lock));
//            spider.addPipeline(new MongoPersistPipeline(statisticContextListener));
//            spider.addPipeline(new PushDataToMQPipeline());//(暂时不需要推送到MQ)
//            spider.addSchedulerListener(new TemplateInfoAddListener());
//
//            spider.addSchedulerListener(statisticContextListener);
//            spider.addSpiderProcessListener(statisticContextListener);
//
//            spider.addRequestFilter(new RequestDeeperFilter());
//            //图片过滤器
//            //spider.addDataFilter(new DefaultImageFilter());
//            spider.addDataFilter(new EsfCellImageFilter());
//            spider.addDataFilter(new HouseImageFilter());
//            spider.addDataFilter(new AgentFilter());
//            //spider.addDataFilter(new PersonalHousePhoneOcrFilter());//58号码识别
//            spider.addDataFilter(new PhoneToBase64Filter());//把58个人房源图片存储为base64字符串
//            spider.addDataFilter(new SchoolImageFilter());// 学校图片
//            spider.addDataFilter(new FillCellArroundInfoFilter()); // 小区周边信息
//
//            spider.thread(workThreadNum);
//            // 启动spider
//            spider.start();
//            // 注册 MBean
//            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
//                    "yyMMddHHmm");
//            ObjectName objectName;
//            try {
//                objectName = new ObjectName("webant:type=spider,name="
//                        + spider.getUUID() + simpleDateFormat.format(new Date()));
//                spider.setObjectName(objectName);
//                server.registerMBean(spider, objectName);
//            } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                logger.info("suceess to register the mbean for spider {} ",
//                        spider.getUUID());
//            }
//        }
//
//    }
//
//}
