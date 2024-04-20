<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${document.number!""} ${document.date!""}</title>
    <style type="text/css">
        *{margin:0;font-family:"Helvetica Neue", Helvetica, Arial, sans-serif;box-sizing:border-box;font-size:14px}
        body{-webkit-font-smoothing:antialiased;width:100% !important;height:100%;line-height:1.6em}
        table td{vertical-align:top}
        body{background-color:#f6f6f6}
        .body-wrap{background-color:#f6f6f6;width:100%}
        .container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}
        .content{max-width:600px;margin:0 auto;display:block;padding:20px}
        .main{background-color:#fff;border:1px solid #e9e9e9;border-radius:3px}
        .content-wrap{padding:20px}.content-block{padding:0 0 20px}
        .header{width:100%;margin-bottom:20px}
        .footer{width:100%;clear:both;color:#999;padding:20px}
        .footer p,.footer a,.footer td{color:#999;font-size:12px}
        .logo{width:200px;left:0;height:100px}
        .logo img{max-width:200px;max-height:100px}
        .logo{position:relative;top:0;left:0}
        .multiline{white-space:pre-line}
        h1,h2,h3{color:#000;margin:0;line-height:1.2em;font-weight:400}h1{font-size:32px;font-weight:500}h2{font-size:24px;font-weight:600}h3{font-size:18px;font-weight:600}h4{font-size:14px;font-weight:600}
        p,ul,ol{margin-bottom:10px;font-weight:normal}
        p li,ul li,ol li{margin-left:5px;list-style-position:inside}
        a{color:#348eda;text-decoration:underline}
        .text-center{text-align:center}.text-right{text-align:right}.text-left{text-align:left}.text-nowrap{white-space:nowrap}
        .clear{clear:both}
        @media only screen and (max-width: 640px){
            body{padding:0 !important}
            h1{font-weight:800 !important;margin:20px 0 5px !important}
            h2,h3,h4{font-weight:800 !important;margin:5px 0 5px !important}
            h1{font-size:22px !important}h2{font-size:18px !important}h3{font-size:16px !important}
            .container{padding:0 !important;width:100% !important}
            .content{padding:0 !important}
            .content-wrap{padding:10px !important}
            .invoice{width:100% !important}
        }
    </style>
</head>
<body itemscope itemtype="http://schema.org/EmailMessage">
<table class="body-wrap" style="background-color:#f6f6f6;width:100%">
    <tr>
        <td></td>
        <td class="container" width="600" style="display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important">
            <div class="content" style="max-width:600px;margin:0 auto;display:block;padding:20px">
                <table class="main" width="100%" cellpadding="0" cellspacing="0" style="background-color:#fff;border:1px solid #e9e9e9;border-radius:3px">
                    <tr>
                        <td class="content-wrap" style="padding:20px">
                        <#if company.logo?has_content>
                            <div class="logo"><img src="${company.logo}"></div>
                        </#if>
                        </td>
                    </tr>
                    <tr>
                        <td class="content-wrap" style="padding:20px">
                            <h2>
                                <#if moneyFormatter.isNegative(document.total) || document.creditInvoice?has_content && document.creditInvoice>Kreditinė </#if>
                                <#if company.vatCode?has_content>PVM </#if>
                                    Sąskaita-faktūra
                            </h2>
                            <h3>Nr: ${document.number!""}</h3>
                            <h3>Data: ${document.date!""}</h3>
                        </td>
                    </tr>
                    <tr>
                        <td class="content-wrap" style="padding:20px">
                            <h3>${company.businessName}</h3>
                            <#if company.code?has_content>Įmonės kodas: <b>${company.code}</b>,</#if>
                            <#if company.vatCode?has_content>PVM kodas: <b>${company.vatCode}</b></#if>
                            <#if company.address?has_content><br>${company.address}</#if>
                            <#if banks?size == 1>
                                <br>Bankas: ${banks[0].account}, ${banks[0].bank.name!""}<#if (banks[0].bank.swift)?has_content>, SWIFT:&nbsp;${banks[0].bank.swift}</#if>
                            </#if>
                            <#if banks?size gt 1>
                                <br>Bankai:
                                <#list banks as bank>
                                    <br>${bank.account}, ${bank.bank.name!""}<#if (bank.bank.swift)?has_content>, SWIFT:&nbsp;${bank.bank.swift}</#if>
                                </#list>
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <td class="content-wrap" style="padding:20px">
                            <h3>Pirkėjas: ${document.counterparty.name}</h3>
                            <#if document.counterparty.comCode?has_content>Įmonės kodas: <b>${document.counterparty.comCode}</b>,</#if>
                            <#if document.counterparty.vatCode?has_content>PVM kodas: <b>${document.counterparty.vatCode}</b></#if>
                            <#if document.address??>
                                <br>${document.address}
                            </#if>
                        </td>
                    </tr>
                    <tr><td class="content-wrap" style="padding:20px"><b>Apmokėti iki: ${document.dueDate}</b></td></tr>
                    <tr>
                        <td class="content-wrap text-center" style="padding:20px;text-align:center">
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td class="content-block text-center" style="text-align:center;padding:0 0 20px">
                                        <table class="invoice" style="margin:auto;text-align:left;width:100%">
                                            <tr>
                                                <td>
                                                    <table class="invoice-items" cellpadding="0" cellspacing="0" style="width:100%">
                                                        <tr>
                                                            <td style="padding:2px;border-top:2px solid #333"><b>Pavadinimas</b></td>
                                                            <td colspan="2" class="text-center" style="padding:2px;text-align:center;border-top:2px solid #333"><b>Kiekis</b></td>
                                                            <td class="text-right" style="padding:2px;text-align:right;border-top:2px solid #333"><b>Kaina</b></td>
                                                            <td class="text-right" style="padding:2px;text-align:right;border-top:2px solid #333"><b>Suma</b></td>
                                                        </tr>
                                                    <#list document.parts as part>
                                                        <tr>
                                                            <td style="padding:2px;border-top:#eee 1px solid">${part.name!""}</td>
                                                            <td class="text-right text-nowrap" style="padding:2px;text-align:right;border-top:#eee 1px solid">${part.quantity!""}</td>
                                                            <td style="padding:2px;border-top:#eee 1px solid">${part.unit!""}</td>
                                                            <td class="text-right text-nowrap" style="padding:2px;text-align:right;border-top:#eee 1px solid">
                                                                <#if part.fixTotal>${moneyFormatter.format(part.discountedPrice)}<#else>${moneyFormatter.formatPrice(part.discountedPrice)}</#if>
                                                            </td>
                                                            <td class="text-right text-nowrap" style="padding:2px;text-align:right;border-top:#eee 1px solid">${moneyFormatter.formatStd(part.total)}</td>
                                                        </tr>
                                                        <#if part.parts??>
                                                            <#list part.parts as partpart>
                                                                <tr class="line">
                                                                    <td style="padding:2px;">&#x25BA;&nbsp; ${partpart.name!""}<#if printSKU> (${part.sku!""})</#if></td>
                                                                    <td class="text-right text-nowrap" style="padding:2px;text-align:right">${partpart.quantity!""}</td>
                                                                    <td style="padding:2px;">${partpart.unit!""}</td>
                                                                    <td colspan="2"></td>
                                                                </tr>
                                                            </#list>
                                                        </#if>
                                                    </#list>

                                                    <#if company.vatCode?has_content>
                                                        <tr>
                                                            <td colspan="4" class="text-right" style="padding:2px;text-align:right;border-top:2px solid #333"><b>Suma be PVM:</b></td>
                                                            <td class="text-right text-nowrap" style="padding:2px;text-align:right;border-top:2px solid #333"><b>${moneyFormatter.formatStd(document.subtotal)}</b></td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan="4" class="text-right" style="padding:2px;text-align:right"><b>PVM Suma:</b></td>
                                                            <td class="text-right text-nowrap" style="padding:2px;text-align:right"><b>${moneyFormatter.formatStd(document.taxTotal)}</b></td>
                                                        </tr>
                                                    </#if>
                                                        <tr>
                                                            <td colspan="4" class="text-right" style="padding:2px;text-align:right"><b>Viso<#if company.vatCode?has_content> su PVM</#if>:</b></td>
                                                            <td class="text-right text-nowrap" style="padding:2px;text-align:right"><b>${moneyFormatter.formatStd(document.total)}</b></td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <#if document.invoiceNote?has_content>
                    <tr>
                        <td class="content-wrap multiline" style="padding:20px">
                            <b>Pastabos:</b> ${document.invoiceNote!""}
                        </td>
                    </tr>
                    </#if>
                    <tr class="no-print">
                        <td class="content-block text-center" style="text-align:center;padding:0 0 20px">
                            <a href="https://gama-online.appspot.com/workers/print?l=lt&0=${document.uuid}" style="color:#348eda;text-decoration:underline">Atidaryti ir spausdinti naršyklėje</a>
                        </td>
                    </tr>
                </table>
                <div class="footer no-print" style="width:100%;clear:both;color:#999;padding:20px">
                    <table width="100%">
                        <tr>
                            <td class="text-center content-block" style="text-align:center;padding:0 0 20px;color:#999;font-size:12px">Dokumentas sugeneruotas
                                <a href="https://gama-online.lt" style="color:#999;text-decoration:underline;font-size:12px">gama-online.lt</a>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </td>
        <td></td>
    </tr>
</table>
</body>
</html>