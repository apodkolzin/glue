import org.hibernate.Query
import ru.naumen.core.ui.BKUIUtils
import ru.naumen.core.usertypes.RegistryType
import ru.naumen.fcntp.bobject.demand.DemandFcntpHibernateHandler
import ru.naumen.fcntp.bobject.expertregistry.ExpertRegistryHibernateHandler
import ru.naumen.fcntp.bobject.registry.AddExpertToRegistryBA
import ru.naumen.guic.components.forms.UIForm

/**
 * Created by azimka on 13.11.14.
 *
 */

def expertRegistry = subject
def lot = expertRegistry.parent

validate(lot)

helper.execute { s ->

    registryTypes = [] as Set
    registryTypes.add(RegistryType.APPROVED_REGISTER_TYPE);
    registryTypes.add(RegistryType.POTENTIAL_REGISTER_TYPE);

    def experts = getExperts(lot, registryTypes, s)
    for (def empStech in experts)
    {
        registryTypes = [] as Set
        registryTypes.add(RegistryType.APPROVED_REGISTER_TYPE);
        registryTypes.add(RegistryType.POTENTIAL_REGISTER_TYPE);

        if (isRelationNotExist(expertRegistry, empStech, s))
        {
            def author = BKUIUtils.currentPerson
            def organizations = empStech.organizations as Set
            def academicDegrees = empStech.academicDegree as Set
            def academicStatuses = empStech.academicStatus as Set
            def stages = [] as Set
            stages.add(expertRegistry.currentStage)
            AddExpertToRegistryBA addExpertToRegistryBA = new AddExpertToRegistryBA(expertRegistry,
                    empStech, organizations, academicDegrees,
                    academicStatuses, stages, author).execute(s)
        }
    }
}

def isRelationNotExist(registry, empStech, session)
{
    return ExpertRegistryHibernateHandler.getExpertRegistryToEmployeeRelation(registry, empStech, session) == null
}

def getExperts(lot, registryTypes, session)
{
    Query query = session.createQuery("select emp from EmployeeStech emp join emp.appliedSciencesPriorities" +
            " appPriors where appPriors.id = :lotSciencePriorsID and exists (from ExpertRegistryEntry ent join ent.programs pr" +
            " join ent.priorityLines pl where  ent.leftBO = emp and pr.id = :programId " +
            " and ent.registryType in (:registryTypes) and pl.id in (:priorLines))")

    query.setParameter("programId", lot.parent.UUID)
    query.setParameterList("registryTypes", registryTypes);
    query.setParameterList("priorLines", lot.subPriorityLines.collect({ it.UUID }))
    query.setParameter("lotSciencePriorsID", lot.appliedSciencesPriorities.UUID)
    query.list()
}

def validate(lot) {
    if (DemandFcntpHibernateHandler.listDemands(lot).size() == 0)
        throw new UIForm.UIFormUserException('Формирование Промежуточного реестра экспертов невозможно. Необходимо наличие Заявок по Лоту.')
  if(lot.appliedSciencesPriorities == null){
  	throw new UIForm.UIFormUserException("Формирование Промежуточного реестра экспертов невозможно. Необходимо заполнить блок 'Рубрикатор направлений исследования' Лота.")
  }
  if (lot.subPriorityLines.isEmpty())
  {
      throw new UIForm.UIFormUserException("Формирование Промежуточного реестра экспертов невозможно. Необходимо заполнить блок 'Направление и технологии' Лота.")
  }
}
