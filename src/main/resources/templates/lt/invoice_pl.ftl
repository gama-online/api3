<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta charset="UTF-8">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>${document.number!""} ${document.date!""}</title>
    <style>
        body{margin:0;padding:0;font-size:12px}
        .logo{width:200px;left:0;height:100px}
        .logo img{max-width:200px;max-height:100px}
        .logo,.title{position:relative;top:0}
        .title{right:0;text-align:right}
        table{border:none;width:100%}tr.line td,tr.line th{border-bottom:1px solid #000}tr.line th{border-top:1px solid #000}th{text-align:left}th,td{padding:5px}td{vertical-align:top}
        .line{border-bottom:1px solid #000}
        .line-top{border-top:1px solid #000}
        .small{padding:0;font-size:.66em}.text-right{text-align:right}.text-center{text-align:center}.text-nowrap{white-space:nowrap}
        label{text-align:right;font-weight:700;white-space:nowrap}
        .multiline{white-space:pre-line}
        h1,h2,h3,h4,h5,p{margin:0;padding:0}
        .wide{width:100%}
        html,body{font-family:Helvetica,Arial,sans-serif}
        @page{@top-left{content:'${document.number!""} ${document.date!""}';font-size:12px;font-family:Helvetica,Arial,sans-serif;font-weight:700}}
        @page{@top-right{content:'Lapas ' counter(page) ' iš ' counter(pages);font-size:12px;font-family:Helvetica,Arial,sans-serif}}
        @page:first{@top-left{content:''}@top-right{content:''}}
    </style>
</head>
<body>
<div>
    <div class="page">
        <div class="subpage">
            <table border="0" cellspacing="0">
                <tr>
                    <td>
                        <#if company.logo?has_content>
                            <div class="logo">
                                <img src="${company.logo}">
                            </div>
                        </#if>
                    </td>
                    <td>
                        <div class="title">
                            <h1>PAKAVIMO LAPAS</h1>
                            <h3>Sąskaitos-faktūros Nr: ${document.number!""}</h3>
                            <h3>Data: ${document.date!""}</h3>
                        </div>
                    </td>
                </tr>
            </table>
            <table border="0" cellspacing="0">
                <tr>
                    <td width="50%">
                        <h3>${company.businessName}</h3>
                        <#if company.code?has_content><p><b>Įmonės kodas:</b> ${company.code}</p></#if>
                        <#if company.vatCode?has_content><p><b>PVM kodas:</b> ${company.vatCode}</p></#if>
                        <#if company.address?has_content><p><b>Adresas:</b> ${company.address}</p></#if>
                        <#if banks?size == 1>
                            <p><b>Bankas: </b>${banks[0].account}, ${banks[0].bank.name!""}<#if (banks[0].bank.swift)?has_content>, SWIFT:&nbsp;${banks[0].bank.swift}</#if></p>
                        </#if>
                        <#if banks?size gt 1>
                            <p><b>Bankai:</b></p>
                            <#list banks as bank>
                                <p>${bank.account}, ${bank.bank.name!""}<#if (bank.bank.swift)?has_content>, SWIFT:&nbsp;${bank.bank.swift}</#if></p>
                            </#list>
                        </#if>
                        <#if company.contactsInfo??>
                            <#list company.contactsInfo as contact>
                                <p>
                                    <#if contact.type == "phone" || contact.type == "mobile"><b>Telefonas: </b></#if>
                                    <#if contact.type == "fax"><b>Faksas: </b></#if>
                                    <#if contact.type == "email"><b>El.paštas: </b></#if>
                                    ${contact.contact}
                                </p>
                            </#list>
                        </#if>
                    </td>

                    <td width="50%">
                        <#if document.counterparty??>
                            <h3>Pirkėjas: ${document.counterparty.name}</h3>
                            <#if document.counterparty.comCode?has_content>
                                <p><b>Įmonės kodas:</b> ${document.counterparty.comCode}</p>
                            </#if>
                            <#if document.counterparty.vatCode?has_content>
                                <p><b>PVM kodas: </b> ${document.counterparty.vatCode}</p>
                            </#if>
                            <#if document.address??>
                                <p><b>Adresas:</b> ${document.address}</p>
                            </#if>
                        </#if>
                    </td>
                </tr>
            </table>

            <table border="0" cellspacing="0" style="-fs-table-paginate: paginate;-fs-page-break-min-height: 1.5cm;">
                <thead>
                    <tr class="line">
                        <th class="text-right">#</th>
                        <th class="wide">Pavadinimas</th>
                        <#if printSKU><th class="">Kodas</th></#if>
                        <th class="">Vnt.</th>
                        <th class="text-right">Kiekis</th>
                        <th class="text-right">Viso Netto</th>
                        <th class="text-right">Viso Brutto</th>
                    </tr>
                </thead>
                <tbody>
                    <#if document.parts??>
                        <#assign nr = 0>
                        <#list document.parts as part>
                            <#if part.type != 'N'>
                            <tr class="line">
                                <#assign nr = nr + 1>
                                <td class="text-right">${nr}</td>
                                <td>${part.name!""}</td>
                                <#if printSKU><td class="text-nowrap">${part.sku!""}</td></#if>
                                <td>${part.unit!""}</td>
                                <td class="text-right text-nowrap">${part.quantity!""}</td>

                                <td class="text-right">
                                    <#if partsMap[part.id?c].netto?? && part.quantity??>
                                        ${partsMap[part.id?c].netto * part.quantity}&nbsp;${partsMap[part.id?c].unitsWeight!"kg"}
                                    </#if>
                                </td>
                                <td class="text-right">
                                    <#if partsMap[part.id?c].brutto?? && part.quantity??>
                                        ${partsMap[part.id?c].brutto * part.quantity}&nbsp;${partsMap[part.id?c].unitsWeight!"kg"}
                                    </#if>
                                </td>
                            </tr>
                            </#if>
                        </#list>
                    </#if>
                </tbody>
                <tbody style="page-break-inside:avoid;">
                    <tr>
                        <th colspan="4" class="text-right">VISO:</th>
                        <th class="text-right">${partsCount!""}</th>
                        <th class="text-right">${netto!""}&nbsp;${unitsWeight!""}</th>
                        <th class="text-right">${brutto!""}&nbsp;${unitsWeight!""}</th>
                    </tr>

                    <#if document.packing??>
                        <tr class="line"><td colspan="7">&nbsp;</td></tr>

                        <#list document.packing as pack>
                            <tr class="line">
                                <td></td>
                                <td colspan="3">${pack.name!""}</td>
                                <td class="text-right text-nowrap">${pack.quantity!""}</td>
                                <td></td>
                                <td class="text-right text-nowrap">${pack.totalWeight!""}&nbsp;${pack.unitsWeight!"kg"}</td>
                            </tr>
                        </#list>

                        <tr>
                            <th colspan="4" class="text-right">BENDRAS SVORIS:</th>
                            <th class="text-right"></th>
                            <th class="text-right"></th>
                            <th class="text-right text-nowrap">${bruttoTotal!""}&nbsp;${unitsWeight!""}</th>
                        </tr>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>