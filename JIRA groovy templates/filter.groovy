import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager

def findAndReplaceInFilters(String find, String replaceTo) {
    searchTextByAllFilters(find).each { filter ->
        def newJql = filter.query?.getQueryString()?.replaceAll(find, replaceTo)
        updateUserFilter(filter, newJql, getOwner(filter))
    }
}

def getFilter(long filterId, user) {
    def searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
    searchRequestService.getFilter(new JiraServiceContextImpl(user), filterId)
}

def searchFiltersByColumnField(String fieldId, user) {
    getAllFilters().findAll { filter ->
        getColumnFieldsFromFilter(filter, user).find { field ->
            field.id == fieldId
        }
    }
}

def searchTextByAllFilters(String text) {
    getAllFilters().findAll { filter ->
        filter.query?.getQueryString()?.contains(text)
    }
}

def getAllFilters() {
    def filters = [] as Set
    def searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
    ComponentAccessor.userManager.getAllUsers().each { user ->
        filters.addAll(searchRequestService.getOwnedFilters(user))
    }
    return filters
}

def getUserFilters(user) {
    def searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
    return searchRequestService.getOwnedFilters(user)
}

def updateUserFilter(filter, String newJql, user) {
    def searchService = ComponentAccessor.getComponent(SearchService)
    def newQuery = searchService.parseQuery(user, newJql).getQuery()
    if (newQuery) {
        filter.setQuery(newQuery)
    }
    def searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
    searchRequestService.updateFilter(new JiraServiceContextImpl(user), filter)
}

def getColumnFieldsFromFilter(filter, user) {
    def columnLayoutManager = ComponentAccessor.getComponentOfType(ColumnLayoutManager)
    columnLayoutManager.getColumnLayout(user, filter).getColumnLayoutItems().collect { columnItem ->
        return columnItem.getNavigableField()
    }
}

def getOwner(filter) {
    filter.getOwner()
}

def setNewOwnerFilter(long filterId, newOwnerUser) {
    //todo //filter.setOwner
}
