import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler; 
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import ru.naumen.ccam.bobject.stage.ContractStage;
import ru.naumen.ccam.bobject.stage.ContractStageWork;

List<ContractStage> stages = ContractStageHibernateHandler.listContractStages(contract);
List<ContractStageWork> result = new ArrayList<ContractStageWork>();

Collections.sort(stages, ContractStage.NUMBER_COMPARATOR);

for (ContractStage stage: stages)
{
	result.addAll(ContractStageHibernateHandler.listWorks(stage));
}
return result;