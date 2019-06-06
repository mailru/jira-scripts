import com.atlassian.crowd.embedded.api.CrowdService
import com.atlassian.crowd.embedded.api.Group
import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.bc.project.component.ProjectComponent
import com.atlassian.jira.bc.project.component.ProjectComponentManager
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.StatusManager
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.*
import com.atlassian.jira.issue.attachment.Attachment
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.atlassian.jira.issue.comments.Comment
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem
import com.atlassian.jira.issue.fields.screen.FieldScreenTab
import com.atlassian.jira.issue.index.IssueIndexingService
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.priority.Priority
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.issue.search.SearchQuery
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.watchers.WatcherManager
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.*
import com.atlassian.jira.jql.query.IssueIdCollector
import com.atlassian.jira.project.Project
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.security.PermissionManager
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.security.roles.ProjectRole
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.UserUtils
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.user.util.UserUtil
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.workflow.TransitionOptions
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil
import com.atlassian.jira.workflow.WorkflowTransitionUtilFactory
import com.atlassian.mail.Email
import com.atlassian.mail.queue.SingleMailQueueItem
import com.atlassian.oauth.Request
import com.atlassian.query.Query
import com.opensymphony.workflow.spi.WorkflowEntry
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.ofbiz.core.entity.DelegatorInterface
import org.ofbiz.core.entity.GenericValue

class GlobalObject {

    private final UserManager userManager;
    private final WorkflowManager workflowManager;
    private final WatcherManager watcherManager;
    private final CrowdService crowdService;
    private final UserService userService
    private final IssueService issueService;
    private final ProjectRoleManager projectRoleManager;
    private final IssueIndexingService issueIndexingService;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final OptionsManager optionsManager;
    private final IssueLinkManager issueLinkManager;
    private final IssueManager issueManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final StatusManager statusManager;
    private final GroupManager groupManager;
    private final UserUtil userUtil;
    private final CustomFieldManager customFieldManager;
    private final ProjectComponentManager projectComponentManager;
    private final CommentManager commentManager
    private final JqlQueryParser jqlQueryParser
    private final SearchProvider searchProvider
    private final AttachmentManager attachmentManager
    private final DelegatorInterface delegatorInterface
    private final WorkflowTransitionUtilFactory workflowTransitionUtilFactory
    private final SearchService searchService;


    GlobalObject(@StandardModule UserManager userManager,
                 @StandardModule WorkflowManager workflowManager,
                 @StandardModule WatcherManager watcherManager,
                 @StandardModule CrowdService crowdService,
                 @StandardModule UserService userService,
                 @StandardModule IssueService issueService,
                 @StandardModule ProjectRoleManager projectRoleManager,
                 @StandardModule IssueIndexingService issueIndexingService,
                 @StandardModule ProjectManager projectManager,
                 @StandardModule PermissionManager permissionManager,
                 @StandardModule OptionsManager optionsManager,
                 @StandardModule IssueLinkManager issueLinkManager,
                 @StandardModule IssueManager issueManager,
                 @StandardModule ChangeHistoryManager changeHistoryManager,
                 @StandardModule StatusManager statusManager,
                 @StandardModule GroupManager groupManager,
                 @StandardModule UserUtil userUtil,
                 @StandardModule CustomFieldManager customFieldManager,
                 @StandardModule ProjectComponentManager projectComponentManager,
                 @StandardModule CommentManager commentManager,
                 @StandardModule JqlQueryParser jqlQueryParser,
                 @StandardModule SearchProvider searchProvider,
                 @StandardModule AttachmentManager attachmentManager,
                 @StandardModule DelegatorInterface delegatorInterface,
                 @StandardModule WorkflowTransitionUtilFactory workflowTransitionUtilFactory,
                 @StandardModule SearchService searchService
    ) {
        this.userManager = userManager
        this.workflowManager = workflowManager
        this.watcherManager = watcherManager
        this.crowdService = crowdService
        this.userService = userService
        this.issueService = issueService
        this.projectRoleManager = projectRoleManager
        this.issueIndexingService = issueIndexingService
        this.projectManager = projectManager
        this.permissionManager = permissionManager
        this.optionsManager = optionsManager
        this.issueLinkManager = issueLinkManager
        this.issueManager = issueManager
        this.changeHistoryManager = changeHistoryManager
        this.statusManager = statusManager
        this.groupManager = groupManager
        this.userUtil = userUtil
        this.customFieldManager = customFieldManager
        this.projectComponentManager = projectComponentManager
        this.commentManager = commentManager
        this.jqlQueryParser = jqlQueryParser
        this.searchProvider = searchProvider
        this.attachmentManager = attachmentManager
        this.delegatorInterface = delegatorInterface
        this.workflowTransitionUtilFactory = workflowTransitionUtilFactory
        this.searchService = searchService
    }

