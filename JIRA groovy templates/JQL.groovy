import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter

//def jql = "assignee = currentUser()"
//runJQL(jql) == getIssuesByJQL(jql)

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

def runJQL(String jql) {
    def searchService = ComponentAccessor.getComponent(SearchService.class)
    def issueManager = ComponentAccessor.getIssueManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

    def parseJQLResult = searchService.parseQuery(user, jql)
    if (parseJQLResult.isValid()) {
        def result = searchService.search(user, parseJQLResult.getQuery(), PagerFilter.getUnlimitedFilter())
        def documentIssues = result.issues

        return documentIssues.collect { issueManager.getIssueObject(it.id) }
    } else {
        log.error("Invalid JQL: " + jql);
    }
    return issuesSearch
}
