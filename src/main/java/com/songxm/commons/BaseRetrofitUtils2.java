package com.songxm.commons;
import com.songxm.commons.exception.RetrofitException;
import jodd.util.ThreadUtil;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.CallAdapter.Factory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@SuppressWarnings({"unchecked","deprecation"})
public class BaseRetrofitUtils2 {
    private static final Logger log = LoggerFactory.getLogger(BaseRetrofitUtils2.class);

    public BaseRetrofitUtils2() {
    }

    public static BaseRetrofitUtils2.Builder newBuilder(String baseUrl) {
        return new BaseRetrofitUtils2.Builder(baseUrl);
    }

    static class SynchronousCallAdapterFactory extends Factory {
        SynchronousCallAdapterFactory() {
        }

        public static Factory create() {
            return new BaseRetrofitUtils2.SynchronousCallAdapterFactory();
        }

        public CallAdapter<Object> get(final Type returnType, Annotation[] annotations, Retrofit retrofit) {
            return returnType.getTypeName().contains("retrofit2.Call")?null:new CallAdapter<Object>() {
                public Type responseType() {
                    return returnType;
                }

                public <R> Object adapt(Call<R> call) {
                    try {
                        Response e = call.execute();
                        return returnType.getTypeName().contains("retrofit2.Response")?e:(e.body() != null?e.body():BaseJsonUtils.readValue(e.errorBody().string(), Map.class));
                    } catch (IOException var3) {
                        throw new RuntimeException(var3);
                    }
                }
            };
        }
    }

    static class MyTrustManager extends X509ExtendedTrustManager {
        MyTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class Builder {
        private String baseUrl;
        private Map<String, String> headers;
        private List<Interceptor> interceptors;
        private Integer connectTimeout;
        private Integer writeTimeout;
        private Integer readTimeout;
        private Integer retryTimes;
        private Integer timeBetweenRetry;
        private retrofit2.Converter.Factory factory;

        private Builder(String baseUrl) {
            this.headers = new HashMap();
            this.interceptors = new ArrayList();
            this.connectTimeout = Integer.valueOf(5000);
            this.writeTimeout = Integer.valueOf(5000);
            this.readTimeout = Integer.valueOf(10000);
            this.retryTimes = Integer.valueOf(0);
            this.timeBetweenRetry = Integer.valueOf(0);
            this.baseUrl = baseUrl;
        }

        private static okhttp3.OkHttpClient.Builder defaultClientBuilder() {
            SSLContext sslContext = null;

            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init((KeyManager[])null, new TrustManager[]{new BaseRetrofitUtils2.MyTrustManager()}, new SecureRandom());
            } catch (Throwable var2) {
                BaseRetrofitUtils2.log.error("初始化SSLContext异常", var2);
            }

            okhttp3.OkHttpClient.Builder clientBuilder = new okhttp3.OkHttpClient.Builder();
            if(sslContext != null) {
                clientBuilder.sslSocketFactory(sslContext.getSocketFactory());
            }

            clientBuilder.hostnameVerifier(new NoopHostnameVerifier());
            clientBuilder.connectionPool(new ConnectionPool());
            return clientBuilder;
        }

        public BaseRetrofitUtils2.Builder headers(Map<String, String> headers) {
            if(headers != null) {
                this.headers.putAll(headers);
            }

            return this;
        }

        public BaseRetrofitUtils2.Builder addInterceptors(Interceptor... interceptors) {
            if(interceptors != null) {
                Interceptor[] var2 = interceptors;
                int var3 = interceptors.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    Interceptor interceptor = var2[var4];
                    this.interceptors.add(interceptor);
                }
            }

            return this;
        }

