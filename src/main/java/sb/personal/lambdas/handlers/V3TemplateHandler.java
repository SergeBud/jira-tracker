package sb.personal.lambdas.handlers;

import sb.personal.lambdas.jira.JiraClient;
import sb.personal.lambdas.model.ApiVersion;

public class V3TemplateHandler extends AbstractTempateHandler {

    public V3TemplateHandler(JiraClient jiraClient) {
        super(jiraClient);
    }

    @Override
    public ApiVersion getType() {
        return ApiVersion.V3;
    }
}
