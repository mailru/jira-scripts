import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Users', type = ParamType.MULTI_USER)
List<ApplicationUser> users

return currentUser in users
