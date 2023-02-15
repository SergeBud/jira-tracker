package sb.personal.lambdas.handlers;

import sb.personal.lambdas.jira.JiraClient;
import sb.personal.lambdas.model.ApiVersion;

public class V2TemplateHandler extends AbstractTempateHandler {

    public V2TemplateHandler(JiraClient jiraClient) {
        super(jiraClient);
    }

    @Override
    public ApiVersion getType() {
        return ApiVersion.V2;
    }
}
