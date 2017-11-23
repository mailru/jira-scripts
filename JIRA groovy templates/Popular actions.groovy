import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter

def issue = getIssue("KEY-1")
//def issues = getIssuesByJQL("project = TEST")
def user = getCurrentUser()
//def user = getUserByName('login')
//issue.reporter = user
//issue.assignee = user

//def cf = getCustomFieldObject(id){
//issue.setCustomFieldValue(cf, "")
//startWatching(user, issue)
//def cfValue = getCustomFieldValue(issue, id)

//update(user, issue)


def getIssue(String key) {
    ComponentAccessor.issueManager.getIssueObject(key)
}

def getUserByName(userName) {
    ComponentAccessor.userManager.getUserByName(userName)
}

def getCurrentUser() {
    ComponentAccessor.jiraAuthenticationContext?.user
}

def update(user, issue) {
    ComponentAccessor.issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

def getCustomFieldObject(Long fieldId) {
    ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)
}

def getCustomFieldValue(issue, Long fieldId) {
    ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)?.getValue(issue)
}

def startWatching(user, issue) {
    ComponentAccessor.watcherManager.startWatching(user, issue)
}

def getIssuesByJQL(String jql) {
    def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def searchProvider = ComponentAccessor.getComponent(SearchProvider)
    def issueManager = ComponentAccessor.getIssueManager()
    def user = getCurrentUser()

    def query = jqlQueryParser.parseQuery(jql)
    def result = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())
    def documentIssues = result.getIssues()

    return documentIssues.collect { documentIssue -> getIssue(documentIssue.id) }
}
