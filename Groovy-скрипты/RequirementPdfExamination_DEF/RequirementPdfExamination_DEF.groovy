import org.apache.commons.lang3.StringUtils
import org.hibernate.Session

import ru.naumen.ccam.bobject.program.action.ProgramAction
import ru.naumen.ccamext.docgen.PdfDocument
import ru.naumen.ccamext.docgen.PdfTable
import ru.naumen.ccamext.docgen.PdfTableCell
import ru.naumen.common.utils.CollectionUtils
import ru.naumen.common.utils.DateUtils
import ru.naumen.common.utils.StringUtilities
import ru.naumen.core.catalogsengine.ICoreCatalog.CoreCatalogItemNotFoundException
import ru.naumen.core.currency.Money
import ru.naumen.core.ui.BKUIUtils
import ru.naumen.fcntp.bobject.contract.ContractFcntp
import ru.naumen.fcntp.bobject.requirement.executors.RequirementCoExecutorsHibernateHandler
import ru.naumen.fcntp.catalog.FormalValidationCriteriaCatalog
import ru.naumen.fcntp.catalog.FormalValidationCriteriaCatalogItem
import ru.naumen.fcntp.examination.ComparativeAnalysisUtil
import ru.naumen.fcntp.examination.ComparativeAnalysisUtil.ItemForSort
import ru.naumen.fcntp.measurement.celltype.RequirementFcntpWorkPriceBox
import ru.naumen.fx.objectloader.PrefixObjectLoaderFacade
import ru.naumen.guic.formatters.DoubleFormatter
import ru.naumen.guic.formatters.TitledFormatter

import com.lowagie.text.Element
import com.lowagie.text.Paragraph

/**
* Скрипт создания pdf-документа предварительной экспертизы Предложения
* version 1.0
*/

//def role = PrefixObjectLoaderFacade.getObjectByUUID("pstcimfs000080000iui7epi7ikr2m00",session)
//def role = PrefixObjectLoaderFacade.getObjectByUUID("pstcimfs000080000jc417hg5np509rg",session)//для транка
def role = PrefixObjectLoaderFacade.getObjectByUUID("pstcimfs000080000jc43fkjjvpo7mrc",session)//для бранча
def customer= PrefixObjectLoaderFacade.getObjectByUUID("corebofs000080000h3201kpjdprk0pk", session)

log.info('Start generating pdf-document for ' + req.getDisplayableTitle());

ProgramAction pm = req.getProgramAction(); // ПМ должен быть
String pmDispTitle = TitledFormatter.sFormat(pm);
String pmTitle = pm.getTitle();
String pmId = pm.getDisplayableIdentifier();

FormalValidationCriteriaCatalog valCatalog = helper.getCatalog("FormalValidationCriteria", session);
FormalValidationCriteriaCatalogItem item = FormalValidationCriteriaCatalog.getItem(valCatalog, pm, session);

// -------------- Реализация раздела «title» -----------------------------------

/*helper.addTextCenter(document, "Отчет о проверке соответствия Предложения № incomeNumber от incomeDate условиям выполнения исследований и проектов в рамках мероприятия федеральной целевой программы «Исследования и разработки по приоритетным направлениям развития научно-технологического комплекса России на 2014 – 2020 годы»",
        [ 'incomeNumber' : req.getIncomeNumber(), 'incomeDate' : DateUtils.date2StrSafe(req.getIncomeDate())]);
*/

text = helper.replaceParameters("Отчет о проверке соответствия Предложения № incomeNumber от incomeDate условиям выполнения исследований и проектов в рамках мероприятия федеральной целевой программы «Исследования и разработки по приоритетным направлениям развития научно-технологического комплекса России на 2014 – 2020 годы»",
        [ 'incomeNumber' : req.getIncomeNumber(), 'incomeDate' : DateUtils.date2StrSafe(req.getIncomeDate())]);
document.addHead(text, PdfDocument.fontSerif12Bold, Paragraph.ALIGN_CENTER);
        helper.addBreak(document);

helper.addText(document, "Инициатор: wwEmployee ,",
[ 'wwEmployee' : req.organization?.title ]);
helper.addBreak(document);
        
helper.addText(document, "Тема проекта: title ,",
[ 'title' : req.title ]);
helper.addBreak(document);

helper.addText(document, "Мероприятие Программы: programAction ,",
[ 'programAction' : req.programAction?.displayableTitle ]);
helper.addBreak(document);

helper.addText(document, "Дата проведения проверки: creationDate г.,",
[ 'creationDate' : DateUtils.date2StrSafe(new Date())]);
helper.addBreak(document);

helper.addText(document, "В результате проведения проверки на соответствие предложения №incomeNumber от incomeDate г., " +
    "тема проекта: «reqTitle», заявитель - reqAuthor», установлено: ",
    [ 'incomeNumber' : req.getIncomeNumber(),
    'incomeDate' : DateUtils.date2StrSafe(req.getIncomeDate()),
    'reqTitle' : req.getTitle(),
    'reqAuthor' : helper.getRequirementAuthor(req) ]);
helper.addBreak(document);

// -------------------- Реализация раздела «body» ------------------------------
log.info("---- generating body ------");
// -------------------- Реализация раздела «chapter1» --------------------------
log.info("generating chapter1");
helper.addTitleText(document, "I. В части соответствия проекта формальным требованиям Программы:");

def formalReqsList = [];

boolean fitByDueDate = true;
boolean fitByBudget = true;

