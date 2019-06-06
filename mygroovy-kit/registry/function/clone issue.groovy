/**
 WARN! Create customField "Clone for"
 Text Field (single line)
 Replace XXXXX on your customfield ID


 - Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, transientVars, componentAccessor, baseUrl, cfValues (key - id as Long/cfName as String), cloneIssue (Additional issue actions)
 In templates, variables are specified as ${issue}.

 - Condition - script. If not specified, it is considered true. If returns true, then the issue will be clone.

 - Copy issue links - default true.

 - Copy issue attachments - default true.

 - Copy only fields - default copy all fields.
 Example: summary, 12345

 system fields - assignee, description, reporter..
 customField - 12345, ...
 - run as User - default currentUser
 */


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueFactory
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.AttachmentUtils
import org.apache.commons.io.FileUtils
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Copy issue links', type = ParamType.BOOLEAN, optional = true)
Boolean createCopyLinks

@WithParam(displayName = 'Copy issue attachments', type = ParamType.BOOLEAN, optional = true)
Boolean createCopyAttachments

@WithParam(displayName = 'Target issue project key', type = ParamType.STRING, optional = false)
String targetProjectKey

@WithParam(displayName = 'Target issue type id', type = ParamType.LONG, optional = false)
Long targetIssueTypeId

@WithParam(displayName = 'Copy only fields', type = ParamType.STRING, optional = true)
String copyFields

@WithParam(displayName = 'prefix summary', type = ParamType.STRING, optional = true)
String prefixSummary

@WithParam(displayName = 'run as User', type = ParamType.USER, optional = true)
ApplicationUser runAsUser

@WithParam(displayName = 'issue link type id', type = ParamType.LONG, optional = false)
Long issueLinkTypeId

@WithParam(displayName = 'Additional issue actions', type = ParamType.SCRIPT, optional = true)
ScriptParam additionalIssueActions

def cfValues = new CfValues(issue)
if (createCopyLinks == null) {
    createCopyLinks = true
}
if (createCopyAttachments == null) {
    createCopyAttachments = true
}
runAsUser = runAsUser ?: currentUser
def paramMap = [httpClient    : httpClient,
                templateEngine: templateEngine,
                log           : log,
                issue         : issue,
                runAsUser     : runAsUser,
                currentUser   : currentUser,
                transientVars : transientVars,
                cfValues      : cfValues,
]

//GLOBAL! we track that task is created from the CLONE ISSUE function
if (issue.getCustomFieldValue(getCustomFieldObject(XXXXX))) {//
    return
}
if (scriptCondition && (scriptCondition.runScript(paramMap) == null || scriptCondition.runScript(paramMap) as Boolean == false)) {
    return
}

def cloneIssue = copyIssue(issue, createCopyLinks, prefixSummary, createCopyAttachments, targetProjectKey, targetIssueTypeId as String, copyFields, runAsUser, issueLinkTypeId)

paramMap << [cloneIssue: cloneIssue]
if (additionalIssueActions) {
    additionalIssueActions.runScript(paramMap)
}

def copyIssue(issue, Boolean createCopyLinks, String prefixSummary, Boolean createCopyAttachments, String targetProjectKey, String targetIssueTypeId, String copyFields, ApplicationUser runAsUser, Long issueLinkTypeId) {
    def fields = []

    if (copyFields) {
        fields = copyFields.split(',').collect { it.trim() }.findAll { it }
    }

    def projectId = _.getProject(targetProjectKey)?.id

    def newIssue = createIssue(runAsUser, issue, prefixSummary, fields, projectId, targetIssueTypeId)

    if (createCopyLinks) {
        copyLinks(issue, newIssue, runAsUser)
    }
    if (createCopyAttachments) {
        copyAttachments(issue, newIssue, runAsUser)
    }
    _.createLink(issue, newIssue, issueLinkTypeId, 0, runAsUser)
    return newIssue
}

def copyAttachments(fromIssue, toIssue, user) {
    fromIssue.getAttachments().each { att ->
        File attFile = AttachmentUtils.getAttachmentFile(att)
        String filename = att.getFilename()
        String contentType = att.getMimetype()
        File newFile = new File(attFile.getAbsolutePath() + toIssue.getKey())
        FileUtils.copyFile(attFile, newFile)
        ComponentAccessor.getAttachmentManager().createAttachment(newFile, filename, contentType, user, toIssue);
    }
}

def copyLinks(fromIssue, toIssue, user) {
    def inwardLinks = _.getInwardLinks(fromIssue)
    inwardLinks.each { link ->
        createLink(link.getSourceObject(), toIssue, link.getLinkTypeId(), link.getSequence(), user)
    }
    def outwardLinks = _.getOutwardLinks(fromIssue)
    outwardLinks.each { link ->
        createLink(toIssue, link.getDestinationObject(), link.getLinkTypeId(), link.getSequence(), user)
    }
}

def createIssue(user, issue, String prefixSummary, List fields, Long projectId, String issueTypeId) {
    def mutableIssue = createCopyMutableIssue(user, issue, prefixSummary, fields, projectId, issueTypeId)
    ComponentAccessor.issueManager.createIssueObject(user, mutableIssue)
}

def createCopyMutableIssue(user, issue, String prefixSummary, List fields, Long projectId, String issueTypeId) {
    def issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class)
    def newIssue = fields ? issueFactory.getIssue() : issueFactory.cloneIssueWithAllFields(issue)
    newIssue.setCustomFieldValue(getCustomFieldObject(50200), issue.key)
    newIssue.projectId = projectId
    newIssue.issueTypeId = issueTypeId
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
    newIssue.summary = (prefixSummary ?: '') + newIssue.summary
    return newIssue
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