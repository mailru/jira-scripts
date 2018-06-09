import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.index.IssueIndexingService

def reIndex(issue){
  def issueIndexingService = ComponentAccessor.getComponent(IssueIndexingService)
  issueIndexingService.reIndex(issue)
}