RequirementFcntpWorkPriceBox box = RequirementFcntpWorkPriceBox.getBox(req, session);
String budgetRFCode = "budgetRF";
// проверка свойства «Срок выполнения работ по проекту (в месяцах)»


    /*if (req.getDueMonth() == null)
{
formalReqsList.add(helper.createListItem("Не удаётся осуществить проверку так как в заявке " +
"на формирование тематики не заполнено поле \"Срок выполнения (месяцы)\".",
[ : ]));
}
else
{
Integer dueDateMin = item.getDueDateMin();
if (dueDateMin == null)
{
dueDateMin = 0;
}
Integer dueDateMax = item.getDueDateMax();
if (dueDateMax == null)
{
dueDateMax = 0;
}
int errorCode = helper.inRange(req.getDueMonth(), dueDateMin, dueDateMax);
fitByDueDate = false;
if (errorCode < 0) // меньше минимума
{
formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth не соответствует " +
"установленному Программой сроку реализации проектов (не менее dueDateMin мес.), " +
"выполняемых в рамках программного мероприятия pmTitle.",
[ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
'dueDateMin' : dueDateMin,
'pmTitle' : pmId]));
}
else if (errorCode > 0) // больше максимума
{
formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth не соответствует " +
"установленному Программой сроку реализации проектов (не более dueDateMax мес.), " +
"выполняемых в рамках программного мероприятия pmTitle.",
[ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
'dueDateMax' : dueDateMax,
'pmTitle' : pmId]));
}
else // все хорошо
{
fitByDueDate = true;
StringBuilder sb = new StringBuilder();
if (dueDateMin > 0)
{
sb.append(" (не менее ");
sb.append(dueDateMin);
sb.append(" мес.");
if (dueDateMax > 0)
{
sb.append(" и ");
}
else
{
sb.append(")");
}
}
if (dueDateMax > 0)
{
if (dueDateMin == 0)
{
sb.append(" (");
}
sb.append("не более ");
sb.append(dueDateMax);
sb.append(" мес.)");
}
String dueString = sb.toString();
formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth соответствует " +
"установленному Программой сроку реализации проектовdueString, " +
"выполняемых в рамках программного мероприятия pmTitle.",
[ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
'dueString' : dueString,
'pmTitle' : pmId]));
}
helper.addBreak(document);
}*/


        Double dueDateMin = report.getMinDueMonth() == null ? item?.getDueDateMin()?.doubleValue() : report.getMinDueMonth()
        
        if (dueDateMin == null)
        {
                dueDateMin = 0.0;
        }
        
        Double dueDateMax = report.getMaxDueMonth() == null ? item?.getDueDateMax()?.doubleValue() : report.getMaxDueMonth()
        
        if (dueDateMax == null)
        {
                dueDateMax = 0.0;
        }

        fitByDueDate = false;
        if (boxAnalyzer(box, budgetRFCode, session).doubleValue()*12 < dueDateMin) // меньше минимума
        {
                formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth не соответствует " +
                                "установленному Программой сроку реализации проектов (не менее dueDateMin мес.), " +
                                "выполняемых в рамках программного мероприятия pmTitle.",
                                [ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
                                        'dueDateMin' : dueDateMin,
                                        'pmTitle' : pmId]));
        }
        else if (boxAnalyzer(box, budgetRFCode, session).doubleValue()*12 > dueDateMax) // больше максимума
        {
                formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth не соответствует " +
                                "установленному Программой сроку реализации проектов (не более dueDateMax мес.), " +
                                "выполняемых в рамках программного мероприятия pmTitle.",
                                [ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
                                        'dueDateMax' : dueDateMax,
                                        'pmTitle' : pmId]));
        }
        else // все хорошо
        {
                fitByDueDate = true;
                StringBuilder sb = new StringBuilder();
                if (dueDateMin > 0)
                {
                        sb.append(" (не менее ");
                        sb.append(dueDateMin);
                        sb.append(" мес.");
                        if (dueDateMax > 0)
                        {
                                sb.append(" и ");
                        }
                        else
                        {
                                sb.append(")");
                        }
                }
                if (dueDateMax > 0)
                {
                        if (dueDateMin == 0)
                        {
                                sb.append(" (");
                        }
                        sb.append("не более ");
                        sb.append(dueDateMax);
                        sb.append(" мес.)");
                }

                String dueString = sb.toString();
                formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth соответствует " +
                                "установленному Программой сроку реализации проектовdueString, " +
                                "выполняемых в рамках программного мероприятия pmTitle.",
                                [ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
                                        'dueString' : dueString,
                                        'pmTitle' : pmId]));
        }
        helper.addBreak(document);





    // проверка по объёму финансирования проекта
    fitByBudget = false;
    
        
        BigDecimal budgetRFMin = report.getMinBudget() == null ? item?.getBudgetMin() : report.getMinBudget()
    if (budgetRFMin == null)
    {
        budgetRFMin = 0;
    }
        BigDecimal budgetRFMax = report.getMaxBudget() == null ? item?.getBudgetMax() : report.getMaxBudget()
    if (budgetRFMax == null)
    {
        budgetRFMax = 0;
    }
    boolean budgetAsYearsSum = item?.isBudgetAsYearsSum();
    
    if (budgetAsYearsSum) // Бюджет указан как сумма по всем годам
    {
        BigDecimal budgetRF = box.getCellValue(budgetRFCode, null, session);
        errorCode = helper.inRangeBigDecimal(budgetRF, budgetRFMin, budgetRFMax);
        if (errorCode < 0) // меньше минимума
        {
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета " +
                "budgetRF руб. не соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не менее budgetMin), выполняемых в рамках программного мероприятия pmTitle.",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMin' : Money.formatBigDecimal(budgetRFMin),
                'pmTitle' : pmId]));
        }
        else if (errorCode > 0) // больше максимума
        {
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета " +
                "budgetRF руб. не соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не более budgetMax), выполняемых в рамках программного мероприятия pmTitle.",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                'pmTitle' : pmId]));
        }
        else
        {
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета " +
                "budgetRF руб. соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не более budgetMax), выполняемых в рамках программного мероприятия pmTitle.",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                'pmTitle' : pmId]));
            fitByBudget = true;
        }
    }
    else
    {
        subFit = true
    
                int period = boxAnalyzer(box, budgetRFCode, session)
                for (yearNumber = 1; yearNumber <= period;yearNumber++)
                {
            BigDecimal budgetRF = box.getCellValue(budgetRFCode, yearNumber.toString(), session);
            errorCode = helper.inRangeBigDecimal(budgetRF, budgetRFMin, budgetRFMax);
            if (errorCode < 0) // меньше минимума
            {
                formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета за yearNumber год " +
                    "budgetRF руб. не соответствует установленному Программой объёму финансирования проектов " +
                    "за счёт федерального бюджета (не менее budgetMin в год), выполняемых в рамках программного мероприятия pmTitle.",
                    ['yearNumber':yearNumber,
                                                 'budgetRF' : Money.formatBigDecimal(budgetRF),
                    'budgetMin' : Money.formatBigDecimal(budgetRFMin),
                    'pmTitle' : pmId]));
                subFit = false;
            }
            else if (errorCode > 0) // больше максимума
            {
                formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета за yearNumber год " +
                    "budgetRF руб. не соответствует установленному Программой объёму финансирования проектов " +
                    "за счёт федерального бюджета (не более budgetMax в год), выполняемых в рамках программного мероприятия pmTitle.",
                    [ 'yearNumber':yearNumber,
                                                'budgetRF' : Money.formatBigDecimal(budgetRF),
                    'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                    'pmTitle' : pmId]));
                subFit = false;
            }
                        else
                        {
                                formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета за yearNumber год " +
                                        "budgetRF руб. соответствует установленному Программой объёму финансирования проектов " +
                                        "за счёт федерального бюджета (не более budgetMax в год), выполняемых в рамках программного мероприятия pmTitle.",
                                        [ 'yearNumber':yearNumber,
                                                'budgetRF' : Money.formatBigDecimal(budgetRF),
                                        'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                                        'pmTitle' : pmId]));
                                subFit &= true;
                        }
                }
    }
    
    fitByBudget = subFit
    
    // проверка на соответствие минимальному ограничению по внебюджету
    
    String offBudgetCode = "offbudget";
    BigDecimal offBudget = box.getCellValue(offBudgetCode, null, session);
        double minPercent
        if (report.getOffBudgetMin() == null)
            minPercent = item?.getOffbudgetMin()?.doubleValue();
        else
                minPercent = report.getOffBudgetMin().doubleValue();
    BigDecimal budgetRF = box.getCellValue(budgetRFCode, null, session);
    BigDecimal wholeBudget = budgetRF.add(offBudget);
    double currentPercent = (wholeBudget.doubleValue() == 0) ? 0 : offBudget.multiply(new BigDecimal(100)).divide(wholeBudget, 2).doubleValue();
    
    boolean offBudgetFit = (minPercent > 0) ? (currentPercent >= minPercent) : true;
    String notStr = (offBudgetFit) ? "" : "не "
    String percentString = (minPercent > 0) ? "(minPercent% от общего финансирования проекта)" : "";
    formalReqsList.add(helper.createListItem("указанный уровень внебюджетного софинансирования проекта wholeBudget руб. " +
        percentString + " notStrсоответствует установленному Программой объёму внебюджетного софинансирования проектов " +
        ", выполняемых в рамках программного мероприятия pmTitle.",
        [ 'notStr' : notStr,
        'wholeBudget' : Money.formatBigDecimal(offBudget),
        'minPercent' : DoubleFormatter.format(minPercent),
        'pmTitle' : pmId]));
        
    fitByBudget &= offBudgetFit
    
    document.addNestedList(formalReqsList, false);
    helper.addBreak(document);
    helper.addBreak(document);


