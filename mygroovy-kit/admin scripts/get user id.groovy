import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.WithParam

@WithParam(displayName = "User", type = ParamType.USER)
ApplicationUser user

return user.id
