<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta charset="UTF-8">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Avanso apyskaita</title>
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

            <h2 class="text-center">Avanso apyskaita Nr. ___________ ${dateTo}</h2>

            <br>
            <h3 class="text-center">Nuo ${dateFrom} iki ${dateTo}</h3>
            <br>
            <table border="0" cellspacing="0">
                <thead>
                <tr class="line-top">
                    <th>Data</th>
                    <th>Kv.Nr.</th>
                    <th class="text-right">Išduoti pinigai</th>
                    <th class="text-right">Atsiskaityta</th>
                    <th width="50%">Paskirtis / Mokėtojas / Gavėjas</th>
                </tr>
                <tr class="line-top">
                    <th colspan="2" class="text-nowrap">Pradinis likutis:</th>
                    <th class="text-right text-nowrap"><#if amount.openingDebit??>${moneyFormatter.format(amount.openingDebit)}</#if></th>
                    <th class="text-right text-nowrap"><#if amount.openingCredit??>${moneyFormatter.format(amount.openingCredit)}</#if></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <#list items as item>
                <tr>
                    <td>${item.doc.date}</td>
                    <td>${item.doc.number!""}</td>
                    <td class="text-right text-nowrap"><#if item.sumDebit??>${moneyFormatter.format(item.sumDebit)}</#if></td>
                    <td class="text-right text-nowrap"><#if item.sumCredit??>${moneyFormatter.format(item.sumCredit)}</#if></td>
                    <td>
                        ${docNames[item.doc.type]}:
                        <#if item.counterparty??>${item.counterparty.name}</#if>
                        <#if item.bankAccount??>${item.bankAccount.account}</#if>
                        <#if item.cash??>${item.cash.name}</#if>
                        <#if item.doc.note??> - ${item.doc.note}</#if>
                    </td>
                </tr>
                </#list>
                </tbody>
                <tfoot>
                <tr class="line-bottom">
                    <th colspan="2">Viso per periodą:</th>
                    <th class="text-right"><#if amount.debit??>${moneyFormatter.format(amount.debit)}</#if></th>
                    <th class="text-right"><#if amount.credit??>${moneyFormatter.format(amount.credit)}</#if></th>
                    <th></th>
                </tr>
                <tr class="line-bottom">
                    <th colspan="2">Galutinis likutis:</th>
                    <th class="text-right"><#if amount.remainderDebit??>${moneyFormatter.format(amount.remainderDebit)}</#if></th>
                    <th class="text-right"><#if amount.remainderCredit??>${moneyFormatter.format(amount.remainderCredit)}</#if></th>
                    <th></th>
                </tr>
                </tfoot>
            </table>
            <br>
            <br>
            <p>
                Atskaitingo asmens parašas ____________________________  <#if employee??><b>${employee.name!""}</b>, ${employee.office!""}</#if>
            </p>
            <br>
            <br>
            <p>
                Tvirtinu: ${dateTo} Direktorius ____________________________
            </p>
            <br>
        </div>
    </div>
</div>
</body>
</html>