// ----------------- Реализация раздела «chapter2» -----------------------------
log.info("generating chapter2");

helper.addTitleText(document, "II. В части совпадения темы проекта и тем государственных контрактов, заключенных ранее на выполнение НИОКР:");
helper.addBreak(document);

double percentage = report.getPercentage();

String hqlThemesWithIDs = "select themeForComparsion, id from ContractFcntp where themeForComparsion is not null and parent.id in "+
"(select p from ProgramMember m inner join m.parent p where m.role=:role and m.customer=:customer)"+
" and creationDate <= :crDate"

ArrayList<ItemForSort> findedContracts = new ArrayList<ItemForSort>();
String searchTheme = ComparativeAnalysisUtil.preprocessTheme(req.getTitle());
final List<String> searchWordList = StringUtilities.wordList(searchTheme);
final int searchThemeSize = searchWordList.size() == 0 ? 1 : searchWordList.size();

final List<Object[]> sample = (List<Object[]>) session.createQuery(hqlThemesWithIDs)
        .setParameter("crDate", req.getIncomeDate())
        .setParameter("role", role)
        .setParameter("customer", customer)
        .list();

try {
    for (Object[] sampleThemeWithId : sample) {
        String sampleTheme = StringUtils.trimToEmpty((String) sampleThemeWithId[0]);                
        final List<String> sampleWordList = StringUtilities.wordList(sampleTheme);
        int sampleWordListCount = sampleWordList.size() == 0 ? 1 : sampleWordList.size();

        int searchToSampleCount = ComparativeAnalysisUtil.findMatchesCount(searchWordList, sampleWordList);
        int sampleToSearhcCount = ComparativeAnalysisUtil.findMatchesCount(sampleWordList, searchWordList);

        int k1 = (searchToSampleCount * 100) / searchThemeSize;
int k2 = (sampleToSearhcCount * 100) / sampleWordListCount;
        if (k1 >= percentage && k2 >= percentage) {
            findedContracts.add(new ItemForSort(sampleThemeWithId[1].toString(), k1, k2));
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}

if (findedContracts.isEmpty())
{
    helper.addText(document, "сравнительный анализ темы проекта и тем государственных контрактов, " +
        "заключенных ранее на выполнение НИОКР, совпадений не выявил (значение процентного соотношения: percentage%).",
        [ 'percentage' : DoubleFormatter.format(percentage) ]);
}
else
{
    helper.addText(document, "сравнительный анализ темы проекта и тем государственных контрактов, " +
        "заключенных ранее на выполнение НИОКР, выявил следующие совпадения " +
        "(значение процентного соотношения: более percentage%):",
        [ 'percentage' : DoubleFormatter.format(percentage) ]);
    
    PdfTable compareTable = new PdfTable(4, 1, 100);
    compareTable.setSpacing(0);
    compareTable.setPadding(1);
    compareTable.setWidths([15.0f, 35.0f, 35.0f, 15.0f]);

    compareTable.addCell("Номер государственного контракта", PdfDocument.fontSerif10);
    compareTable.addCell("Тема государственного контракта", PdfDocument.fontSerif10);
    compareTable.addCell("Исполнитель государственного контракта", PdfDocument.fontSerif10);
    compareTable.addCell("Значение процентного соотношения", PdfDocument.fontSerif10);
    
    findedContracts.each() { findedItem ->
        ContractFcntp contract = PrefixObjectLoaderFacade.getObjectByUUIDSafe(findedItem.getId(), session);
        compareTable.addCell(PdfTableCell.createHorizontalAndVerticalAlignmentCell(contract.getIdentifier(),
                PdfDocument.fontSerif12, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER));
        compareTable.addCell(contract.getFullTitle(), PdfDocument.fontSerif10);
        compareTable.addCell(contract.getPerformer() == null ? "" : contract.getPerformer().getTitle(), PdfDocument.fontSerif10);
        compareTable.addCell(String.valueOf(Math.min(findedItem.getK1(), findedItem.getK2())), PdfDocument.fontSerif10);
    };
    document.add(compareTable);
}
helper.addBreak(document);
helper.addBreak(document);

// ------------------ Реализация раздела «chapter3» ----------------------------
def childVisibility = helper.getChildVisibility(req, session);
def getEmptinessString(list, size) {
        if (list == null)
        {
                return 'Заполнение не предусмотрено';
        }
        else if (list.isEmpty())
        {
                return 'Заполнен полностью';
        }
        else if (list.size() == size)
        {
                return 'Полностью не заполнен';
        }
        else
        {
                return 'Заполнен не полностью';
        }
};

def isVisibleField(map, fieldName) {
        if (!map.containsKey(fieldName))
        {
                return true;
        }
        return map.get(fieldName);
};

def getCell(text) {
        return PdfTableCell.createHorizontalAndVerticalAlignmentCell(text,
                PdfDocument.fontSerif10, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER);
}
def criticallyFields = [
        'requirementProperties.researchEffortSet' : 'Научно-исследовательская работа',
        'requirementProperties.experimentalDevelopment' : 'Опытно-конструкторские работы',
        'requirementProperties.experimentalDevelopment' : 'Опытно-технологическая работа',
        'requirementProperties.developmentManagement' : 'Организация производства высокотехнологичной продукции',
        'requirementProperties.production' : 'Производство',
        'requirementProperties.conferenceSet' : 'Конференция/школа-семинар/выставка',
        'description' : 'Формулировка цели предлагаемого к реализации проекта',
        'leadingCriticalTech' : 'Основная критическая технология' ];
def emptyFields = helper.getEmptyFields(req, criticallyFields, childVisibility);
def executors = RequirementCoExecutorsHibernateHandler.listExecutorsRelation(req, session);
boolean cardCostBasisNirEmpty = false;
boolean cardCostBasisOkrEmpty = false;
boolean projectLineupEmpty = false;
boolean projectBudgetFinEmpty = false;
def emptyBudgetFields = helper.getEmptyFields(req, criticallyFields, childVisibility);
/*log.info("generating chapter3");

helper.addTitleText(document, "III. В части заполнения разделов информационной карты заявки данными:");
helper.addBreak(document);

PdfTable fillTable = new PdfTable(4, 1, 100);
fillTable.setSpacing(0);
fillTable.setPadding(1);
fillTable.setWidths([5.0f, 20.0f, 20.0f, 55.0f]);

fillTable.addCell("№ раздела", PdfDocument.fontSerif10Bold);
fillTable.addCell("Наименование раздела", PdfDocument.fontSerif10Bold);
fillTable.addCell("Оценка заполненности разделов данными", PdfDocument.fontSerif10Bold);
fillTable.addCell("Перечень основных информационных полей, не заполненных данными", PdfDocument.fontSerif10Bold);

// Раздел №1:
log.info("generating chapter3.1");

fillTable.addCell(getCell("1."));
fillTable.addCell(getCell("Основные данные"));

fillTable.addCell(getCell(getEmptinessString(emptyFields, criticallyFields.size())));
fillTable.addCell(getCell(StringUtilities.join(emptyFields, ", ")));

// Раздел №2:
log.info("generating chapter3.2");

fillTable.addCell(getCell("2."));
fillTable.addCell(getCell("Сведения о заявителе и исполнителях"));
log.info("l1");
emptyFields = [];
int critSize = 0;
if (req.getOrganization() != null) // в блоке «Заявитель» выбрана опция «Юридическое лицо»
{
def org = req.getOrganization();
if (helper.isEmpty(org.getTitle()))
{
emptyFields.add("Полное наименование организации (в соответствии с учредительными документами)");
}
def contactEmp = req.getReqManager();
if ((contactEmp == null) || helper.isEmpty(contactEmp.getLastName()))
{
emptyFields.add("Фамилия представителя организации");
}
if ((contactEmp == null) || helper.isEmpty(contactEmp.getFirstName()))
{
emptyFields.add("Имя представителя организации");
}
if ((contactEmp == null) || helper.isEmpty(contactEmp.getFirstName()))
{
emptyFields.add("Отчество представителя организации");
}
critSize = 4;
log.info("l2");
}
else
{
def employee = req.getEmployee();
if (employee == null || helper.isEmpty(employee.getLastName()))
{
emptyFields.add("Фамилия");
}
if (employee == null || helper.isEmpty(employee.getFirstName()))
{
emptyFields.add("Имя");
}
if (employee == null || helper.isEmpty(employee.getFirstName()))
{
emptyFields.add("Отчество");
}
critSize = 3;
log.info("l3");
}

if (executors.isEmpty())
{
emptyFields.add("Организации–потенциальные исполнители проекта");
}
critSize++;
log.info("l4");
fillTable.addCell(getCell(getEmptinessString(emptyFields, critSize)));
fillTable.addCell(getCell(StringUtilities.join(emptyFields, ", ")));
log.info("l5");
// Раздел №3:
log.info("generating chapter3.3");

fillTable.addCell(getCell("3."));
fillTable.addCell(getCell("Ожидаемые результаты"));

emptyFields = [];
critSize = 0;
if (isVisibleField(childVisibility, 'workResultTable'))
{
critSize++;
if (req.listWorkResultProperties(session).isEmpty())
{
emptyFields.add('Наименование планируемого (ожидаемого) результата НИР (прикладная), ОКР, ОТР');
}
}
criticallyFields = [
'requirementProperties.worldLevelPublication' : 'Публикации по теме проекта, отражающие мировой уровень разработки',
'requirementProperties.executorPublication' : 'Публикации по теме проекта, авторами которых являются работники организаций - потенциальных исполнителей',
'requirementProperties.experimentalDevelopment' : 'Опытно-технологическая работа',
'requirementProperties.projectKeyword' : 'Ключевые слова по проекту',
'requirementProperties.worldLevelLicence' : 'Сведения о патентах (заявках) по теме проекта, отражающих мировой уровень',
'requirementProperties.authorLicence' : 'Сведения о патентах (заявках) по теме проекта, правообладателями которых является организации - потенциальные исполнители',
'requirementProperties.protectedResult' : 'Оценка возможности получения результатов, способных к правовой охране'];
emptyFields.addAll(helper.getEmptyFields(req, criticallyFields, childVisibility));

fillTable.addCell(getCell(getEmptinessString(emptyFields, criticallyFields.size() + critSize)));
fillTable.addCell(getCell(StringUtilities.join(emptyFields, ", ")));

// Раздел №4:
log.info("generating chapter3.4");

fillTable.addCell(getCell("4."));
fillTable.addCell(getCell("Обоснование проекта"));

emptyFields = [];
critSize = 0;

if (isVisibleField(childVisibility, 'cardCostBasisNir'))
{
critSize++;
cardCostBasisNirEmpty = helper.isEmptyCardCostBasis(req.getRequirementProperties().getCostBasisNir());
if (cardCostBasisNirEmpty)
{
emptyFields.add('Виды основных работ по проекту. НИР');
}
}

if (isVisibleField(childVisibility, 'cardCostBasisOkr'))
{
critSize++;
cardCostBasisOkrEmpty = helper.isEmptyCardCostBasis(req.getRequirementProperties().getCostBasisOkr());;
if (cardCostBasisOkrEmpty)
{
emptyFields.add('Виды основных работ по проекту. НИР');
}
}

if (isVisibleField(childVisibility, 'projectLineup'))
{
critSize++;
projectLineupEmpty = helper.isProjectLineupEmpty(req, session);
if (projectLineupEmpty)
{
emptyFields.add('Состав исполнителей (команда), непосредственно занятых в проекте');
}
}

if (isVisibleField(childVisibility, 'projectBudgetFin'))
{
critSize++;
projectBudgetFinEmpty = helper.isProjectBudgetFinEmpty(req, session);
if (projectBudgetFinEmpty)
{
emptyFields.add('Объемы бюджетного финансирования проекта по статьям затрат');
}
}

criticallyFields = [
'requirementProperties.profitabilityIndex' : 'Прибыль на инвестируемый капитал (индекс прибыльности), PI, доля',
'requirementProperties.payoffPeriod' : 'Срок окупаемости капитала (период окупаемости) PBP, лет',
'requirementProperties.budgetDuty' : 'Налоги в бюджеты всех уровней (НДС, ЕСН, ПДФЛ, прибыль) за РВР, руб.',
'requirementProperties.efficiencyBudget' : 'Отдача на рубль бюджетных средств, руб/руб.',
'requirementProperties.payoffBudget' : 'Срок окупаемости бюджетных средств (от начала производства), лет'];
if (emptyBudgetFields != null)
{
emptyFields.addAll(emptyBudgetFields);
}

fillTable.addCell(getCell(getEmptinessString(emptyFields, criticallyFields.size() + critSize)));
fillTable.addCell(getCell(StringUtilities.join(emptyFields, ", ")));

// Раздел №5:
log.info("generating chapter3.5");

fillTable.addCell(getCell("5."));
fillTable.addCell(getCell("Другие данные"));

criticallyFields = [
'requirementProperties.expediencyOfInternationalCollaboration' : 'Целесообразность международного сотрудничества',
'requirementProperties.foreignPartnerCharacteristicsSet' : 'Характеристика зарубежного партнёра ',
'requirementProperties.fullCKPName' : 'Полное наименование ЦКП/УСУ',
'requirementProperties.CKPSpecialization' : 'Специализация ЦКП/УСУ (тематика исследований)'];
emptyFields = helper.getEmptyFields(req, criticallyFields, childVisibility);

fillTable.addCell(getCell(getEmptinessString(emptyFields, criticallyFields.size())));
fillTable.addCell(getCell(StringUtilities.join(emptyFields, ", ")));

document.add(fillTable);
helper.addBreak(document);
helper.addBreak(document);
*/
// -------------------- Реализация раздела «chapter4» --------------------------

def resumeListItems = [];

String str1 = fitByDueDate ? "" : "не ";
String str2 = fitByBudget ? "" : "не ";
resumeListItems.add(helper.createListItem(
    "Предложение №incomeNumber от incomeDate г. fitByDueDateсоответствует " +
    "формальным требованиям Программы по сроку реализации проекта и fitByBudgetсоответствует " +
    "по объему финансирования проекта.",
    [ 'incomeNumber': req.getIncomeNumber(),
    'incomeDate' : DateUtils.date2StrSafe(req.getIncomeDate()),
    'fitByDueDate' : str1,
    'fitByBudget' : str2 ]));

String simContractsStr = findedContracts.isEmpty() ? 'с вероятностью ' + DoubleFormatter.format(percentage) + '%, не' :
    'с вероятностью более ' + DoubleFormatter.format(percentage) + '%,';
resumeListItems.add(helper.createListItem(
    "Тема проекта, simContractsStr является повтором тем государственных контрактов, " +
    "заключенных ранее на выполнение НИОКР.",
    [ 'simContractsStr': simContractsStr ]));

 //проверка соответствия ПМ, НР, КТ
if (helper.existsConfItemWithPA(pm, session))
{
    def confErrorsList = [];
   
    def leadPL = req.getLeadingPriorityLine();
    if (!helper.checkPAAndPL(pm, leadPL, session))
    {
        confErrorsList.add(helper.replaceParameters(
            "мероприятие Программы — ProgramAction, не согласуется с основным направлением реализации ФЦП — leadPL",
            [ 'ProgramAction': TitledFormatter.sFormat(pm),
              'leadPL' : TitledFormatter.sFormat(leadPL) ]));
    }
    
    def leadCT = req.getLeadingCriticalTech();
    if (!helper.checkPAAndCT(pm, leadCT, session))
    {
        confErrorsList.add(helper.replaceParameters(
            "мероприятие Программы — ProgramAction, не согласуется с основной критической технологией — leadCT",
            [ 'ProgramAction': TitledFormatter.sFormat(pm),
              'leadCT' : TitledFormatter.sFormat(leadCT) ]));
    }
    
    if (!helper.checkPLAndCT(leadPL, leadCT, session))
    {
        confErrorsList.add(helper.replaceParameters(
            "основное направление реализации ФЦП — leadPL, не согласуется с основной критической технологией — leadCT",
            [ 'leadPL' : TitledFormatter.sFormat(leadPL),
              'leadCT' : TitledFormatter.sFormat(leadCT) ]));
    }
    
    if (confErrorsList.isEmpty())
    {
        resumeListItems.add(helper.createListItem(
            "Указанные основные для проекта данные (мероприятие Программы, основное направление реализации ФЦП, " +
            "основная критическая технология) полностью согласуются между собой", [ : ]));
    }
    else
    {
        def listItem = helper.createListItem(
            "Указанные основные для проекта данные (мероприятие Программы, основное направление реализации ФЦП, " +
            "основная критическая технология) не полностью согласуются между собой:", [ : ]);
        listItem.addNestedItems(confErrorsList);
        resumeListItems.add(listItem);
    }
}

// ------- вывод по заполненности данными заявки ------------------------

// 2.2.4.2

def emptinessErrors = [];

if (executors.isEmpty())
{
    emptinessErrors.add("обеспечения состязательности при осуществлении конкурсного отбора исполнителей");
}

// 2.2.4.3

def errorsExists = false;


if (isVisibleField(childVisibility, 'workResultTable') && req.listWorkResultProperties(session).isEmpty())
{
    errorsExists = true;
}
if (!errorsExists)
{
    criticallyFields = [
        'requirementProperties.uniqueResult' : 'Планируемый результат не будет иметь аналогов или сопоставимых прототипов',
        'requirementProperties.scientificAndTechnicalLevelOfComingResult' : 'Научно-технический уровень планируемого (ожидаемого) результата',
        'requirementProperties.worldLevelPublication' : 'Публикации по теме проекта, отражающие мировой уровень разработки',
        'requirementProperties.executorPublication' : 'Публикации по теме проекта, авторами которых являются работники организаций - потенциальных исполнителей',
        'requirementProperties.projectKeyword' : 'Ключевые слова по проекту',
        'requirementProperties.worldLevelLicence' : 'Сведения о патентах (заявках) по теме проекта, отражающих мировой уровень',
        'requirementProperties.authorLicence' : 'Сведения о патентах (заявках) по теме проекта, правообладателями которых является организации - потенциальные исполнители',
        'requirementProperties.protectedResult' : 'Оценка возможности получения результатов, способных к правовой охране',
        'requirementProperties.definitionEndResult' : 'Формулировка конечного продукта, в котором предполагается использование планируемого (ожидаемого) результата',
        'requirementProperties.economicFieldNameSet' : 'Наименование отрасли экономики, к которой может быть применен планируемый (ожидаемый) результат',
        'requirementProperties.forthcomingSocialAndEconomicalEffectSet' : 'Ожидаемый социально-экономический эффект использования планируемого (ожидаемого) результата',
        'requirementProperties.nirResultNameSet' : 'Наименование планируемого (ожидаемого) результата НИР (поисковая)',
        'requirementProperties.conferenceResultNameSet' : 'Наименование планируемого (ожидаемого) результата конференции/школы-семинара/выставки',
        'requirementProperties.resultMainLineSet' : 'Основные направления дальнейшего использования планируемого (ожидаемого) результата',
        'requirementProperties.levelOfConferenceParticipantsSet' : 'Уровень участников конференции/семинара',
        'requirementProperties.dateAndLocationOfConferencing' : 'Сроки и место проведения конференции/школы-семинара/выставки' ];
    errorsExists = !CollectionUtils.isEmptyCollection(helper.getEmptyFields(req, criticallyFields, childVisibility));
}
if (errorsExists)
{
    emptinessErrors.add("планируемого (ожидаемого) научно-технического уровня результатов проекта");
}

// 2.2.4.4 - достаточности ресурсного обеспечения для реализации проекта
errorsExists = false;
criticallyFields = [
    'requirementProperties.scientificAndTechnicalStartsSet' : 'Научно-технические заделы организации - потенциального головного исполнителя работ по проекту',
    'requirementProperties.trainedPotentialSet' : 'Кадровый потенциал организации - потенциального головного исполнителя, необходимый для выполнения проекта',
    'requirementProperties.supplyMachineryAndEquipmentSet' : 'Обеспеченность организации - потенциального головного исполнителя машинами и оборудованием (в том числе научным), необходимыми для выполнения работ по проекту',
    'requirementProperties.experienceOfImplementingSimilarProjectsSet' : 'Опыт выполнения организацией - потенциальным головным исполнителем аналогичных проектов' ];
errorsExists = !CollectionUtils.isEmptyCollection(helper.getEmptyFields(req, criticallyFields, childVisibility));
if (errorsExists)
{
    emptinessErrors.add("достаточности ресурсного обеспечения для реализации проекта");
}

// 2.2.4.5 обоснованности стоимости осуществления реализации проекта
errorsExists = projectLineupEmpty || projectBudgetFinEmpty || cardCostBasisNirEmpty || cardCostBasisOkrEmpty;
if (errorsExists)
{
    emptinessErrors.add("обоснованности стоимости осуществления реализации проекта");
}

// 2.2.4.6 экономической эффективности реализации проекта
errorsExists = isVisibleField(childVisibility, 'projectNewestProduction') &&
    helper.isProjectNewestProductionEmpty(req, session);
if (!errorsExists)
{
    criticallyFields = [
        'requirementProperties.productionPrerequisites' :
        'Производственные предпосылки, необходимые для организации выпуска продукции, производимой за счет коммерциализации технологий, созданных в ходе реализации проекта' ];
    errorsExists = !CollectionUtils.isEmptyCollection(helper.getEmptyFields(req, criticallyFields, childVisibility));
}
if (errorsExists)
{
    emptinessErrors.add("экономической эффективности реализации проекта");
}
    
// 2.2.4.7 уровня рисков успешной реализации проекта
errorsExists = false;
criticallyFields = [
    'requirementProperties.technicalRiskSet' : 'Существует вероятность технического риска или возможность',
    'requirementProperties.industrialRiskSet' : 'Существует вероятность производственного риска или невозможность производства продукции проекта из-за',
    'requirementProperties.contractRisk' : 'Существует вероятность контрактного риска из-за ненадежности контрагентов и поставщиков',
    'requirementProperties.commercialRiskSet' : 'Существует вероятность коммерческого риска или возможность:',
    'requirementProperties.marketRiskSet' : 'Существует вероятность конъюнктурного риска или возможность:',
    'requirementProperties.saleRiskSet' : 'Существует вероятность сбытового риска или невозможность получения запланированных доходов от продажи продукции проекта:' ];
errorsExists = !CollectionUtils.isEmptyCollection(helper.getEmptyFields(req, criticallyFields, childVisibility));
if (errorsExists)
{
    emptinessErrors.add("уровня рисков успешной реализации проекта");
}

// 2.2.4.8 обоснования необходимости международного сотрудничества при реализации проекта
errorsExists = false;
criticallyFields = [
    'requirementProperties.expediencyOfInternationalCollaboration' : 'Целесообразность международного сотрудничества',
    'requirementProperties.internationalCollaborationAimsSet' : 'Цели, достигаемые при международном сотрудничестве в выполнении работ по проекту',
    'requirementProperties.russianParticipantMotivationSet' : 'Мотивация российского потенциального исполнителя при международном сотрудничестве в выполнении работ по проекту',
    'requirementProperties.internationalCollaborationMarkSet' : 'Оценка результативности мероприятий по совместному выпуску высокотехнологичной продукции или оказанию наукоёмких услуг',
    'requirementProperties.foreignPartnerCharacteristicsSet' : 'Характеристика зарубежного партнёра',
    'requirementProperties.ensuringParity' : 'Оценка возможности обеспечения паритета научных и экономических интересов в рамках проекта, включая использование совместно полученных результатов',
    'requirementProperties.reasonsForInternationalProject' : 'Основания для международного сотрудничества при выполнении проекта' ];
errorsExists = !CollectionUtils.isEmptyCollection(helper.getEmptyFields(req, criticallyFields, childVisibility));
if (errorsExists)
{
    emptinessErrors.add("обоснования необходимости международного сотрудничества при реализации проекта");
}

// 2.2.4.9 необходимости реализации проекта по выбранному мероприятию Программы
errorsExists = false;
criticallyFields = [
    'requirementProperties.fullCKPName' : 'Полное наименование ЦКП/УСУ',
    'requirementProperties.CKPLocation' : 'Месторасположение ЦКП/УСУ',
    'requirementProperties.CKPCreationYear': 'Год создания ЦКП/УСУ',
    'requirementProperties.beingEquippedSet': 'Техническая оснащенность ЦКП/УСУ',
    'requirementProperties.equipmentCount' : 'Количество оборудования, единиц',
    'requirementProperties.equipmentPrice' : 'Стоимость оборудования, руб.',
    'requirementProperties.complianceDegreeSet' : 'Степень соответствия уровня оборудования ЦКП/УСУ мировым стандартам',
    'requirementProperties.programExistingSet' : 'Научно-методическое обеспечение ЦКП/УСУ (наличие программы развития ЦКП/УСУ)',
    'requirementProperties.modernMethodsExisting' : 'Обеспеченность ЦКП/УСУ современными методиками измерений',
    'requirementProperties.CKPSpecialization' : 'Специализация ЦКП/УСУ (тематика исследований)',
    'requirementProperties.cooperationLevel' : 'Уровень кооперации ЦКП/УСУ с другими организациями в совместных исследованиях и разработках (их доля в общем времени использования оборудования ЦКП/УСУ)',
    'requirementProperties.plannedLevelOfCharging' : 'Планируемая степень загрузки оборудования ЦКП/УСУ',
    'requirementProperties.relevanceDegreeSet' : 'Степень востребованности ЦКП/УСУ',
    'requirementProperties.qualificationDegreeSet' : 'Степень квалификации персонала ЦКП/УСУ',
    'requirementProperties.certifiedEquipmentPercentString' : 'Процент сертифицированного оборудования',
    'requirementProperties.certifiedMethodsPercentString' : 'Процент аттестованных измерительных методик' ];
errorsExists = !CollectionUtils.isEmptyCollection(helper.getEmptyFields(req, criticallyFields, childVisibility));
if (errorsExists)
{
    emptinessErrors.add("необходимости реализации проекта по выбранному мероприятию Программы");
}

if (emptinessErrors.isEmpty())
{
    resumeListItems.add(helper.createListItem(
        "Полное заполнение данными основных информационных полей всех разделов информационной карты заявки обеспечивает " +
        "возможность ее объективной оценки на предмет рекомендации темы проекта в качестве темы конкурсного лота", [ : ]));
}
/*else
{
def listItem = helper.createListItem(
"Не полное заполнение данными основных информационных полей всех разделов информационной карты заявки " +
"не обеспечивает в полной мере возможности ее объективной оценки на предмет рекомендации темы проекта " +
"в качестве темы конкурсного лота в части:", [ : ]);
listItem.addNestedItems(emptinessErrors);
resumeListItems.add(listItem);
}*/

// полнота комплекта предусмотренных видов документов в электронном виде

def notExistingCases = helper.listNotExistingDBFileCatagories(req, session);
if (notExistingCases.isEmpty())
{
    resumeListItems.add(helper.createListItem(
        "Предложение содержит полный комплект предусмотренных видов документов в электронном виде", [ : ]));
}
/*else
{
resumeListItems.add(helper.createListItem(
"Заявка содержит не полный комплект предусмотренных видов документов в электронном виде " +
"(не прикреплены документы: notExistingCases).",
[ 'notExistingCases' : StringUtilities.join(CollectionUtils.transformInList(notExistingCases, new TitledTransformer()), ', ')]));
}*/

document.addNestedList(resumeListItems, false);
helper.addBreak(document);
helper.addBreak(document);

// ---------- Реализация раздела «footer»(подпись) -----------------

helper.addText(document, "Проверку проводил");
helper.addBreak(document);


PdfTable signTable = new PdfTable(3, 0, 100);
signTable.setSpacing(0);
signTable.setPadding(1);
signTable.setWidths([28.5f, 29.5f, 42.0f]);


signTable.addCell(PdfTableCell.createHorizontalAndVerticalAlignmentCell(BKUIUtils.currentPerson?.corePosts[0]?.title + " ФГБНУ «Дирекция научно-технических программ»",
        PdfDocument.fontSerif10, Element.ALIGN_MIDDLE, Element.ALIGN_LEFT));
signTable.addCell(PdfTableCell.createHorizontalAndVerticalAlignmentCell(" ",
        PdfDocument.fontSerif10, Element.ALIGN_MIDDLE, Element.ALIGN_LEFT));
signTable.addCell(PdfTableCell.createHorizontalAndVerticalAlignmentCell(helper.getFIO(user),
        PdfDocument.fontSerif10, Element.ALIGN_BOTTOM, Element.ALIGN_LEFT));
document.add(signTable);

//подсчет сроков исполнения проекта в годах
int boxAnalyzer(RequirementFcntpWorkPriceBox box, String budgetRFCode, Session session)
{
        values = []
        int i = 1
        while(true)
        {
                try{
                        values.add(box.getCellValue(budgetRFCode, i.toString(), session))
                }
                catch(CoreCatalogItemNotFoundException e){
                        break;
                }
                i++
        }
        
        for (i = --values.size(); i >= 0 ; i--)
        {
                if (values.get(i) != 0)
                        return (++i)
        }
}

