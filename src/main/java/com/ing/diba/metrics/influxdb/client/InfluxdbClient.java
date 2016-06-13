package com.ing.diba.metrics.influxdb.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class InfluxdbClient {

    private final Logger logger;
    private String dbName;
    private boolean isBatchRequest = true;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private URL[] urlArray;
    private HTTPClient writeDataClient;
    private String username;
    private String password;


    public InfluxdbClient() {
        this(LoggerFactory.getLogger(InfluxdbClient.class));
    }


    public InfluxdbClient(final Logger logger) {
        this.logger = logger;
    }

    private String firstURLString() {
        final URL[] urls = this.writeDataClient.getUrl();
        String urlStr = "<null array>";
        if (urls != null) {
            urlStr = (urls.length > 0 ? urls[0].toExternalForm() : "<empty array>");
        }
        return urlStr;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(final String dbName) {
        this.dbName = dbName;
        reset();
    }

    private String getPrecision() {
        switch (this.timeUnit) {
        case NANOSECONDS:
            return "n";
        case MICROSECONDS:
            return "u";
        case MILLISECONDS:
            return "ms";
        case SECONDS:
            return "s";
        case MINUTES:
            return "m";
        case HOURS:
            return "h";
        case DAYS:
            throw new IllegalArgumentException("Days are not allowed");
        }
        return null;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(final TimeUnit newTimeUnit) {
        switch (newTimeUnit) {
        case DAYS:
            throw new IllegalArgumentException("Days are not allowed");
        default:
            break;
        }
        this.timeUnit = newTimeUnit;
        reset();
    }

    public URL[] getUrl() {
        return this.urlArray;
    }

    public void setUrl(final URL url) {
        this.urlArray = new URL[1];
        this.urlArray[0] = url;
        reset();
    }

    public void setUrl(final URL[] urlArray) {
        this.urlArray = urlArray;
        reset();
    }

    public boolean isBatchRequest() {
        return this.isBatchRequest;
    }

    public void reset() {
        this.writeDataClient = null;
    }

    public void setIsBatchRequest(final boolean isBatchRequest) {
        this.isBatchRequest = isBatchRequest;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String getAuthentication() {
        return (username != null ? "&u=" + username + "&p=" + password : "");
    }


    public String writeData(final List<SingleMetric> singleMetricList)
            throws Exception {
        if (singleMetricList.size() > 0) {
            if (this.writeDataClient == null) {
                // curl -i -XPOST 'http://localhost:8086/write?db=mydb&precision=s' --data-binary 'temperature,machine=unit42,type=assembly external=25,internal=37 1434059627'
                final URL[] writeDataURLArray = new URL[this.urlArray.length];
                for (int i = 0; writeDataURLArray.length > i; ++i) {
                    String writeUrl = getUrl()[i].toExternalForm();
                    writeUrl += "write?db=" + URLEncoder.encode(this.dbName,
                                                                "UTF-8") + "&precision=" + getPrecision() + getAuthentication();
                    writeDataURLArray[i] = new URL(writeUrl);
                }
                this.writeDataClient = new HTTPClient(this.logger);
                this.writeDataClient.setUrl(writeDataURLArray);
            }

            if (this.isBatchRequest) {
                String body = "";
                for (final SingleMetric singleMetric : singleMetricList) {
                    singleMetric.convertToTimeUnit(this.timeUnit);
                    final String singleMetricStr = singleMetric.build();
                    if (singleMetricStr != null) {
                        body += singleMetricStr + "\n";
                    }
                }

                if (this.logger.isTraceEnabled()) {
                    final String urlStr = firstURLString();
                    this.logger.trace("Line-Protocol: [{}] on URL [{}]", body, urlStr);
                }
                return writeData(body);
            } else {
                for (final SingleMetric singleMetric : singleMetricList) {
                    singleMetric.convertToTimeUnit(this.timeUnit);
                    final String singleMetricStr = singleMetric.build();
                    if (singleMetricStr != null) {
                        final String reply = writeData(singleMetricStr);
                        if (this.logger.isTraceEnabled()) {
                            final String urlStr = firstURLString();
                            this.logger.trace("Line-Protocol: [{}] on URL [{}] with reply {}",
                                              singleMetricStr,
                                              urlStr,
                                              reply);
                        }
                    }
                }
            }
        }
        return null;
    }


    private String writeData(final String body)
            throws Exception {
        final boolean returnOnlyReplyContent = false;
        final String replyRaw = this.writeDataClient.request(returnOnlyReplyContent, body, null, false);
        final String reply = (replyRaw != null ? replyRaw : "");
        return (reply.isEmpty() ? "" + this.writeDataClient.getResponseCode() : "" + this.writeDataClient.getResponseCode() + "" + reply);
    }

}
