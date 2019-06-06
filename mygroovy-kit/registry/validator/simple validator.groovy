import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT)
ScriptParam scriptCondition

@WithParam(displayName = 'Field name', type = ParamType.STRING, optional = true)
String fieldName

@WithParam(displayName = 'Message', type = ParamType.STRING)
String message

def cfValues = new CfValues(issue)

def paramMap = [httpClient    : httpClient,
                templateEngine: templateEngine,
                log           : log,
                issue         : issue,
                currentUser   : currentUser,
                transientVars : transientVars,
                cfValues      : cfValues,
]

if (scriptCondition && scriptCondition.runScript(paramMap) == false) {
    if (fieldName) {
        throw new InvalidInputException(fieldName, message)
    } else {
        throw new InvalidInputException(message)
    }
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
