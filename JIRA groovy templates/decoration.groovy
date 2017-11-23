import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import groovy.transform.Synchronized
import groovy.transform.ToString

def issue = IssueAdvanceManager.getAdvanceIssue(getIssue('KEY-1'))
issue.setSummary('newSummary')

def getIssue(String key) {
    ComponentAccessor.issueManager.getIssueObject(key)
}

def class IssueAdvanceManager {
    @Synchronized
    def static getAdvanceIssue(Issue issue) {
        return new IssueAdvanceDecorator(issue)
    }
}

@ToString(includeNames = true, includeFields = true)
class IssueAdvanceDecorator {
    @Delegate
    final Issue issue

    IssueAdvanceDecorator(Issue issue) {
        this.issue = issue;
    }

    String getSummary() {
        issue.summary + issue.summary
    }

    def setSummary(String newSummary) {
        issue.with {
            summary = newSummary
            update(getCurrentUser(), it)
        }
    }

    def getCurrentUser() {
        ComponentAccessor.jiraAuthenticationContext?.user
    }

    def update(user, issue) {
        ComponentAccessor.issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)
    }
}
