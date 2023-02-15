package sb.personal.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;
import sb.personal.lambdas.handlers.V3TemplateHandler;
import sb.personal.lambdas.handlers.V2TemplateHandler;
import sb.personal.lambdas.handlers.TemplateHandler;
import sb.personal.lambdas.jira.JiraClient;
import sb.personal.lambdas.jira.V3JiraClient;
import sb.personal.lambdas.jira.V2JiraClient;
import sb.personal.lambdas.model.ApiVersion;
import sb.personal.lambdas.model.JiraTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JiraFunction {

    private static final String BUCKET_NAME = "jira-template-files";

    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final Gson mapper = new Gson();
    private final Map<ApiVersion, TemplateHandler> settingHandlers;

    {
        JiraClient v3JiraClient = new V3JiraClient();
        JiraClient v2JiraClient = new V2JiraClient();
        TemplateHandler piesoftTemplateHandler = new V3TemplateHandler(v3JiraClient);
        TemplateHandler scnsoftTemplateHandler = new V2TemplateHandler(v2JiraClient);
        settingHandlers = Map.of(piesoftTemplateHandler.getType(),
                                 piesoftTemplateHandler,
                                 scnsoftTemplateHandler.getType(),
                                 scnsoftTemplateHandler);
    }

    static {
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            private final ObjectMapper jacksonObjectMapper = new ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch(JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void handle(Context context) {
        var logger = context.getLogger();
        settingHandlers.values()
                       .forEach(templateHandler -> templateHandler.setLogger(logger));

        var listing = s3.listObjectsV2(BUCKET_NAME);

        logger.log("\n");
        logger.log("Prefixes:\n");
        for(String commonPrefix : listing.getCommonPrefixes()) {
            logger.log(commonPrefix + "\n");
        }

        logger.log("\n");
        logger.log("Files:\n");
        for(S3ObjectSummary summary : listing.getObjectSummaries()) {
            String fileName = summary.getKey();
            try {
                handleFile(fileName, logger);
            } catch(RuntimeException ex) {
                logger.log(String.format("Error while logging file %s \n", fileName));
                logger.log(String.format("Cause: %s \n", ex.getMessage()));
                System.out.println("Cause: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        logger.log("\n");
    }

    @SneakyThrows
    private void handleFile(String fileName, LambdaLogger logger) {
        var jiraTemplate = mapper.fromJson(new InputStreamReader(s3.getObject(BUCKET_NAME, fileName)
                                                                   .getObjectContent()), JiraTemplate.class);

        if(!jiraTemplate.getEnabled()) {
            logger.log(fileName + "is disabled \n");
            return;
        }

        final var daysTobeLogged = calculateWorklogWeek();

        Optional.ofNullable(settingHandlers.get(jiraTemplate.getApiVersion()))
                .orElseThrow(() -> new RuntimeException(String.format("There is no handler for %s",
                                                                      jiraTemplate.getApiVersion())))
                .handle(jiraTemplate, daysTobeLogged);
    }

    private Map<DayOfWeek, ZonedDateTime> calculateWorklogWeek() {
        var adjustToPrevMonday = TemporalAdjusters.previous(DayOfWeek.MONDAY);

        var zoneId = ZoneId.systemDefault();
        var now = LocalDate.now(zoneId);
        var monday = now.with(adjustToPrevMonday);
        var sunday = monday.plusWeeks(1);

        return monday.datesUntil(sunday)
                     .map(it -> it.atTime(LocalTime.NOON)
                                  .atZone(zoneId))
                     .collect(Collectors.toMap(ZonedDateTime::getDayOfWeek, value -> value));
    }

}
