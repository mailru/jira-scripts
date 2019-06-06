import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.issue.search.SearchQuery
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.IssueIdCollector

def getIssuesByJQL(String jql) {
    def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def searchProvider = ComponentAccessor.getComponent(SearchProvider)
    def issueManager = ComponentAccessor.getIssueManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

    def query = jqlQueryParser.parseQuery(jql)
    SearchQuery searchQuery = SearchQuery.create(query, user)
    IssueIdCollector collector = new IssueIdCollector()
    searchProvider.search(searchQuery, collector)
    return collector.getIssueIds().collect { getIssue(it as Long) }
}

def getIssue(Long id) {
    ComponentAccessor.issueManager.getIssueObject(id)
}