package org.example.image_search_api;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import static spark.Spark.*;

public class SwaggerConfiguration {
    public static void setupSwagger() {
        OpenAPI openApi = new OpenAPI()
                .info(new Info().title("Image Matcher API").version("1.0"));

        System.out.println("Setting up Swagger: " + openApi.getOpenapi());

        staticFiles.location("/swagger");
        redirect.get("/", "/swagger/index.html");
        redirect.get("/swagger", "/swagger/index.html");

        setupSwaggerJsonEndpoint(openApi);
        setupSwaggerUiEndpoint(openApi);

        System.out.println("Swagger initialized");
    }
    private static void setupSwaggerJsonEndpoint(OpenAPI openApi) {
        get("/swagger.json", (request, response) -> {
            response.type("application/json");
            return openApi;
        });
    }

    private static void setupSwaggerUiEndpoint(OpenAPI openApi) {
        get("/swagger/*", (request, response) -> {
            OpenApiResource openApiResource = new OpenApiResource();

            openApiResource.setOpenApiConfiguration(
                    new io.swagger.v3.oas.integration.SwaggerConfiguration().openAPI(openApi)
            );

            return openApiResource;
        });
    }
}
