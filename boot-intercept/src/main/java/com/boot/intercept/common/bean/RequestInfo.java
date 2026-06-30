package com.boot.intercept.common.bean;

import lombok.Data;

import java.util.Map;

@Data
public class RequestInfo {

    private String requestId;
    private String ip;
    private String url;
    private String httpMethod;
    private String classMethod;
    private Map<String, Object> requestParams;
    private Object result;
    private long timeCost;
    private Boolean isError = false;
    private String errorMessage;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{requestId='").append(requestId).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", httpMethod='").append(httpMethod).append('\'');
        sb.append(", classMethod='").append(classMethod).append('\'');
        sb.append(", timeCost=").append(timeCost).append("ms");
        if (Boolean.TRUE.equals(isError)) {
            sb.append(", error='").append(errorMessage).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