    String getBaseUrl() {
        ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
    }

    /**
     * Возвращает количество issues по JQL.
     * Выполняется с учетом прав текущего пользователя.
     * @param jql : "project = TEST"
     * */
    long getCountIssuesByJQL(String jql) {
        getCountIssuesByJQL(jql, currentUser)
    }

    /**
     * Возвращает количество issues по JQL.
     * Выполняется с учетом прав пользователя.
     * @param jql : "project = TEST"
     * @param user : getUser("admin")
     * */
    long getCountIssuesByJQL(String jql, ApplicationUser user) {
        Query query = parseQuery(jql);
        SearchQuery searchQuery = SearchQuery.create(query, user)
        searchProvider.getHitCount(searchQuery)
    }

    /**
     * Возвращает issues по JQL.
     * Выполняется с учетом прав текущего пользователя.
     * @param jql : "project = TEST"
     * */
    Collection<Issue> getIssuesByJQL(String jql) {
        getIssuesByJQL(jql, currentUser)
    }

    /**
     * Возвращает issues по JQL.
     * Выполняется с учетом прав пользователя.
     * @param jql : "project = TEST"
     * @param user : getUser("admin")
     * */
    Collection<Issue> getIssuesByJQL(String jql, ApplicationUser user) {
        Query query = parseQuery(jql)
        getIssuesByJQL(query, user)
    }
    /**
     * Возвращает issues по JQL в указанном диапазоне.
     * Выполняется с учетом прав текущего пользователя.
     * @param jql : "project = TEST"
     * @param user : getUser("admin")
     * @param startIndex : вернуть issues начиная с указанной позиции
     * @param endIndex : вернуть issues заканчивая указанной позиции
     * */
    Collection<Issue> getIssuesByJQL(String jql, ApplicationUser user, int startIndex, int endIndex) {
        Query query = parseQuery(jql)
        getIssuesByJQL(query, user, startIndex, endIndex)
    }

    long getCountIssuesByJQL(Query query) {
        getCountIssuesByJQL(query, currentUser)
    }

    /**
     * Возвращает количество issues по JQL.
     * Выполняется с учетом прав пользователя.
     * @param jql : "project = TEST"
     * @param user : getUser("admin")
     * */
    long getCountIssuesByJQL(Query query, ApplicationUser user) {
        SearchQuery searchQuery = SearchQuery.create(query, user)
        searchProvider.getHitCount(searchQuery)
    }

    /**
     * Возвращает количество issues по JQL.
     * Выполняется с учетом прав текущего пользователя.
     * @param query : parseQuery("project = TEST")
     * */
    Collection<Issue> getIssuesByJQL(Query query) {
        getIssuesByJQL(query, currentUser)
    }

    /**
     * Возвращает количество issues по JQL.
     * Выполняется с учетом прав пользователя.
     * @param query : parseQuery("project = TEST")
     * @param user : getUser("admin")
     * */
    Collection<Issue> getIssuesByJQL(Query query, ApplicationUser user) {
        SearchQuery searchQuery = SearchQuery.create(query, user)
        IssueIdCollector collector = new IssueIdCollector()
        searchProvider.search(searchQuery, collector)
        return collector.getIssueIds().collect { getIssue(it as Long) }
    }

    /**
     * Возвращает issues по JQL в указанном диапазоне.
     * Выполняется с учетом прав текущего пользователя.
     * @param query : parseQuery("project = TEST")
     * @param user : getUser("admin")
     * @param startIndex : вернуть issues начиная с указанной позиции
     * @param endIndex : вернуть issues заканчивая указанной позиции
     * */
    Collection<Issue> getIssuesByJQL(Query query, ApplicationUser user, int startIndex, int endIndex) {
        return searchService.search(user, query, PagerFilter.newPageAlignedFilter(startIndex, endIndex)).getResults()
    }

    /**
     * Возвращает запрос полученный в результате разбора строки jql.
     * @param jql : "project = TEST"
     * */
    Query parseQuery(String jql) {
        return jqlQueryParser.parseQuery(jql)
    }

    /**
     * Возвращает Attachments из issue.
     * @param issue : getIssue("TEST-1")
     * */
    Collection<Attachment> getAttachments(Issue issue) {
        issue.attachments
    }

    /**
     * Создает комментарий в issue.
     * Выполняется с учетом прав текущего пользователя.
     * Без отправки уведомлений.
     * @param issue : getIssue("TEST-1")
     * @param text : "test comment"
     * */
    Comment createComment(Issue issue, String text) {
        createComment(issue, currentUser, text, false);
    }

