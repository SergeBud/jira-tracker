package sb.personal.lambdas.jira;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import sb.personal.lambdas.model.JiraSettings;
import sb.personal.lambdas.model.LogWork;
import sb.personal.lambdas.model.SearchRequest;
import sb.personal.lambdas.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractJiraClient implements JiraClient {

    @Getter
    @Setter
    private LambdaLogger logger;

    protected String jiraUrl;
    protected String username;
    protected String token;

    private Set<LocalDate> loggedDays;
    private LocalDate from;
    private LocalDate till;

    @Override
    public void initSettings(JiraSettings jiraSettings, Collection<ZonedDateTime> daysToLog) {
        this.jiraUrl = jiraSettings.getUrl();
        this.username = jiraSettings.getUsername();
        this.token = jiraSettings.getToken();

        var localDates = daysToLog.stream()
                                  .map(ZonedDateTime::toLocalDate)
                                  .collect(Collectors.toList());

        this.till = localDates.stream()
                              .max(LocalDate::compareTo)
                              .orElseThrow();

        this.from = localDates.stream()
                              .min(LocalDate::compareTo)
                              .orElseThrow();

        this.loggedDays = collectExistingLogs();
    }

    protected String getUrlPrefix() {
        return this.jiraUrl + getRestApiSuffix();
    }

    private Set<LocalDate> collectExistingLogs() {
        Set<LocalDate> result = new HashSet<>();

        var searchRequest = new SearchRequest();
        searchRequest.setJql(String.format(searchRequest.getJql(),
                                           username.contains("@") ?
                                                   username.replace("@", "\\u0040") :
                                                   username));

        var searchResponse = postRequest("/search", searchRequest);
        var issues = searchResponse.getBody()
                                   .getObject()
                                   .getJSONArray("issues");

        var issueNames = IntStream.range(0, issues.length())
                                  .mapToObj(issues::getJSONObject)
                                  .map(jsonObject -> jsonObject.getString("key"))
                                  .collect(Collectors.toSet());

        issueNames.forEach(issueName -> {
            try {
                var issueResp = getRequest("/issue/" + issueName + "/worklog");

                var worklogs = issueResp.getBody()
                                        .getObject()
                                        .getJSONArray("worklogs");

                var weekdates = IntStream.range(0, worklogs.length())
                                         .mapToObj(worklogs::getJSONObject)
                                         .filter(jsonObject -> jsonObject.has("started"))
                                         .map(jsonObject -> LocalDateTime.parse(jsonObject.getString("started"),
                                                                                DateUtil.OFFSET_DATE_TIME_FORMATTER)
                                                                         .toLocalDate())
                                         .filter(Objects::nonNull)
                                         .filter(this::filterDate)
                                         .collect(Collectors.toSet());

                System.out.println(weekdates);
                result.addAll(weekdates);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });

        return result;
    }

    @Override
    @SneakyThrows
    public void logDay(ZonedDateTime dateTime, List<LogWork> worklogsPerDay) {
        for(LogWork logWork : worklogsPerDay) {
            if(loggedDays.contains(dateTime.toLocalDate())) {
                logger.log(String.format("%s already has some worklogs for %s, check it manually",
                                         logWork.getIssue(),
                                         dateTime.toLocalDate()));
                continue;
            }

            HttpResponse<JsonNode> getIssueResponse = getRequest("/issue/" + logWork.getIssue());

            if(200 != getIssueResponse.getStatus()) {
                logger.log(String.format("Issue %s not found", logWork.getIssue()));
                continue;
            }

            var payload = createPayload(logWork, dateTime);

            HttpResponse<JsonNode> postResponse = postRequest("/issue/" + logWork.getIssue() + "/worklog", payload);

            if(200 != postResponse.getStatus() && 201 != postResponse.getStatus()) {
                var errors = postResponse.getBody()
                                         .getObject()
                                         .getJSONObject("errors");
                for(String key : errors.keySet()) {
                    logger.log("\n Errors: \n");
                    logger.log(String.format("%s: %s \n", key, errors.get(key)));
                }
            }
        }
    }

    private boolean filterDate(LocalDate localDate) {
        return (from.isEqual(localDate) || from.isBefore(localDate)) && (till.isEqual(localDate) || till.isAfter(
                localDate));
    }
}
