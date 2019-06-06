import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.WithParam

@WithParam(displayName = "Comment", type = ParamType.TEXT)
String comment

_.createComment(issue, userManager.getUserByName("systemuser"), comment, true)