    /**
     * Создает комментарий в issue.
     * Выполняется с учетом прав пользователя.
     * Без отправки уведомлений.
     * @param issue : getIssue("TEST-1")
     * @param user : getUser("admin")
     * @param text : "test comment"
     * */
    Comment createComment(Issue issue, ApplicationUser user, String text) {
        createComment(issue, user, text, false);
    }

    /**
     * Создает комментарий в issue.
     * Выполняется с учетом прав пользователя.
     * @param issue : getIssue("TEST-1")
     * @param user : getUser("admin")
     * @param text : "test comment"
     * @param notification : флаг отправки уведомлений
     * */
    Comment createComment(Issue issue, ApplicationUser user, String text, boolean notification) {
        commentManager.create(issue, user, text, notification);
    }

    /**
     * Получить список комментариев(оставленное пользователем текстовое сообщение)
     * @param issue : getIssue("TEST-1")
     * */
    Collection<String> getAllCommentsBody(Issue issue) {
        getAllComments(issue)*.getBody()
    }

    /**
     * Получить список комментариев(объектов)
     * @param issue : getIssue("TEST-1")
     * */
    Collection<Comment> getAllComments(Issue issue) {
        commentManager.getComments(issue)
    }

    /**
     * Получить список компонент из проекта
     * @param project : getProject("TEST")
     * */
    Collection<ProjectComponent> getComponens(Project project) {
        projectComponentManager.findAllForProject(project.id)
    }

    /**
     * Получить список компонент из проекта
     * @param projectId : 10001
     * */
    Collection<ProjectComponent> getComponens(Long projectId) {
        projectComponentManager.findAllForProject(projectId)
    }

    /**
     * Получить компоненту из проекта по ее названию
     * @param componentName : "component 1"
     * @param projectId : 10001
     * */
    ProjectComponent getComponent(String componentName, Long projectId) {
        projectComponentManager.findByComponentName(projectId, componentName)
    }

    /**
     * Получить компоненту из проекта по ее названию
     * @param componentName : "component 1"
     * @param project : getProject("TEST")
     * */
    ProjectComponent getComponent(String componentName, Project project) {
        projectComponentManager.findByComponentName(project.id, componentName)
    }

    /**
     * Получить компоненту по ее id
     * @param componentName : 10001
     * */
    ProjectComponent getComponent(Long componentId) {
        projectComponentManager.find(componentId)
    }

    /**
     * Проверить, что в задаче есть компоненты
     * @param issue : getIssue("TEST-1")
     * */
    boolean hasComponents(Issue issue) {
        if (issue.components == null || issue.components.size() == 0) {
            return false
        } else {
            return true
        }
    }

    /**
     * Проверить, что в задаче есть указанная компонента
     * @param issue : getIssue("TEST-1")
     * @param component : getComponent("component 1",  getProject("TEST"))
     * */
    boolean hasComponent(Issue issue, ProjectComponent component) {
        issue.components.find { it == component } != null
    }

    /**
     * Проверить, что в задаче есть компонента c указанным названием
     * @param issue : getIssue("TEST-1")
     * @param componentName : "component 1"
     * */
    boolean hasComponent(Issue issue, String componentName) {
        issue.components.find { it.name == componentName } != null
    }

    /**
     * Проверить, что в задаче есть компонента c указанным id
     * @param issue : getIssue("TEST-1")
     * @param componentId : 10001
     * */
    boolean hasComponent(Issue issue, Long componentId) {
        issue.components.find { it.id == componentId } != null
    }

    /**
     * Проверить, что в задаче есть компоненты из списка
     * @param issue : getIssue("TEST-1")
     * @param componentNames : ["component 1", "component 2"]
     * */
//    boolean hasComponents(Issue issue, Collection<String> componentNames) {
//        issue.components.any { it.name in componentNames }
//    }

    /**
     * Проверить, что в задаче есть компоненты из списка
     * @param issue : getIssue("TEST-1")
     * @param components : [getComponent(10001), getComponent(10002)]
     * */
    boolean hasComponents(Issue issue, Collection<ProjectComponent> components) {
        issue.components.any { it in components }
    }

    //todo updateIssue
    //[customField: value]
    /**
     * Получить customField по id
     * @param fieldId : 10001
     * */
    CustomField getCustomFieldObject(Long fieldId) {
        customFieldManager.getCustomFieldObject(fieldId)
    }

    /**
     * Получить customField по названию
     * @param fieldId : "customfield_10001"
     * */
    CustomField getCustomFieldObject(String fieldId) {
        customFieldManager.getCustomFieldObject(fieldId)
    }

