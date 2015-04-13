import ru.naumen.fcntp.bobject.requirement.RequirementFcntp;
import ru.naumen.fcntp.FcntpActivator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import ru.naumen.core.hibernate.HibernateUtil;
import ru.naumen.fcntp.examination.ComparativeAnalysisUtil.ItemForSort;
import ru.naumen.fcntp.catalog.ConformityProgActionsAndPriLinesCatalogItem;

import ru.naumen.ccam.bobject.program.action.ProgramAction;
import java.util.Date;

import ru.naumen.common.utils.CollectionUtils;
import ru.naumen.common.utils.DateUtils;
import ru.naumen.common.utils.TitledTransformer;
import ru.naumen.fcntp.catalog.FormalValidationCriteriaCatalog;
import ru.naumen.fcntp.catalog.FormalValidationCriteriaCatalogItem;
import ru.naumen.guic.formatters.TitledFormatter;
import ru.naumen.fcntp.measurement.celltype.RequirementFcntpWorkPriceBox;
import ru.naumen.core.attrs.conversions.converters.DisplayableTitledToStringConverter;
import ru.naumen.core.currency.Money;
import ru.naumen.guic.formatters.DoubleFormatter;
import ru.naumen.fcntp.examination.ComparativeAnalysisUtil;
import ru.naumen.ccamext.docgen.PdfTable;
import ru.naumen.ccamext.docgen.PdfTableCell;
import ru.naumen.fcntp.bobject.contract.ContractFcntp;
import ru.naumen.fx.objectloader.PrefixObjectLoaderFacade;
import com.lowagie.text.Element;
import ru.naumen.ccamext.docgen.PdfDocument;
import ru.naumen.common.utils.StringUtilities;
import ru.naumen.fcntp.bobject.requirement.executors.RequirementCoExecutorsHibernateHandler;
import ru.naumen.ccamext.docgen.PdfNestedListItem;

/**
 * Скрипт создания pdf-документа предварительной экспертизы Предложения 
 * version 1.0
 */

log.info('Start generating pdf-document for ' + req.getDisplayableTitle());

String budgetRFCode = "budgetRF"; 
ProgramAction pm  = req.getProgramActions().get(0); // ПМ должен быть
String pmDispTitle = TitledFormatter.sFormat(pm);
String pmTitle = pm.getTitle();
String pmId = pm.getDisplayableIdentifier();

FormalValidationCriteriaCatalog valCatalog = helper.getCatalog("FormalValidationCriteria", session);
FormalValidationCriteriaCatalogItem item = FormalValidationCriteriaCatalog.getItem(valCatalog, pm, session);

// -------------- Реализация раздела «title» -----------------------------------
document.addH1("Отчет по предварительной экспертизе");
helper.addBreak(document);

helper.addText(document, "заявки на формирование тематики и объемов финансирования работ в рамках " +
    "федеральной целевой программы «Развитие инфраструктуры наноиндустрии в Российской Федерации на 2008 - 2011 годы»");
helper.addBreak(document);

helper.addTextCenter(document, "№ incomeNumber от incomeDate г.,", 
    [ 'incomeNumber' : req.getIncomeNumber(), 'incomeDate' : DateUtils.date2StrSafe(req.getIncomeDate()) ]);
helper.addBreak(document);

helper.addText(document, "поданной на осуществление реализации проекта в рамках мероприятия pmNumber «pmTitle».",
    [ 'pmNumber' : pmId, 'pmTitle' : pmTitle ]);
helper.addBreak(document);

helper.addText(document, "Дата проведения: date г.",
//    [ 'date' : DateUtils.date2StrSafe(new Date())]);
    [ 'date' : DateUtils.date2StrSafe(req.getIncomeDate()) ]); //ashvedchikov: на случай, если потребуется соблюсти формальное соответствие между датой входящего и датой проведения экспертизы.
helper.addBreak(document);

