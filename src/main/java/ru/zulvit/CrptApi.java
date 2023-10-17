package ru.zulvit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrptApi {
    private static final Logger LOGGER = Logger.getLogger(CrptApi.class.getName());

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String SIGNATURE = "Signature";
    private static final String RATE_LIMIT_EXCEEDED_MESSAGE = "Rate limit exceeded, please try again later.";
    private static final String ERROR_SENDING_DOCUMENT = "Error sending document: ";
    private static final long NANO_CONVERSION_FACTOR = 1_000_000;

    private final String apiUrl;
    private final int requestLimit;
    private final long limitRefreshPeriod;
    private final AtomicInteger requestCount;
    private LocalDateTime nextRefreshTime;

    public CrptApi(String apiUrl, TimeUnit timeUnit, int requestLimit) {
        this.apiUrl = apiUrl;
        this.requestLimit = requestLimit;
        this.limitRefreshPeriod = timeUnit.toMillis(1);
        this.requestCount = new AtomicInteger(0);
        this.nextRefreshTime = calculateNextRefreshTime();
    }

    private LocalDateTime calculateNextRefreshTime() {
        return LocalDateTime.now().plusNanos(limitRefreshPeriod * NANO_CONVERSION_FACTOR);
    }

    private synchronized boolean canSendRequest() {
        if (requestCount.get() >= requestLimit) {
            if (LocalDateTime.now().isAfter(nextRefreshTime)) {
                requestCount.set(0);
                nextRefreshTime = calculateNextRefreshTime();
            } else {
                return false;
            }
        }
        return true;
    }

    public CompletableFuture<String> createDocument(Document document, String signature) {
        if (!canSendRequest()) {
            LOGGER.log(Level.WARNING, RATE_LIMIT_EXCEEDED_MESSAGE);
            return CompletableFuture.completedFuture(RATE_LIMIT_EXCEEDED_MESSAGE);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(document);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(SIGNATURE, signature)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        requestCount.incrementAndGet();
                        LOGGER.log(Level.INFO, "Response received. Status code: {0}", response.statusCode());
                        return "Response status code: " + response.statusCode() + " Body: " + response.body();
                    })
                    .exceptionally(e -> {
                        LOGGER.log(Level.SEVERE, ERROR_SENDING_DOCUMENT, e);
                        return ERROR_SENDING_DOCUMENT + e.getMessage();
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, ERROR_SENDING_DOCUMENT, e);
            return CompletableFuture.completedFuture(ERROR_SENDING_DOCUMENT + e.getMessage());
        }
    }
}

class Document {
    protected static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;
        @JsonProperty("certificate_document_date")
        private String certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("tnved_code")
        private String tnvedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("uitu_code")
        private String uituCode;

        public String getCertificateDocument() {
            return certificateDocument;
        }

        public void setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
        }

        public String getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public void setCertificateDocumentDate(String certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public void setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            if (isValidDate(productionDate)) {
                this.productionDate = productionDate;
            } else {
                throw new IllegalArgumentException("Invalid date format provided");
            }
        }

        public String getTnvedCode() {
            return tnvedCode;
        }

        public void setTnvedCode(String tnvedCode) {
            this.tnvedCode = tnvedCode;
        }

        public String getUitCode() {
            return uitCode;
        }

        public void setUitCode(String uitCode) {
            this.uitCode = uitCode;
        }

        public String getUituCode() {
            return uituCode;
        }

        public void setUituCode(String uituCode) {
            this.uituCode = uituCode;
        }

        private boolean isValidDate(String date) {
            return true; //todo тут можно было бы сделать проверку даты
        }
    }

    @JsonProperty("description")
    private String description;
    @JsonProperty("participant_inn")
    private String participantInn;
    @JsonProperty("doc_id")
    private String docId;
    @JsonProperty("doc_status")
    private String docStatus;
    @JsonProperty("doc_type")
    private String docType;
    @JsonProperty("import_request")
    private boolean importRequest;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    private String productionDate;
    @JsonProperty("production_type")
    private String productionType;
    @JsonProperty("products")
    private List<Product> products;
    @JsonProperty("reg_date")
    private String regDate;
    @JsonProperty("reg_number")
    private String regNumber;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParticipantInn() {
        return participantInn;
    }

    public void setParticipantInn(String participantInn) {
        if (isValidInn(participantInn)) {
            this.participantInn = participantInn;
        } else {
            throw new IllegalArgumentException("Invalid INN provided");
        }
    }

    private boolean isValidInn(String inn) {
        return true; //TODO тут можно было бы сделать проверку ИНН
    }

    private boolean isValidDate(String date) {
        return true; //TODO тут можно было бы сделать проверку даты
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(String docStatus) {
        this.docStatus = docStatus;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public boolean isImportRequest() {
        return importRequest;
    }

    public void setImportRequest(boolean importRequest) {
        this.importRequest = importRequest;
    }

    public String getOwnerInn() {
        return ownerInn;
    }

    public void setOwnerInn(String ownerInn) {
        this.ownerInn = ownerInn;
    }

    public String getProducerInn() {
        return producerInn;
    }

    public void setProducerInn(String producerInn) {
        this.producerInn = producerInn;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    public String getProductionType() {
        return productionType;
    }

    public void setProductionType(String productionType) {
        if ("OWN_PRODUCTION".equals(productionType) || "CONTRACT_PRODUCTION".equals(productionType)) {
            this.productionType = productionType;
        } else {
            throw new IllegalArgumentException("Invalid production type provided");
        }
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }
}