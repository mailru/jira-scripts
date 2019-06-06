import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.index.IssueIndexingService

@WithParam(displayName = 'issueKey', type = ParamType.STRING)
String issueKey

reIndex(ComponentAccessor.issueManager.getIssueObject(issueKey))

def reIndex(issue) {
    def issueIndexingService = ComponentAccessor.getComponent(IssueIndexingService)
    issueIndexingService.reIndex(issue)
    return 'Done'
}
