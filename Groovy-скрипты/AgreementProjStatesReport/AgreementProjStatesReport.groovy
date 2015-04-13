/*
   Скрипт для генерации отчетов стадий проектов соглашений
   Автор: aboronnikov
  */


HAS_NO_MANAGER = "[нет куратора]"

//Получаем все документы, принадлежащие контрактам категории 'contractSimple_Creative'
docs = helper.query("SELECT d FROM ContractProjectDocument d WHERE d.parent.ccamBOCase.code='"+ contractCategory +"' and d.ccamBOCase.code='"+contractProjectCategory+"'")
//Получаем список всех возможных стадий ЖЦ
stateList = helper.getCatalogItem("CCAMDocumentCases", contractProjectCategory ).workflowStates
  
list = []
  
for(doc in docs){
	contract = doc.parent
	if(contract.programActions.size()==0){
        	continue
        }
	programAction = contract.programActions[0].originalIdentifier
        
	if(contract.manager==null) {
		manager = HAS_NO_MANAGER
	}else{
		manager=contract.manager.fio("fi.o.")
	}
        
        state = doc.workflowState.title

        handleEntry(list, programAction, manager, state)
}

return list

//конец главного метода

//Дальше идут классы и внутренние процедуры..

class Entry{
	String manager
    String programAction
    String state
    int count
}  

def findEntry(list, programAction, manager, state){
  for(entry in list){
    if(entry.programAction.equals(programAction)){
      if(entry.state.equals(state)){
        if( reportType == 1 || entry.manager.equals(manager) ){
      	  return entry
        }
      }
    }
  }
  return null
}

def handleEntry(list, programAction, manager, state){
	entry = findEntry(list, programAction, manager, state)
        if(entry == null){
            createEntries(list, programAction, manager)
        }
        entry = findEntry(list, programAction, manager, state)
        entry.count++
}

def createEntries(list, programAction, manager){
     for(state in stateList){
            entry = new Entry()
            entry.programAction = programAction
            entry.state = state.title
	    if(reportType == 2){	  
            	entry.manager = manager
	    }
            entry.count = 0
            list.add(entry)
     }
}