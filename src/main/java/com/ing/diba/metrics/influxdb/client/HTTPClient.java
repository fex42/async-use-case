package com.ing.diba.metrics.influxdb.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


public class HTTPClient {

    private final boolean isEnabled = true;
    private final Logger logger;
    private String contentType = null;
    private int replyBufferSize = 1024;
    private int requestBufferSize = 1024;
    private String requestMethod = "POST";
    private int responseCode;
    private URL[] urlArray;


    public HTTPClient() {
        this(LoggerFactory.getLogger(HTTPClient.class));
    }


    public HTTPClient(final Logger logger) {
        this.logger = logger;
    }


    private String buildQueryString(final Map<String, String> requestProperties)
            throws UnsupportedEncodingException {
        final StringBuilder result = new StringBuilder();
        boolean first = true;

        for (final String key : requestProperties.keySet()) {
            if (first) {
                first = false;
            } else {
                result.append('&');
            }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append('=');
            result.append(URLEncoder.encode(requestProperties.get(key), "UTF-8"));
        }

        return result.toString();
    }


    protected InputStreamReader createErrorInputStreamReader(final HttpURLConnection connection)
            throws IOException {
        final InputStream inputStream = connection.getErrorStream();
        return (inputStream != null ? new InputStreamReader(inputStream) : null);
    }


    protected InputStreamReader createReplyInputStreamReader(final HttpURLConnection connection)
            throws IOException {
        final InputStream inputStream = connection.getInputStream();
        return (inputStream != null ? new InputStreamReader(inputStream) : null);
    }


    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public int getReplyBufferSize() {
        return this.replyBufferSize;
    }

    public void setReplyBufferSize(final int replyBufferSize) {
        this.replyBufferSize = replyBufferSize;
    }

    public int getRequestBufferSize() {
        return this.requestBufferSize;
    }

    public void setRequestBufferSize(final int requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public void setRequestMethod(final String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public URL[] getUrl() {
        return this.urlArray;
    }

    public void setUrl(final URL[] urlArray) {
        this.urlArray = urlArray;
    }

    protected boolean isResponseCodeOk() {
        return (this.responseCode >= 200) && (this.responseCode < 300);
    }

    protected void readError(final HttpURLConnection connection, final StringBuilder replyBuffer)
            throws IOException {
        final Reader reader1 = createErrorInputStreamReader(connection);
        if (reader1 != null) {
            try (
                    final BufferedReader reader2 = new BufferedReader(reader1)) {
                final char[] cbuf = new char[this.replyBufferSize];
                int charactersRead = -1;
                while ((charactersRead = reader2.read(cbuf)) > 0) {
                    replyBuffer.append(cbuf, 0, charactersRead);
                }
            }
        }
    }

    protected void readReply(final HttpURLConnection connection, final StringBuilder replyBuffer)
            throws IOException {
        final Reader reader1 = createReplyInputStreamReader(connection);
        if (reader1 != null) {
            try (
                    final BufferedReader reader2 = new BufferedReader(reader1)) {
                final char[] cbuf = new char[this.replyBufferSize];
                int charactersRead = -1;
                while ((charactersRead = reader2.read(cbuf)) > 0) {
                    replyBuffer.append(cbuf, 0, charactersRead);
                }
            }
        }
    }

    public String request(final boolean returnOnlyReplyContent, final String body,
                          final Map<String, String> requestProperties, final boolean arePropertiesInURL)
            throws Exception {
        if (!isEnabled) {
            return null;
        }
        Exception caughtException = null;
        this.responseCode = 0;

        for (final URL url : this.urlArray) {
            HttpURLConnection connection = null;
            try {
                URL currentURL = url;

                String currentContentType = this.contentType;
                String newQueryString = "";
                if ((requestProperties != null) && !requestProperties.isEmpty()) {
                    newQueryString = buildQueryString(requestProperties);
                    if (arePropertiesInURL) {
                        final String path = url.getPath();
                        String query = url.getQuery();

                        query = (query != null ? query.trim() : "");
                        query = (query.isEmpty() ? newQueryString : query + '&' + newQueryString);
                        final String file = path + '?' + query;

                        currentURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
                        newQueryString = "";
                    } else {
                        currentContentType = (this.contentType == null ? "application/x-www-form-urlencoded" : null);
                    }
                } else {
                    currentContentType = (this.contentType == null ? "text/plain; charset=utf-8" : null);
                }

                this.logger.trace("try to open [{}]", currentURL.toExternalForm());

                connection = (HttpURLConnection) currentURL.openConnection();
                connection.setRequestMethod(this.requestMethod);

                final boolean isOutput = !"GET".equalsIgnoreCase(this.requestMethod) && !"DELETE".equalsIgnoreCase(this.requestMethod);

                connection.setDoInput(true);
                connection.setDoOutput(isOutput);
                connection.setUseCaches(false);

                if (isOutput) {
                    final String contentString = (newQueryString.isEmpty() ? body : newQueryString);
                    connection.setRequestProperty("Content-Type", currentContentType);
                    final byte[] content = contentString.getBytes("UTF-8");
                    connection.setRequestProperty("Content-Length", String.valueOf(content.length));
                    this.logger.trace("going to write {} bytes to [{}]", content.length, currentURL.toExternalForm());
                    writeContent(connection, content);
                }

                this.responseCode = connection.getResponseCode();
                final StringBuilder replyBuffer = new StringBuilder(this.requestBufferSize);

                if (isResponseCodeOk()) {
                    this.logger.trace("ResponseCode was Ok [{}]", this.responseCode);
                    readReply(connection, replyBuffer);
                } else if (!returnOnlyReplyContent) {
                    this.logger.trace("(1) ResponseCode was NOT Ok [{}]", this.responseCode);
                    readError(connection, replyBuffer);
                } else {
                    this.logger.trace("(2) ResponseCode was NOT Ok [{}]", this.responseCode);
                    return null;
                }

                return replyBuffer.toString();
            } catch (final Exception ex) {
                if (caughtException == null) {
                    caughtException = ex;
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        if (caughtException != null) {
            throw caughtException;
        }

        return null;
    }

    public String request(final String body)
            throws Exception {
        if (!isEnabled) {
            return null;
        }
        final boolean arePropertiesInURL = false;
        final boolean returnOnlyReplyContent = true;
        return request(returnOnlyReplyContent, body, null, arePropertiesInURL);
    }

    public String request(final String body, final Map<String, String> requestProperties,
                          final boolean arePropertiesInURL)
            throws Exception {
        if (!isEnabled) {
            return null;
        }
        final boolean returnOnlyReplyContent = true;
        return request(returnOnlyReplyContent, body, requestProperties, arePropertiesInURL);
    }

    public void setUrl(final URL url) {
        this.urlArray = new URL[1];
        this.urlArray[0] = url;
    }

    protected void writeContent(final HttpURLConnection connection, final byte[] content)
            throws IOException {
        try (
                final OutputStream os = connection.getOutputStream()) {
            os.write(content);
            os.flush();
        }
    }

}
