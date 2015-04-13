package resources.groovy.birt

import ru.naumen.stech.bobject.program.ProgramStech;
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.common.utils.StringUtilities
import ru.naumen.ccamcore.bobject.CCAMCoreBO;
import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.ccamcore.security.grant.GrantPerson
import ru.naumen.ccamcore.security.grant.GrantPersonHibernateHandler;
import ru.naumen.core.CoreBO
import ru.naumen.core.roles.Role
import ru.naumen.core.hibernate.HibernateUtil;
import ru.naumen.core.hierarchycache.CoreBOHierarchyCache;
import ru.naumen.core.catalogsengine.CoreCatalogItem
import ru.naumen.core.bobjects.person.login.CoreLogin;
import ru.naumen.fcntp.bobject.competitivecommission.CompetitiveCommissionStaff;
import ru.naumen.fcntp.bobject.program.workgroup.ProgramWorkGroup
import ru.naumen.fcntp.bobject.relations.PersonProgramActionRelation
import ru.naumen.fcntp.bobject.relations.PersonWorkgroupRelation
import ru.naumen.fcntp.bobject.relations.EmployeeAcceptanceCommissionRelation;
import ru.naumen.fcntp.bobject.relations.EmployeeCompetitiveCommissionRelation;

import java.util.ArrayList
import java.util.List
import java.util.Set
import java.util.HashSet
import java.util.Map
import java.util.HashMap


import org.apache.poi.hssf.record.ContinueRecord;
import org.hibernate.Query;

/*
 vars['email'] = ''
 vars['workName'] = ''
 */


String getProgramObjectsByEmployee (Map<Class, List<String>> mapObjectByClass)
{
    if (mapObjectByClass == null)
        return "";
    StringBuilder sbResult = new StringBuilder();
    for (Class clazz: mapObjectByClass.keySet())
    {
        def String columnName = "title";
        if (CCAMCoreBO.class.isAssignableFrom(clazz))
            columnName = "persistedDisplayableTitle";
        
        def List<String> uuidsByClass = mapObjectByClass.get(clazz);
        
        def int noBatch = 0;
        def final int COUNT_IN_BATCH = 1000;
        while (noBatch < uuidsByClass.size())
        {
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append("select ")
                .append(columnName)
                .append(" from ")
                .append(clazz.getName())
                .append(" obj where obj.id in (");
            
            def int startLengthSbQuery = sbQuery.length();
            def int maxInBatch = ((noBatch + COUNT_IN_BATCH) < uuidsByClass.size()) ? (noBatch + COUNT_IN_BATCH) : uuidsByClass.size();
            for(def int indexInBatch = noBatch; indexInBatch < maxInBatch; indexInBatch++)
            {
                sbQuery.append("'").append(uuidsByClass.get(indexInBatch)).append("',");
            }
            sbQuery.deleteCharAt(sbQuery.length() - 1);
            sbQuery.append(")");
            
            def List<String> objectTitles = helper.select(sbQuery.toString())
            sbResult.append(StringUtilities.join(objectTitles, ", "));
            noBatch += COUNT_IN_BATCH;
        }
    }
    return sbResult.toString();
}


def final String SEPARATOR = ", ";
def Set<String> nonParentProgram = new HashSet<String>()

def StringBuilder sb = new StringBuilder();
sb.append("SELECT es FROM CoreLogin login INNER JOIN login.person es "); //Выводим пользователей у которых есть логин и пароль

if (!StringUtilities.isEmptyTrim(email))
    sb.append(" WHERE lower(es.email) like :email");

if (!StringUtilities.isEmptyTrim(workName))
{
    if (!StringUtilities.isEmptyTrim(email))
        sb.append(" and ")
    else
        sb.append(" where ")
    sb.append(" es.id in (select post.employee.id from CorePost post where lower(post.parent.persistedDisplayableTitle) like :workName)");
}

//выводим всех пользователей с ролями определенными выше
Query q = HibernateUtil.currentSession().createQuery(sb.toString());
if (!StringUtilities.isEmptyTrim(email))
    q.setParameter("email", "%$email%".toLowerCase());
if (!StringUtilities.isEmptyTrim(workName))
    q.setParameter("workName", "%$workName%".toLowerCase());
def employees = q.list(); 
     
def xPrograms = report.createList();
def programs = helper.select("select ps.id, ps.persistedDisplayableTitle from ProgramStech ps")

