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
                            <h1>Išankstinė sąskaita apmokėjimui</h1>
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
                    <#if company.code?has_content><p><b>Įmonės kodas:</b> ${company.code}</p></#if>
                    <#if company.vatCode?has_content><p><b>PVM kodas:</b> ${company.vatCode}</p></#if>
                    <#if company.address?has_content><p><b>Adresas:</b> ${company.address}</p></#if>
                    <#if banks?has_content && banks?size == 1>
                        <p><b>Bankas: </b>${banks[0].account}, ${banks[0].bank.name!""}</p>
                    </#if>
                    <#if banks?has_content && banks?size gt 1>
                        <p><b>Bankai:</b></p>
                        <#list banks as bank>
                            <p>${bank.account}, ${bank.bank.name!""}</p>
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
            <!-- VAT info will be printed if where are more than 1 VAT rate -->
            <#assign showVat = !document.zeroVAT>
            <table border="0" cellspacing="0">
                <thead>
                    <tr class="line">
                        <th class="text-right">#</th>
                        <th class="wide">Pavadinimas</th>
                        <#if printSKU><th>Kodas</th></#if>
                        <#if showVat><th class="text-right text-nowrap">PVM %</th></#if>
                        <th class="text-right">Kiekis</th>
                        <th>Vnt.</th>
                        <th class="text-right">Kaina</th>
                        <th class="text-right">Suma</th>
                    </tr>
                </thead>
                <tbody>
                    <#assign nr = 0>
                    <#list document.parts as part>
                        <tr class="line"><#assign nr = nr + 1>
                            <td class="text-right">${nr}</td>
                            <td>${part.name!""}</td>
                            <#if printSKU><td class="text-nowrap">${part.sku!""}</td></#if>
                            <#if showVat><td class="text-right text-nowrap"><#if part.taxable && part.vat.rate?has_content>${part.vat.rate}%</#if></td></#if>
                            <td class="text-right text-nowrap">${part.estimate!""}</td>
                            <td>${part.unit!""}</td>
                            <td class="text-right text-nowrap">${moneyFormatter.formatPrice(part.price)}</td>
                            <td class="text-right text-nowrap">${moneyFormatter.formatStd(part.total)}</td>
                        </tr>
                        <#if part.parts??>
                            <#list part.parts as partpart>
                                <tr class="line">
                                    <td></td>
                                    <td>&#x25BA;&nbsp; ${partpart.name!""}</td>
                                    <#if printSKU><td class="text-nowrap">${partpart.sku!""}</td></#if>
                                    <#if showVat><td></td></#if>
                                    <td class="text-right text-nowrap">${partpart.estimate!""}</td>
                                    <td>${partpart.unit!""}</td>
                                    <td colspan="2"></td>
                                </tr>
                            </#list>
                        </#if>
                    </#list>
                </tbody>
            </table>
            <table border="0" cellspacing="0" style="page-break-inside:avoid;">
                <tbody>
                    <#if company.vatCode?has_content>
                        <tr>
                            <td></td>
                            <td class="text-right"><label>Suma be PVM:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(document.subtotal)}</div></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td class="text-right"><label>PVM Suma:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.formatStd(document.taxTotal)}</div></td>
                        </tr>
                    </#if>
                        <tr>
                            <td class="text-nowrap"><b>Viso: ${moneyFormatter.text(document.total)}</b></td>
                            <td class="text-right"><label>Viso<#if company.vatCode?has_content> su PVM</#if>:</label></td>
                            <td class="text-right"><div class="text-nowrap"><b>${moneyFormatter.formatStd(document.total)}</b></div></td>
                        </tr>
                    <#if document.prepayment?has_content>
                        <tr>
                            <td></td>
                            <td class="text-right line-top"><label>Išankstinė suma:</label></td>
                            <td class="text-right line-top text-nowrap"><div><b>${moneyFormatter.formatStd(document.prepayment)}</b></div></td>
                        </tr>
                    </#if>
                </tbody>
            </table>

            <#if document.estimateNote?has_content>
            <br>
            <table border="0" cellspacing="0">
                <tr><td class="line multiline"><b>Pastabos:</b> ${document.estimateNote!""}</td></tr>
            </table>
            </#if>
        </div>
    </div>
</div>
</body>
</html>
