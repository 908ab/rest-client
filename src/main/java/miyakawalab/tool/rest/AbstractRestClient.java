package miyakawalab.tool.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import miyakawalab.tool.config.ExceptionInformation;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractRestClient<Req, Res> {
    private String baseUri;
    private List<Header> headers;

    private Class<Res> resClass;
    private ObjectMapper mapper;

    public AbstractRestClient(String baseUri, List<Header> headers, List<NameValuePair> pathParams, Class<Res> resClass) {
        this.baseUri = baseUri;
        pathParams.forEach(param -> this.baseUri = this.baseUri.replace("{" + param.getName() + "}", param.getValue()));

        this.headers = new ArrayList<>(headers);

        this.resClass = resClass;
        this.mapper = new ObjectMapper();
    }

    protected Res get(Long targetId, List<NameValuePair> requestParams, String contentType) {
        try {
            this.baseUri = this.baseUri + "/" + targetId;
            HttpGet httpGet = new HttpGet(this.getUriWithRequestParams(requestParams));
            this.headers.add(new BasicHeader("content-type", contentType));
            HttpResponse response = this.getHttpClient().execute(httpGet);
            this.checkError(response, Response.Status.OK);

            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return this.mapper.readValue(json, this.resClass);
        } catch (IOException e) {
            throw new InternalServerErrorException(this.resClass.getSimpleName() + " get method.\n" + e.getMessage());
        }
    }

    protected Res get(Long targetId, String contentType) {
        return this.get(targetId, new ArrayList<>(), contentType);
    }

    protected List<Res> get(List<NameValuePair> requestParams, String contentType) {
        try {
            HttpGet httpGet = new HttpGet(this.getUriWithRequestParams(requestParams));
            this.headers.add(new BasicHeader("content-type", contentType));
            HttpResponse response = this.getHttpClient().execute(httpGet);
            this.checkError(response, Response.Status.OK);

            String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return this.mapper.readValue(json, this.mapper.getTypeFactory().constructCollectionType(List.class, this.resClass));
        } catch (IOException e) {
            throw new InternalServerErrorException(this.resClass.getSimpleName() + " get method.\n" + e.getMessage());
        }
    }

    protected List<Res> get(String contentType) {
        return this.get(new ArrayList<>(), contentType);
    }

    protected HttpResponse post(Req body, List<NameValuePair> requestParams, String contentType) {
        try {
            HttpPost httpPost = new HttpPost(this.getUriWithRequestParams(requestParams));
            String json = this.mapper.writeValueAsString(body);
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            this.headers.add(new BasicHeader("content-type", contentType));
            HttpResponse response = this.getHttpClient().execute(httpPost);
            this.checkError(response, Response.Status.CREATED, Response.Status.OK);
            return response;
        } catch (IOException e) {
            throw new InternalServerErrorException(this.resClass.getSimpleName() + " post method.\n" + e.getMessage());
        }
    }

    protected HttpResponse post(Req body, String contentType) {
        return this.post(body, new ArrayList<>(), contentType);
    }

    protected HttpResponse put(Long targetId, Req body, List<NameValuePair> requestParams, String contentType) {
        try {
            this.baseUri = this.baseUri + "/" + targetId;
            HttpPut httpPut = new HttpPut(this.getUriWithRequestParams(requestParams));
            String json = this.mapper.writeValueAsString(body);
            StringEntity entity = new StringEntity(json);
            httpPut.setEntity(entity);
            this.headers.add(new BasicHeader("content-type", contentType));
            HttpResponse response = this.getHttpClient().execute(httpPut);
            this.checkError(response, Response.Status.NO_CONTENT);
            return response;
        } catch (IOException e) {
            throw new InternalServerErrorException(this.resClass.getSimpleName() + " put method.\n" + e.getMessage());
        }
    }

    protected HttpResponse put(Long targetId, Req body, String contentType) {
        return this.put(targetId, body, new ArrayList<>(), contentType);
    }

    protected HttpResponse delete(Long targetId, List<NameValuePair> requestParams) {
        try {
            this.baseUri = this.baseUri + "/" + targetId;
            HttpDelete httpDelete = new HttpDelete(this.getUriWithRequestParams(requestParams));
            HttpResponse response = this.getHttpClient().execute(httpDelete);
            this.checkError(response, Response.Status.NO_CONTENT);
            return response;
        } catch (IOException e) {
            throw new InternalServerErrorException(this.resClass.getSimpleName() + " delete method.\n" + e.getMessage());
        }
    }

    protected HttpResponse delete(Long targetId) {
        return this.delete(targetId, new ArrayList<>());
    }

    protected void addHeader(Header header) {
        this.headers.add(header);
    }

    private HttpClient getHttpClient() {
        return HttpClientBuilder.create()
            .setDefaultHeaders(this.headers)
            .build();
    }

    private String getUriWithRequestParams(List<NameValuePair> requestParams) {
        if (requestParams.size() > 0) {
            String requestParamString = requestParams.stream()
                .map(NameValuePair::toString)
                .collect(Collectors.joining("&"));
            return this.baseUri + "?" + requestParamString;
        }

        return this.baseUri;
    }

    private void checkError(HttpResponse response, Response.Status... successStatuses) throws IOException {
        for (Response.Status status: successStatuses) {
            if (response.getStatusLine().getStatusCode() == status.getStatusCode()) { return; }
        }

        String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        ExceptionInformation information =
            this.mapper.readValue(json, ExceptionInformation.class);

        throw new WebApplicationException(information.getMessage(), information.getStatus());
    }
}