    /**
     * Получить customField по названию
     * @param fieldName : "my custom field"
     * */
    Collection<CustomField> getCustomFieldObjectsByName(String fieldName){
        customFieldManager.getCustomFieldObjectsByName(fieldName)
    }

    /**
     * Получить значение customField из issue
     * @param issue : getIssue("TEST-1")
     * @param fieldId : 10001
     * */
    Object gcfv(Issue issue, Long fieldId) {
        issue.getCustomFieldValue(getCustomFieldObject(fieldId))
    }

    //todo send email eamil with file...
    /** Отправить email
     * @param emailAddr : "test@test.test"
     * @param copy - ящики которые нужно добавить в копию, указываются через запятую.
     * @param subject - шаблон для заголовка сообщений.
     * @param body - тело сообщения.
     * @param from - адрес, который будет указан как отправитель этого сообщения. По умолчанию адрес JIRA.
     * @param replyTo - адрес, на который будет отправлен ответ на письмо.
     * @param emailFormat - "text/html" or "text/plain" or ...
     * */
    void sendEmail(String emailAddr, String copy, String subject, String body, String from, String replyTo, String emailFormat) {
        Email email = new Email(emailAddr, copy, '')
        email.setSubject(subject)
        email.setBody(body)
        email.setMimeType("${emailFormat}; charset=utf-8")
        if (from) {
            email.setFrom(from)
        }
        if (replyTo) {
            email.setReplyTo(replyTo)
        }
        SingleMailQueueItem smqi = new SingleMailQueueItem(email)
        ComponentAccessor.getMailQueue().addItem(smqi)
    }

    /**
     * Получить группы в которых состоит пользователь
     * @param user : getUser("admin")
     * */
    Collection<Group> getGroupsForUser(ApplicationUser user) {
        userUtil.getGroupsForUser(user.name)
    }

    /**
     * Получить пользователй состоящих в группе
     * @param groupName : "jira-users"
     * */
    Collection<ApplicationUser> getUsersInGroup(String groupName) {
        groupManager.with {
            getUsersInGroup(getGroup(groupName))
        }
    }

    /**
     * Получить группы в которых состоит пользователь
     * @param group : getGroup("jira-users")
     * */
    Collection<ApplicationUser> getUsersInGroup(Group group) {
        groupManager.getUsersInGroup(group)
    }

    /**
     * Получить группу по названию
     * @param groupName : "jira-users"
     * */
    Group getGroup(String groupName) {
        groupManager.getGroup(groupName)
    }

    /**
     * Проверить, что текущий пользователь состоит в группе
     * @param groupName : "jira-users"
     * */
    boolean isCurrentUserInGroup(String groupName) {
        groupManager.with {
            isUserInGroup(currentUser, getGroup(groupName))
        }
    }

    /**
     * Проверить, что текущий пользователь состоит в группе
     * @param groupName : getGroup("jira-users")
     * */
    boolean isCurrentUserInGroup(Group group) {
        groupManager.isUserInGroup(currentUser, group)
    }

    /**
     * Проверить, что указанный пользователь состоит в группе
     * @param userName : "admin"
     * @param groupName : "jira-users"
     * */
    boolean isUserInGroup(String userName, String groupName) {
        groupManager.with {
            isUserInGroup(getUser(userName), getGroup(groupName))
        }
    }

    /**
     * Проверить, что указанный пользователь состоит в группе
     * @param userName : getUser("admin")
     * @param groupName : getGroup("jira-users")
     * */
    boolean isUserInGroup(ApplicationUser user, Group group) {
        groupManager.isUserInGroup(user, group)
    }

    /**
     * Проверить, что указанный пользователь состоит в группе
     * @param userName : getUser("admin")
     * @param groupName : "jira-users"
     * */
    boolean isUserInGroup(ApplicationUser user, String groupName) {
        groupManager.with {
            isUserInGroup(user, getGroup(groupName))
        }
    }

    /**
     * Получить статус по id
     * @param statusId : "10001"
     * */
    Status getStatus(String statusId) {
        statusManager.getStatus(statusId)
    }

    /**
     * Получить предыдущий статус статус issue из истории
     * @param issue : getIssue("TEST-1")
     * */
    Status getPreviousStatus(Issue issue) {
        String id = ComponentAccessor.changeHistoryManager.getChangeItemsForField(issue, "status").max {
            it.created
        }.from
        getStatus(id)
    }

    /**
     * Получить предыдущее значение поля для issue из истории
     * @param customField : getCustomFieldObject(10001)
     * @param issue : getIssue("TEST-1")
     * */
    String getPreviousCustomFieldValue(CustomField customField, Issue issue) {
        changeHistoryManager.getChangeItemsForField(issue, customField.name).max { it.created }?.from
    }

