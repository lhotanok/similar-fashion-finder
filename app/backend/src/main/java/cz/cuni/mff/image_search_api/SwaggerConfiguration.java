package cz.cuni.mff.image_search_api;

import static spark.Spark.*;

/**
 * This class configures the Swagger UI for the API documentation.
 */
public class SwaggerConfiguration {
    /**
     * Sets up the Swagger UI for the API documentation.
     * The Swagger UI is served from the generated static files in the target/classes/swagger directory.
     * The root path is redirected to the Swagger UI, as well as the /swagger path.
     */
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
