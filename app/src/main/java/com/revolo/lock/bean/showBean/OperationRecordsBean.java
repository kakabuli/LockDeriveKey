package com.revolo.lock.bean.showBean;

import java.util.List;

/**
 * author :
 * time   : 2021/2/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class OperationRecordsBean {

    private long titleOperationTime;
    private List<OperationRecord> operationRecords;

    public class OperationRecord {

        private int eventType;
        private int eventSource;

    }

    public long getTitleOperationTime() {
        return titleOperationTime;
    }

    public void setTitleOperationTime(long titleOperationTime) {
        this.titleOperationTime = titleOperationTime;
    }

    public List<OperationRecord> getOperationRecords() {
        return operationRecords;
    }

    public void setOperationRecords(List<OperationRecord> operationRecords) {
        this.operationRecords = operationRecords;
    }
}
