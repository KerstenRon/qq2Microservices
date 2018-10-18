package service.requestlogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.ResponseInterceptor;

public class ResponseLoggerInterceptor implements ResponseInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoogerInterceptor.class);

    @Override
    public boolean interceptResponse (Request request, Response response) throws Exception {

        //log.info("Logging HTTP response { Headers: {}, Properties: {},  Status: {} }", response.getHeaders().getLanguage(), response.getProperties(), response.getStatusCode());
        //log.info(response.getProperties().toString());
        //log.info(response.getHeaders().toString());
        //log.info("" + response.getProperties());
        //log.info("" + response.getHeaders());

        /*String propertyName = "SampleProperty";

        String property = "WSO2-2017";

        request.setProperty(propertyName, property);

        log.info("Property {} with value {} set to request", propertyName, property);*/

        return true;

    }
}
