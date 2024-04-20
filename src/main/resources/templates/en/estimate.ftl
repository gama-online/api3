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
                            <h1>Proforma Invoice</h1>
                            <h3>No.: ${document.number!""}</h3>
                            <h3>Date: ${document.date!""}</h3>
                        </div>
                    </td>
                </tr>
            </table>
            <table border="0" cellspacing="0">
                <tr>
                    <td width="50%">
                        <h3>${company.businessName}</h3>
                    <#if company.code?has_content><p><b>Reg. code:</b> ${company.code}</p></#if>
                    <#if company.vatCode?has_content><p><b>VAT code:</b> ${company.vatCode}</p></#if>
                    <#if company.address?has_content><p><b>Address:</b> ${company.address}</p></#if>
                    <#if banks?size == 1>
                        <p><b>Account: </b>${banks[0].account}, ${banks[0].bank.name!""}<#if (banks[0].bank.swift)?has_content>, SWIFT:&nbsp;${banks[0].bank.swift}</#if></p>
                    </#if>
                    <#if banks?size gt 1>
                        <p><b>Accounts:</b></p>
                        <#list banks as bank>
                            <p>${bank.account}, ${bank.bank.name!""}<#if (bank.bank.swift)?has_content>, SWIFT:&nbsp;${bank.bank.swift}</#if></p>
                        </#list>
                    </#if>
                    </td>

                    <td width="50%">
                        <h3>Bill To: ${document.counterparty.name}</h3>
                    <#if document.counterparty.comCode?has_content>
                        <p><b>Reg. code:</b> ${document.counterparty.comCode}</p>
                    </#if>
                    <#if document.counterparty.vatCode?has_content>
                        <p><b>VAT code: </b> ${document.counterparty.vatCode}</p>
                    </#if>
                    <#if (document.address)?has_content><p><b>Address:</b> ${document.address}</p></#if>
                    </td>
                </tr>
            </table>
            <#assign showVat = company.vatCode?has_content && !document.zeroVAT>
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
                                <#if showVat><td class="text-right text-nowrap"><#if part.taxable && part.vat.rate?has_content>${part.vatRate!""}%</#if></td></#if>
                                <td class="text-right">${part.estimate!""}</td>
                                <td>${translate.translate(part.unit, part.translation, "en", "unit")!""}</td>
                                <td class="text-right text-nowrap">${moneyFormatter.formatPrice(part.price)}</td>
                                <td class="text-right text-nowrap">${moneyFormatter.formatStd(part.total)}</td>
                            </tr>
                            <#if part.parts??>
                                <#list part.parts as partpart>
                                    <tr class="line">
                                        <td></td>
                                        <td>&#x25BA;&nbsp; ${translate.translate(partpart.name, partpart.translation, "en", "name")!""}</td>
                                        <#if printSKU><td class="text-nowrap">${partpart.sku!""}</td></#if>
                                        <#if showVat><td></td></#if>
                                        <td class="text-right">${partpart.estimate!""}</td>
                                        <td>${translate.translate(partpart.unit, partpart.translation, "en", "unit")!""}</td>
                                        <td colspan="2"></td>
                                    </tr>
                                </#list>
                            </#if>
                        </#list>
                    </#if>
                </tbody>
            </table>
            <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                <tbody>
                    <#if company.vatCode?has_content>
                        <tr>
                            <td width="100%"></td>
                            <td class="text-right"><label>Subtotal w/o VAT:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(document.subtotal)}</div></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td class="text-right"><label>VAT Total:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(document.taxTotal)}</div></td>
                        </tr>
                    </#if>
                        <tr>
                            <td><b>Total: ${moneyFormatter.text(document.total)}</b></td>
                            <td class="text-right"><label>Total<#if company.vatCode?has_content> w/ VAT</#if>:</label></td>
                            <td class="text-right"><div class="text-nowrap"><b>${moneyFormatter.formatStd(document.total)}</b></div></td>
                        </tr>
                    <#if document.prepayment?has_content>
                        <tr>
                            <td></td>
                            <td class="text-right line-top"><label>Prepayment:</label></td>
                            <td class="text-right line-top"><div class="text-nowrap"><b>${moneyFormatter.formatStd(document.prepayment)}</b></div></td>
                        </tr>
                    </#if>
                </tbody>
            </table>

            <#if document.estimateNote?has_content>
            <br>
            <table border="0" cellspacing="0">
                <tr><td class="line"><b>Notes:</b> ${document.estimateNote!""}</td></tr>
            </table>
            </#if>
        </div>
    </div>
</div>
</body>
</html>
