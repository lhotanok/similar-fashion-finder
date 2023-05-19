package org.example.image_search_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.image_match.ImagesRetrieval;
import org.example.mongodb.ProductsRetrieval;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import static spark.Spark.get;

public class ImageSearchApi {
    private static final String URL_BASED_SEARCH_PATH = "/imageMatcher";
    private final ImagesRetrieval imagesRetrieval;
    private final ProductsRetrieval productsRetrieval;
    private final ObjectMapper objectMapper;

    public ImageSearchApi(
            String mysqlUsername, String mysqlPassword, String mongoUsername, String mongoPassword
    ) throws SQLException {
        imagesRetrieval = new ImagesRetrieval(mysqlUsername, mysqlPassword);
        productsRetrieval = new ProductsRetrieval(mongoUsername, mongoPassword);
        objectMapper = new ObjectMapper();
    }

    @Operation(
            summary = "URL Based Image Search",
            parameters = {
                    @Parameter(
                            name = "imageUrl",
                            description = "The URL of the image to be used for search of similar images",
                            in = ParameterIn.QUERY,
                            required = true,
                            example = "https://img01.ztat.net/article/spp-media-p1/4d886b16c24641208f2f592f6bfb4208/50d0758fc3b840daa4e4ff4c35144371.jpg?imwidth=1800"
                    )
            },
            description = "Searches for similar images based on the provided image URL."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of matched image names",
            content = @Content(schema = @Schema(
                    type = "array",
                    example = "[\"image1.jpg\", \"image2.jpg\"]"
            ))
    )
    public void setupUrlBasedSearchEndpoint() {
        System.out.println("Setting up API endpoint for URL based image search: " + URL_BASED_SEARCH_PATH);

        get(URL_BASED_SEARCH_PATH, (request, response) -> {
            String imageUrl = request.queryParams("imageUrl");

            String decodedImageUrl = decodeImageUrl(imageUrl);
            System.out.println("Searching similar images for imageUrl: " + decodedImageUrl);

            var matchedImages = imagesRetrieval.getMatchingImages(decodedImageUrl);

            System.out.printf("Extracted %d matching images%n", matchedImages.size());

            var matchedImageNames = matchedImages.stream()
                    .map(result -> new File(result.value).getName());

            var matchedProductIds = matchedImageNames.map(
                    fileName -> fileName.substring(0, fileName.lastIndexOf('.'))
            );

            var products = productsRetrieval.fetchProducts(matchedProductIds.toList());

            response.type("application/json");

            return objectMapper.writeValueAsString(products);
        });
    }

    private static String decodeImageUrl(String imageUrl) {
        return URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
    }
}
