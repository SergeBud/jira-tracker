package sb.personal.lambdas.jira;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;
import sb.personal.lambdas.model.ApiVersion;
import sb.personal.lambdas.model.LogWork;
import sb.personal.lambdas.util.DateUtil;

import java.time.ZonedDateTime;

public class V2JiraClient extends AbstractJiraClient {
    @Override
    public ApiVersion getType() {
        return ApiVersion.V3;
    }

    @Override
    public String getRestApiSuffix() {
        return "/rest/api/2";
    }

    @Override
    @SneakyThrows
    public HttpResponse<JsonNode> getRequest(String urlSuffix) {
        return Unirest.get(getUrlPrefix() + urlSuffix)
                      .header("Accept", "application/json")
                      .header("Authorization", "Bearer " + token)
                      .asJson();
    }

    @Override
    @SneakyThrows
    public HttpResponse<JsonNode> postRequest(String urlSuffix, Object payload) {
        return Unirest.post(getUrlPrefix() + urlSuffix)
                      .header("Authorization", "Bearer " + token)
                      .header("Accept", "application/json")
                      .header("Content-Type", "application/json")
                      .body(payload)
                      .asJson();
    }


    /*
        curl --request POST \
              --url 'https://your-domain.atlassian.net/rest/api/2/issue/{issueIdOrKey}/worklog' \
              --user 'email@example.com:<api_token>' \
              --header 'Accept: application/json' \
              --header 'Content-Type: application/json' \
              --data '{
              "comment": "I did some work here.",
              "started": "2021-01-17T12:34:00.000+0000",
              "timeSpentSeconds": 12000,
              "visibility": {
                "identifier": "276f955c-63d7-42c8-9520-92d01dca0625",
                "type": "group"
              }
            }'
     */
    @Override
    public ObjectNode createPayload(LogWork logWork, ZonedDateTime dateTime) {
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
        payload.put("started", DateUtil.OFFSET_DATE_TIME_FORMATTER.format(dateTime));
        payload.put("timeSpent", logWork.getTime());
        payload.put("comment", logWork.getComment());
        return payload;
    }
}
