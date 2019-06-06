import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.index.IssueIndexingService
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'JQL', type = ParamType.STRING)
String jql

reIndex(getIssuesByJQL(jql))

def reIndex(issues) {
    def issueIndexingService = ComponentAccessor.getComponent(IssueIndexingService)
    issueIndexingService.reIndexIssueObjects(issues)
    return "DONE issues - ${issues.size()}"
}

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