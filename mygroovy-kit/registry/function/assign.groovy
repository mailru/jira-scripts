/**
 * Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, transientVars, componentAccessor.

 Condition - скрипт. Если не указан, то считается true. Eсли возвращает true, то сообщение отправляется.
 Users fields - поля с юзерами, которым нужно отправить сообщения. Доступны значения - assignee, reporter, projectLead, componentLead, customfield_XXXXX. Указываются через запятую.
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Current user', type = ParamType.BOOLEAN, optional = true)
Boolean addCurrentUser

@WithParam(displayName = 'New assignee', type = ParamType.USER, optional = true)
ApplicationUser newAssignee


def paramMap = [httpClient       : httpClient,
                templateEngine   : templateEngine,
                log              : log,
                issue            : issue,
                currentUser      : currentUser,
                transientVars    : transientVars,
                componentAccessor: ComponentAccessor
]
if (scriptCondition && scriptCondition.runScript(paramMap) == false) {
    return
}

if (addCurrentUser) {
    issue.assignee = currentUser
} else {
    issue.assignee = newAssignee
}

