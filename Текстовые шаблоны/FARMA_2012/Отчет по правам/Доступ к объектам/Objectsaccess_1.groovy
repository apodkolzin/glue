package resources.groovy.birt

import ru.naumen.ccamcore.bobject.CCAMCoreBO;
import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.core.CoreBO
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.core.roles.Role
import ru.naumen.common.utils.StringUtilities
import ru.naumen.ccamcore.security.grant.GrantPerson
import ru.naumen.ccamcore.security.grant.GrantPersonHibernateHandler;
import ru.naumen.core.catalogsengine.CoreCatalogItem
import ru.naumen.fcntp.bobject.program.workgroup.ProgramWorkGroup
import ru.naumen.core.hibernate.HibernateUtil;
import ru.naumen.core.hierarchycache.CoreBOHierarchyCache;
import ru.naumen.stech.bobject.program.ProgramStech;

import java.util.List
import java.util.Set
import java.util.HashSet
import java.util.Map
import java.util.HashMap

import org.apache.poi.hssf.record.ContinueRecord;
import org.hibernate.Query;

/*
 vars['programUUID'] = 'corebofs000080000gte9v29hjmmodmk'
 vars['email'] = ''
 vars['workName'] = ''
 */
    def program = helper.get(programUUID)
    
    def roles = ["AssistantManagerFromExecuter", "MonitorContract", "MonitorProjectLot","ManagerFromExecuter", "ContractEmployeeSimple", "ContractAdminisratorAssign",
        "ContractAdminisratorAssignEx", "DemandReader", "RequirementReader", "ExaminationTaskExpert", "ReportMaterialExpert", "CompetitionDocumentExpert",
        "ContractReaderEx", "ContractReader", "ContractExpert", "ExaminationTaskExpertEn", "ExpertRquirementTechplatform"
    ]
    def Set<String> nonParentProgram = new HashSet<String>()
    def StringBuilder sb = new StringBuilder();
    sb.append("select distinct es FROM RoleCapabilityRelation rcRel, EmployeeStech es ")
    sb.append("WHERE rcRel.leftBO = es.id and rcRel.rightBO in (select r from Role r where r.Id in (")
    for (def role: roles)
    {
        sb.append("'").append(role).append("'").append(",");
    }
    sb.deleteCharAt(sb.length() - 1);    
    sb.append(")) ");  
    
    if (!StringUtilities.isEmptyTrim(email))
    {
        sb.append(" and lower(es.email) like :email");
    }
    
    if (!StringUtilities.isEmptyTrim(workName))
    {
        sb.append(" and es.id in (select post.employee.id from CorePost post where lower(post.parent.persistedDisplayableTitle) like :workName)")
    }
    
    //выводим всех пользователей с ролями определенными выше
    Query q = HibernateUtil.currentSession().createQuery(sb.toString());
    if (!StringUtilities.isEmptyTrim(email))
    {
        q.setParameter("email", "%$email%".toLowerCase());
    }
    if (!StringUtilities.isEmptyTrim(workName))
    {
        q.setParameter("workName", "%$workName%".toLowerCase());
    }
    def employees = q.list(); 
     
    
    def String SEPARATOR = ", "
        
    int indexRow = 0
    def values = report.createList()
    for (def EmployeeStech memberEmployee in employees)
    {
        def row = report.createObject()
        indexRow++;
        row.no = indexRow
        
        row.name = "member"
        row.email = memberEmployee.getEmail()
        row.fio =  memberEmployee.getDisplayableTitle()
            
        //Вывожу только те объекты, которые относятся к программе        
        def Map<Class, List<String>> mapObjectByClass = new HashMap<Class, List<String>>();
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
            if (!uuidProgram.equals(program.UUID))
                continue;
                
            if (!mapObjectByClass.containsKey(clazz))
            {
                mapObjectByClass.put(clazz, new ArrayList<String>());
            }
            mapObjectByClass.get(clazz).add(uuidObj);
        }
        
        sb.setLength(0)
        for (Class clazz: mapObjectByClass.keySet())
        {
            def String columnName = "title";
            if (CCAMCoreBO.class.isAssignableFrom(clazz))
            {
                columnName = "persistedDisplayableTitle"
            }
            
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
                for (String objectTitle: objectTitles)
                {
                    sb.append(objectTitle).append(SEPARATOR)
                }
                if (objectTitles.size() > 0)
                {
                    sb.delete(sb.length() - SEPARATOR.length(), sb.length());
                }
                noBatch += COUNT_IN_BATCH;
            }
            
        }            
        row.objects = sb.toString()
        
        sb.setLength(0)
        for (def role: roles)
        {
            sb.append("'").append(role).append("',");;
        }
        sb.deleteCharAt(sb.length() - 1);
        def roleRels = helper.select("select rcRel FROM RoleCapabilityRelation rcRel" +
            " WHERE rcRel.rightBO in (select r from Role r where r.Id in (" + sb.toString() + ")) " +
            "  and rcRel.leftBO.id = '$memberEmployee.UUID' "
         )
        
        def List<CorePost> listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
        
        int maxCountRowInMember = (listPosts.size() > roleRels.size()) ? listPosts.size() : roleRels.size();
        maxCountRowInMember = (maxCountRowInMember > 0) ? maxCountRowInMember : 1;
         
        //Каждые место работы или роль в отдельной строке
        for (int i = 0; i < maxCountRowInMember; i++)
        {
            if (i > 0)
            {
                values.add(row);
                row = report.createObject()
                indexRow++;
                row.no = indexRow;
                row.name = "member"
                row.email = ""
                row.fio = ""
                row.objects = ""
            }
            
            //Добавление места работы
            if (i < listPosts.size())
            {
                def CorePost post = listPosts.get(i);
                row.post = post.getTitle()
                if (post.getParent() != null)
                {
                    row.job = post.getParent().getDisplayableTitle()
                }
                else
                {
                    row.job = ""
                }
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
        
        values.add(row)
            
    }
    
    sb.setLength(0)
    for(String err : nonParentProgram)
    {
        sb.append(err).append(",")
    }
    if (sb.length() > 0)
    {
        sb.deleteCharAt(sb.length() - 1);    
    }
        
    def xc = report.createObject().grabObject(program, "title")    
    xc.members=values    
    if (sb.length() > 0)
    {
        xc.nonParentProgram = sb.toString()
    }
    report.rootObject = xc

report