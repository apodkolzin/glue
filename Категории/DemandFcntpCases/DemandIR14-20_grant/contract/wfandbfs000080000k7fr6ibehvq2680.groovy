//(new ru.naumen.fcntp.bobject.demand.workflow.CreateContractScript("LOT_PROPORTIONAL")).create(workflowInstance, oldState, newState, session); 
// Закомментил 28.08.2014 добавил последний (new ru.naumen.fcntp.bobject.demand.workflow.CreateContractScript("LOT_PROPORTIONAL", "FROM_PROGRAM_ACTION")).create(workflowInstance, oldState, newState, session);

//(new ru.naumen.fcntp.bobject.demand.workflow.CreateContractScript()).create(workflowInstance, oldState, newState, session);

//Скрипт с логикой на основе значений справочников ОКОПФ и Ведомство 
(new ru.naumen.fcntp.bobject.demand.workflow.CreateContractScript("LOT_PROPORTIONAL", "FROM_PROGRAM_ACTION")).create(workflowInstance, oldState, newState, session);