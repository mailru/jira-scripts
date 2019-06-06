/**
 WARN! Add in workflow after create issue.
 - Access variables in scripts - httpClient, cfValues, templateEngine, log, issue, currentUser, transientVars, componentAccessor.

 - Condition - script. If not specified, it is considered true. If returns true, then the start watching.

 - Users fields - assignee, reporter, projectLead, componentLead, customfield_XXXXX (user fields). Comma-separated (,).
 Example:
 assignee, customfield_10000

 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Current user', type = ParamType.BOOLEAN, optional = true)
Boolean addCurrentUser

@WithParam(displayName = 'Users', type = ParamType.MULTI_USER, optional = true)
List<ApplicationUser> users

@WithParam(displayName = 'Users fields', type = ParamType.TEXT, optional = true)
String fields

def cfValues = new CfValues(issue)

def paramMap = [httpClient       : httpClient,
                templateEngine   : templateEngine,
                log              : log,
                issue            : issue,
                currentUser      : currentUser,
                transientVars    : transientVars,
                cfValues         : cfValues,
                componentAccessor: ComponentAccessor
]
if (scriptCondition && (scriptCondition.runScript(paramMap) == false)) {
    return
}

if (addCurrentUser) {
    _.startWatching(currentUser, issue)
}

users.each {
    try {
        _.startWatching(it, issue)
    } catch (any) {
        throw new Exception("issue=${issue}, project=${issue.project}, error=${any}")
    }
}
if (fields) {
    getUsersFromStringWithFields(fields).each {
        _.startWatching(it, issue)
    }
}

def getUsersFromStringWithFields(String stringWithFields) {
    return stringWithFields.split(',').collect { return it.trim() }.collect { fieldName ->
        if (fieldName == 'reporter') {
            return issue.reporter
        } else if (fieldName == 'assignee') {
            return issue.assignee
        } else if (fieldName == 'projectLead') {
            return issue.getProjectObject().getProjectLead()
        } else if (fieldName == 'componentLead') {
            return issue.getComponents()*.getComponentLead()
        } else {
            def cf = ComponentAccessor.customFieldManager.getCustomFieldObject(fieldName)
            if (!cf) {
                return
            }
            def value = issue.getCustomFieldValue(cf)
            if (value instanceof ApplicationUser) {
                return value
            } else if (value instanceof Collection<ApplicationUser>) {
                return value
            }
        }
    }?.flatten().findAll { it }
}

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