package com.wly.center.core.helper;

import com.wly.center.core.exception.BusinessException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

/**
 * REST 客户端工具类
 * 支持泛型参数和返回值，统一的异常处理
 */
public class RestClientHelper {
    private final RestClient restClient;

    private RestClientHelper(Builder builder) {
        RestClient.Builder restClientBuilder = RestClient.builder();
        if (builder.baseUrl != null) {
            restClientBuilder = restClientBuilder.baseUrl(builder.baseUrl);
        }
        if (builder.defaultHeaders != null) {
            restClientBuilder = restClientBuilder.defaultHeaders(builder.defaultHeaders);
        }

        this.restClient = restClientBuilder.build();
    }

    /**
     * GET 请求
     */
    public <T> T get(String url, Class<T> responseType) {
        return executeRequest(url, HttpMethod.GET, null, responseType, null, null, null);
    }

    public <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return executeRequest(url, HttpMethod.GET, null, responseType, null, null, null);
    }

    public <T> T get(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, HttpMethod.GET, null, responseType, uriVariables, null, null);
    }

    public <T> T get(String url, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, HttpMethod.GET, null, responseType, uriVariables, null, null);
    }

    // GET with query parameters (use MultiValueMap to avoid erasure clashes)
    public <T> T get(String url, Class<T> responseType, MultiValueMap<String, ?> queryParams) {
        return executeRequest(url, HttpMethod.GET, null, responseType, null, queryParams, null);
    }

    public <T> T get(String url, ParameterizedTypeReference<T> responseType, MultiValueMap<String, ?> queryParams) {
        return executeRequest(url, HttpMethod.GET, null, responseType, null, queryParams, null);
    }

    /**
     * POST 请求
     */
    public <T> T post(String url, Object request, Class<T> responseType) {
        return executeRequest(url, HttpMethod.POST, request, responseType, null, null, null);
    }

    public <T> T post(String url, Object request, ParameterizedTypeReference<T> responseType) {
        return executeRequest(url, HttpMethod.POST, request, responseType, null, null, null);
    }

    public <T> T post(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, HttpMethod.POST, request, responseType, uriVariables, null, null);
    }

    public <T> T post(String url, Object request, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, HttpMethod.POST, request, responseType, uriVariables, null, null);
    }

    public <T> T post(String url, Class<T> responseType, MultiValueMap<String, ?> queryParams) {
        return executeRequest(url, HttpMethod.POST, null, responseType, null, queryParams, null);
    }

    public <T> T post(String url, ParameterizedTypeReference<T> responseType, MultiValueMap<String, ?> queryParams) {
        return executeRequest(url, HttpMethod.POST, null, responseType, null, queryParams, null);
    }

    // POST form (application/x-www-form-urlencoded)
    public <T> T postForm(String url, MultiValueMap<String, ?> formParams, Class<T> responseType) {
        return executeRequest(url, HttpMethod.POST, null, responseType, null, null, formParams);
    }

    public <T> T postForm(String url, MultiValueMap<String, ?> formParams, ParameterizedTypeReference<T> responseType) {
        return executeRequest(url, HttpMethod.POST, null, responseType, null, null, formParams);
    }

    public <T> T postForm(String url, MultiValueMap<String, ?> formParams, Class<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, HttpMethod.POST, null, responseType, uriVariables, null, formParams);
    }

    public <T> T postForm(String url, MultiValueMap<String, ?> formParams, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, HttpMethod.POST, null, responseType, uriVariables, null, formParams);
    }

    /**
     * PUT 请求
     */
    public <T> T put(String url, Object request, Class<T> responseType) {
        return executeRequest(url, HttpMethod.PUT, request, responseType, null, null, null);
    }

    public <T> T put(String url, Object request, ParameterizedTypeReference<T> responseType) {
        return executeRequest(url, HttpMethod.PUT, request, responseType, null, null, null);
    }

    /**
     * DELETE 请求
     */
    public <T> T delete(String url, Class<T> responseType) {
        return executeRequest(url, HttpMethod.DELETE, null, responseType, null, null, null);
    }

    public <T> T delete(String url, ParameterizedTypeReference<T> responseType) {
        return executeRequest(url, HttpMethod.DELETE, null, responseType, null, null, null);
    }

    /**
     * 执行请求的核心方法
     */
    private <T> T executeRequest(String url, HttpMethod method, Object request,
                                 Class<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, method, request, responseType, uriVariables, null, null);
    }

    private <T> T executeRequest(String url, HttpMethod method, Object request,
                                 ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
        return executeRequest(url, method, request, responseType, uriVariables, null, null);
    }

    private <T> T executeRequest(String url, HttpMethod method, Object request,
                                 Class<T> responseType, Map<String, ?> uriVariables,
                                 MultiValueMap<String, ?> queryParams, MultiValueMap<String, ?> formParams) {
        try {
            RestClient.RequestBodySpec requestSpec = buildRequest(url, method, request, uriVariables, queryParams, formParams);

            if (responseType == Void.class) {
                requestSpec.retrieve().toBodilessEntity();
                return null;
            } else {
                return requestSpec.retrieve().body(responseType);
            }
        } catch (RestClientException e) {
            throw new BusinessException("HTTP_REQUEST_FAIL", "HTTP request fail: " + e.getMessage());
        }
    }

    private <T> T executeRequest(String url, HttpMethod method, Object request,
                                 ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables,
                                 MultiValueMap<String, ?> queryParams, MultiValueMap<String, ?> formParams) {
        try {
            RestClient.RequestBodySpec requestSpec = buildRequest(url, method, request, uriVariables, queryParams, formParams);
            return requestSpec.retrieve().body(responseType);
        } catch (RestClientException e) {
            throw new BusinessException("HTTP_REQUEST_FAIL", "HTTP request fail: " + e.getMessage());
        }
    }

    /**
     * 构建请求（现在支持 uriVariables、queryParams 与 formParams）
     */
    private RestClient.RequestBodySpec buildRequest(String url, HttpMethod method,
                                                    Object request, Map<String, ?> uriVariables,
                                                    MultiValueMap<String, ?> queryParams, MultiValueMap<String, ?> formParams) {
        RestClient.RequestBodyUriSpec requestSpec = restClient.method(method);

        // build URI with query params and expand uri variables if provided
        UriComponentsBuilder ub = UriComponentsBuilder.fromUriString(url);
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, ?> e : queryParams.entrySet()) {
                Object value = e.getValue();
                if (value instanceof java.util.Collection) {
                    for (Object v : (java.util.Collection<?>) value) {
                        ub.queryParam(e.getKey(), v);
                    }
                } else {
                    ub.queryParam(e.getKey(), value);
                }
            }
        }

        URI uri;
        if (uriVariables != null) {
            uri = ub.buildAndExpand(uriVariables).toUri();
        } else {
            uri = ub.build().toUri();
        }

        RestClient.RequestBodySpec bodySpec = requestSpec
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // If form parameters are provided, use form content type and set them as body
        if (formParams != null && !formParams.isEmpty()) {
            bodySpec = requestSpec
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON);

            bodySpec.body(formParams);
            return bodySpec;
        }

        if (request != null) {
            bodySpec.body(request);
        }

        return bodySpec;
    }

    /**
     * 自定义请求（高级用法）
     */
    public <T> T exchange(String url, HttpMethod method, Object request,
                          HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(method)
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (headers != null) {
                requestSpec.headers(httpHeaders -> httpHeaders.addAll(headers));
            }

            if (request != null) {
                requestSpec.body(request);
            }

            return requestSpec.retrieve().body(responseType);
        } catch (RestClientException e) {
            throw new BusinessException("HTTP_REQUEST_FAIL", "HTTP request fail:" + e.getMessage());
        }
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl;
        private Consumer<HttpHeaders> defaultHeaders;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder defaultHeaders(Consumer<HttpHeaders> headersConsumer) {
            this.defaultHeaders = headersConsumer;
            return this;
        }

        public Builder defaultHeader(String name, String value) {
            if (this.defaultHeaders == null) {
                this.defaultHeaders = headers -> headers.add(name, value);
            } else {
                this.defaultHeaders = this.defaultHeaders.andThen(headers -> headers.add(name, value));
            }
            return this;
        }

        public Builder bearerToken(String token) {
            return defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        public RestClientHelper build() {
            return new RestClientHelper(this);
        }
    }
}
