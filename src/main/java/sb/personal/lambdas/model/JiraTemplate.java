package sb.personal.lambdas.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JiraTemplate {
    private ApiVersion apiVersion;
    private Boolean enabled = false;
    private JiraSettings settings;
    private Map<DayOfWeek, List<LogWork>> template;
}
