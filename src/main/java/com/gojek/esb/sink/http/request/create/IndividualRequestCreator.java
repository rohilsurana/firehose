package com.gojek.esb.sink.http.request.create;

import com.gojek.esb.config.enums.HttpRequestMethod;
import com.gojek.esb.consumer.EsbMessage;
import com.gojek.esb.sink.http.request.HttpRequestMethodFactory;
import com.gojek.esb.sink.http.request.body.JsonBody;
import com.gojek.esb.sink.http.request.entity.EntityBuilder;
import com.gojek.esb.sink.http.request.header.HeaderBuilder;
import com.gojek.esb.sink.http.request.uri.URIBuilder;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class IndividualRequestCreator implements RequestCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRequestCreator.class);

    private HeaderBuilder headerBuilder;
    private JsonBody body;
    private HttpRequestMethod method;
    private URIBuilder uriBuilder;

    public IndividualRequestCreator(URIBuilder uriBuilder, HeaderBuilder headerBuilder, HttpRequestMethod method, JsonBody body) {
        this.uriBuilder = uriBuilder;
        this.headerBuilder = headerBuilder;
        this.body = body;
        this.method = method;
    }

    @Override
    public List<HttpEntityEnclosingRequestBase> create(List<EsbMessage> esbMessages, EntityBuilder entity) throws URISyntaxException {
        List<HttpEntityEnclosingRequestBase> requests = new ArrayList<>();
        List<String> bodyContents = body.serialize(esbMessages);
        for (int i = 0; i < esbMessages.size(); i++) {
            EsbMessage esbMessage = esbMessages.get(i);
            URI requestUrl = uriBuilder.build(esbMessage);
            HttpEntityEnclosingRequestBase request = HttpRequestMethodFactory.create(requestUrl, method);

            headerBuilder.build(esbMessage).forEach(request::addHeader);
            request.setEntity(entity.buildHttpEntity(bodyContents.get(i)));
            requests.add(request);
        }
        return requests;
    }
}