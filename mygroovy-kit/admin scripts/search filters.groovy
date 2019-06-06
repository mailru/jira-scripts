import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchRequestManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.StandardModule
import ru.mail.jira.plugins.groovy.api.script.WithParam

@WithParam(type = ParamType.STRING, displayName = "Search for")
String query

@StandardModule
SearchRequestManager searchRequestManager
@StandardModule
UserManager userManager

List<String> rows = new ArrayList<>()
String baseUrl = ComponentAccessor.applicationProperties.getString("jira.baseurl")

searchRequestManager.visitAll({
    if (StringUtils.containsIgnoreCase(it.request, query)) {
        ApplicationUser owner = userManager.getUserByKey(it.user);
        rows.add(
                "<tr>" +
                        "<td>${StringEscapeUtils.escapeHtml(it.name)}</td>" +
                        "<td>${StringEscapeUtils.escapeHtml(owner.displayName)} (${StringEscapeUtils.escapeHtml(owner.name)})</td>" +
                        "<td><a href='${baseUrl}/issues/?filter=${it.id}'>link</a></td>" +
                        "</tr>"
        )
    }
})

return '<table class="aui" style="width: 100%">' + rows.join('') + '</table>'
