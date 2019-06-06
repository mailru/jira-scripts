import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.calendar.service.CalendarService
import ru.mail.jira.plugins.calendar.service.CustomEventService

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@WithPlugin("ru.mail.jira.plugins.mailrucal")
@PluginModule
CustomEventService customEventService
@PluginModule
CalendarService calendarService

consts = [calendarId: 228,
          eventType : "Дежурство",
          systemUser: "systemUser"
]

if (Counter.date != LocalDate.now()) {
    Counter.date = LocalDate.now()
    Counter.employees = [:]
}

def map = [:]
def employes = getDutyPerson(consts.calendarId, LocalDate.now(), 10)
if (!employes) {
    return
}
return employes

employes.each {
    map << [(it.name): Counter.employees.get(it.name) ?: 0]
    _.startWatching(it, issue)
}
def assigneeName = map.min { it.value }.key
if (!(issue.assignee) && assigneeName) {
    issue.assignee = _.getUser(assigneeName)
}
map << [(assigneeName): map.get(assigneeName) + 1]
Counter.employees = map
_.update(issue)

class Counter {
    static date
    static employees = [:]
}

def getDutyPerson(int id, LocalDate date, viewNextDays) {
    if (viewNextDays < 0) {
        return []
    }

    ApplicationUser systemUser = _.getUserByName(consts.systemUser)
    def events = customEventService.getEvents(
            systemUser,
            calendarService.getCalendar(id),
            Date.from(
                    ZonedDateTime.of(
                            date,
                            LocalTime.MIN,
                            ZoneId.systemDefault()
                    ).toInstant()
            ),
            Date.from(
                    ZonedDateTime.of(
                            date,
                            LocalTime.MAX,
                            ZoneId.systemDefault()
                    ).toInstant()
            ),
            Date.from(
                    ZonedDateTime.of(
                            date,
                            LocalTime.MIN,
                            ZoneId.of("UTC")
                    ).toInstant()
            ),
            Date.from(
                    ZonedDateTime.of(
                            date,
                            LocalTime.MAX,
                            ZoneId.of("UTC")
                    ).toInstant()
            )
    )

    if (events) {
        def event = events.find { it.eventType.name == consts.eventType }
        if (event) {
            if (event.participants) {
                return event.participants.collect { _.getUser(it.name) }
            }
        }
    }
    return getDutyPerson(id, date.plusDays(1), viewNextDays - 1)
}

def getDateFromString(string) {
    def format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return format.parse(string)
}