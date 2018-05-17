import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.util.AttachmentUtils

def getAttachments(issue) {
    issue.attachments
}

def getAttachmentFiles(issue) {
    issue.attachments.collect { AttachmentUtils.getAttachmentFile(it) }
}

//contentType = "text/thml"
def createAttachment(file, String fileName, String contentType, user, issue) {
    ComponentAccessor.getAttachmentManager().createAttachment(file, "${fileName}", contentType, user, issue);
}

def deleteAttachment(attachment) {
    ComponentAccessor.getAttachmentManager().deleteAttachment(attachment)
}
