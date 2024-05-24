package cz.cuni.mff.image_search_api;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

public class CorsFilter {

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    public static void apply() {
        Filter filter = (Request request, Response response) -> {
            response.header(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.header(ACCESS_CONTROL_REQUEST_METHOD, "GET, POST, PUT, DELETE, OPTIONS");
            response.header(ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization, X-Requested-With, Content-Length, Accept, Origin");
            response.header(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
            response.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        };
        Spark.after(filter);
    }
}

