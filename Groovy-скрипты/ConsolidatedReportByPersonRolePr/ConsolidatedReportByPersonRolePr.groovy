package resources.groovy.birt

import ru.naumen.stech.bobject.program.ProgramStech
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
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

import java.util.ArrayList
import java.util.List
import java.util.Set
import java.util.HashSet
import java.util.Map
import java.util.HashMap


import org.apache.poi.hssf.record.ContinueRecord
import org.hibernate.Query

/*
    Скрипт для отчета "Сводный отчет по персонам, ролям, программам Системы."
    ТЗ: http://ssh-gate.naumen.ru:10305/lab_labour/show/11762
    
    Предполагается наличие входных параметров email и workName
    Если переменные email, workName пустые, то будут выводиться все данные. 
    При поиске по email автоматически ставится % перед параметром, поэтому можно написать @fcntp.ru. 
    workName - часть названия организации
    
    Также, добавляется третий параметр programId, программа, для которой формируется отчет.
    Этот параметр необходим ввиду того, что xml-файл, сгенерированный по всем программам получается слишком большим.
 */

/*email = '@fcntp.ru'
workName = ''
programId = 'corebofs000080000gte9v29hjmmodmk'*/

def employees = getEmployees(email, workName)
def xPrograms = report.createList()
def programs = helper.select("select ps.id, ps.persistedDisplayableTitle from ProgramStech ps where ps.id = '"+programId+"'")
def Set<String> nonParentProgram = new HashSet<String>()
def Map<String, Object> rowByProgram = new HashMap<String, Object>()
queries = new Queries(session)

for (def memberEmployee in employees) {
    //Заполняю те свойства (конкурсные комиссии, приемочные комиссии, мероприятия, рабочие группы), которые можно точно отсортировать по программе
    handleEmployee(programs, queries, memberEmployee, nonParentProgram, rowByProgram)
}


def programList = report.createList()
for (def String[] programIdAndName : programs) {
    def xProgram = report.createObject()
    xProgram.title=programIdAndName[1]
    xProgram.name = "program"

    if (rowByProgram.containsKey(programIdAndName[0]))
        xProgram.members = rowByProgram.get(programIdAndName[0])
    else
        xProgram.members = report.createList()
    programList.add(xProgram)
}

def xc = report.createObject()
xc.nonParentProgram = StringUtilities.join(nonParentProgram, Constants.SEPARATOR)
xc.name = "group"
xc.programs = programList
report.rootObject = xc


// Дальше идут функции:

String getProgramObjectsByEmployee (Map<Class, List<String>> mapObjectByClass) {
    if (mapObjectByClass == null)
        return ""
    StringBuilder sbResult = new StringBuilder()
    for (Class clazz: mapObjectByClass.keySet()) {
        def String columnName = "title"
        if (CCAMCoreBO.class.isAssignableFrom(clazz))
            columnName = "persistedDisplayableTitle"

        def List<String> uuidsByClass = mapObjectByClass.get(clazz)

        def int noBatch = 0
        def final int COUNT_IN_BATCH = 1000
        while (noBatch < uuidsByClass.size()) {
            StringBuilder sbQuery = new StringBuilder()
            sbQuery.append("select ")
                    .append(columnName)
                    .append(" from ")
                    .append(clazz.getName())
                    .append(" obj where obj.id in (")

            def int startLengthSbQuery = sbQuery.length()
            def int maxInBatch = ((noBatch + COUNT_IN_BATCH) < uuidsByClass.size()) ? (noBatch + COUNT_IN_BATCH) : uuidsByClass.size()
            for(def int indexInBatch = noBatch; indexInBatch < maxInBatch; indexInBatch++) {
                sbQuery.append("'").append(uuidsByClass.get(indexInBatch)).append("',")
            }
            sbQuery.deleteCharAt(sbQuery.length() - 1)
            sbQuery.append(")")

            def List<String> objectTitles = helper.select(sbQuery.toString())
            sbResult.append(StringUtilities.join(objectTitles, ", "))
            noBatch += COUNT_IN_BATCH
        }
    }
    return sbResult.toString()
}

private getReportFieldsByProgramID(memberEmployee, queries, programId, valuesByProgram) {
    def Object[] val = new Object[7]

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
    
    valuesByProgram.put(programId, val)
}

private List getEmployees(String email, String workName) {
    def StringBuilder sb = new StringBuilder()
    sb.append("SELECT es FROM CoreLogin login INNER JOIN login.person es ") //Выводим пользователей у которых есть логин и пароль

    if (!StringUtilities.isEmptyTrim(email))
        sb.append(" WHERE lower(es.email) like :email")

    if (!StringUtilities.isEmptyTrim(workName))
    {
        if (!StringUtilities.isEmptyTrim(email))
            sb.append(" and ")
        else
            sb.append(" where ")
        sb.append(" es.id in (select post.employee.id from CorePost post where lower(post.parent.persistedDisplayableTitle) like :workName)")
    }

    //выводим всех пользователей с ролями определенными выше
    Query q = session.createQuery(sb.toString())
    if (!StringUtilities.isEmptyTrim(email))
        q.setParameter("email", "%$email%".toLowerCase())
    if (!StringUtilities.isEmptyTrim(workName))
        q.setParameter("workName", "%$workName%".toLowerCase())
    def employees = q.list()
    return employees
}