def Map<String, Object> rowByProgram = new HashMap<String, Object>();
for (def memberEmployee in employees)
{
    //Заполняю те свойства (конкурсные комиссии, приемочные комиссии, мероприятия, рабочие группы), которые можно точно отсортировать по программе
    def Map<String, Object[]> valuesByProgram = new HashMap<Integer, Object[]>();
    for (def programIdAndName in programs)
    {
        def String programId = programIdAndName[0];
        def Object[] val = new Object[7];
        
        val[0] = memberEmployee.getEmail();
        val[1] =  memberEmployee.getDisplayableTitle();
        
        
        def List<String> progActions = helper.select("select rel.rightBO.persistedDisplayableTitle from " + PersonProgramActionRelation.class.getName() +
                " as rel where rel.leftBO.id = '$memberEmployee.UUID' and rel.rightBO.parent.id='$programId' and rel.typeKey='" +
                EmployeeStech.PROGRAM_ACTION_TYPE_KEY + "'");        
        val[2] = StringUtilities.join(progActions, SEPARATOR);
        
        def List<String> workGroupsByProgram = helper.select("select rel.rightBO.title from PersonWorkgroupRelation as rel  " +
            " where rel.leftBO.id = '$memberEmployee.UUID' " +
            "   and rel.rightBO.id in (select wgroup.workGroup.id from ProgramWorkGroup as wgroup where wgroup.parent.id='$programId')" +
            "   and rel.typeKey='" +EmployeeStech.WORK_GROUP_TYPE_KEY + "'"
        )
        val[3] = StringUtilities.join(workGroupsByProgram, SEPARATOR);
        
        def List<String> acceptanceCommissionTiltes = helper.select("select rel.rightBO.title from EmployeeAcceptanceCommissionRelation as rel  " +
            " where rel.leftBO.id = '$memberEmployee.UUID' " +
            "   and rel.rightBO.id in (select com.commission.id from ProgramAcceptanceCommission as com where com.parent.id='$programId')" +
            "   and rel.typeKey='$EmployeeAcceptanceCommissionRelation.TYPE_KEY'"
        )
        val[4] = StringUtilities.join(acceptanceCommissionTiltes, SEPARATOR)
                
        def List<String> competitiveCommissionTiltes = helper.select("select rel.leftBO.persistedDisplayableTitle from CompetitiveCommissionStaff as rel " +
            " where rel.rightBO.id='$memberEmployee.UUID' and rel.typeKey='$CompetitiveCommissionStaff.TYPE_KEY' and rel.leftBO.parent.id='$programId'"
        )
        val[5] = StringUtilities.join(competitiveCommissionTiltes, SEPARATOR)
        valuesByProgram.put(programId, val);
    }
    
    
    //вывожу связанные с пользователем объекты и сортирую по программам
    def List<String> objectUUIDs =  helper.select("select gp.object.id from GrantPerson gp where  gp.roleCapability.leftBO.id='$memberEmployee.UUID'")        
    for(def String uuidObj in objectUUIDs)
    {
        Class clazz = CoreBOHierarchyCache.getService().getClassByUUID(uuidObj);
        String uuidProgram = CoreBOHierarchyCache.getService().findParentByClass(uuidObj, ProgramStech.class);
        if (uuidProgram == null)
        {
            nonParentProgram.add(clazz);
            continue;
        }
        
        if (valuesByProgram.containsKey(uuidProgram))
        {
            def Object[] val = valuesByProgram.get(uuidProgram);
            if (val[6] == null) {
                 val[6] = new HashMap<Class, List<String>>();
            }
            
            def Map<Class, List<String>> mapObjectByClass = val[6];
            
            if (!mapObjectByClass.containsKey(clazz))
                mapObjectByClass.put(clazz, new ArrayList<String>());
            mapObjectByClass.get(clazz).add(uuidObj);
        }
    }
    
    //Создаю строку для таблицы отчета и заношу их в таблицу относящуюся к программе (через Map<Программа, Таблица>) 
    for (def String programId : valuesByProgram.keySet())
    {
        Object[] vals = valuesByProgram.get(programId);
                
        if (!rowByProgram.containsKey(programId))
            rowByProgram.put(programId, report.createList());
        def listRows = rowByProgram.get(programId);
         
        def row = report.createObject();
        row.no = listRows.getRows().size() + 1;        
        row.name = "member"
        row.email = (String)vals[0]
        row.fio = (String)vals[1]
        row.programActions = (String)vals[2]
        row.workGroups = (String)vals[3]
        row.acceptanceCommission = (String)vals[4]
        row.competitiveCommission = (String)vals[5]
        row.objects = getProgramObjectsByEmployee(vals[6]);
        
        
        def roleRels = helper.select("select rcRel FROM RoleCapabilityRelation rcRel" +
            " WHERE rcRel.leftBO.id = '$memberEmployee.UUID' "
        )
        
        def List<CorePost> listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
        
        int maxCountRowInMember = (listPosts.size() > roleRels.size()) ? listPosts.size() : roleRels.size();
        maxCountRowInMember = (maxCountRowInMember > 0) ? maxCountRowInMember : 1;
         
        //Каждые место работы или роль в отдельной строке
        for (int i = 0; i < maxCountRowInMember; i++)
        {
            if (i > 0)
            {
                listRows.add(row);
                row = report.createObject()
                row.no = listRows.getRows().size() + 1;
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
                def CorePost post = listPosts.get(i);
                row.post = post.getTitle();
                if (post.getParent() != null)
                    row.job = post.getParent().getDisplayableTitle();
                else
                    row.job = "";
            }
            else
            {
                row.post = ""
                row.job = ""
            }
            //Добавление роли
            if (i < roleRels.size())
            {
                def roleRel = roleRels.get(i);
                row.role = roleRel.getRightBO().getDisplayableTitle()
                row.createRole = StringUtilities.toReverseDateFormat(roleRel.getCreationDate())
            }
            else
            {
                row.role = ""
                row.createRole = ""
            }
        }
        
        listRows.add(row);
            
    }
}

def programList = report.createList();
for (def String[] programIdAndName : programs)
{
    def xProgram = report.createObject();
    xProgram.title=programIdAndName[1];
    xProgram.name = "program"
    
    if (rowByProgram.containsKey(programIdAndName[0]))    
        xProgram.members = rowByProgram.get(programIdAndName[0]);
    else
    xProgram.members = report.createList();
    programList.add(xProgram);
}
    
def xc = report.createObject();
xc.nonParentProgram = StringUtilities.join(nonParentProgram, SEPARATOR);
xc.name = "group"
xc.programs = programList;
report.rootObject = xc

report