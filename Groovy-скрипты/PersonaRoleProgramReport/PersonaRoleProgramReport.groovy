package resources.groovy.birt

import ru.naumen.stech.bobject.program.ProgramStech
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.common.utils.StringUtilities
import ru.naumen.ccamcore.bobject.CCAMCoreBO
import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.ccamcore.security.grant.GrantPerson
import ru.naumen.ccamcore.security.grant.GrantPersonHibernateHandler
import ru.naumen.core.CoreBO
import ru.naumen.core.roles.Role
import ru.naumen.core.hibernate.HibernateUtil
import ru.naumen.core.hierarchycache.CoreBOHierarchyCache
import ru.naumen.core.catalogsengine.CoreCatalogItem
import ru.naumen.core.bobjects.person.login.CoreLogin
import ru.naumen.fcntp.bobject.competitivecommission.CompetitiveCommissionStaff
import ru.naumen.fcntp.bobject.program.workgroup.ProgramWorkGroup
import ru.naumen.fcntp.bobject.relations.PersonProgramActionRelation
import ru.naumen.fcntp.bobject.relations.PersonWorkgroupRelation
import ru.naumen.fcntp.bobject.relations.EmployeeAcceptanceCommissionRelation
import ru.naumen.fcntp.bobject.relations.EmployeeCompetitiveCommissionRelation
import ru.naumen.ccam.bobject.program.Program

import java.util.ArrayList
import java.util.List
import java.util.Set
import java.util.HashSet
import java.util.Map
import java.util.HashMap


import org.apache.poi.hssf.record.ContinueRecord
import org.hibernate.Query


//email = ''
//workName = ''
//programId = 'corebofs000080000iiqdc0ntboi5jv4'
  

def employees = getEmployees(email, workName, programId)
def xPrograms = report.createList()
def Set<String> nonParentProgram = new HashSet<String>()
def Map<String, Object> rowByProgram = new HashMap<String, Object>()
queries = new Queries(session)

rowNumber = 0
for (def memberEmployee in employees) {
	rowNumber++
    //Заполняю те свойства (конкурсные комиссии, приемочные комиссии, мероприятия, рабочие группы), которые можно точно отсортировать по программе
    if(!handleEmployee(programId, queries, memberEmployee, nonParentProgram, rowByProgram, rowNumber)){
    	rowNumber--
    }
}

def programList = report.createList()

program = helper.get(programId)

def xProgram = report.createObject()
xProgram.title = program.displayableTitle
xProgram.name = "program"

if (rowByProgram.containsKey(programId))
    xProgram.members = rowByProgram.get(programId)
else
    xProgram.members = report.createList()
programList.add(xProgram)

def xc = report.createObject()
xc.nonParentProgram = StringUtilities.join(nonParentProgram, Constants.SEPARATOR)
xc.name = "group"
xc.programs = programList
report.rootObject = xc

report


// Дальше идут функции:

List getProgramObjectsByEmployee(queries, memberEmployee, roleEntry, programId) {
    queries.objects.setParameter("roleId", "$roleEntry.UUID".toLowerCase())
    return queries.objects.list()
}

String objectsToString (objs) {
    objSeparator = ";<br>"
    
    return StringUtilities.join(objs, objSeparator)
}

private getReportFieldsByProgramID(memberEmployee, queries, programId) {
    def Object[] val = new Object[6]
    val[0] = memberEmployee.getEmail()
    val[1] = memberEmployee.getDisplayableTitle()

    queries.queryForEmployee1.setParameter("memberId", "$memberEmployee.UUID".toLowerCase())
    queries.queryForEmployee1.setParameter("programId", "$programId".toLowerCase())
    queries.queryForEmployee2.setParameter("memberId", "$memberEmployee.UUID".toLowerCase())
    queries.queryForEmployee2.setParameter("programId", "$programId".toLowerCase())
    queries.queryForEmployee3.setParameter("memberId", "$memberEmployee.UUID".toLowerCase())
    queries.queryForEmployee3.setParameter("programId", "$programId".toLowerCase())
    queries.queryForEmployee4.setParameter("memberId", "$memberEmployee.UUID".toLowerCase())
    queries.queryForEmployee4.setParameter("programId", "$programId".toLowerCase())

    progActions = queries.queryForEmployee1.list()

    val[2] = StringUtilities.join(progActions, Constants.SEPARATOR)

    def List<String> workGroupsByProgram = queries.queryForEmployee2.list()
    val[3] = StringUtilities.join(workGroupsByProgram, Constants.SEPARATOR)

    def List<String> acceptanceCommissionTiltes = queries.queryForEmployee3.list()
    val[4] = StringUtilities.join(acceptanceCommissionTiltes, Constants.SEPARATOR)

    def List<String> competitiveCommissionTiltes = queries.queryForEmployee4.list()
    val[5] = StringUtilities.join(competitiveCommissionTiltes, Constants.SEPARATOR)

    return val
}

private List getEmployees(mail, workName, programId) {
    def StringBuilder sb = new StringBuilder()
    //Выводим пользователей у которых есть логин и пароль и имеющих назначение для данной программы
    sb.append("SELECT distinct login.person From CoreLogin login, GrantPerson gp where login.person.id = gp.roleCapability.leftBO and (gp.object.id=:programId or gp.object.parent.id=:programId)")
    if (!StringUtilities.isEmptyTrim(email))
        sb.append(" and lower(login.person.email) like :email")
    if (!StringUtilities.isEmptyTrim(workName))
    {
        sb.append(" and login.person.id in (select post.employee.id from CorePost post where lower(post.parent.persistedDisplayableTitle) like :workName)")
    }

    //выводим всех пользователей с ролями определенными выше
    Query q = session.createQuery(sb.toString())

    if (!StringUtilities.isEmptyTrim(email))
        q.setParameter("email", "%$email%".toLowerCase())
    if (!StringUtilities.isEmptyTrim(workName))
        q.setParameter("workName", "%$workName%".toLowerCase())

    q.setParameter("programId", programId)

    def employees = q.list()
    return employees
}

