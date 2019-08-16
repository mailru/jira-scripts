import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.util.AttachmentUtils
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.atlassian.jira.issue.IssueFactory
import com.atlassian.jira.util.JiraUtils
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl
import com.atlassian.jira.workflow.TransitionOptions
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter

import ru.mail.jira.plugins.calendar.service.CalendarService
import ru.mail.jira.plugins.calendar.service.CustomEventService//plugin-package

import javax.ws.rs.core.Response

def result = [:]

//TEST WithPlugin
@WithPlugin("ru.mail.jira.plugins.mailrucal")//plugin-key
@PluginModule
CustomEventService customEventService
@PluginModule
CalendarService calendarService

assert customEventService != null
assert calendarService != null
result << [withPlugin : true]

def user = ComponentAccessor.userManager.getUserByName('test')//currentUser
//TEST JSON
projectKey = "JIRATEST"
summary = "Test ${new Date()}"
def textJSON = """{
	"projectKey": "${projectKey}",
	"reporter":"${currentUser.key}",
	"issueTypeId": "173",
	"summary": "${summary}",
	"priorityId": "3",
}
"""

def jsonFromText = parseText(textJSON)

assert jsonFromText.projectKey == projectKey
result << [jsonParse: true]

//TEST CREATE ISSUE
def issue = craeteIssue(jsonFromText, user)
assert issue != null
result << [createIssue: true]

//TEST CONDITION only assignee
int actionId = 21
assert isTransitionCondition(issue, actionId, user) == false
result << [condition: true]

//TEST UPDATE
assert issue.assignee == null
setAssignee(issue, user)
assert issue.assignee == user
result << [update: true]

assert isTransitionCondition(issue, actionId, user) == true

//TEST VALIDATION - need description
def statusId = issue.getStatusObject().id
assert doTransition(issue, actionId, user) == false
assert statusId == issue.getStatusObject().id
result << [validation: true]

def description = "TEST DESCRIPTION ${new Date()}"
setDescription(issue, description, user)
//TEST TRANSITION
assert doTransition(issue, actionId, user) == true
issue = ComponentAccessor.issueManager.getIssueObject(issue.key)
assert statusId != issue.getStatusObject().id
result << [transition: true]

//TEST JYTHON
assert issue.summary == summary + summary
result << [JYTHON: true]

//TEST JSU
assert issue.dueDate != null
result << [JSU: true]

//TEST REST
def baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
def userName = "test"
def password = "test"

assert getIssue(baseUrl, userName, password, issue.key).id.toString() == issue.id.toString()
result << [rest: true]

//TEST JQL
def jql = "key = ${issue.key}"
def jqlResult = getIssuesByJQL(jql)
assert jqlResult.size() == 1
assert jqlResult.first().id == issue.id
result << [jql: true]


if(result.values().every{it == true}){
    return Response.ok("issue = ${issue} the tests were successful ${result}").build();
}

return Response.status(500).header('tests', "${result}").build()

def getIssuesByJQL(String jql) {
    def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def searchProvider = ComponentAccessor.getComponent(SearchProvider)
    def issueManager = ComponentAccessor.getIssueManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

    def query = jqlQueryParser.parseQuery(jql)
    def result = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())
    def documentIssues = result.getIssues()

    return documentIssues.collect { documentIssue -> issueManager.getIssueObject(documentIssue.id) }
}



def getIssue(String baseUrl,
             String userName,
             String password,
             String issueKey) {
    def authString = ("${userName}:${password}").getBytes().encodeBase64().toString()
    def connection = ("${baseUrl}/rest/api/2/issue/${issueKey}").toURL().openConnection()
    connection.addRequestProperty('Authorization', 'Basic ' + authString)
    connection.addRequestProperty('Content-Type', 'application/json')
    connection.setRequestMethod('GET')
    connection.setReadTimeout(30000)
    try {
        connection.connect()
        def line = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine()
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(line)

    } finally {
        connection.disconnect();
    }
}

def setDescription(issue, description, user){
    issue.description = description
    update(user, issue)
}

def doTransition(issue, int actionId, user){
    def builder = new TransitionOptions.Builder()
    def transitionOptions = builder.build()
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters();
    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters, transitionOptions);
    if (transitionValidationResult.isValid()) {
        return issueService.transition(user, transitionValidationResult).isValid()
    } else {
        return false
    }
}

def setAssignee(issue, user){
    issue.assignee = user
    update(user, issue)
}

def update(user, issue) {
    ComponentAccessor.issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

def isTransitionCondition(issue, int actionId, user){
    def builder = new TransitionOptions.Builder()
    def transitionOptions = builder.build()
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters();
    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters, transitionOptions);
    return transitionValidationResult.isValid()


}

def craeteIssue(json, user){
    def issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class)
    def newIssue = issueFactory.getIssue()

    newIssue.projectId = ComponentAccessor.projectManager.getProjectObjByKey(json.projectKey).id
    newIssue.issueTypeId = json.issueTypeId
    newIssue.summary = json.summary
    newIssue.reporterId = json.reporter

    newIssue.priorityId = json.priorityId

    ComponentAccessor.issueManager.createIssueObject(user, newIssue)
}


def parseText(text) {
    def jsonSlurper = new JsonSlurper()
    return jsonSlurper.parseText(text)
}

def toJson(obj) {
    JsonOutput.toJson(obj)
}