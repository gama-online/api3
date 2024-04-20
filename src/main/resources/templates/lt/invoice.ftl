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
        .line-left{border-left:1px solid #000}
        .line-right{border-right:1px solid #000}
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
                            <h1>
                            <#if moneyFormatter.isNegative(document.total) || document.creditInvoice?has_content && document.creditInvoice>Kreditinė </#if>
                            <#if company.vatCode?has_content>PVM </#if>
                                Sąskaita-faktūra
                            </h1>
                            <h3>Nr: ${document.number!""}</h3>
                            <h3>Data: ${document.date!""}</h3>
                        </div>
                    </td>
                </tr>
            </table>
            <table border="0" cellspacing="0">
                <tr>
                    <td width="50%">
                        <h3>${company.businessName}</h3>
                        <#if company.code?has_content><p>Įmonės kodas: <b>${company.code}</b></p></#if>
                        <#if company.vatCode?has_content><p>PVM kodas: <b>${company.vatCode}</b></p></#if>
                        <#if company.address?has_content><p>Adresas: ${company.address}</p></#if>
                        <#if banks?has_content && banks?size == 1>
                            <p>Bankas: ${banks[0].account}, ${banks[0].bank.name!""}<#if (banks[0].bank.swift)?has_content>, SWIFT:&nbsp;${banks[0].bank.swift}</#if></p>
                        </#if>
                        <#if banks?has_content && banks?size gt 1>
                            <p>Bankai:</p>
                            <#list banks as bank>
                                <p>${bank.account}, ${bank.bank.name!""}<#if (bank.bank.swift)?has_content>, SWIFT:&nbsp;${bank.bank.swift}</#if></p>
                            </#list>
                        </#if>
                        <#if company.contactsInfo??>
                            <#list company.contactsInfo as contact>
                                <p>
                                    <#if contact.type == "phone" || contact.type == "mobile">Telefonas: </#if>
                                    <#if contact.type == "fax">Faksas: </#if>
                                    <#if contact.type == "email">El.paštas: </#if>
                                    ${contact.contact}
                                </p>
                            </#list>
                        </#if>
                    </td>

                    <td width="50%">
                        <#if document.counterparty??>
                            <h3>Pirkėjas: ${document.counterparty.name}</h3>
                            <#if document.counterparty.comCode?has_content>
                                <p>Įmonės kodas: <b>${document.counterparty.comCode}</b></p>
                            </#if>
                            <#if document.counterparty.vatCode?has_content>
                                <p>PVM kodas: <b>${document.counterparty.vatCode}</b></p>
                            </#if>
                            <#if document.address??>
                                <p>Adresas: ${document.address}</p>
                            </#if>
                        </#if>
                    </td>
                </tr>
            </table>
            <#if document.dueDate?has_content && document.dueDate != document.date>
                <table border="0" cellspacing="0">
                    <tr><td><b>Apmokėti iki: ${document.dueDate}</b></td></tr>
                </table>
            <#else>
                <br>
            </#if>
            <!-- VAT info will be printed if where are more than 1 VAT rate -->
            <#assign showVat = company.vatCode?has_content && !document.zeroVAT && document.vatCodeTotals?has_content>
            <table border="0" cellspacing="0" style="-fs-table-paginate: paginate;-fs-page-break-min-height: 1.5cm;">
                <thead>
                    <tr class="line">
                        <th class="text-right">#</th>
                        <th class="wide">Pavadinimas</th>
                        <#if printSKU><th>Kodas</th></#if>
                        <#if showVat><th class="text-right text-nowrap">PVM %</th></#if>
                        <th class="text-right">Kiekis</th>
                        <th>Vnt.</th>
                        <th class="text-right">Kaina</th>
                        <#if hasDiscounts>
                            <th>Nuolaida</th>
                            <th class="text-right">Gal.kaina</th>
                        </#if>
                        <th class="text-right">Suma</th>
                    </tr>
                </thead>
                <tbody>
                    <#if document.parts??>
                        <#assign nr = 0>
                        <#list document.parts as part>
                            <tr class="line">
                                <#assign nr = nr + 1>
                                <td class="text-right">${nr}</td>
                                <td>${part.name!""}</td>
                                <#if printSKU><td class="text-nowrap">${part.sku!""}</td></#if>
                                <#if showVat>
                                    <td class="text-right">
                                        <#if part.taxable>
                                            <#if (part.vat.rate)?has_content>${part.vat.rate}%
                                            <#elseif (part.vatRate)?has_content>${part.vatRate}%
                                            </#if>
                                        </#if>
                                    </td>
                                </#if>
                                <td class="text-right">${part.quantity!""}</td>
                                <td>${part.unit!""}</td>
                                <td class="text-right text-nowrap"><#if part.fixTotal>${moneyFormatter.format(part.price)}<#else>${moneyFormatter.formatPrice(part.price)}</#if></td>
                                <#if hasDiscounts>
                                    <td class="text-right text-nowrap"><#if part.discount?has_content && part.discount != 0>${part.discount}%</#if></td>
                                    <td class="text-right text-nowrap"><#if part.fixTotal>${moneyFormatter.format(part.discountedPrice)}<#else>${moneyFormatter.formatPrice(part.discountedPrice)}</#if></td>
                                </#if>
                                <td class="text-right text-nowrap">${moneyFormatter.formatStd(part.discountedTotal)}</td>
                            </tr>

                            <#if part.parts?? && (!part.noPrint?has_content || !part.noPrint)>
                                <#list part.parts as partpart>
                                    <tr class="line">
                                        <td></td>
                                        <td>&#x25BA;&nbsp; ${partpart.name!""}</td>
                                        <#if printSKU><td class="text-nowrap">${partpart.sku!""}</td></#if>
                                        <#if showVat><td></td></#if>
                                        <td class="text-right">${partpart.quantity!""}</td>
                                        <td>${partpart.unit!""}</td>
                                        <td></td>
                                        <#if hasDiscounts>
                                            <td colspan="2"></td>
                                        </#if>
                                        <td></td>
                                    </tr>
                                </#list>
                            </#if>

                        </#list>
                    </#if>
                </tbody>
            </table>
            <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                <tbody>
                <#if document.discount?has_content && document.discount != 0>
                    <tr>
                        <td width="100%"></td>
                        <td class="text-right"><label>Suma be nuolaidos:</label></td>
                        <td class="text-right text-nowrap">${moneyFormatter.formatStd(document.partsTotal)}</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td class="text-right">
                            <label>Nuolaida:</label>
                            <span>${document.discount}%</span>
                        </td>
                        <td class="text-right text-nowrap">${moneyFormatter.formatStd(moneyFormatter.negated(document.discountTotal))}</td>
                    </tr>
                </#if>
                <#if !document.zeroVAT && company.vatCode?has_content>
                    <tr>
                        <td></td>
                        <td class="text-right"><label>Suma be PVM:</label></td>
                        <td class="text-right text-nowrap">${moneyFormatter.formatStd(document.subtotal)}</td>
                    </tr>
                    <#if document.vatCodeTotals?has_content>
                        <#list document.vatCodeTotals as vat>
                            <#if (vat.rate)?? && document.vatCodeTotals?size == 1>
                                 <tr>
                                    <td></td>
                                    <td class="text-right"><label>PVM ${vat.rate}%:</label></td>
                                    <td class="text-right text-nowrap">${moneyFormatter.formatStd(document.taxTotal)}</td>
                                </tr>
                            <#elseif (vat.rate)??>
                                <tr>
                                    <td></td>
                                    <td class="text-right"><label>PVM ${vat.rate}%:</label></td>
                                    <td class="text-right text-nowrap">${moneyFormatter.formatStd(vat.tax)}</td>
                                </tr>
                            </#if>
                        </#list>
                    </#if>
                </#if>
                    <tr>
                        <td><b>Suma žodžiais: ${moneyFormatter.text(document.total)}</b></td>
                        <td class="text-right"><label>Viso<#if !document.zeroVAT && company.vatCode?has_content> su PVM</#if>:</label></td>
                        <td class="text-right text-nowrap"><b>${moneyFormatter.formatStd(document.total)}</b></td>
                    </tr>
                <#if document.reverseVAT?has_content && document.reverseVAT>
                    <tr>
                        <td></td>
                        <td class="text-right line line-top line-left"><label>Viso mokėti:</label></td>
                        <td class="text-right text-nowrap line line-top line-right"><b>${moneyFormatter.formatStd(document.subtotal)}</b></td>
                    </tr>
                </#if>
                </tbody>
            </table>

            <#if document.ecr!false>
                <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                    <tr>
                        <td class="line">
                            EKA Nr.:
                                <#if document.ecrNo?has_content>
                                    ${document.ecrNo},
                                <#else>
                                    <span style="width:150px;display:inline-block;">&nbsp;</span>
                                </#if>
                            Čekio Nr.:
                                <#if document.ecrReceipt?has_content>
                                    ${document.ecrReceipt},
                                <#else>
                                    <span style="width:150px;display:inline-block;">&nbsp;</span>
                                </#if>

                            <#if company.vatCode?has_content && document.vatCodeTotals?has_content>
                                <#list document.vatCodeTotals as vat>
                                    PVM ${vat.rate}% ${moneyFormatter.formatStd(vat.tax)},
                                </#list>
                            </#if>

                            Data: ${document.date!""}
                        </td>
                    </tr>
                </table>
            </#if>

            <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                <#if document.paymentId?has_content>
                    <tr><td class="line"><b>Apmokant, mokėjimo paskirtyje prašome nurodyti kodą:</b> ${document.paymentId!""}</td></tr>
                </#if>
                <#if document.invoiceNote?has_content>
                    <tr><td class="line multiline"><b>Pastabos:</b> ${document.invoiceNote!""}</td></tr>
                </#if>
                <#if document.according?has_content>
                    <tr><td class="line"><b>Pagal:</b> ${document.according!""}</td></tr>
                </#if>
                <tr><td></td></tr>
                <tr>
                    <td class="line">
                        <b>Prekes išdavė / paslaugą atliko:</b>
                        <#if document.employee??>
                            <#if document.employee.name?has_content> ${document.employee.name}<#if document.employee.office?has_content>, ${document.employee.office}</#if></#if>
                        </#if>
                    </td>
                </tr>
                <tr><td></td></tr>
                <tr><td class="line"><b>Prekes / paslaugą priėmė:</b></td></tr>
                <tr><td class="small text-center">vardas, pavardė, pareigos, parašas</td></tr>
            </table>

            <#if document.driver?has_content || document.transportId?has_content || document.transportMarque?has_content ||
                           (document.loadAddress.address1)?has_content || (document.unloadAddress.address1)?has_content>
            <div style="page-break-inside:avoid;">
                <br>
                <br>
                <br>
                <table border="0" cellspacing="0">
                    <tr>
                        <td colspan="2" class="line"><b>Vairuotojas:</b> ${document.driver!""}</td>
                    </tr>
                    <tr>
                        <td class="line"><b>Automobilio valstybinis Nr:</b> ${document.transportId!""}</td>
                        <td class="line"><b>Automobilio markė:</b> ${document.transportMarque!""}</td>
                    </tr>
                    <tr>
                        <td width="50%">
                            <p><b>Pakrovimo adresas:</b></p>
                            <#if (document.loadAddress.address1)?has_content><p>${document.loadAddress.address}</p></#if>
                        </td>
                        <td width="50%">
                            <p><b>Iškrovimo adresas:</b></p>
                            <#if (document.unloadAddress.address1)?has_content><p>${document.unloadAddress.address}</p></#if>
                        </td>
                    </tr>
                </table>
            </div>
            </#if>
        </div>
    </div>
</div>
</body>
</html>