    //todo getFromField
    /**
     * Получить предыдущего исполнителя для issue из истории
     * @param issue : getIssue("TEST-1")
     * */
    ApplicationUser getPreviousAssigneeFromHistory(Issue issue) {
        String from = changeHistoryManager.getChangeItemsForField(issue, "Assignee").max { it.created }?.from
        if (from) {
            return getUser(from)
        }
        return null
    }

    /**
     * Возвращает html код таблицы из переданных заголовков и строк.
     * @params heads: ["first","second","third"]
     * @params rows:
     def row1 = [1,2,3]
     def row2 = [2,2,3]
     def row3 = [3,2,3]
     def row4 = [4,2,3]
     def rows = [row1, row2, row3, row4]
     @return getHTMLTable ( heads , rows )
     */

    String createHTMLTable(List<String> heads, List<List<String>> rows) {
        String html = "<table class='aui'>"
        html += getRowHeadsHTML(heads)
        rows.each { cells ->
            html += getRowHTML(cells)
        }
        html += "</table>"
    }

    private String getRowHTML(List cells) {
        if (cells == null || cells.size() == 0) {
            return ''
        }
        String html = "<tr>"
        cells.each { cell ->
            html += "<td>${cell}</td>"
        }
        html += "</tr>"
    }

    private String getRowHeadsHTML(List heads) {
        if (heads == null || heads.size() == 0) {
            return ''
        }
        String html = "<tr>"
        heads.each { head ->
            html += "<th>${head}</th>"
        }
        html += "</tr>"
    }

    //todo create Issue from json/map
    //todo createSubIssue
    //todo issue to map
    /**
     * Сохраняет изменения issue в базу данных.
     * Выполняется с учетом прав текущего пользователя.
     * Без уведомлений.
     * @param issue : getIssue("TEST-1")
     * */
    Issue update(MutableIssue issue) {
        update(currentUser, issue, false)
    }

    /**
     * Сохраняет изменения issue в базу данных.
     * Выполняется с учетом прав пользователя.
     * Без уведомлений.
     * @param user : getUser("admin")
     * @param issue : getIssue("TEST-1")
     * */
    Issue update(ApplicationUser user, MutableIssue issue) {
        update(user, issue, false)
    }

