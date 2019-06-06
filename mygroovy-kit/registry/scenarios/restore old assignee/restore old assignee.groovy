import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.user.ApplicationUser

//THIS IS RESTORE OLD ASSIGNEE SCRIPT
long FIELD_ID = 10200

//get old assignee value
ApplicationUser oldAssignee = _.gcfv(issue, FIELD_ID) as ApplicationUser

if (oldAssignee) {
    //set field value
    issue.setAssignee(oldAssignee)
}