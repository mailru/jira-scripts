import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.mail.Email
import com.atlassian.mail.queue.SingleMailQueueItem

def sendEmail(String emailAddr, String copy, String subject, String body, String from) {
    email = new Email(emailAddr, copy, '')
    email.setSubject(subject)
    email.setBody(body)
    email.setMimeType("text/html; charset=utf-8")
    if(from){
      email.setFrom(from)
    }
    SingleMailQueueItem smqi = new SingleMailQueueItem(email)
    ComponentAccessor.getMailQueue().addItem(smqi)
}




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