private getUserObjects(memberEmployee, queries, nonParentProgram, valuesByProgram) {
    
    queries.grantPerson.setParameter("memberId",  "$memberEmployee.UUID".toLowerCase())
    def List<String> objectUUIDs =  queries.grantPerson.list()
    for(def String uuidObj in objectUUIDs)
    {
        Class clazz = CoreBOHierarchyCache.getService().getClassByUUID(uuidObj)
        String uuidProgram = CoreBOHierarchyCache.getService().findParentByClass(uuidObj, ProgramStech.class)
        if (uuidProgram == null)
        {
            nonParentProgram.add(clazz)
            continue
        }

        if (valuesByProgram.containsKey(uuidProgram))
        {
            def Object[] val = valuesByProgram.get(uuidProgram)
            if (val[6] == null) {
                val[6] = new HashMap<Class, List<String>>()
            }

            def Map<Class, List<String>> mapObjectByClass = val[6]

            if (!mapObjectByClass.containsKey(clazz))
                mapObjectByClass.put(clazz, new ArrayList<String>())
            mapObjectByClass.get(clazz).add(uuidObj)
        }
    }
}

private handleEmployee(programs, queries, memberEmployee, nonParentProgram, rowByProgram) {
    def Map<String, Object[]> valuesByProgram = new HashMap<Integer, Object[]>()
   
    for (def programIdAndName in programs)
    {
        def String programId = programIdAndName[0]
        getReportFieldsByProgramID(memberEmployee, queries, programId, valuesByProgram)
    }

    //вывожу связанные с пользователем объекты и сортирую по программам
    getUserObjects(memberEmployee, queries, nonParentProgram, valuesByProgram)

    //Создаю строку для таблицы отчета и заношу их в таблицу относящуюся к программе (через Map<Программа, Таблица>)
    for (def String programId : valuesByProgram.keySet())
    {
        createRowEntry(valuesByProgram, programId, rowByProgram, report, queries, memberEmployee)
    }
}

private createRowEntry(Map valuesByProgram, String programId, rowByProgram, report, queries, memberEmployee) {
	Object[] vals = valuesByProgram.get(programId)

	if (!rowByProgram.containsKey(programId))
		rowByProgram.put(programId, report.createList())
	def listRows = rowByProgram.get(programId)

	def row = report.createObject()
	row.no = listRows.getRows().size() + 1
	row.name = "member"
	row.email = (String)vals[0]
	row.fio = (String)vals[1]
	row.programActions = (String)vals[2]
	row.workGroups = (String)vals[3]
	row.acceptanceCommission = (String)vals[4]
	row.competitiveCommission = (String)vals[5]
	row.objects = getProgramObjectsByEmployee(vals[6])

	queries.roleCapabilityRelation.setParameter("memberId",  "$memberEmployee.UUID".toLowerCase())
	
	def roleRels = queries.roleCapabilityRelation.list()
	def List<CorePost> listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
	int maxCountRowInMember = (listPosts.size() > roleRels.size()) ? listPosts.size() : roleRels.size()
	maxCountRowInMember = (maxCountRowInMember > 0) ? maxCountRowInMember : 1

	//Каждые место работы или роль в отдельной строке
	for (int i = 0; i < maxCountRowInMember; i++)
	{
		if (i > 0)
		{
			listRows.add(row)	
			row = report.createObject()
			row.no = listRows.getRows().size() + 1
			row.name = "member"
			row.email = ""
     		row.fio = ""
			row.programActions = ""
			row.workGroups = ""
			row.acceptanceCommission = ""
			row.competitiveCommission = ""
			row.objects = ""
		}

		//Добавление места работы
		if (i < listPosts.size())
		{
			def CorePost post = listPosts.get(i)
			row.post = post.getTitle()
			if (post.getParent() != null)
				row.job = post.getParent().getDisplayableTitle()
			else
				row.job = ""
		}
		else
		{
			row.post = ""
			row.job = ""
		}
		//Добавление роли
		if (i < roleRels.size())
		{
			def roleRel = roleRels.get(i)
			row.role = roleRel.getRightBO().getDisplayableTitle()
			row.createRole = StringUtilities.toReverseDateFormat(roleRel.getCreationDate())
		}
		else
		{
			row.role = ""
			row.createRole = ""
		}
	}

	listRows.add(row)
}

class Constants{
    public static def final String SEPARATOR = ", "
}

class Queries{
    Query queryForEmployee1
    Query queryForEmployee2
    Query queryForEmployee3
    Query queryForEmployee4
    Query roleCapabilityRelation
    Query grantPerson

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

        queryString = "select rcRel FROM RoleCapabilityRelation rcRel WHERE rcRel.leftBO.id = :memberId"        
        roleCapabilityRelation = session.createQuery(queryString)
        
        queryString = "select gp.object.id from GrantPerson gp where gp.roleCapability.leftBO.id=:memberId"
        grantPerson = session.createQuery(queryString)
    }
}