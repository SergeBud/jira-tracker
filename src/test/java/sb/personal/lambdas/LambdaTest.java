package sb.personal.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(value = MockitoExtension.class)
public class LambdaTest {

    private final JiraFunction lambda = new JiraFunction();

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @Test
    public void shouldFillJiraTemplate() {
        Mockito.when(context.getLogger())
               .thenReturn(logger);
        Mockito.doNothing()
               .when(logger)
               .log(anyString());
        lambda.handle(context);
    }
}
