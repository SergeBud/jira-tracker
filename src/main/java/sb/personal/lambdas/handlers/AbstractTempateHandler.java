package sb.personal.lambdas.handlers;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.Getter;
import lombok.Setter;
import sb.personal.lambdas.jira.JiraClient;
import sb.personal.lambdas.model.JiraSettings;
import sb.personal.lambdas.model.JiraTemplate;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractTempateHandler implements TemplateHandler {

    @Getter
    @Setter
    private LambdaLogger logger;

    protected final JiraClient jiraClient;

    public AbstractTempateHandler(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    @Override
    public void handle(JiraTemplate jiraTemplate, Map<DayOfWeek, ZonedDateTime> daysTobeLogged) {
        JiraSettings jiraSettings = jiraTemplate.getSettings();
        if(Objects.isNull(jiraSettings)) {
            return;
        }

        jiraClient.initSettings(jiraSettings, daysTobeLogged.values());

        jiraTemplate.getTemplate()
                    .keySet()
                    .forEach(dayOfWeek -> {
                        var datetime = daysTobeLogged.get(dayOfWeek);
                        if(Objects.nonNull(datetime)) {
                            jiraClient.logDay(datetime,
                                              jiraTemplate.getTemplate()
                                                          .get(dayOfWeek));
                        }
                    });
    }
}
