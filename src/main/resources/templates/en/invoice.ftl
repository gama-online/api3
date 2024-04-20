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
        html,body{font-family:Helvetica,Arial,sans-serif}
        .wide{width:100%}
        @page{@top-left{content:'${document.number!""} ${document.date!""}';font-size:12px;font-family:Helvetica,Arial,sans-serif;font-weight:700}}
        @page{@top-right{content:'Page ' counter(page) ' of ' counter(pages);font-size:12px;font-family:Helvetica,Arial,sans-serif}}
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
                            <#if moneyFormatter.isNegative(document.total) || document.creditInvoice?has_content && document.creditInvoice>Credit </#if>
                            Invoice
                            </h1>
                            <h3>Invoice No.: ${document.number!""}</h3>
                            <h3>Invoice Date: ${document.date!""}</h3>
                        </div>
                    </td>
                </tr>
            </table>
            <table border="0" cellspacing="0">
                <tr>
                    <td width="50%">
                        <h3>${company.businessName}</h3>
                        <#if company.code?has_content><p>Reg. code: <b>${company.code}</p></b></#if>
                        <#if company.vatCode?has_content><p>VAT code: <b>${company.vatCode}</p></b></#if>
                        <#if company.address?has_content><p>Address: ${company.address}</p></#if>
                        <#if banks?has_content && banks?size == 1>
                            <p>Account: ${banks[0].account}, ${banks[0].bank.name!""}<#if (banks[0].bank.swift)?has_content>, SWIFT:&nbsp;${banks[0].bank.swift}</#if></p>
                        </#if>
                        <#if banks?has_content && banks?size gt 1>
                            <p>Accounts:</p>
                            <#list banks as bank>
                                <p>${bank.account}, ${bank.bank.name!""}<#if (bank.bank.swift)?has_content>, SWIFT:&nbsp;${bank.bank.swift}</#if></p>
                            </#list>
                        </#if>
                    </td>

                    <td width="50%">
                        <h3>Bill To: ${document.counterparty.name}</h3>
                        <#if document.counterparty.comCode?has_content>
                            <p>Reg. code: <b>${document.counterparty.comCode}</b></p>
                        </#if>
                        <#if document.counterparty.vatCode?has_content>
                            <p>VAT code: <b>${document.counterparty.vatCode}</b></p>
                        </#if>
                        <#if document.address??>
                            <p>Address: ${document.address}</p>
                        </#if>
                    </td>
                </tr>
            </table>
            <#if document.dueDate?has_content && document.dueDate != document.date>
                <table border="0" cellspacing="0">
                    <tr><td><b>Due Date: ${document.dueDate}</b></td></tr>
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
                        <th class="wide">Item Title</th>
                        <#if printSKU><th class="text-nowrap">SKU</th></#if>
                        <#if showVat><th class="text-right text-nowrap">VAT %</th></#if>
                        <th class="text-right text-nowrap">Q-ty</th>
                        <th class="">Units</th>
                        <th class="text-right">Price</th>
                        <#if hasDiscounts>
                        <th class="">Discount</th>
                        <th class="text-right">Disc.Price</th>
                        </#if>
                        <th class="text-right">Total</th>
                    </tr>
                </thead>
                <tbody>
                    <#if document.parts??>
                        <#assign nr = 0>
                        <#list document.parts as part>
                            <tr class="line"><#assign nr = nr + 1>
                                <td class="text-right">${nr}</td>
                                <td>${translate.translate(part.name, part.translation, "en", "name")!""}</td>
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
                                <td class="text-right text-nowrap">${part.quantity!""}</td>
                                <td>${translate.translate(part.unit, part.translation, "en", "unit")!""}</td>
                                <td class="text-right text-nowrap"><#if part.fixTotal>${moneyFormatter.format(part.price)}<#else>${moneyFormatter.formatPrice(part.price)}</#if></td>
                                <#if hasDiscounts>
                                    <td class="text-right text-nowrap"><#if part.discount?has_content && part.discount != 0>${part.discount}%</#if></td>
                                    <td class="text-right text-nowrap"><#if part.fixTotal>${moneyFormatter.format(part.discountedPrice)}<#else>${moneyFormatter.formatPrice(part.discountedPrice)}</#if></td>
                                </#if>
                                <td class="text-right text-nowrap">${moneyFormatter.formatStd(part.discountedTotal)}</td>
                            </tr>
                            <#if part.parts??>
                                <#list part.parts as partpart>
                                    <tr class="line">
                                        <td></td>
                                        <td>&#x25BA;&nbsp; ${translate.translate(partpart.name, partpart.translation, "en", "name")!""}</td>
                                        <#if printSKU><td class="text-nowrap">${partpart.sku!""}</td></#if>
                                        <#if showVat><td></td></#if>
                                        <td class="text-right text-nowrap">${partpart.quantity!""}</td>
                                        <td>${translate.translate(partpart.unit, partpart.translation, "en", "unit")!""}</td>
                                        <td colspan="2"></td>
                                        <#if hasDiscounts>
                                        <td colspan="2"></td>
                                        </#if>
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
                            <td class="text-right"><label>Subtotal w/o discount:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(document.partsTotal)}</div></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td class="text-right">
                                <label>Discount:</label>
                                <span class="text-nowrap">${document.discount}%</span>
                            </td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(moneyFormatter.negated(document.discountTotal))}</div></td>
                        </tr>
                    </#if>
                    <#if !document.zeroVAT && company.vatCode?has_content>
                        <tr>
                            <td></td>
                            <td class="text-right"><label>Subtotal w/o VAT:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(document.subtotal)}</div></td>
                        </tr>
                        <#if document.vatCodeTotals?has_content>
                            <#list document.vatCodeTotals as vat>
                                <#if (vat.rate)?? && document.vatCodeTotals?size == 1>
                                     <tr>
                                        <td></td>
                                        <td class="text-right"><label>VAT ${vat.rate}%:</label></td>
                                        <td class="text-right text-nowrap">${moneyFormatter.formatStd(document.taxTotal)}</td>
                                    </tr>
                                <#elseif (vat.rate)??>
                                    <tr>
                                        <td></td>
                                        <td class="text-right"><label>VAT ${vat.rate}%:</label></td>
                                        <td class="text-right text-nowrap">${moneyFormatter.formatStd(vat.tax)}</td>
                                    </tr>
                                </#if>
                            </#list>
                        </#if>
                    </#if>
                    <tr>
                        <td><b>Total: ${moneyFormatter.text(document.total)}</b></td>
                        <td class="text-right"><label>TOTAL<#if !document.zeroVAT && company.vatCode?has_content> w/ VAT</#if>:</label></td>
                        <td class="text-right"><div class="text-nowrap"><b>${moneyFormatter.formatStd(document.total)}</b></div></td>
                    </tr>
                <#if document.reverseVAT?has_content && document.reverseVAT>
                    <tr>
                        <td></td>
                        <td class="text-right line line-top line-left"><label>Total amount to be paid:</label></td>
                        <td class="text-right text-nowrap line line-top line-right"><b>${moneyFormatter.formatStd(document.subtotal)}</b></td>
                    </tr>
                </#if>
                </tbody>
            </table>

            <#if document.ecr!false>
                <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                    <tr>
                        <td class="line">
                            ECR No.:
                                <#if document.ecrNo?has_content>
                                    ${document.ecrNo},
                                <#else>
                                    <span style="width:150px;display:inline-block;">&nbsp;</span>
                                </#if>
                            Receipt No.:
                                <#if document.ecrReceipt?has_content>
                                    ${document.ecrReceipt},
                                <#else>
                                    <span style="width:150px;display:inline-block;">&nbsp;</span>
                                </#if>

                            <#if company.vatCode?has_content && document.vatCodeTotals?has_content>
                                <#list document.vatCodeTotals as vat>
                                    VAT ${vat.rate}% ${moneyFormatter.formatStd(vat.tax)},
                                </#list>
                            </#if>

                            Date: ${document.date!""}
                        </td>
                    </tr>
                </table>
            </#if>

            <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                <#if document.invoiceNote?has_content>
                    <tr>
                      <td class="line multiline">
                        <b>Notes:</b> ${translate.translate(document.invoiceNote, document.translation, "en", "invoiceNote")!""}
                      </td>
                    </tr>
                </#if>
                <#if document.employee??>
                    <tr>
                        <td class="line">
                            <b><#if document.employee.name?has_content> ${document.employee.name}<#if document.employee.office?has_content>, ${translate.translate(document.employee.office, document.employee.translation, "en", "office")!""}</#if></#if></b>
                        </td>
                    </tr>
                </#if>
                <#if document.according?has_content>
                    <tr><td class="line"><b>According:</b> ${document.according!""}</td></tr>
                </#if>
            </table>
        </div>
    </div>
</div>
</body>
</html>