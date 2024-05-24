package cz.cuni.mff.image_search_api;

import static spark.Spark.*;

public class SwaggerConfiguration {
    public static void setupSwagger() {
        System.out.println("Setting up Swagger");

        // Serve the generated static files from the target/classes/swagger directory
        staticFiles.externalLocation("target/classes/swagger");

        // Redirect root to Swagger UI
        redirect.get("/", "/index.html");
        redirect.get("/swagger", "/index.html");

        System.out.println("Swagger initialized");
    }
}
