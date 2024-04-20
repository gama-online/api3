<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta charset="UTF-8">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Tarpusavio atsiskaitymo suderinimo aktas</title>
    <style>
        body{margin:0;padding:0;font-size:12px}
        *{box-sizing:border-box}
        .logo{width:200px;left:0;height:100px}
        .logo img{max-width:200px;max-height:100px}
        .logo,.title{position:relative;top:0}
        .title{right:0;text-align:right}
        table{border:none;width:100%}
        tr.line-bottom td,tr.line-bottom th{border-bottom:1px solid #000}
        tr.line-top th,tr.line-top td,th.line-top,td.line-top{border-top:1px solid #000}
        th{text-align:left}th,td{padding:5px}
        td{vertical-align:top}
        td.line-left, th.line-left {border-left:1px solid #000}
        .line{border-bottom:1px solid #000}
        .small{padding:0;font-size:.66em}
        .text-right{text-align:right}
        .text-center{text-align:center}
        .text-nowrap{white-space:nowrap}
        label{text-align:right;font-weight:700;white-space:nowrap}
        .multiline{white-space:pre-line}
        h1,h2,h3,h4,h5,p{margin:0;padding:0}
        html,body{font-family:Helvetica,Arial,sans-serif}
        @page{@top-left{content:'Tarpusavio atsiskaitymo suderinimo aktas';font-size:12px;font-family:Helvetica,Arial,sans-serif;font-weight:700}}
        @page{@top-center{content:'${company.name} - ${counterparty.name}';font-size:12px;font-family:Helvetica,Arial,sans-serif}}
        @page{@top-right{content:'Lapas ' counter(page) ' iš ' counter(pages);font-size:12px;font-family:Helvetica,Arial,sans-serif}}
        @page:first{@top-left{content:''}@top-center{content:''}@top-right{content:''}}
</style>
</head>
<body>
<div>
    <div class="page">
        <div class="subpage">
            <h2 class="text-center">Tarpusavio atsiskaitymo suderinimo aktas</h2>
            <br><br>
            <p>
                Mes, žemiau pasirašiusieji, ${company.name}, įmonės kodas ${company.code}, adresas ${company.address} ir
                ${counterparty.name},
                <#if counterparty.comCode?has_content>įmonės kodas ${counterparty.comCode},</#if>
                <#if counterparty.address?has_content>adresas ${counterparty.address},</#if>
                atstovai surašėme šį tarpusavio atsiskaitymo suderinimo aktą.
            </p>
            <br>
            <table border="0" cellspacing="0" style="-fs-table-paginate:paginate">
                <colgroup>
                    <col>
                    <col>
                    <col>
                    <col width="15%">
                    <col width="15%">
                    <col width="15%">
                    <col width="15%">
                </colgroup>
                <thead>
                    <tr class="line-top line-bottom">
                        <th rowspan="2" class="text-center">Data</th>
                        <th rowspan="2" class="text-center line-left">Nr.</th>
                        <th rowspan="2" class="text-center line-left">Dok.tipas</th>

                        <th colspan="2" class="text-center line-left">${company.name}</th>
                        <th colspan="2" class="text-center line-left">${counterparty.name}</th>
                    </tr>
                    <tr class="line-bottom">
                        <th class="text-center line-left line-top">Debetas</th>
                        <th class="text-center line-left line-top">Kreditas</th>

                        <th class="text-center line-left line-top">Debetas</th>
                        <th class="text-center line-left line-top">Kreditas</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="line-bottom">
                        <th class="text-nowrap">${dateFrom}</th>
                        <th></th>
                        <th></th>

                        <th class="text-right line-left text-nowrap">
                            <#list debt?keys as key>
                                <div><#if debt[key].debitFrom??>${moneyFormatter.format(debt[key].debitFrom)}<#else>${moneyFormatter.formatZero(key)}</#if></div>
                            </#list>
                        </th>

                        <th class="text-right line-left text-nowrap">
                            <#list debt?keys as key>
                                <div><#if debt[key].creditFrom??>${moneyFormatter.format(debt[key].creditFrom)}<#else>${moneyFormatter.formatZero(key)}</#if></div>
                            </#list>
                        </th>

                        <th class="line-left"></th>
                        <th class="line-left"></th>
                    </tr>
                    <#list items as item>
                        <tr>
                            <td class="text-nowrap">${item.doc.docDate!item.doc.date}</td>
                            <td class="line-left">${item.doc.number!""}</td>
                            <td class="line-left">${docNames[item.doc.type]}</td>

                            <td class="text-right line-left text-nowrap"><#if item.debit??>${moneyFormatter.format(item.debit)}</#if></td>
                            <td class="text-right line-left text-nowrap"><#if item.credit??>${moneyFormatter.format(item.credit)}</#if></td>

                            <td class="text-right line-left"></td>
                            <td class="text-right line-left"></td>
                        </tr>
                    </#list>
                    <tr class="line-top">
                        <th class="text-nowrap">${dateTo}</th>
                        <th></th>
                        <th></th>

                        <th class="text-right line-left text-nowrap">
                            <#list debt?keys as key>
                                <div><#if debt[key].debitTo??>${moneyFormatter.format(debt[key].debitTo)}<#else>${moneyFormatter.formatZero(key)}</#if></div>
                            </#list>
                        </th>

                        <th class="text-right line-left text-nowrap">
                            <#list debt?keys as key>
                                <div><#if debt[key].creditTo??>${moneyFormatter.format(debt[key].creditTo)}<#else>${moneyFormatter.formatZero(key)}</#if></div>
                            </#list>
                        </th>

                        <th class="text-right line-left"></th>
                        <th class="text-right line-left"></th>
                    </tr>
                </tbody>
                <tfoot>
                    <tr class="line-top"><th colspan="7"></th></tr>
                </tfoot>
            </table>
            <br>
            <p>Galutinis saldo ${dateTo} sudaro:</p>
            <#list debt?keys as key>
                <p>${moneyFormatter.text(debt[key].debtTo)}</p>
            </#list>
            <br><br><br>
            <table>
                <tr>
                    <td width="40%" class="line"></td>
                    <td width="20%"></td>
                    <td width="40%" class="line"></td>
                </tr>
                <tr>
                    <td class="text-center">${company.name}</td>
                    <td></td>
                    <td class="text-center">${counterparty.name}</td>
                </tr>
            </table>
            <br>
            <p>
                Prašome 10-ties dienų laikotarpyje aktą pasirašyti ir išsiųsti ${company.name}
                <#if company.email?has_content>el.paštu ${company.email} arba</#if>
                adresu ${company.address}.
                Negavus nustatytu laiku iš Jūsų patvirtinto ir pasirašyto vieno tarpusavio atsiskaitymo suderinimo akto egzemplioriaus,
                savo likutį laikysime teisingu.
            </p>
        </div>
    </div>
</div>
</body>
</html>
