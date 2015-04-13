package resources.groovy

import ru.naumen.fcntp.bobject.stage.AddFcntpStageWorkResultBA
import ru.naumen.common.containers.MapPropertySource
import ru.naumen.core.catalogsengine.CoreCatalogRegistry
import ru.naumen.ccamcore.bobject.CcamCoreCategories

docCases = CoreCatalogRegistry.getInstance().getCatalog(CcamCoreCategories.CATALOG_CATEGORY_DOCUMENT);

void createDoc(String caseCode)
{
    def q = session.createQuery("select count (*) from FcntpDocumentContract where parent.id='$subject.UUID' and BOCase.code='$caseCode'")
    if (q.uniqueResult() > 0)
        return;
    boCase = docCases.getItem(caseCode)
    new AddFcntpStageWorkResultBA(new MapPropertySource(["identifier":'1', "title":boCase.title, "category":boCase]), user, subject).execute(session)
}

createDoc("contractProject");