private handleEmployee(programId, queries, memberEmployee, nonParentProgram, rowByProgram, rowNumber) {
    vals = getReportFieldsByProgramID(memberEmployee, queries, programId)

    //Создаю строку для таблицы отчета и заношу их в таблицу относящуюся к программе (через Map<Программа, Таблица>)
    createRowEntry(vals, programId, rowByProgram, report, queries, memberEmployee, rowNumber)
}

private createRowEntry(vals, String programId, rowByProgram, report, queries, memberEmployee, rowNumber) {
	added = false

    if (!rowByProgram.containsKey(programId))
    rowByProgram.put(programId, report.createList())
    def listRows = rowByProgram.get(programId)

    queries.grants.setParameter("memberId",  "$memberEmployee.UUID".toLowerCase())
    
    def grantPersons = queries.grants.list()
    
    roleRels = []
    roleIds = new java.util.HashSet()
    for(grantPerson in grantPersons) {
    	program = Program.findProgram(grantPerson.object)
    	if(program!=null && program.UUID.equals(programId)){
    		if(!roleIds.contains(grantPerson.roleCapability.UUID)){
    			roleRels.add(grantPerson.roleCapability)
    			roleIds.add(grantPerson.roleCapability.UUID)
    		}
    	}
    }

    queries.posts.setParameter("employee", memberEmployee)
    def List<CorePost> listPosts = queries.posts.list()

    def post=""
    for(curPost in listPosts){

        post = "<p>"+curPost.getTitle()+"</p>"

        if (curPost.getParent() != null)
        {
            post += "<br><p style=\"margin-left: 10px;\">" + curPost.getParent().getDisplayableTitle()+"</p>"
        }
    }

    counter = 0
    //Каждые место работы или роль в отдельной строке
    for (roleEntry in roleRels)
    {
        def row = report.createObject()

        grants = getProgramObjectsByEmployee(queries, memberEmployee, roleEntry, programId)
        titles = []
        for(grant in grants){
        	titles.add(grant.object.displayableTitle)
        }
          
        if(grants.size()==0){
            continue
        }

        row.objects = objectsToString(titles)

        if (counter == 0)
        {
            row.no = rowNumber
            row.rowNumber = rowNumber
            row.name = "member"
            row.email = (String)vals[0]
            row.fio = (String)vals[1]
            row.programActions = (String)vals[2]
            row.workGroups = (String)vals[3]
            row.acceptanceCommission = (String)vals[4]
            row.competitiveCommission = (String)vals[5]
            row.post = post
        }else{
            row = report.createObject()
            row.no = ""
            row.rowNumber = rowNumber
            row.name = "member"
            row.email = ""
            row.fio = ""
            row.programActions = ""
            row.workGroups = ""
            row.acceptanceCommission = ""
            row.competitiveCommission = ""
            row.objects = ""
        }

        //Добавление роли
        row.role = roleEntry.getRightBO().getDisplayableTitle()
        row.createRole = roleEntry.getCreationDate()

		added = true
        listRows.add(row)
    }
    
    return added
}

class Constants{
    public static def final String SEPARATOR = ", "
}

class Queries{
    Query queryForEmployee1
    Query queryForEmployee2
    Query queryForEmployee3
    Query queryForEmployee4
    Query grants
    Query objects
    Query posts

    Queries(session){
        def queryString = "select rel.rightBO.persistedDisplayableTitle from PersonProgramActionRelation \
            as rel where rel.leftBO.id = :memberId and rel.rightBO.parent.id=:programId and rel.typeKey='${EmployeeStech.PROGRAM_ACTION_TYPE_KEY}'"
        queryForEmployee1 = session.createQuery(queryString)

        queryString = "select rel.rightBO.title from PersonWorkgroupRelation as rel where rel.leftBO.id = :memberId and rel.rightBO.id in \
            (select wgroup.workGroup.id from ProgramWorkGroup as wgroup where wgroup.parent.id=:programId) and rel.typeKey='${EmployeeStech.WORK_GROUP_TYPE_KEY}'"
        queryForEmployee2 = session.createQuery(queryString)

        queryString = "select rel.rightBO.title from EmployeeAcceptanceCommissionRelation as rel where rel.leftBO.id = :memberId and rel.rightBO.id in \
            (select com.commission.id from ProgramAcceptanceCommission as com where com.parent.id=:programId) and rel.typeKey='$EmployeeAcceptanceCommissionRelation.TYPE_KEY'"
        queryForEmployee3 = session.createQuery(queryString)

        queryString = "select rel.leftBO.persistedDisplayableTitle from CompetitiveCommissionStaff as rel where rel.rightBO.id=:memberId and \
            rel.typeKey='$CompetitiveCommissionStaff.TYPE_KEY' and rel.leftBO.parent.id=:programId"
        queryForEmployee4 = session.createQuery(queryString)

        queryString = "select gp FROM GrantPerson gp WHERE gp.roleCapability.leftBO.id = :memberId"
        grants = session.createQuery(queryString)

        queryString = "select gp from GrantPerson gp where gp.roleCapability.id =:roleId"
        objects = session.createQuery(queryString)
        
        queryString = "from " + CorePost.class.getName() + " post where post.employee=:employee"
        posts = session.createQuery(queryString)
    }
}