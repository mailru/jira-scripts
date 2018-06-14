import com.atlassian.jira.bc.JiraServiceContextImpl
import com.atlassian.jira.bc.filter.SearchRequestService
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager
//import com.atlassian.jira.issue.search.SearchRequestManager

columnLayoutManager = ComponentAccessor.getComponentOfType(ColumnLayoutManager)
searchRequestService = ComponentAccessor.getComponent(SearchRequestService)

searchFiltersByColumnField("customfield_42001", currentUser)

def getFilter(long filterId, user) {

    //todo
    //def searchRequestManager = ComponentAccessor.getComponentOfType(SearchRequestManager)
    //def request = searchRequestManager.getSearchRequestById(filterId)
    //def searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
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
    getAllFilters().findAll{filter->
        filter.query?.getQueryString()?.contains(text)
    }
}

def getAllFilters() {

    def filters = [] as Set
    ComponentAccessor.userManager.getAllUsers().each { user ->
        filters.addAll(searchRequestService.getOwnedFilters(user))
    }
    return filters
}

def getUserFilters(user) {
    //def searchRequestService = ComponentAccessor.getComponent(SearchRequestService)
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
    columnLayoutManager.getColumnLayout(user, filter).getColumnLayoutItems().collect { columnItem ->
        return columnItem.getNavigableField()
    }
}

def setNewOwnerFilter(long filterId, newOwnerUser) {
    //todo //filter.setOwner
}