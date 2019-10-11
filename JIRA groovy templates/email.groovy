import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.attachment.Attachment
import com.atlassian.jira.util.AttachmentUtils
import com.atlassian.mail.Email
import com.atlassian.mail.queue.SingleMailQueueItem

import javax.mail.*
import javax.mail.internet.*

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


/** Отправить email
 * @param emailAddr : "test@test.test"
 * @param copy - ящики которые нужно добавить в копию, указываются через запятую.
 * @param subject - шаблон для заголовка сообщений.
 * @param body - тело сообщения.
 * @param from - адрес, который будет указан как отправитель этого сообщения. По умолчанию адрес JIRA.
 * @param replyTo - адрес, на который будет отправлен ответ на письмо.
 * @param emailFormat - "text/html" or "text/plain" or ...
 * @param attachments - list of attachments
 * */
void sendEmail(String emailAddr, String copy, String subject, String body, String from, String replyTo, String emailFormat, List<Attachment> attachments) {
    Email email = new Email(emailAddr, copy, '')
    email.setSubject(subject)
    Multipart multipart = new MimeMultipart("mixed")

    if (from) {
        email.setFrom(from)
    }
    if (replyTo) {
        email.setReplyTo(replyTo)
    }

    MimeBodyPart bodyPart = new MimeBodyPart()
    bodyPart.setContent(body, "${emailFormat ?: 'text/html'}; charset=utf-8")
    multipart.addBodyPart(bodyPart)

    attachments?.each {
        File attachment = AttachmentUtils.getAttachmentFile(it)

        MimeBodyPart attachmentPart = new MimeBodyPart()
        attachmentPart.attachFile(attachment, it.getMimetype(), null)
        attachmentPart.setFileName(it.getFilename())
        multipart.addBodyPart(attachmentPart)
    }
    email.setMultipart(multipart)
    email.setMimeType("multipart/mixed")
    SingleMailQueueItem smqi = new SingleMailQueueItem(email)
    ComponentAccessor.getMailQueue().addItem(smqi)
}