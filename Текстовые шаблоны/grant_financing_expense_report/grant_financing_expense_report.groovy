import ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import java.util.Set
import java.util.HashSet
import java.util.HashMap

stage = BusinessActionBase.ensureReload(session, object.parent)
//stage = helper.get('corebofs000080000jm7dra07qhsqqj4')

report.vars.stage = stage
report.vars.contract = stage.contract

if(stage.contract.performer !=null){
	report.vars.performer_chief = stage.contract.performer.chief
} else{
	report.vars.performer_chief = stage.contract.applicant
}

//report.vars.financing = BoxHibernateHandler.getBoxWithCode(stage, "contractFcntpExpenseBudgetCostItems")

xcatalog =  CoreCatalogHibernateHandler.getCatalogByCode('ExpenseBudgetCostItems')
ycatalog = CoreCatalogHibernateHandler.getCatalogByCode('CostingsSources')

box = helper.query("select box from ContractStageCostingsItemsBox box join box.boxDefinition boxDef where boxDef.code = 'contractStageCostingItems' and box.owner = '"+stage.UUID+"'")

entries = []

if(box.size()>0){
  
          coordList = []
          firstCoordCodes = new HashSet()
          secondCoordCodes = new HashSet()
          for(coordset in box.get(0).coordinates){
          	  pCounter = 0
                  for(icoord in coordset.coordinates.values()){
                    if(icoord!=null){
                        if(pCounter == 0){
                        	firstCoordCodes.add(icoord)
                    	}else{
				secondCoordCodes.add(icoord)	
			}
                    }
                    
                    pCounter++                      
                  }            		
          }
  	
      	coordSets = new HashMap()
	for(cellItem in box.get(0).cells){
		coordMap = cellItem.coordinates.coordinates
	        coordAr = coordMap.values().toArray()

	        boxEntry = new BoxEntry()
                  
        	if(coordAr[1]==null){
        		continue
        	}
                  
        	if(coordAr[0]!=null){
        		boxEntry.xcoord_id = getCatalogItemIndex(xcatalog, coordAr[0].UUID)
        		boxEntry.xcoord_title = coordAr[0].title
                        
			if(coordSets.get(coordAr[0])==null){
				coordSets.put(coordAr[0], new HashSet())
			}
			coordSets.get(coordAr[0]).add(coordAr[1])
        	}else{
            		boxEntry.xcoord_id = 999
        		boxEntry.xcoord_title = "Итого"
        	}

        	boxEntry.ycoord_id =  getCatalogItemIndex(ycatalog, coordAr[1].UUID)
        	boxEntry.ycoord_title = coordAr[1].title
	        boxEntry.value = cellItem.getValue()

		entries.add(boxEntry)
        }  	
  	
	//Нулевые значения не добавляются в коллекцию cells объекта box.
	//Приходится добавлять недостающие нулевые ячейки вручную
  	for(item in firstCoordCodes){
		for(item1 in secondCoordCodes){
			if(!coordSets.get(item).contains(item1)){
				boxEntry = new BoxEntry()
		        	boxEntry.xcoord_id = getCatalogItemIndex(xcatalog, item.UUID)
        			boxEntry.xcoord_title = item.title
		        	boxEntry.ycoord_id =  getCatalogItemIndex(ycatalog, item1.UUID)
        			boxEntry.ycoord_title = item1.title
		        	boxEntry.value = 0

				entries.add(boxEntry)
			}
		}
        }
} else {
	for(xcoord in xcatalog.listItems()){
  		for(ycoord in ycatalog.listItems()){
    		boxEntry = new BoxEntry()
        	boxEntry.xcoord_id = getCatalogItemIndex(xcatalog, xcoord.UUID)
        	boxEntry.xcoord_title = xcoord.title
        	boxEntry.ycoord_id =  getCatalogItemIndex(ycatalog, ycoord.UUID)
        	boxEntry.ycoord_title = ycoord.title
        	boxEntry.value = 0
      
      		entries.add(boxEntry)
  		}
	}
}

report.vars.box = entries 

class BoxEntry{
	int xcoord_id
        int ycoord_id
        String xcoord_title
        String ycoord_title
        BigDecimal value
}

def getCatalogItemIndex(catalog, id){
	counter = 0
	for(item in catalog.listItems()){
  		if(item.UUID.equals(id)){
      			return counter
  		}
    		counter++
	}
}