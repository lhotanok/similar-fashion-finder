package org.example.image_search_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.brachtendorf.jimagehash.datastructures.tree.Result;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.DatasetBaseProduct;
import org.example.image_match.ImagesRetrieval;
import org.example.mongodb.ProductsRetrieval;
import spark.Request;
import spark.Response;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static spark.Spark.get;

@OpenAPIDefinition(
        info = @Info(
                title = "Image Matcher API",
                version = "1.0",
                description = "This API enables users to search for similar images based on the URL of the image.",
                license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT"),
                contact = @Contact(url = "https://github.com/lhotanok/similar-fashion-finder", name = "Kristýna Lhoťanová")
        )
)
@Tag(name = "Similar Products", description = "Fetching similar products based on the image URL.")
public class ImageSearchApi {
    private static final String URL_BASED_SEARCH_PATH = "/imageMatcher";
    private static final int MAX_PRODUCTS_IN_RESPONSE = 500;
    private static final String IMAGE_MATCH_ERROR_MESSAGE = "We are sorry for the inconvenience" +
            " but image matching failed. Please, try your request later.";

    private static final String PRODUCTS_RETRIEVAL_ERROR_MESSAGE = "We are sorry for the inconvenience" +
            " but matching products could not be retrieved. Please, try your request later.";
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

    @GET
    @Path(URL_BASED_SEARCH_PATH)
    @Operation(
            summary = "URL Based Image Search",
            parameters = {
                    @Parameter(
                            name = "imageUrl",
                            description = "The URL of the image to be used for search of similar images",
                            in = ParameterIn.QUERY,
                            required = true,
                            example = "https://www.mall.cz/i/108493875/1000/1000"
                    )
            },
            description = "Searches for similar images based on the provided image URL.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of matched image names",
                            content = @Content(schema = @Schema(
                                    type = "array"
                            ))
                    )
            }
    )
    public void setupUrlBasedSearchEndpoint() {
        System.out.println("Setting up API endpoint for URL based image search: " + URL_BASED_SEARCH_PATH);

        get(URL_BASED_SEARCH_PATH, (request, response) -> {
            response.type("application/json");

            String imageUrl = getDecodeImageUrl(request);

            PriorityQueue<Result<String>> matchedImages;
            try {
                matchedImages = imagesRetrieval.getMatchingImages(imageUrl);
            } catch (Exception e) {
                return reportServerError(e, response, IMAGE_MATCH_ERROR_MESSAGE);
            }

            System.out.printf("Extracted %d matching images%n", matchedImages.size());
            var matchedProductIds = parseMatchedProductIds(matchedImages);

            List<DatasetBaseProduct> products;
            try {
                products = productsRetrieval.fetchProducts(matchedProductIds);
            } catch (Exception e) {
                return reportServerError(e, response, PRODUCTS_RETRIEVAL_ERROR_MESSAGE);
            }

            try {
                var responseItems = buildResponseItems(products, matchedImages);
                return objectMapper.writeValueAsString(responseItems);
            } catch (Exception e) {
                return reportServerError(e, response, PRODUCTS_RETRIEVAL_ERROR_MESSAGE);
            }
        });
    }

    private static List<String> parseMatchedProductIds (PriorityQueue<Result<String>> matchedImages) {
        var matchedImageNames = matchedImages.stream()
                .map(result -> new File(result.value).getName());

        return matchedImageNames.map(
                fileName -> fileName.substring(0, fileName.lastIndexOf('.'))
        ).toList();
    }

    private static String getDecodeImageUrl (Request request) {
        String imageUrl = request.queryParams("imageUrl");

        String decodedImageUrl = decodeImageUrl(imageUrl);
        System.out.println("Searching similar images for imageUrl: " + decodedImageUrl);

        return decodedImageUrl;
    }

    private String reportServerError(Exception e, Response response, String message) throws JsonProcessingException {
        e.printStackTrace();
        response.status(500);

        return objectMapper.writeValueAsString(
                new Error(message)
        );
    }

    private static String decodeImageUrl(String imageUrl) {
        return URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
    }

    private static List<ProductMatch> buildResponseItems (
            List<? extends DatasetBaseProduct> products,
            PriorityQueue<Result<String>> matches
    ) {
        var matchedImages = matches.stream().toList();

        var responseProductsCount = Math.min(products.size(), MAX_PRODUCTS_IN_RESPONSE);

        var productsToReturn = products.subList(0, responseProductsCount);

        List<ProductMatch> responseItems = new ArrayList<>();

        for (int i = 0; i < productsToReturn.size(); i++) {
            var product = productsToReturn.get(i);
            var match = matchedImages.get(i);

            responseItems.add(
                    new ProductMatch(
                            match.distance,
                            match.normalizedHammingDistance,
                            product
                    )
            );
        }

        return responseItems;
    }
}
