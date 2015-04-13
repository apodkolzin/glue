import org.hibernate.Query

import ru.naumen.ccamcore.bobject.CCAMCoreBO
import ru.naumen.ccamcore.security.snippets.DistributionRoleSnippetBase
import ru.naumen.ccamcore.security.snippets.RoleCheckContextForCriteria
import ru.naumen.ccamcore.security.snippets.RoleSnippetFcntpBase
import ru.naumen.common.utils.StringUtilities
import ru.naumen.core.hibernate.HibernateUtil
import ru.naumen.core.hierarchycache.CoreBOHierarchyCache
import ru.naumen.core.roles.RoleCheckContext
import ru.naumen.core.util.hquery.HCriteria
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.stech.bobject.program.ProgramStech
import ru.naumen.ccamcore.security.snippets.MultiDistributionSnippetBase

//email = ''
//workName = ''
//programUUID = 'corebofs000080000gte9v29hjmmodmk'
  
    def program = helper.get(programUUID)
    
    queries = new Queries4(session)
    
    def roles = ["AssistantManagerFromExecuter", "MonitorContract", "MonitorProjectLot","ManagerFromExecuter", "ContractEmployeeSimple", "ContractAdminisratorAssign",
        "ContractAdminisratorAssignEx", "DemandReader", "RequirementReader", "ExaminationTaskExpert", "ReportMaterialExpert", "CompetitionDocumentExpert",
        "ContractReaderEx", "ContractReader", "ContractExpert", "ExaminationTaskExpertEn", "ExpertRquirementTechplatform"
    ]
    
    def Set<String> nonParentProgram = new HashSet<String>()
    def StringBuilder sb = new StringBuilder()
    sb.append("select distinct es FROM RoleCapabilityRelation rcRel, EmployeeStech es ")
    sb.append("WHERE rcRel.leftBO = es.id and rcRel.rightBO in (select r from Role r where r.Id in (")
    for (def role: roles)
    {
        sb.append("'").append(role).append("'").append(",")
    }
    sb.deleteCharAt(sb.length() - 1)
    sb.append(")) ")
    
    if (!StringUtilities.isEmptyTrim(email))
    {
        sb.append(" and lower(es.email) like :email")
    }
    
    if (!StringUtilities.isEmptyTrim(workName))
    {
        sb.append(" and es.id in (select post.employee.id from CorePost post where lower(post.parent.persistedDisplayableTitle) like :workName)")
    }
    
    //выводим всех пользователей с ролями определенными выше
    Query q = HibernateUtil.currentSession().createQuery(sb.toString())
    if (!StringUtilities.isEmptyTrim(email))
    {
        q.setParameter("email", "%$email%".toLowerCase())
    }
    if (!StringUtilities.isEmptyTrim(workName))
    {
        q.setParameter("workName", "%$workName%".toLowerCase())
    }
    def employees = q.list() 
     
    
    def String SEPARATOR = ", "
        
    int indexRow = 0
    def values = report.createList()
    
    for(def EmployeeStech memberEmployee in employees){
    
    	rQuery = queries.roleRels
        rQuery.setParameter('memberId', memberEmployee.UUID)
        roleRels = rQuery.list()
        
        valSizeBefore = values.rows.size()
    	
    	//Вывожу только те объекты, которые относятся к программе
        objMap = new java.util.HashMap()
        roleCreations = new java.util.HashMap()
        for(roleEntry in roleRels){
          
            sb.setLength(0)
            snippets = getAllSnippets(roleEntry.getRightBO().getSnippet())
                  
            foundObjects = []
            for(snippet in snippets){
            	foundObjects.addAll(getPermittedObjects(snippet, memberEmployee, roleEntry, session))	
            }
                
            for(curObj in foundObjects){
                    String uuidProgram = CoreBOHierarchyCache.getService().findParentByClass(curObj.UUID, ProgramStech.class)
                    if (uuidProgram == null)
                    {
                        nonParentProgram.add(curObj.getClass())
                        continue
                    }
                    if (!uuidProgram.equals(programUUID))
                        continue
                    
                    if (!CCAMCoreBO.class.isAssignableFrom(curObj.getClass()))
                    {
                          sb.append(curObj.title).append(SEPARATOR)
                    } else {
                          sb.append(curObj.persistedDisplayableTitle).append(SEPARATOR)
                    }
                }
        
        	objStr = sb.toString()
        	
        	if(objStr.length()==0){
        		continue
        	}
        	
           	objStr = objStr.substring(0, objStr.length()-2)
       	
            objMap.put(roleEntry.getRightBO(), objStr)
            roleCreations.put(roleEntry.getRightBO(), roleEntry.getCreationDate())
        }
      
        if(objMap.keySet().size()==0)
        	continue
        	
        def List<CorePost> listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
        def post=""
        for(curPost in listPosts){
        	
        	post = "<p>"+curPost.getTitle()+"</p>"
        	
            if (curPost.getParent() != null)
            {
               	post += "<br><p style=\"margin-left: 10px;\">" + curPost.getParent().getDisplayableTitle()+"</p>"
            }
        }
        	
        for(role in objMap.keySet()){
        	def row = report.createObject()
        	
        	if(valSizeBefore==values.rows.size()){
        		indexRow++
        	}
        	
         	row.no = indexRow
         	
        	row.name = "member"
        	row.email = memberEmployee.getEmail()
        	row.fio =  memberEmployee.getDisplayableTitle()
        	row.post = post
			
			
			row.role = role.getDisplayableTitle()
    		row.objects = objMap.get(role)
            row.createRole = roleCreations.get(role)
            values.add(row)
        }
    }
    
    sb.setLength(0)
    for(String err : nonParentProgram)
    {
        sb.append(err).append(",")
    }
    if (sb.length() > 0)
    {
        sb.deleteCharAt(sb.length() - 1)
    }
        
    def xc = report.createObject().grabObject(program, "title")
    xc.members=values
    if (sb.length() > 0)
    {
        xc.nonParentProgram = sb.toString()
    }
    report.rootObject = xc