    /**
     * Сохраняет изменения issue в базу данных.
     * Выполняется с учетом прав пользователя.
     * @param user : getUser("admin")
     * @param issue : getIssue("TEST-1")
     * @param notification : флаг отправки уведомлений
     * */
    Issue update(ApplicationUser user, MutableIssue issue, boolean notification) {
        issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, notification)
    }

    /**
     * Получить issue по id
     * @param issueId : 10001
     * */
    Issue getIssue(Long issueId) {
        issueManager.getIssueObject(issueId)
    }

    /**
     * Получить issue по key
     * @param issueKey : "TEST-1"
     * */
    Issue getIssue(String issueKey) {
        issueManager.getIssueObject(issueKey)
    }

    /**
     * Связать задачи указанным типом связи. Связи ориентированные.
     * Выполняется с учетом прав текущего пользователя.
     * @param fromIssue : getIssue("TEST-1")
     * @param toIssue : getIssue("TEST-1")
     * @param issueLinkTypeId : 10001
     * */
    void createLink(Issue fromIssue, Issue toIssue, Long issueLinkTypeId) {
        createLink(fromIssue, toIssue, issueLinkTypeId, 0, currentUser)
    }

    /**
     * Связать задачи указанным типом связи. Связи ориентированные.
     * Выполняется с учетом прав текущего пользователя.
     * @param fromIssue : getIssue("TEST-1")
     * @param toIssue : getIssue("TEST-1")
     * @param issueLinkTypeId : 10001
     * @param sequence : 0 - номер позиции новой связи в исходной issue.
     * */
    void createLink(Issue fromIssue, Issue toIssue, Long issueLinkTypeId, Long sequence) {
        createLink(fromIssue, toIssue, issueLinkTypeId, sequence, currentUser)
    }

    /**
     * Связать задачи указанным типом связи. Связи ориентированные.
     * Выполняется с учетом прав пользователя.
     * @param fromIssue : getIssue("TEST-1")
     * @param toIssue : getIssue("TEST-1")
     * @param issueLinkTypeId : 10001
     * @param sequence : 0 - номер позиции новой связи в исходной issue.
     * */
    void createLink(Issue fromIssue, Issue toIssue, Long issueLinkTypeId, Long sequence, ApplicationUser user) {
        issueLinkManager.createIssueLink(fromIssue.id, toIssue.id, issueLinkTypeId, sequence, user)
    }

    /**
     * Получить список issues которые ссылаются на указанную issue определенным типом связи
     * @param issue : getIssue("TEST-1")
     * @param linkTypeId : 10001
     * */
    Collection<Issue> getInwardIssues(Issue issue, long linkTypeId) {
        getInwardLinks(issue).findAll {
            it.getLinkTypeId() == linkTypeId
        }*.getSourceObject()
    }

    /**
     * Получить список issues на которые ссылается указанная issue определенным типом связи
     * @param issue : getIssue("TEST-1")
     * @param linkTypeId : 10001
     * */
    Collection<Issue> getOutwardIssues(Issue issue, long linkTypeId) {
        getOutwardLinks(issue).findAll {
            it.getLinkTypeId() == linkTypeId
        }*.getDestinationObject()
    }

    /**
     * Получить список issues связанных с issue определенным типом связи
     * @param issue : getIssue("TEST-1")
     * @param linkTypeId : 10001
     * */
    Collection<Issue> getAllLinkedIssues(Issue issue, long linkTypeId) {
        Set<Issue> allLinkedIssue = [] as Set
        allLinkedIssue.addAll(getInwardIssues(issue, linkTypeId))
        allLinkedIssue.addAll(getOutwardIssues(issue, linkTypeId))
        return allLinkedIssue
    }

    /**
     * Получить список issues которые ссылаются на указанную issue
     * @param issue : getIssue("TEST-1")
     * */
    Collection<Issue> getInwardIssues(Issue issue) {
        getInwardLinks(issue)*.getSourceObject()
    }

    /**
     * Получить список issues на которые ссылается указанная issue
     * @param issue : getIssue("TEST-1")
     * */
    Collection<Issue> getOutwardIssues(Issue issue) {
        getOutwardLinks(issue)*.getDestinationObject()
    }

    /**
     * Получить список issues связанных с issue
     * @param issue : getIssue("TEST-1")
     * */
    Collection<Issue> getAllLinkedIssues(Issue issue) {
        Set<Issue> allLinkedIssue = [] as Set
        allLinkedIssue.addAll(getInwardIssues(issue))
        allLinkedIssue.addAll(getOutwardIssues(issue))
        return allLinkedIssue
    }

    /**
     * Получить список исходящих связей
     * @param issue : getIssue("TEST-1")
     * */
    Collection<IssueLink> getOutwardLinks(Issue issue) {
        issueLinkManager.getOutwardLinks(issue.id);
    }

    /**
     * Получить список входях связей
     * @param issue : getIssue("TEST-1")
     * */
    Collection<IssueLink> getInwardLinks(Issue issue) {
        issueLinkManager.getInwardLinks(issue.id);
    }

    /**
     * Получить Option по id
     * @param optionId : 10001
     * */
    Option getOptionById(Long optionId) {
        optionsManager.findByOptionId(optionId)
    }

    /**
     * Получить все Option по текстовому значению
     * @param value : "option name 1"
     * */
    Collection<Option> getOptionsByValue(String value) {
        optionsManager.findByOptionValue(value)
    }

    /**
     * Получить все список опций для поля в указанном issue. (Смотрится соответствующий контекст поля)
     * @param customField : getCustomFieldObject(10001)
     * @param issue : getIssue("TEST-1")
     * */
    Options getOptionsFor(CustomField customField, Issue issue) {
        optionsManager.getOptions(customField.getRelevantConfig(issue))
    }

    /**
     * Получить project по id
     * @param projectId : 10001
     * */
    Project getProject(Long projectId) {
        projectManager.getProjectObj(projectId)
    }

    /**
     * Получить project по key
     * @param projectKey : "TEST"
     * @return объект проект
     * */
    Project getProject(String projectKey) {
        projectManager.getProjectObjByKey(projectKey)
    }

    Collection<Project> getAllProjects() {
        projectManager.getProjectObjects()
    }

    /**
     * Реиндексировать список issues
     * @param issues : [getIssue("TEST-2"), getIssue("TEST-2")]
     * */
    Collection<Issue> reIndex(Collection<Issue> issues) {
        issueIndexingService.reIndexIssueObjects(issues)
        return issues
    }

    /**
     * Реиндексировать issue
     * @param issue : getIssue("TEST-2")
     * */
    Issue reIndex(Issue issue) {
        issueIndexingService.reIndex(issue)
        return issue
    }

    /**
     * Выполнить GET запрос по указанному URL и вернуть ответ в виде json объекта
     * */
    Object GET(String url) {
        parseJSON(url.toURL().text)
    }

    /**
     * Выполнить POST запрос по указанному URL и вернуть ответ в виде json объекта
     * */
    Object POST(String url, String content, String userName, String password) {
        makeRequest(url, content, userName, password, Request.HttpMethod.POST);
    }

    /**
     * Выполнить PUT запрос по указанному URL и вернуть ответ в виде json объекта
     * */
    Object PUT(String url, String content, String userName, String password) {
        makeRequest(url, content, userName, password, Request.HttpMethod.PUT);
    }

    /**
     * Выполнить GET запрос по указанному URL и вернуть ответ в виде json объекта
     * */
    Object GET(String url, String userName, String password) {
        makeRequest(url, null, userName, password, Request.HttpMethod.GET);
    }

    private Object makeRequest(String url, String content, String userName, String password, Request.HttpMethod httpMethod) {
        String authString = "${userName}:${password}".getBytes().encodeBase64().toString()
        HttpURLConnection connection = (HttpURLConnection) url.toURL().openConnection()
        connection.addRequestProperty("Authorization", "Basic ${authString}")
        connection.addRequestProperty("Content-Type", "application/json")
        connection.setReadTimeout(30000)
        connection.setRequestMethod(httpMethod.name())
        if (content) {
            connection.doOutput = true
            connection.outputStream.withWriter {
                it.write(content)
                it.flush()
            }
        }
        try {
            connection.connect()
            String line = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine()
            return parseJSON(line)
        } catch (IOException e) {
            try {
                return parseJSON(connection.errorStream.text)
            } catch (Exception ignored) {
                throw e
            }
        } finally {
            connection.disconnect()
        }
        return null
    }

    /**
     * Распарсить текст в json объект
     * @param text : '{"name": "Ivan"}'
     * */
    Object parseJSON(String text) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        jsonSlurper.parseText(text)
    }

    /**
     * Конвертировать объект в json строку
     * */
    String toJson(Object obj) {
        JsonOutput.toJson(obj)
    }

    /**
     * Проверить что указанный пользователь установлен в соответсвующей роли на проекте
     * @param user : getUser("admin")
     * @param projectRole : "ADMINISTRATORS"
     * @param project : getProject("TEST")
     * */
    boolean isUserInProjectRole(ApplicationUser user, String projectRole, Project project) {
        isUserInProjectRole(user, getProjectRole(projectRole), project)
    }

    /**
     * Проверить что указанный пользователь установлен в соответсвующей роли на проекте
     * @param user : getUser("admin")
     * @param projectRole : getProjectRole("ADMINISTRATORS")
     * @param project : getProject("TEST")
     * */
    boolean isUserInProjectRole(ApplicationUser user, ProjectRole projectRole, Project project) {
        projectRoleManager.isUserInProjectRole(user, projectRole, project)
    }

    /**
     * Получить список ролей в проекте для укзанного пользователя
     * @param user : getUser("admin")
     * @param project : getProject("TEST")
     * */
    Collection<ProjectRole> getRolesForUser(ApplicationUser user, Project project) {
        projectRoleManager.getProjectRoles(user, project)
    }

    /**
     * Получить список пользователей в проекте для указанной роли
     * @param user : getUser("admin")
     * @param role : "ADMINISTRATORS"
     * */
    Collection<ApplicationUser> getUsersOnProjectRole(Project project, String role) {
        projectRoleManager.getProjectRoleActors(getProjectRole(role), project)?.getApplicationUsers()
    }

    /**
     * Получить список пользователей в проекте для указанной роли
     * @param user : getUser("admin")
     * @param role : getProjectRole("ADMINISTRATORS")
     * */
    Collection<ApplicationUser> getUsersOnProjectRole(Project project, ProjectRole role) {
        projectRoleManager.getProjectRoleActors(role, project)?.getApplicationUsers()
    }

    /**
     * Получить проектную роль по названию
     * @param projectRoleName : "ADMINISTRATORS"
     * */
    ProjectRole getProjectRole(String projectRoleName) {
        projectRoleManager.getProjectRole(projectRoleName)
    }

    /**
     * Получить все проектные роли
     * */
    Collection<ProjectRole> getAllRoles() {
        projectRoleManager.getProjectRoles()
    }

    /**
     * Найти пользователя по почте
     * */
    ApplicationUser findUserByEmail(String email) {
        UserUtils.getUserByEmail(email)
    }

    /**
     * Выполнить указанный переход с учетом прав текущего пользователя.
     * @param issue : getIssue("TEST-1")
     * @param actionId : 121
     * */
    boolean doTransition(MutableIssue issue, int actionId) {
        doTransition(issue, actionId, currentUser);
    }

    /**
     * Выполнить указанный переход с учетом прав указанного пользователя.
     * @param issue : getIssue("TEST-1")
     * @param actionId : 121
     * @param user : getUser("admin")
     * */
    boolean doTransition(MutableIssue issue, int actionId, ApplicationUser user) {
        GenericValue gv = delegatorInterface.findByPrimaryKey("OSWorkflowEntry", ["id": issue.getWorkflowId()])
        if (!gv) {
            return false
        }
        if (gv.get("state") == WorkflowEntry.CREATED) {
            gv.set("state", WorkflowEntry.ACTIVATED)
            gv.store()
        }
        TransitionOptions.Builder builder = new TransitionOptions.Builder()
        TransitionOptions transitionOptions = builder.build()

        WorkflowTransitionUtil workflowTransitionUtil = workflowTransitionUtilFactory.create();
        Map<String, Object> params = getCurrentParamsForAction(issue, actionId)
        transitionOptions.getWorkflowParams().each { k, v ->
            workflowTransitionUtil.addAdditionalInput(k, v)
        }
        workflowTransitionUtil.setAction(actionId)
        workflowTransitionUtil.setIssue(issue)
        workflowTransitionUtil.setUserkey(user.name)
        workflowTransitionUtil.setParams(params)
        Map<String, ErrorCollection> errors = [validate: workflowTransitionUtil.validate()]
        boolean result = true
        if (errors.validate.hasAnyErrors()) {
            // log.error("Fast Track Transition ${issue} errors.validate ${errors.validate}")
            result = false
        }
        errors << [progress: workflowTransitionUtil.progress()]
        if (errors.progress.hasAnyErrors()) {
            // log.error("Fast Track Transition ${issue} errors.progress ${errors.validate}")
            result = false
        }
        return result
    }

    private Map<String, Object> getCurrentParamsForAction(Issue issue, int actionId) {
        def fields = getFieldsFromTransition(issue, actionId)
        def params = fields.collectEntries { fieldName ->
            [(fieldName): getFieldValueByFieldName(issue, fieldName)]
        }
        return params
    }

    private Object getFieldValueByFieldName(Issue issue, String fieldName) {
        if (fieldName.contains('customfield')) {
            def cf = getCustomFieldObject(fieldName)
            if (!cf) {
                return null
            }
            return issue.getCustomFieldValue(cf)
        } else if (fieldName == 'priority') {
            Priority priority = (Priority) issue[fieldName]
            return priority.id
        } else if (fieldName in ['assignee', 'reporter']) {
            ApplicationUser user = (ApplicationUser) issue[fieldName]
            return user ? user.name : null
        } else {
            return issue[fieldName]
        }
    }

    private Collection<String> getFieldsFromTransition(Issue issue, int actionId) {
        String screenIdString = workflowManager.getActionDescriptor(issue, actionId).getMetaAttributes()['jira.fieldscreen.id']
        Long screenId = screenIdString ? screenIdString as Long : null
        Collection<String> fields = []
        if (screenId) {
            FieldScreen screen = getScreen(screenId)
            if (screen) {
                fields = getFieldsFromScreen(screen)
            }
        }
        if (fields.contains('timetracking')) {
            fields.remove('timetracking')
            fields.add('originalEstimate')
            fields.add('estimate')
        }
        return fields
    }

    private Collection<String> getFieldsFromScreen(FieldScreen screen) {
        List<FieldScreenTab> fieldScreenTabs = screen.getTabs()
        List<FieldScreenLayoutItem> fieldScreenLayoutItems = []
        fieldScreenTabs.each { fieldScreenTab ->
            fieldScreenLayoutItems.addAll(fieldScreenTab.getFieldScreenLayoutItems())
        }
        return fieldScreenLayoutItems*.getFieldId() ?: [] as List<String>
    }

    private FieldScreen getScreen(Long id) {
        ComponentAccessor.fieldScreenManager.getFieldScreen(id)
    }

    private ApplicationUser getCurrentUser() {
        ComponentAccessor.jiraAuthenticationContext?.getLoggedInUser()
    }

    /**
     * Вернуть пользователя по логину
     * @param userName : "admin"
     * */
    ApplicationUser getUser(String userName) {
        userManager.getUserByName(userName)
    }

    /**
     * Вернуть список наблюдателей за issue
     * @param issue : getIssue("TEST-1")
     * */
    Collection<ApplicationUser> getAllWatchers(Issue issue) {
        watcherManager.getWatchers(issue, Locale.US)
    }

    /**
     * Убрать пользователя из наблюдателей
     * @param user : getUser("admin")
     * @param issue : getIssue("TEST-1")
     * */
    Issue stopWatching(ApplicationUser user, Issue issue) {
        watcherManager.stopWatching(user, issue)
    }

    /**
     * Добавить пользователя из наблюдателей
     * @param user : getUser("admin")
     * @param issue : getIssue("TEST-1")
     * */
    Issue startWatching(ApplicationUser user, Issue issue) {
        watcherManager.startWatching(user, issue)
    }
}
