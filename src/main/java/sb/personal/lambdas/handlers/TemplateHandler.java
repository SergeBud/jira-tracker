package sb.personal.lambdas.handlers;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import sb.personal.lambdas.model.ApiVersion;
import sb.personal.lambdas.model.JiraTemplate;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Map;

public interface TemplateHandler {

    ApiVersion getType();

    void setLogger(LambdaLogger logger);

    void handle(JiraTemplate jiraTemplate, Map<DayOfWeek, ZonedDateTime> daysTobeLogged);

}