report
      
class Queries4{
    Query roleRels

    Queries4(session){
    	def roles = ["AssistantManagerFromExecuter", "MonitorContract", "MonitorProjectLot","ManagerFromExecuter", "ContractEmployeeSimple", "ContractAdminisratorAssign",
        "ContractAdminisratorAssignEx", "DemandReader", "RequirementReader", "ExaminationTaskExpert", "ReportMaterialExpert", "CompetitionDocumentExpert",
        "ContractReaderEx", "ContractReader", "ContractExpert", "ExaminationTaskExpertEn", "ExpertRquirementTechplatform"]
    
    	def sb = new StringBuilder()
        for (def role: roles)
        {
            sb.append("'").append(role).append("',")
        }
        sb.deleteCharAt(sb.length() - 1)
        roleRels = session.createQuery("select distinct rcRel FROM RoleCapabilityRelation rcRel" +
            " WHERE rcRel.rightBO in (select r from Role r where r.Id in (" + sb.toString() + ")) " +
            "  and rcRel.leftBO.id = :memberId"
        )
    }
}

def getPermittedObjects(snippet, memberEmployee, roleEntry, session){
    RoleCheckContext ctx = new RoleCheckContext(memberEmployee, roleEntry.getRightBO(), session);
    
    String alias = "alias";
    final Class objClass = snippet.getDistributingClass()

    HCriteria c = new HCriteria()
                .addSource(objClass, alias)
                .setPredicate(alias)

    ctx1 = new RoleCheckContextForCriteria(ctx)
            .setCriteria(c)
            .setSourceAlias(alias)
            .setSourceClass(objClass)
    
    if(((RoleSnippetFcntpBase)snippet).customizeCriteria(ctx1)){
        foundObjects = c.createQuery(ctx.getSession()).list()
        return foundObjects
    }
return []
}

def getAllSnippets(snippet){
    snippets = []
    if(snippet instanceof DistributionRoleSnippetBase){
    	snippets.add(snippet)           
    }
    else if(MultiDistributionSnippetBase.class.isAssignableFrom(snippet.class)){
    	for(childSnippet in snippet.getSnippets().values()){
    		snippets.add(getAllSnippets(childSnippet))
        }
    }
    
	return snippets        
}