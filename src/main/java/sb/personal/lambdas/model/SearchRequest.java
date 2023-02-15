package sb.personal.lambdas.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchRequest {

    private String jql = "assignee = %s AND worklogDate >= startOfWeek() AND worklogDate <= endOfWeek()";
    private int maxResults = 100;
    private List<String> fields = List.of("id", "key", "summary");
}