helper.addText(document, "В результате проведения предварительной экспертизы заявки № incomeNumber от incomeDate г., " +
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
helper.addBreak(document);
def formalReqsList = [];

boolean fitByDueDate = true;
boolean fitByBudget = true;

// проверка свойства «Срок выполнения работ по проекту (в месяцах)»

if ((req.getBeginDate() == null) || (req.getEndDate() == null))
{
	helper.addText(document, "-   указанный срок выполнения проекта соответствует установленному Программой сроку реализации проектов, выполняемых в рамках программного мероприятия «pmTitle».",
              [ 'pmTitle' : pmTitle ]);
}
else
{
	helper.addText(document, "-   указанный срок выполнения проекта — c beginDate по endDate — соответствует установленному Программой сроку реализации " +
              "проектов, выполняемых в рамках программного мероприятия «pmTitle».",
              [ 'beginDate' : DateUtils.date2StrSafe(req.getBeginDate()),
                'endDate' : DateUtils.date2StrSafe(req.getEndDate()),
                'pmTitle' : pmTitle ]);
}
helper.addBreak(document);

RequirementFcntpWorkPriceBox box = RequirementFcntpWorkPriceBox.getBox(req, session);
BigDecimal budgetRF = box.getCellValue(budgetRFCode, null, session);
helper.addText(document, "-   указанный объём финансирования проекта за счёт средств федерального " + 
              "бюджета — budgetRF руб. — соответствует " +
              "установленному Программой объёму финансирования проектов за счёт федерального бюджета, выполняемых в рамках программного мероприятия «pmTitle».",
              [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'pmTitle' : pmTitle ]);
helper.addBreak(document);

    




/*
if (item != null) // если есть элемент справочнике ограничений
{
    if (req.getDueMonth() == null)
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
            formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth — не соответствует " +
                "установленному Программой сроку реализации проектов (не менее dueDateMin мес.), " +
                "выполняемых в рамках программного мероприятия «pmTitle».",
                [ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
                'dueDateMin' : dueDateMin,
                'pmTitle' : pmId]));
        }
        else if (errorCode > 0) // больше максимума
        {
            formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth — не соответствует " +
                "установленному Программой сроку реализации проектов (не более dueDateMax мес.), " +
                "выполняемых в рамках программного мероприятия «pmTitle».",
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
            formalReqsList.add(helper.createListItem("указанный срок выполнения проекта — dueMonth — соответствует " +
                "установленному Программой сроку реализации проектовdueString, " +
                "выполняемых в рамках программного мероприятия «pmTitle».",
                [ 'dueMonth' : TitledFormatter.sFormat(req.getDueMonth()),
                'dueString' : dueString,
                'pmTitle' : pmId]));
        }
        helper.addBreak(document);
    }


    // проверка по объёму финансирования проекта
    fitByBudget = false;
    RequirementFcntpWorkPriceBox box = RequirementFcntpWorkPriceBox.getBox(req, session);
    BigDecimal budgetRFMin = item.getBudgetMin();
    if (budgetRFMin == null)
    {
        budgetRFMin = 0;
    }
    BigDecimal budgetRFMax = item.getBudgetMax();
    if (budgetRFMax == null)
    {
        budgetRFMax = 0;
    }
    
    boolean budgetAsYearsSum = item.isBudgetAsYearsSum();
    String budgetRFCode = "budgetRF";
    if (budgetAsYearsSum) // Бюджет указан как сумма по всем годам
    {
        BigDecimal budgetRF = box.getCellValue(budgetRFCode, null, session);
        errorCode = helper.inRangeBigDecimal(budgetRF, budgetRFMin, budgetRFMax);
        if (errorCode < 0) // меньше минимума
        {
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета — " +
                "budgetRF руб. — не соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не менее budgetMin), выполняемых в рамках программного мероприятия «pmTitle».",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMin' : Money.formatBigDecimal(budgetRFMin),
                'pmTitle' : pmId]));
        }
        else if (errorCode > 0) // больше максимума
        {
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета — " +
                "budgetRF руб. — не соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не более budgetMax), выполняемых в рамках программного мероприятия «pmTitle».",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                'pmTitle' : pmId]));
        }
        else
        {
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета — " +
                "budgetRF руб. — соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не более budgetMax), выполняемых в рамках программного мероприятия «pmTitle».",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                'pmTitle' : pmId]));
            fitByBudget = true;
        }
    }
    else
    {
        int i = 1;
        int dueYears = helper.getCountDueYears(req);
        boolean isBudgetFit = true;
        while (i <= dueYears)
        {
            BigDecimal budgetRF = box.getCellValue(budgetRFCode, String.valueOf(i), session);
            errorCode = helper.inRangeBigDecimal(budgetRF, budgetRFMin, budgetRFMax);
            if (errorCode < 0) // меньше минимума
            {
                formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета — " +
                    "budgetRF руб. — не соответствует установленному Программой объёму финансирования проектов " +
                    "за счёт федерального бюджета (не менее budgetMin в год), выполняемых в рамках программного мероприятия «pmTitle».",
                    [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                    'budgetMin' : Money.formatBigDecimal(budgetRFMin),
                    'pmTitle' : pmId]));
                isBudgetFit = false;
                break;
            }
            else if (errorCode > 0) // больше максимума
            {
                formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета — " +
                    "budgetRF руб. — не соответствует установленному Программой объёму финансирования проектов " +
                    "за счёт федерального бюджета (не более budgetMax в год), выполняемых в рамках программного мероприятия «pmTitle».",
                    [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                    'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                    'pmTitle' : pmId]));
                isBudgetFit = false;
                break;
            }
            i++;
        }
        if (isBudgetFit)
        {
            BigDecimal budgetRF = box.getCellValue(budgetRFCode, "1", session);
            formalReqsList.add(helper.createListItem("указанный объём финансирования проекта за счёт средств федерального бюджета — " +
                "budgetRF руб. — соответствует установленному Программой объёму финансирования проектов " +
                "за счёт федерального бюджета (не более budgetMax в год), выполняемых в рамках программного мероприятия «pmTitle».",
                [ 'budgetRF' : Money.formatBigDecimal(budgetRF),
                'budgetMax' : Money.formatBigDecimal(budgetRFMax),
                'pmTitle' : pmId]));
            fitByBudget = true;
        }
    }
    
    // проверка на соответствие минимальному ограничению по внебюджету
    
    String offBudgetCode = "offbudget";
    BigDecimal offBudget = box.getCellValue(offBudgetCode, null, session);
    double minPercent = item.getOffbudgetMin().doubleValue();
    BigDecimal budgetRF = box.getCellValue(budgetRFCode, null, session);
    BigDecimal wholeBudget = budgetRF.add(offBudget);
    double currentPercent = (wholeBudget.doubleValue() == 0) ? 0 : offBudget.multiply(new BigDecimal(100)).divide(wholeBudget, 2).doubleValue();
    
    boolean offBudgetFit = (minPercent > 0) ? (currentPercent > minPercent) : true;
    String notStr = (offBudgetFit) ? "" : "не "
    String percentString = (minPercent > 0) ? "(minPercent% от общего финансирования проекта)" : "";
    formalReqsList.add(helper.createListItem("указанный уровень внебюджетного софинансирования проекта wholeBudget руб. — " +
        percentString + " notStrсоответствует установленному Программой объёму внебюджетного софинансирования проектов " +
        ", выполняемых в рамках программного мероприятия «pmTitle».",
        [ 'notStr' : notStr,
        'wholeBudget' : Money.formatBigDecimal(offBudget),
        'minPercent' : DoubleFormatter.format(minPercent),
        'pmTitle' : pmId]));
    
    document.addNestedList(formalReqsList, false);
    helper.addBreak(document);
    helper.addBreak(document);
}// end of если есть элемент справочнике ограничений
*/
// ----------------- Реализация раздела «chapter2» -----------------------------
log.info("generating chapter2");

helper.addTitleText(document, "II. В части совпадения темы проекта и тем государственных контрактов, заключенных ранее:");
helper.addBreak(document);

    
double percentage = report.getPercentage();
    String hqlThemesWithIDs = String.format("select themeForComparsion, id from ContractFcntp where themeForComparsion is not null and parent.id in ('%s','%s','%s')" +
        " and creationDate <= :crDate",
FcntpActivator.MAIN_PROGRAM_UUID, FcntpActivator.OLD_PROGRAM_UUID, FcntpActivator.NANO_PROGRAM_UUID);
    ArrayList<ItemForSort> findedContracts = new ArrayList<ItemForSort>();
    String searchTheme = ComparativeAnalysisUtil.preprocessTheme(req.getTitle());
    final List<String> searchWordList = StringUtilities.wordList(searchTheme);
    final int searchThemeSize = searchWordList.size() == 0 ? 1 : searchWordList.size();

    final List<Object[]> sample = (List<Object[]>) session.createQuery(hqlThemesWithIDs)
        .setParameter("crDate", req.getIncomeDate()).list();

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
//def findedContracts = findSimilarThemesStrong(req, req.getTitle(), percentage, false);

if (findedContracts.isEmpty())
{
    helper.addText(document, "сравнительный анализ темы проекта и тем государственных контрактов, " +
        "заключенных ранее, совпадений не выявил (значение процентного соотношения: percentage%).",
        [ 'percentage' : DoubleFormatter.format(percentage) ]);
}
else
{
    helper.addText(document, "сравнительный анализ темы проекта и тем государственных контрактов, " +
        "заключенных ранее, выявил следующие совпадения " +
        "(значение процентного соотношения: percentage%):",
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
log.info("generating chapter3");

helper.addTitleText(document, "Заключение");

helper.addBreak(document);

helper.addText(document, "-   Заявка № incomeNumber от incomeDate г., соответствует формальным "+
                     "требованиям Программы по сроку реализации проекта и соответствует по объему финансирования проекта.", 
    [ 'incomeNumber' : req.getIncomeNumber(), 'incomeDate' : DateUtils.date2StrSafe(req.getIncomeDate()) ]);
helper.addBreak(document);

if (findedContracts.isEmpty())
{
helper.addText(document, "-   Тема проекта не является повтором тем государственных "+
                       "контрактов, заключенных ранее. (значение процентного соотношения для сравнительного анализа: percentage%)", 
               [ 'percentage' : DoubleFormatter.format(percentage) ]);
}
else
{
helper.addText(document, "-   Существуют государственные контракты, заключенных ранее, тема которых схожа с темой проекта. "+
                       "(значение процентного соотношения для сравнительного анализа: percentage%)", 
               [ 'percentage' : DoubleFormatter.format(percentage) ]);
}
helper.addBreak(document);

helper.addText(document, "-   Указанные основные для проекта данные (мероприятие Программы, основное "+
                "направление реализации ФЦП, основная критическая технология) полностью согласуются между собой.");
helper.addBreak(document);
helper.addText(document, "-   Заявка содержит полный комплект предусмотренных видов документов в электронном виде.");
helper.addBreak(document);  
helper.addBreak(document); 
helper.addBreak(document); 

// ---------- Реализация раздела «footer»(подпись) -----------------

PdfTable signTable = new PdfTable(3, 0, 100);
signTable.setSpacing(0);
signTable.setPadding(1);
signTable.setWidths([28.5f, 29.5f, 42.0f]);

def post = helper.getPostAndOrgStr(user, session);

/*
def postSign = "Экспертизу проводил\nспециалист " + (StringUtilities.isEmpty( post ) ? "" : "(" + post + ")") + " ФГБНУ \"Дирекция научно-технических программ\"";
signTable.addCell(
  PdfTableCell.createHorizontalAndVerticalAlignmentCell( postSign,
  PdfDocument.fontSerif10, Element.ALIGN_MIDDLE, Element.ALIGN_LEFT));
*/

signTable.addCell(
  PdfTableCell.createHorizontalAndVerticalAlignmentCell("                                                  ",
  PdfDocument.fontSerif10, Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT));

signTable.addCell(
  PdfTableCell.createHorizontalAndVerticalAlignmentCell(helper.getFIO(user),
  PdfDocument.fontSerif10Bold, Element.ALIGN_BOTTOM, Element.ALIGN_LEFT));  

/*
signTable.addCell("");

signTable.addCell(
  PdfTableCell.createHorizontalAndVerticalAlignmentCell("(подпись)",
  PdfDocument.fontSerif10, Element.ALIGN_TOP, Element.ALIGN_CENTER));  

signTable.addCell(
  PdfTableCell.createHorizontalAndVerticalAlignmentCell("(И.О.Фамилия)",
  PdfDocument.fontSerif10, Element.ALIGN_TOP, Element.ALIGN_LEFT));  
*/

document.add(signTable); 