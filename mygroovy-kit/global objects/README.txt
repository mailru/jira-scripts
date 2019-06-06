global objects(go): classes defined by jira-administrators. T
hese classes will be available in all my-groovy scripts through the variable - "script name"

Add scripts in the interface by reference:
{baseUrl}/plugins/servlet/my-groovy/go

The class collected methods from https://github.com/mailru/jira-scripts/tree/master/JIRA%20groovy%20templates
Follow the link {baseUrl}/plugins/servlet/my-groovy/go and copy to the script _.groovy
Name the new script in Jira "_".

In each script you will have a variable "_".
Through it, all methods from the class will be available.

It was
import com.atlassian.jira.component.ComponentAccessor

def getIssue(String key) {
    ComponentAccessor.issueManager.getIssueObject(key)
}
getIssue("TEST")

Now
_.getIssue("TEST")
