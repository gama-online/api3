<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta charset="UTF-8">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Kasos knyga</title>
    <style>
        body{margin:0;padding:0;font-size:12px}
        *{box-sizing:border-box}
        .logo{width:200px;left:0;height:100px}
        .logo img{max-width:200px;max-height:100px}
        .logo,.title{position:relative;top:0}
        .title{right:0;text-align:right}
        table{border:none;width:100%}
        tr.line-bottom td,tr.line-bottom th{border-bottom:1px solid #000}
        tr.line-top th, tr.line-top td{border-top:1px solid #000}
        th{text-align:left}th,td{padding:5px}
        td{vertical-align:top}
        .line{border-bottom:1px solid #000}
        .border-left{border-left:1px solid #000}
        .border-right{border-right:1px solid #000}
        .small{padding:0;font-size:.66em}
        .text-right{text-align:right}
        .text-center{text-align:center}
        .text-nowrap{white-space:nowrap}
        label{text-align:right;font-weight:700;white-space:nowrap}
        .multiline{white-space:pre-line}
        h1,h2,h3,h4,h5,p{margin:0;padding:0}
        html,body{font-family:Helvetica,Arial,sans-serif}
    </style>
</head>
<body>
<div>
    <div class="page">
        <div class="subpage">
            <table border="0" cellspacing="0">
                <tr>
                    <td class="line">
                        <b>
                        ${company.businessName}<#if
                            company.code?has_content>, Įmonės kodas: ${company.code}</#if><#if
                            company.vatCode?has_content>, PVM kodas: ${company.vatCode}</#if><#if
                            company.address?has_content><br>Adresas: ${company.address}</#if>
                        </b>
                    </td>
                </tr>
            </table>

            <br>

            <h2 class="text-center">Kasos knyga</h2>
            <h3 class="text-center">Nuo ${dateFrom} iki ${dateTo}</h3>

            <br>

            <table border="0" cellspacing="0">
                <thead>
                    <tr>
                        <th colspan="7"><h3>${cash.name} (${currency})</h3></th>
                        <th class="text-right"><div class="pageNo"></div></th>
                    </tr>
                    <tr><td colspan="8"></td></tr>
                    <tr class="line-top line-bottom">
                        <th class="border-right"></th>
                        <th>Data</th>
                        <th>Dok.Nr.</th>
                        <th width="30%">Turinys</th>
                        <th width="20%">Mokėtojas / Gavėjas</th>
                        <th class="border-left text-right">Debitas</th>
                        <th class="border-left text-right">Kreditas</th>
                        <th class="border-left text-right">Likutis</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="line-bottom">
                        <th colspan="5" class="text-nowrap">Pradinis likutis:</th>
                        <th class="border-left"></th>
                        <th class="border-left"></th>
                        <th class="border-left text-right text-nowrap"><#if amount.opening??>${moneyFormatter.format(amount.opening)}</#if></th>
                    </tr>
                    <#list items as item>
                    <tr>
                        <td class="border-right text-right">${item?counter}</td>
                        <td class="text-nowrap">${item.doc.date}</td>
                        <td>${item.doc.number!""}</td>
                        <td>${item.doc.note!""}</td>
                        <td>
                            <#if item.counterparty??>${item.counterparty.name!""}</#if>
                            <#if item.employee??>${item.employee.name!""}</#if>
                        </td>
                        <td class="border-left text-right text-nowrap"><#if item.sumDebit??>${moneyFormatter.format(item.sumDebit)}</#if></td>
                        <td class="border-left text-right text-nowrap"><#if item.sumCredit??>${moneyFormatter.format(item.sumCredit)}</#if></td>
                        <td class="border-left text-right text-nowrap"><#if item.remainder??>${moneyFormatter.format(item.remainder)}</#if></td>
                    </tr>
                    </#list>
                    <tr class="line-top">
                        <th colspan="5">Viso per periodą:</th>
                        <th class="border-left text-right text-nowrap"><#if amount.debit??>${moneyFormatter.format(amount.debit)}</#if></th>
                        <th class="border-left text-right text-nowrap"><#if amount.credit??>${moneyFormatter.format(amount.credit)}</#if></th>
                        <th class="border-left"></th>
                    </tr>
                    <tr class="line-bottom">
                        <th colspan="5">Galutinis likutis:</th>
                        <th class="border-left text-right"></th>
                        <th class="border-left text-right"></th>
                        <th class="border-left text-right text-nowrap"><#if amount.remainder??>${moneyFormatter.format(amount.remainder)}</#if></th>
                    </tr>
                </tbody>

            </table>
            <br>
            <br>
            <table border="0" cellspacing="0">
                <tr class="line-bottom"><td>Kasininkas: </td></tr>
                <tr><td></td></tr>
                <tr><td>Įrašus knygoje patikrinau ir priėmiau ${debitCount} pajamų ir ${creditCount} išlaidų dokumentus</td></tr>
                <tr><td></td></tr>
                <tr class="line-bottom"><td>Buhalteris: </td></tr>
            </table>
            <br>
        </div>
    </div>
</div>
</body>
</html>
