/**
 - WARN! In the workflow, set the function after changing the status!

 - Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, runAsUser, transientVars, cfValues (key - id as Long/cfName as String)

 - run as User - default currentUser
 */
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Action id', type = ParamType.LONG)
Long actionId

@WithParam(displayName = 'Additional issue actions', type = ParamType.SCRIPT, optional = true)
ScriptParam additionalIssueActions

@WithParam(displayName = 'run as User', type = ParamType.USER, optional = true)
ApplicationUser runAsUser

runAsUser = runAsUser ?: currentUser

def cfValues = new CfValues(issue)

def paramMap = [httpClient    : httpClient,
                templateEngine: templateEngine,
                log           : log,
                issue         : issue,
                runAsUser     : runAsUser,
                currentUser   : currentUser,
                transientVars : transientVars,
                cfValues      : cfValues,
]

if (scriptCondition && (scriptCondition.runScript(paramMap) == false)) {
    return
}

def result = _.doTransition(issue, actionId as int, runAsUser)
if (result) {
    if (additionalIssueActions) {
        additionalIssueActions.runScript(paramMap)
    }
}

class CfValues {
    private issue

    CfValues(issue) {
        this.issue = issue
    }

    def getAt(String fieldName) {
        def cfs = _.getCustomFieldObjectsByName(fieldName)
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
        def cf = _.getCustomFieldObject(fieldId)
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