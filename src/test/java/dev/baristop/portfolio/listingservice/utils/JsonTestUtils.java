package dev.baristop.portfolio.listingservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.test.web.servlet.MvcResult;

public class JsonTestUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonTestUtils() {
        // prevent instantiation
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pretty-print a JSON string to the console for debugging purposes.
     */
    public static void printJson(String json) {
        printJson(json, null);
    }

    public static void printJson(MvcResult result) {
        printJson(result, null);
    }

    public static void printJson(MvcResult result, String message) {
        try {
            String json = result.getResponse().getContentAsString();
            printJson(json, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get response content", e);
        }
    }

    public static void printJson(String json, String message) {
        try {
            Object obj = mapper.readValue(json, Object.class);
            String pretty = mapper.writeValueAsString(obj);

            String header = "### Response JSON";
            if (message != null && !message.isBlank()) {
                header += " (" + message + ")";
            }

            System.out.println(header + "\n" + pretty);
        } catch (Exception e) {
            throw new RuntimeException("Failed to pretty-print JSON", e);
        }
    }
}
