import com.atlassian.jira.component.ComponentAccessor;

def createComment(issue, user, text) {
    def commentManager = ComponentAccessor.getCommentManager();
    commentManager.create(issue, user, text, false);
    return issue
}

def deleteCommentsForIssue(issue) {
    def commentManager = ComponentAccessor.getCommentManager();
    commentManager.deleteCommentsForIssue(issue)
}
