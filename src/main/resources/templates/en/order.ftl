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
                                <h1>Purchase Order</h1>
                                <h3>No.: ${document.number!""}</h3>
                                <h3>Date: ${document.date!""}</h3>
                            </div>
                        </td>
                    </tr>
                </table>
                <table border="0" cellspacing="0">
                    <tr>
                        <td width="50%">
                            <h3>Buyer: ${company.businessName}</h3>
                            <#if company.code?has_content><p><b>Reg. code:</b> ${company.code}</p></#if>
                            <#if company.vatCode?has_content><p><b>VAT code:</b> ${company.vatCode}</p></#if>
                            <#if company.address?has_content><p><b>Address:</b> ${company.address}</p></#if>
                        </td>

                        <td width="50%">
                            <h3>Seller: ${document.counterparty.name}</h3>
                            <#if document.counterparty.comCode?has_content>
                                <p><b>Reg. code:</b> ${document.counterparty.comCode}</p>
                            </#if>
                            <#if document.counterparty.vatCode?has_content>
                                <p><b>VAT code: </b> ${document.counterparty.vatCode}</p>
                            </#if>
                            <#if document.address??>
                                <p><b>Address:</b> ${document.address}</p>
                            </#if>
                        </td>
                    </tr>
                </table>
                <table border="0" cellspacing="0">
                    <thead>
                    <tr class="line">
                        <th class="text-right">#</th>
                        <th class="wide">Item Title</th>
                        <th class="">Code</th>
                        <th class="">SKU</th>
                        <th class="">Barcode</th>
                        <th class="text-right"><#if company.vatCode?has_content && !document.zeroVAT>VAT</#if></th>
                        <th class="text-right">Q-ty</th>
                        <th class="">Units</th>
                        <th class="text-right">Price</th>
                        <th class="text-right">Total</th>
                    </tr>
                    </thead>
                    <#assign nr = 0>
                    <#list document.parts as part>
                        <tr class="line"><#assign nr = nr + 1>
                            <td class="text-right">${nr}</td>
                            <td>${translate.translate(part.name, part.translation, "en", "name")!""}</td>
                            <td class="text-nowrap">${part.vendorCode!""}</td>
                            <td class="text-nowrap">${part.sku!""}</td>
                            <td class="text-nowrap">${part.barcode!""}</td>
                            <td class="text-right text-nowrap"><#if company.vatCode?has_content && part.taxable && !document.zeroVAT>${part.vatRate!""}%</#if></td>
                            <td class="text-right text-nowrap">${part.estimate!""}</td>
                            <td>${translate.translate(part.unit, part.translation, "en", "unit")!""}</td>
                            <td class="text-right text-nowrap">${moneyFormatter.format(part.price)}</td>
                            <td class="text-right text-nowrap">${moneyFormatter.format(part.total)}</td>
                        </tr>
                    </#list>
                    <#if company.vatCode?has_content>
                        <tr>
                            <td colspan="9" class="text-right"><label>Subtotal w/o VAT:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.format(document.subtotal)}</div></td>
                        </tr>
                        <tr>
                            <td colspan="9" class="text-right"><label>VAT Total:</label></td>
                            <td class="text-right"><div class="text-nowrap">${moneyFormatter.format(document.taxTotal)}</div></td>
                        </tr>
                    </#if>
                    <tr>
                        <td colspan="6" class="text-nowrap"><b>Total: ${moneyFormatter.text(document.total)}</b></td>
                        <td colspan="3" class="text-right"><label>Total<#if company.vatCode?has_content> w/ VAT</#if>:</label></td>
                        <td class="text-right"><div class="text-nowrap"><b>${moneyFormatter.format(document.total)}</b></div></td>
                    </tr>
                    <#if document.prepayment?has_content>
                        <tr>
                            <td colspan="9" class="text-right line-top"><label>Prepayment:</label></td>
                            <td class="text-right line-top"><div class="text-nowrap"><b>${moneyFormatter.format(document.prepayment)}</b></div></td>
                        </tr>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</body>
</html>
