import com.atlassian.jira.favourites.FavouritesManager
import com.atlassian.jira.portal.PortalPage
import com.atlassian.jira.portal.PortalPageManager
import com.atlassian.jira.user.ApplicationUser
import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.StandardModule
import ru.mail.jira.plugins.groovy.api.script.WithParam
import ru.mail.jira.plugins.groovy.util.ValidationException


@WithParam(displayName = "User", type = ParamType.MULTI_USER)
List<ApplicationUser> users
@WithParam(displayName = "Dashboard id", type = ParamType.LONG)
long dashboardId

@StandardModule
PortalPageManager portalPageManager
@StandardModule
FavouritesManager favouritesManager

PortalPage portalPage = portalPageManager.getPortalPageById(dashboardId)

if (portalPage == null) {
    throw new ValidationException("Dashboard not found")
}

List<ApplicationUser> withoutPermission = users.findAll { !portalPageManager.hasPermissionToUse(it, portalPage) }

if (withoutPermission) {
    throw new ValidationException("Some users don't have access to dashboard: " + withoutPermission.join(", "))
}

users.findAll { !favouritesManager.isFavourite(it, portalPage) }
        .each { favouritesManager.addFavourite(it, portalPage) }

return 'done'
