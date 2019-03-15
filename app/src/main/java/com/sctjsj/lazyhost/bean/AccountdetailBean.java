package com.sctjsj.lazyhost.bean;

import java.util.List;

public class AccountdetailBean {
    /**
     * domainBase : WEB-INF/zh_CN/pc/
     * resultList : [{"content":"55115408892245093订单完成","id":5,"inOut":1,"insertTime":"2018-10-30 19:29:11","price":0}]
     * pageIndex : 1
     * pageSize : 8
     * pageCount : 1
     * rowCount : 1
     * data : {"content":"55115408892245093订单完成","id":5,"inOut":1,"insertTime":"2018-10-30 19:29:11","price":0}
     * pageHtml : 首页 上一页 1下一页 尾页
     * result : true
     * msg : 成功
     */

    private String domainBase;
    private int pageIndex;
    private int pageSize;
    private int pageCount;
    private int rowCount;
    private DataBean data;
    private String pageHtml;
    private boolean result;
    private String msg;
    private List<ResultListBean> resultList;

    public String getDomainBase() {
        return domainBase;
    }

    public void setDomainBase(String domainBase) {
        this.domainBase = domainBase;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getPageHtml() {
        return pageHtml;
    }

    public void setPageHtml(String pageHtml) {
        this.pageHtml = pageHtml;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ResultListBean> getResultList() {
        return resultList;
    }

    public void setResultList(List<ResultListBean> resultList) {
        this.resultList = resultList;
    }

    public static class DataBean {
        /**
         * content : 55115408892245093订单完成
         * id : 5
         * inOut : 1
         * insertTime : 2018-10-30 19:29:11
         * price : 0
         */

        private String content;
        private int id;
        private int inOut;
        private String insertTime;
        private double price;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getInOut() {
            return inOut;
        }

        public void setInOut(int inOut) {
            this.inOut = inOut;
        }

        public String getInsertTime() {
            return insertTime;
        }

        public void setInsertTime(String insertTime) {
            this.insertTime = insertTime;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    public static class ResultListBean {
        /**
         * content : 55115408892245093订单完成
         * id : 5
         * inOut : 1
         * insertTime : 2018-10-30 19:29:11
         * price : 0
         */

        private String content;
        private int id;
        private int inOut;
        private String insertTime;
        private double price;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getInOut() {
            return inOut;
        }

        public void setInOut(int inOut) {
            this.inOut = inOut;
        }

        public String getInsertTime() {
            return insertTime;
        }

        public void setInsertTime(String insertTime) {
            this.insertTime = insertTime;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
