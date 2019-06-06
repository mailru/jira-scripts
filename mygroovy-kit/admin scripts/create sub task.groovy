/**
 - Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, runAsUser, transientVars, cfValues (key - id as Long/cfName as String), subTask (Additional issue actions)

 - Summary - if empty inherits from parent

 - Copy only fields - default copy all fields.
 Example: summary, 12345

 system fields - assignee, description, reporter..
 customField - 12345, ...

 - run as User - default currentUser
 */
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueFactory
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Issue key', type = ParamType.STRING, optional = false)
String issueKey
issue = ComponentAccessor.issueManager.getIssueObject(issueKey)

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Target issue type id', type = ParamType.LONG, optional = false)
Long targetIssueTypeId

@WithParam(displayName = 'Copy only fields', type = ParamType.STRING, optional = true)
String copyFields

@WithParam(displayName = 'prefix summary', type = ParamType.STRING, optional = true)
String prefixSummary

@WithParam(displayName = 'Summary', type = ParamType.STRING, optional = true)
String summary

@WithParam(displayName = 'run as User', type = ParamType.USER, optional = true)
ApplicationUser runAsUser

@WithParam(displayName = 'Additional issue actions', type = ParamType.SCRIPT, optional = true)
ScriptParam additionalIssueActions


def cfValues = new CfValues(issue)
runAsUser = runAsUser ?: currentUser

def paramMap = [httpClient    : httpClient,
                templateEngine: templateEngine,
                log           : log,
                issue         : issue,
                runAsUser     : runAsUser,
                currentUser   : currentUser,
                cfValues      : cfValues,
]
if (scriptCondition && (scriptCondition.runScript(paramMap) == null || scriptCondition.runScript(paramMap) as Boolean == false)) {
    return
}
def subTask = createSubIssue(runAsUser, issue, targetIssueTypeId.toString(), prefixSummary, summary, copyFields)

paramMap << [subTask: subTask]
if (additionalIssueActions) {
    additionalIssueActions.runScript(paramMap)

}

def createMutableIssue(ApplicationUser user, perrentIssue, String issueTypeId, String summary, List fields) {
    def issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class)
    def newIssue = fields ? issueFactory.getIssue() : issueFactory.cloneIssueWithAllFields(issue)

    if (fields) {
        fields.each { fieldName ->
            if (newIssue.hasProperty(fieldName)) {
                newIssue."${fieldName}" = issue."${fieldName}"
            } else {
                def cf = getCustomFieldObject(fieldName as Long)
                newIssue.setCustomFieldValue(cf, issue.getCustomFieldValue(cf))
            }
        }
        if (!newIssue.reporter) {
            newIssue.reporter = user
        }
    }

    newIssue.projectId = perrentIssue.project.id
    newIssue.issueTypeId = issueTypeId
    newIssue.summary = summary
    newIssue.reporter = user

    return newIssue
}

def createSubIssue(ApplicationUser user, perrentIssue, String issueTypeId, String prefixSummary, String summary, String copyFields) {

    def fields = []
    if (copyFields) {
        fields = copyFields.split(',').collect { it.trim() }.findAll { it }
    }
    if (!summary) {
        summary = perrentIssue.summary
    }
    if (prefixSummary) {
        summary = prefixSummary + summary
    }

    def mutableSubIssue = createMutableIssue(user, perrentIssue, issueTypeId, summary, fields)
    mutableSubIssue.setParentObject(perrentIssue)
    def subTask = ComponentAccessor.issueManager.createIssueObject(user, mutableSubIssue)
    ComponentAccessor.subTaskManager.createSubTaskIssueLink(perrentIssue, mutableSubIssue, user)
    return subTask
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
 