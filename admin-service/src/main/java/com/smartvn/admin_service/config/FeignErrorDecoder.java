// File: sangvirgo/microservice-smartvn/microservice-smartVN-364f1d91a647f9cfe5f909a583db8e7c1eebd55d/admin-service/src/main/java/com/smartvn/admin_service/config/FeignErrorDecoder.java

package com.smartvn.admin_service.config;

// ... (các import khác giữ nguyên)
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartvn.admin_service.dto.response.ApiResponse;
import com.smartvn.admin_service.exceptions.AppException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        String generalErrorMessage = String.format("Error calling %s: Status %d", methodKey, response.status());
        String specificErrorMessage = extractErrorMessageFromBody(response);

        // ✅ Ưu tiên thông báo lỗi cụ thể từ service gốc nếu có
        String messageToUse = specificErrorMessage != null ? specificErrorMessage : generalErrorMessage;

        log.error("Feign client error. Method: {}, Status: {}, Message: {}", methodKey, response.status(), messageToUse);

        // Ném AppException với HttpStatus tương ứng và thông báo lỗi đã được xử lý
        switch (status) {
            case NOT_FOUND:
                return new AppException(messageToUse, HttpStatus.NOT_FOUND);
            case BAD_REQUEST:
                return new AppException(messageToUse, HttpStatus.BAD_REQUEST);
            case UNAUTHORIZED:
            case FORBIDDEN:
                return new AppException(messageToUse, status);
            case INTERNAL_SERVER_ERROR:
                // Vẫn nên giữ thông tin về service gốc khi lỗi 500
                String internalErrorMsg = "Lỗi hệ thống từ " + methodKey.split("#")[0] + (specificErrorMessage != null ? ": " + specificErrorMessage : "");
                return new AppException(internalErrorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
            case SERVICE_UNAVAILABLE:
                return new AppException(messageToUse, HttpStatus.SERVICE_UNAVAILABLE);
            default:
                log.warn("Unhandled Feign error status {} for method {}. Using default decoder and wrapping in AppException.", status, methodKey);
                return new AppException(messageToUse, status); // Ném AppException cho các lỗi khác
        }
    }

    // Phương thức extractErrorMessageFromBody giữ nguyên như trước
    private String extractErrorMessageFromBody(Response response) {
        if (response.body() != null) {
            try (InputStream bodyIs = response.body().asInputStream()) {
                byte[] bodyBytes = bodyIs.readAllBytes();
                if (bodyBytes.length == 0) return null;

                try {
                    ApiResponse<?> apiResponse = objectMapper.readValue(bodyBytes, ApiResponse.class);
                    if (apiResponse != null && apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty()) {
                        return apiResponse.getMessage();
                    }
                } catch (IOException parseException) {
                    String bodyString = new String(bodyBytes);
                    log.warn("Failed to parse Feign error body as ApiResponse for method {}. Body: {}", response.request().requestTemplate().methodMetadata().configKey(), bodyString, parseException);
                    if (!bodyString.isEmpty() && bodyString.length() < 500) {
                        // Cố gắng loại bỏ tiền tố nếu body là text thuần
                        if(bodyString.contains("Runtime Error:")){
                            return bodyString.substring(bodyString.indexOf("Runtime Error:") + "Runtime Error:".length()).trim();
                        }
                        return "Received error: " + bodyString;
                    }
                }
            } catch (IOException readException) {
                log.error("Failed to read error response body from Feign client", readException);
            }
        }
        return null;
    }
}