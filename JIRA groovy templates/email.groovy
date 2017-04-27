import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.mail.Email

def sendEmail(String emailAddr, String subject, String body) {
    def mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer()
    if (mailServer) {
        email = new Email(emailAddr)
        email.setSubject(subject)
        email.setBody(body)
        email.setMimeType("text/html; charset=utf-8")
        mailServer.send(email)
        log.debug("Mail sent")
    } else {
        log.warn("Please make sure that a valid mailServer is configured")
    }
}