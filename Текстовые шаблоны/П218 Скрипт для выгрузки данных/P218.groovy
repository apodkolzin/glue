//Для sstp
//programId = "corebofs000080000im6dvt49crml4m4"

requisitesQ = session.createQuery("select r from PaymentRequisites r where r.parent=:parent") 

list = helper.select("from ContractFcntp where parent.id='$programId'")
  
entries = []
def added
for(contract in list){
   	added = false
        if( contract.performer!=null ){
            requisitesQ.setParameter("parent", contract.performer)
            list = requisitesQ.list()

            for(req in list ){
                addContractReportEntry(entries, req, contract)
                  
                added = true
            }
        }
  
  	if(!added){
        	addContractReportEntry(entries, null, contract)
        }
  }

report.vars.entries = entries  
  
def addContractReportEntry(entries, req, contract){
    entry = new ContractReportEntry()
    entry.paymentRequisites = req
      
    if(req!=null){
	entry.titleOFK = req.titleOFK
    }
    
    if( contract.performer!=null ){
    	entry.orgTitle = contract.performer.title
        entry.orgShortTitle = contract.performer.shortTitle
        entry.orgOKOPF = contract.performer.OKOPF!=null ? contract.performer.OKOPF.title : ""
        entry.orgINN = contract.performer.INN
	if(contract.performer.juridicalAddress != null){
          	entry.orgJuridicalAddress = new AddressEntry()
		fillAddressFields(entry.orgJuridicalAddress, contract.performer.juridicalAddress)
	}
	if(contract.performer.factAddress != null){
          	entry.orgFactAddress = new AddressEntry()
		fillAddressFields(entry.orgFactAddress, contract.performer.factAddress)
	}
	if(contract.performer.postAddress != null){
          	entry.orgPostAddress = new AddressEntry()
		fillAddressFields(entry.orgPostAddress, contract.performer.postAddress)
	}
        if(contract.performer.chief != null){
		entry.orgChief = contract.performer.chief.displayableTitle
		entry.orgChiefPost = contract.performer.chief.post
		entry.orgChiefPhone = contract.performer.chief.cityPhoneNumber
		entry.orgChiefEmail = contract.performer.chief.email
	}
        entry.orgKPP = contract.performer.KPP
        entry.orgOKPO = contract.performer.OKPO
        entry.orgPhoneNumber = contract.performer.phoneNumber
    }
      
    entry.contractNumber = contract.identifier
    entry.demandNumber = contract.demand !=null ? contract.demand.viewNumber : ""
   
    entry.contractStage = contract.currentStage.displayableTitle
    entry.offerNumber = contract.lot!=null ? contract.lot.inOfferNumber : ""
      
    entries.add(entry)
}

def fillAddressFields(addrEntry, address){
	addrEntry.federalRegion = address.federalRegion != null ? address.federalRegion.title : ""
	addrEntry.region = address.region != null ? address.region.title : ""
	addrEntry.city = address.city != null ? address.city.title : ""
	addrEntry.street = address.street != null ? address.street.title : ""
	addrEntry.postalCode = address.postalCode
	addrEntry.houseNumber = address.houseNumber
	addrEntry.houseUnitNumber = address.houseUnitNumber
}
  
class ContractReportEntry{
   String orgTitle
   String orgShortTitle
   String orgOKOPF
   String orgINN

   String orgKPP
   String orgOKPO

   String orgPhoneNumber
   AddressEntry orgJuridicalAddress
   AddressEntry orgFactAddress
   AddressEntry orgPostAddress

   String titleOFK
   ru.naumen.fcntp.bobject.paymentrequisites.PaymentRequisites paymentRequisites
     
   String orgChief
   String orgChiefPost
   String orgChiefPhone
   String orgChiefEmail
     
   String contractNumber
   String demandNumber
   String offerNumber
   String contractStage
}

class AddressEntry{
   String federalRegion
   String region
   String city
   String street
   String postalCode
   String houseNumber
   String houseUnitNumber
}