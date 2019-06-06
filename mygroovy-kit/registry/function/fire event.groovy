/**
 - Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, transientVars, componentAccessor, baseUrl.

 - runAsUser - default currentUser

 - notify (email) - default true

 - event id - from {baseUrl}/secure/admin/ListEventTypes.jspa
 */

import com.atlassian.event.api.EventPublisher
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'run as user', type = ParamType.USER, optional = true)
ApplicationUser runAsUser

@WithParam(displayName = 'Event Id', type = ParamType.LONG)
Long eventId

@WithParam(displayName = 'notify', type = ParamType.BOOLEAN)
Boolean notify

if (runAsUser == null) {
    runAsUser = currentUser
}

def cfValues = new CfValues(issue)

def paramMap = [httpClient       : httpClient,
                templateEngine   : templateEngine,
                log              : log,
                issue            : issue,
                currentUser      : currentUser,
                transientVars    : transientVars,
                componentAccessor: ComponentAccessor,
                baseUrl          : ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL),
                cfValues         : cfValues,
]

if (scriptCondition && (scriptCondition.runScript(paramMap) == null || scriptCondition.runScript(paramMap) as Boolean == false)) {
    return
}

EventPublisher eventPublisher = ComponentAccessor.getComponent(EventPublisher)
if (!eventPublisher) {
    return
}
eventPublisher.publish(new IssueEvent(issue, [:], runAsUser, eventId, notify))


class CfValues {
    private issue
    private customFieldManager = ComponentAccessor.customFieldManager

    CfValues(issue) {
        this.issue = issue
    }

    def getAt(String fieldName) {
        def cfs = customFieldManager.getCustomFieldObjectsByName(fieldName)
        def value
        if (cfs) {
            for (def cf in cfs) {
                value = getCustomFieldValue(cf)
                if (value) {
                    return value
                }
            }
        }
    }

    def getAt(Long fieldId) {
        def cf = customFieldManager.getCustomFieldObject(fieldId)
        if (cf) {
            return getCustomFieldValue(cf)
        }
    }

    def getCustomFieldValue(cf) {
        def value = this.issue.getCustomFieldValue(cf)
        if (value && value instanceof Collection) {
            value = value.sort()
        }
        return value
    }
}