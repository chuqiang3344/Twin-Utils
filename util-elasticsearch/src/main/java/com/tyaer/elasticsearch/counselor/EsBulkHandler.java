package com.tyaer.elasticsearch.counselor;

import com.tyaer.elasticsearch.manage.EsClientMananger;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Twin on 2017/9/20.
 */
public class EsBulkHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(EsBulkHandler.class);
    private static final int DEAL_TIME = 3000;
    //    private ArrayBlockingQueue<String> arrayBlockingQueue=new ArrayBlockingQueue<String>(2048);
//    private ArrayBlockingQueue<String> arrayBlockingQueue=new ArrayBlockingQueue<String>(2048);
    private static final int bundleNum = 100;
    private static EsClientMananger esClientMananger;
    private AtomicInteger send_count = new AtomicInteger();
    private ArrayBlockingQueue<ActionRequestBuilder> arrayBlockingQueue = new ArrayBlockingQueue(8192);
    private long lastCommitTime = Calendar.getInstance().getTimeInMillis();

    public EsBulkHandler(String es_hosts, String esClusterName) {
        esClientMananger = new EsClientMananger(es_hosts, esClusterName);
        Executors.newSingleThreadExecutor().execute(this);
    }


    public DeleteRequestBuilder deleteChangeToBuilder(String index, String type, String id) {
        try {
            DeleteRequestBuilder deleteRequestBuilder = esClientMananger.getEsClient().prepareDelete(index, type, id);
            arrayBlockingQueue.put(deleteRequestBuilder);
            return deleteRequestBuilder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UpdateRequestBuilder deleteFieldChangeToBuilder(String index, String type, String id, String field) {
        try {
            UpdateRequestBuilder deleteBuilder = esClientMananger.getEsClient().prepareUpdate(index, type, id).setScript(new Script("ctx._source.remove(\"" + field + "\")", ScriptService.ScriptType.INLINE, null, null));
            arrayBlockingQueue.put(deleteBuilder);
            return deleteBuilder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public UpdateRequestBuilder updateChangeToBuilder(String index, String type, String id, Map<String, Object> objectMap) {
        try {
            Map<String, String> map = new HashMap();
            for (String key : objectMap.keySet()) {
                String value = objectMap.get(key) + "";
                if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                    map.put(key, value);
                }
            }
            UpdateRequestBuilder updateRequestBuilder = esClientMananger.getEsClient().prepareUpdate(index, type, id).setDoc(map).setUpsert(map);
            arrayBlockingQueue.put(updateRequestBuilder);
            return updateRequestBuilder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UpdateRequestBuilder updateChangeToBuilder(String index, String type, Map<String, Object> objectMap) {
        try {
            Object id = objectMap.get("_id");
            if (id == null) {
                logger.warn("ID为空！");
                return null;
            }else{
                String _id = id + "";
                objectMap.remove("_id");
                return updateChangeToBuilder(index, type, _id, objectMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        ArrayList<ActionRequestBuilder> futureList = new ArrayList<>();
        while (true) {
            long now = Calendar.getInstance().getTimeInMillis();
            ActionRequestBuilder send = arrayBlockingQueue.poll();
            if (null != send) {
                futureList.add(send);
                if (futureList.size() >= bundleNum || now - lastCommitTime > DEAL_TIME) {
                    logger.info("ES|send sum:" + send_count.addAndGet(futureList.size()) + "|send num:" + futureList.size() + "|queue size:" + arrayBlockingQueue.size());
                    if (!futureList.isEmpty()) {
                        batchUpdate(futureList);
                    }
                    futureList.clear();
                    lastCommitTime = now;
                }
            } else {
                if (futureList.size() > 0 && now - lastCommitTime > DEAL_TIME) {
                    logger.info("ES|send sum:" + send_count.addAndGet(futureList.size()) + "|send num:" + futureList.size() + "|queue size:" + arrayBlockingQueue.size());
                    if (!futureList.isEmpty()) {
                        batchUpdate(futureList);
                    }
                    futureList.clear();
                    lastCommitTime = now;
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 批量更新
     *
     * @param lists
     */
    public void batchUpdate(List<ActionRequestBuilder> lists) {
        long start = Calendar.getInstance().getTimeInMillis();
        TransportClient client = esClientMananger.getEsClient();
        try {
            ArrayList<ActionRequestBuilder> updateRequestBuilders = new ArrayList<>();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (ActionRequestBuilder request : lists) {
                if (request == null) {
                    continue;
                }
                if (request instanceof UpdateRequestBuilder) {
                    bulkRequest.add((UpdateRequestBuilder) request);
                } else if (request instanceof DeleteRequestBuilder) {
                    bulkRequest.add((DeleteRequestBuilder) request);
                } else if (request instanceof IndexRequestBuilder) {
                    bulkRequest.add((IndexRequestBuilder) request);
                } else {
                    logger.warn(request.getClass());
                }
                updateRequestBuilders.add(request);
            }
            BulkResponse bulkResponse = bulkRequest.get();
            BulkItemResponse[] items = bulkResponse.getItems();
//            if (bulkResponse.hasFailures()) {
            ArrayList<Object> list_yes = new ArrayList<>();
            ArrayList<Object> list_no = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                BulkItemResponse itemResponse = items[i];
                if (itemResponse.isFailed()) {
                    BulkItemResponse.Failure failure = itemResponse.getFailure();
                    Throwable cause = failure.getCause();
                    logger.error(i + " cause==>>>> " + cause);
                    ActionRequestBuilder requestBuilder = updateRequestBuilders.get(i);
                    list_no.add(itemResponse.getId());
//                    retryhandle(requestBuilder, cause);
                } else {
                    list_yes.add(itemResponse.getId());
                }
            }
//            }
            logger.info("useTime:" + (Calendar.getInstance().getTimeInMillis() - start) + "ms,update success:" + list_yes+" total:"+list_yes.size());
            if(!list_no.isEmpty()){
                logger.info("update failed:" + list_no+" total:"+list_no.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error(index + " ==>>  type : " + type + "  size: " + lists.size());
        }
    }

    /**
     * 重试处理
     *
     * @param cause
     * @throws InterruptedException
     */
    private void retryhandle(ActionRequestBuilder requestBuilder, Throwable cause) throws InterruptedException {
//        if (!cause.toString().contains("AlreadyExistsException")) {
        String error = cause.toString();
        if (!error.contains("AlreadyExistsException")&&!error.contains("DocumentMissingException")) {
            boolean add = arrayBlockingQueue.add(requestBuilder);
            logger.info("arrayBlockingQueue.size():" + arrayBlockingQueue.size());
            if (!add) {
                arrayBlockingQueue.put(requestBuilder);
            }
        }
    }


}
