package sb.personal.lambdas.handlers;

import sb.personal.lambdas.jira.JiraClient;
import sb.personal.lambdas.model.ApiVersion;
import sb.personal.lambdas.model.JiraTemplate;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Map;

public interface TemplateHandler {

    ApiVersion getType();

    void handle(JiraTemplate jiraTemplate, Map<DayOfWeek, ZonedDateTime> daysTobeLogged);

    JiraClient getJiraClient();
}
