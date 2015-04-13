package resources.groovy

import ru.naumen.fcntp.bobject.stage.AddFcntpStageWorkResultBA
import ru.naumen.common.containers.MapPropertySource
import ru.naumen.core.catalogsengine.CoreCatalogRegistry
import ru.naumen.ccamcore.bobject.CcamCoreCategories
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import org.hibernate.Session

docCases = CoreCatalogRegistry.getInstance().getCatalog(CcamCoreCategories.CATALOG_CATEGORY_DOCUMENT);

int getDocVersion(Object parent, String caseCode, Session s)
{
    def q = s.createQuery("select docVersion from StageReportDocumentation where parent.id='$parent.UUID' and BOCase.code='$caseCode' order by docVersion desc")
    q.setMaxResults(1)
    value = q.uniqueResult()
    docVersion = value == null ? 1 : value.intValue() + 1;
    return docVersion;
}

void updateObject(Object obj, Object ps, Session s)
{
    ru.naumen.common.utils.ReflectionTools.setFieldValuesFromPropertySource(obj, ps)
    obj.doUpdate(s)
}

MapPropertySource ps(Map map)
{
    return new MapPropertySource(map)
}

void createDoc(String caseCode, Session s)
{
    parent = subject.parent
    docVersion = getDocVersion(parent, caseCode, s);
    boCase = docCases.getItem(caseCode)
    new AddFcntpStageWorkResultBA(ps(["identifier":'1', "title":boCase.title, "docVersion":docVersion,"activeVersion":true, "category":boCase]), user, parent).execute(session)
    updateObject(subject, ps(["activeVersion":false]), s)
}

createDoc("stageReportDocumentation_218", session);