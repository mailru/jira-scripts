/**
 Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, componentAccessor, baseUrl.
 runAsUser - default currentUser
 notify (email) - default true
 event id - from {baseUrl}/secure/admin/ListEventTypes.jspa
 */

import com.atlassian.event.api.EventPublisher
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'issueKey', type = ParamType.STRING)
String issueKey

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'run as user', type = ParamType.USER, optional = true)
ApplicationUser runAsUser

@WithParam(displayName = 'Event Id', type = ParamType.LONG)
Long eventId

Boolean notify
issue = ComponentAccessor.issueManager.getIssueObject(issueKey)

if (notify == null) {
    notify = true
}
if (runAsUser == null) {
    runAsUser = currentUser
}

def paramMap = [httpClient       : httpClient,
                templateEngine   : templateEngine,
                log              : log,
                issue            : issue,
                currentUser      : currentUser,
                componentAccessor: ComponentAccessor,
                baseUrl          : ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL),
]

if (scriptCondition && scriptCondition.runScript(paramMap) == false) {
    return
}

EventPublisher eventPublisher = ComponentAccessor.getComponent(EventPublisher)
if (!eventPublisher) {
    return
}
eventPublisher.publish(new IssueEvent(issue, [:], runAsUser, eventId, true))