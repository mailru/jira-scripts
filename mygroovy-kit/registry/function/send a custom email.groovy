/**
 - Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, componentAccessor, baseUrl, cfValues (key - id as Long/cfName as String)
 In templates, variables are specified as ${issue}.

 - Condition - script. If not specified, it is considered true. If returns true, then the message will be sent.

 - Init params for "templates" - script. Its result is available in templates "Email template" и "Subject template" through the "params" variable. Used to initialize additional variables in the template.
 example :
 return ['test variable1': 1] -> params['test variable1'] == 1

 - Email template - message body template.
 example :
 ${issue} - issue will be set params ${params['test variable1']}- Subject template - template for the message header.

 - Email format - default text / plain. You can specify the desired, for example text / html.

 - Send to emails - E-mail addresses to which you want to send a message are comma-separated (,).

 - Send to users - users to whom the message will be sent.

 - Send to issue fields - fields with users who need to send messages. Available values - assignee, reporter, projectLead, componentLead, customfield_XXXXX. Specified by comma (,).

 ! Sent to all 1 message! not separate copies.

 - Send copy to emails - E-mail addresses that need to be added to the copy are comma-separated (,).

 - Send copy to users - users who will be sent a copy of the message.

 - Send copy to issue fields - fields with users that you want to add to the copy.

 - from address - The address that will be specified as the sender of this message. The default is JIRA address.

 - Reply-to - the address to which the reply to the letter will be sent.
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Init params for "templates"', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptInit

@WithParam(displayName = 'Email template', type = ParamType.TEXT)
String emailTemplateString

@WithParam(displayName = 'Subject template', type = ParamType.TEXT)
String subjectTemplateString

@WithParam(displayName = 'Email format', type = ParamType.STRING, optional = true)
String emailFormat

@WithParam(displayName = 'Send to emails', type = ParamType.TEXT, optional = true)
String emailsString

@WithParam(displayName = 'Send to users', type = ParamType.MULTI_USER, optional = true)
List<ApplicationUser> sendToUsers

@WithParam(displayName = 'Send to issue fields', type = ParamType.TEXT, optional = true)
String toIssueFieldsString

@WithParam(displayName = 'Send copy to emails', type = ParamType.TEXT, optional = true)
String emailsCopyString

@WithParam(displayName = 'Send copy to users', type = ParamType.MULTI_USER, optional = true)
List<ApplicationUser> sendCopyToUsers

@WithParam(displayName = 'Send copy to issue fields', type = ParamType.TEXT, optional = true)
String toIssueFieldsCopyString

@WithParam(displayName = 'from address', type = ParamType.STRING, optional = true)
String from

@WithParam(displayName = 'Reply-to', type = ParamType.STRING, optional = true)
String replyTo

@WithParam(displayName = 'Send attachments', type = ParamType.BOOLEAN, optional = true)
Boolean sendAttachments

def cfValues = new CfValues(issue)

def paramMap = [httpClient       : httpClient,
                templateEngine   : templateEngine,
                log              : log,
                issue            : issue,
                currentUser      : currentUser,
                transientVars    : transientVars,
                componentAccessor: ComponentAccessor,
                cfValues         : cfValues,
                baseUrl          : ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL),
]

if (scriptCondition && (scriptCondition.runScript(paramMap) == false)) {
    return
}

if (scriptInit) {
    def initParams = scriptInit.runScript(paramMap)
    paramMap << [params: initParams]
}

String body = templateEngine.createTemplate(emailTemplateString).make(paramMap).toString()
String subject = templateEngine.createTemplate(subjectTemplateString).make(paramMap).toString()

if (!emailFormat) {
    emailFormat = 'text/plain'
}

def emailsTo = [] as Set
def copyTo = [] as Set
if (emailsString) {
    emailsTo = emailsTo + emailsString.split(',').collect { return it.trim() }
}
if (sendToUsers) {
    emailsTo = emailsTo + sendToUsers.findAll { it }.collect { return it.getEmailAddress() }
}

if (emailsCopyString) {
    copyTo = copyTo + emailsCopyString.split(',').collect { return it.trim() }
}
if (sendCopyToUsers) {
    copyTo = copyTo + sendCopyToUsers.findAll { it }.collect { return it.getEmailAddress() }
}


if (toIssueFieldsString) {
    emailsTo = emailsTo + getEmailFromStringWithFields(toIssueFieldsString)
}
if (toIssueFieldsCopyString) {

    copyTo = copyTo + getEmailFromStringWithFields(toIssueFieldsCopyString)
}

emailsTo = emailsTo.findAll { it && it != '' }
copyTo = copyTo.findAll { it && it != '' }

String stringEmailsTo = emailsTo.findAll { it }.toString().replace('[', '').replace(']', '')
String stringCopyTo = copyTo.findAll { it }.toString().replace('[', '').replace(']', '')

if (stringEmailsTo) {
    if(sendAttachments) {
        _.sendEmail(stringEmailsTo, stringCopyTo, subject, body, from, replyTo, emailFormat, issue.attachments)
    } else {
        _.sendEmail(stringEmailsTo, stringCopyTo, subject, body, from, replyTo, emailFormat)
    }
}

def getEmailFromStringWithFields(String stringWithFields) {
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
    }?.flatten().findAll { it && it?.emailAddress }.collect { return it.emailAddress }
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

