import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.WithParam

@WithParam(type = ParamType.USER, displayName = "User")
ApplicationUser user

return user != currentUser