        public BaseRetrofitUtils2.Builder connectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public BaseRetrofitUtils2.Builder writeTimeout(Integer writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public BaseRetrofitUtils2.Builder readTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public BaseRetrofitUtils2.Builder retryWhenTimeout(Integer retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public BaseRetrofitUtils2.Builder timeBetweenRetry(Integer timeBetweenRetry) {
            this.timeBetweenRetry = timeBetweenRetry;
            return this;
        }

        public BaseRetrofitUtils2.Builder addConverterFactory(retrofit2.Converter.Factory factory) {
            this.factory = factory;
            return this;
        }

        public Retrofit build() {
            okhttp3.OkHttpClient.Builder clientBuilder = defaultClientBuilder();
            if(this.headers != null && !this.headers.isEmpty()) {
                clientBuilder.addInterceptor((chain) -> {
                    okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
                    this.headers.entrySet().forEach((entry) -> {
                        requestBuilder.addHeader((String)entry.getKey(), (String)entry.getValue());
                    });
                    return chain.proceed(requestBuilder.build());
                });
            }

            if(CollectionUtils.isNotEmpty(this.interceptors)) {
                this.interceptors.forEach((interceptor) -> {
                    clientBuilder.addInterceptor(interceptor);
                });
            }

            clientBuilder.addInterceptor((chain) -> {
                Request request = chain.request();
                okhttp3.Response response = null;
                IOException ex = null;
                int tryCount = 0;
                boolean isTimeout = true;

                while(isTimeout && tryCount <= this.retryTimes.intValue()) {
                    try {
                        response = chain.proceed(request);
                        isTimeout = false;
                        ex = null;
                    } catch (IOException var11) {
                        ex = var11;
                        if(!(var11 instanceof SocketTimeoutException)) {
                            isTimeout = false;
                        } else if(tryCount < this.retryTimes.intValue()) {
                            BaseRetrofitUtils2.log.info("超时,第{}次重试...", Integer.valueOf(tryCount + 1));
                            request = request.newBuilder().build();
                            if(this.timeBetweenRetry.intValue() > 0) {
                                ThreadUtil.sleep((long)this.timeBetweenRetry.intValue());
                            }
                        }
                    } finally {
                        ++tryCount;
                    }
                }

                if(ex != null) {
                    if(ex instanceof SocketTimeoutException) {
                        BaseRetrofitUtils2.log.warn("调用第三方接口[{}]超时", request.url().toString());
                        throw ex;
                    } else {
                        BaseRetrofitUtils2.log.error("调用第三方接口[{}]异常:{}", request.url().toString(), ExceptionUtils.getStackTrace(ex));
                        throw new RetrofitException(Integer.valueOf(500), ex.getMessage());
                    }
                } else {
                    String body;
                    if(response != null && response.body() != null && response.body().contentLength() != 0L) {
                        if(response.code() >= 200 && response.code() < 300) {
                            return response;
                        } else {
                            body = response.body().string();
                            BaseRetrofitUtils2.log.warn("请求第三方api[{}]响应异常:{}", request.url().toString(), body);
                            IOUtils.closeQuietly(response);
                            throw new RetrofitException(Integer.valueOf(response.code()), body);
                        }
                    } else {
                        body = String.format("调用第三方接口[%s]响应为空", new Object[]{request.url().toString()});
                        throw new RetrofitException(Integer.valueOf(500), body);
                    }
                }
            });
            clientBuilder.connectTimeout((long)this.connectTimeout.intValue(), TimeUnit.MILLISECONDS).writeTimeout((long)this.writeTimeout.intValue(), TimeUnit.MILLISECONDS).readTimeout((long)this.readTimeout.intValue(), TimeUnit.MILLISECONDS);
            retrofit2.Retrofit.Builder builder = (new retrofit2.Retrofit.Builder()).baseUrl(this.baseUrl);
            if(this.factory != null) {
                builder.addConverterFactory(this.factory);
            }

            return builder.addConverterFactory(JacksonConverterFactory.create()).addCallAdapterFactory(BaseRetrofitUtils2.SynchronousCallAdapterFactory.create()).client(clientBuilder.build()).build();
        }
    }
}
