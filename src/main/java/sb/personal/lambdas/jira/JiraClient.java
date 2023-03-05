package sb.personal.lambdas.jira;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import sb.personal.lambdas.model.ApiVersion;
import sb.personal.lambdas.model.JiraSettings;
import sb.personal.lambdas.model.LogWork;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

public interface JiraClient {

    ApiVersion getType();

    void setLogger(LambdaLogger logger);

    String getRestApiSuffix();

    void initSettings(JiraSettings jiraSettings, Collection<ZonedDateTime> values);

    void logDay(ZonedDateTime datetime, List<LogWork> logWorks);

    HttpResponse<JsonNode> getRequest(String url);

    HttpResponse<JsonNode> postRequest(String url, Object payload);

    ObjectNode createPayload(LogWork logWork, ZonedDateTime dateTime);
}
