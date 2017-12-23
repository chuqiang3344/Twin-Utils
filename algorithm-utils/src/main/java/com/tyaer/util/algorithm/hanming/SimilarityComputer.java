package com.tyaer.util.algorithm.hanming;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by Twin on 2017/12/18.
 */
public class SimilarityComputer {
    private static final Logger logger = Logger.getLogger(SimilarityComputer.class);

    public List<List<String>> cluster1(List<EsArticle> esArticles, double minSimilarity, boolean orderByCount) {
        if (minSimilarity >= 0.8) {
            minSimilarity = 0.7;
        }

        logger.info("orderByCount:" + orderByCount + ",minSimilarity:" + minSimilarity);
        List<byte[]> hanmings = new ArrayList<byte[]>();
        List<String> mids = new ArrayList<String>();

        for (EsArticle esArticle : esArticles) {
            mids.add(esArticle.getId());
            String hanming = esArticle.getHanmingCode();
            if (null != hanming && hanming.length() == 32) {
                hanmings.add(HanmingCode.decode(hanming));
            } else {
                hanmings.add(null);
            }
        }
        List<List<String>> groups = new ArrayList<List<String>>();
        Set<String> grouped = new HashSet<String>();
        for (int k = 0; k < hanmings.size(); k++) {
            if (grouped.contains(mids.get(k))) {
                continue;
            } else {
                grouped.add(mids.get(k));
            }
            List<String> group = new ArrayList<String>();
            group.add(mids.get(k));
            groups.add(group);
            if (hanmings.get(k) == null || notZoreBitCount(hanmings.get(k)) < 5) {
                continue;
            }

            for (int l = k + 1; l < hanmings.size(); l++) {
                if (grouped.contains(mids.get(l)) || hanmings.get(l) == null) {
                    continue;
                }


                short intersection = getNoneZeroCommon(hanmings.get(k), hanmings.get(l));
                short union = getNoneZeroUnion(hanmings.get(k), hanmings.get(l));
                double similirity = new BigDecimal(intersection).divide(new BigDecimal(union), 4, RoundingMode.HALF_UP).doubleValue();


                if (notZoreBitCount(hanmings.get(k)) < 200) {
                    if (similirity >= minSimilarity) {
                        group.add(mids.get(l));
                        grouped.add(mids.get(l));
                    }
                }

                if (notZoreBitCount(hanmings.get(k)) >= 200) {
                    if (similirity >= 0.8) {
                        group.add(mids.get(l));
                        grouped.add(mids.get(l));
                    }
                }


            }

        }
        if (orderByCount) {
            List<Group> gs = new ArrayList<Group>();
            for (List<String> group : groups) {
                Group g = new Group(group);
                gs.add(g);
            }
            Collections.sort(gs);
            groups.clear();
            for (Group g : gs) {
                groups.add(g.getEles());
            }
        }

        return groups;
    }

    public static List<List<String>> cluster(List<String> midHanmingCodeList, double minSimilarity, boolean orderByCount) {
        if (minSimilarity >= 0.8) {
            minSimilarity = 0.7;
        }

        logger.info("orderByCount:" + orderByCount + ",minSimilarity:" + minSimilarity);
        List<byte[]> hanmings = new ArrayList<byte[]>();
        List<String> mids = new ArrayList<String>();

        for (String midHanmingCode : midHanmingCodeList) {
            String[] fields = midHanmingCode.split(",");
            mids.add(fields[0]);
            String hanming = fields.length == 2 ? fields[1] : null;
            if (null != hanming && hanming.length() == 32) {
                hanmings.add(HanmingCode.decode(hanming));
            } else {
                hanmings.add(null);
            }
        }
        List<List<String>> groups = new ArrayList<List<String>>();
        Set<String> grouped = new HashSet<String>();
        for (int k = 0; k < hanmings.size(); k++) {
            if (grouped.contains(mids.get(k))) {
                continue;
            } else {
                grouped.add(mids.get(k));
            }
            List<String> group = new ArrayList<String>();
            group.add(mids.get(k));
            groups.add(group);
            if (hanmings.get(k) == null || notZoreBitCount(hanmings.get(k)) < 5) {
                continue;
            }

            for (int l = k + 1; l < hanmings.size(); l++) {
                if (grouped.contains(mids.get(l)) || hanmings.get(l) == null) {
                    continue;
                }


                short intersection = getNoneZeroCommon(hanmings.get(k), hanmings.get(l));
                short union = getNoneZeroUnion(hanmings.get(k), hanmings.get(l));
                double similirity = new BigDecimal(intersection).divide(new BigDecimal(union), 4, RoundingMode.HALF_UP).doubleValue();


                if (notZoreBitCount(hanmings.get(k)) < 200) {
                    if (similirity >= minSimilarity) {
                        group.add(mids.get(l));
                        grouped.add(mids.get(l));
                    }
                }

                if (notZoreBitCount(hanmings.get(k)) >= 200) {
                    if (similirity >= 0.8) {
                        group.add(mids.get(l));
                        grouped.add(mids.get(l));
                    }
                }


            }

        }
        if (orderByCount) {
            List<Group> gs = new ArrayList<Group>();
            for (List<String> group : groups) {
                Group g = new Group(group);
                gs.add(g);
            }
            Collections.sort(gs);
            groups.clear();
            for (Group g : gs) {
                groups.add(g.getEles());
            }
        }

        return groups;
    }

    public static short getNoneZeroCommon(byte[] a, byte[] b) {
        short common = 0;
        for (int i = 0; i < a.length; i++) {
            byte c = (byte) (a[i] & b[i]);

            for (int j = 0; j < 8; j++) {
                common += c & 1;
                c = (byte) (c >> 1);
            }
        }

        return common;

    }

    public static short notZoreBitCount(byte[] a) {
        short count = 0;
        for (int j = 0; j < a.length; j++) {
            byte t = a[j];
            for (int i = 0; i < 8; i++) {

                count += t & 1;
                t = (byte) (t >> 1);
            }
        }

        return count;
    }

    public static short getNoneZeroUnion(byte[] a, byte[] b) {
        short common = 0;
        for (int i = 0; i < a.length; i++) {
            byte c = (byte) (a[i] | b[i]);

            for (int j = 0; j < 8; j++) {
                common += c & 1;
                c = (byte) (c >> 1);
            }
        }

        return common;

    }
}
