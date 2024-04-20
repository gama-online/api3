<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta charset="UTF-8">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Pinigų kvitai</title>
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
        <div class="subpage" style="min-height:125mm">
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

            <br><br>

            <h2 class="text-center">
                PINIGŲ
                <#if (moneyFormatter.isPositive(document.amount))>PRIĖMIMO</#if>
                <#if (moneyFormatter.isNegative(document.amount))>IŠMOKĖJIMO</#if>
                KVITAS
            </h2>

            <br>

            <h3 class="text-center">Nr: ${document.number!""}</h3>
            <h3 class="text-center">Data: ${document.date}</h3>

            <br>

            <table border="0" cellspacing="0">
                <tr>
                    <td width="25%">
                        <#if (moneyFormatter.isPositive(document.amount))>Gauta už:</#if>
                        <#if (moneyFormatter.isNegative(document.amount))>Sumokėta už:</#if>
                    </td>
                    <td class="multiline">
                        ${document.note!""}
                    </td>
                </tr>
                <tr>
                    <td width="25%">
                        <#if (moneyFormatter.isNegative(document.amount))>Gavėjas:</#if>
                        <#if (moneyFormatter.isPositive(document.amount))>Mokėtojas:</#if>
                    </td>
                    <td>
                        <#if document.counterparty??>
                            ${document.counterparty.name!""},
                            Įm.kodas: ${document.counterparty.comCode!""}
                        </#if>
                    </td>
                </tr>
                <tr>
                    <td>Suma:</td>
                    <td><b>${moneyFormatter.text(moneyFormatter.abs(document.amount))}</b></td>
                </tr>

                <tr><td><br><br></td></tr>


                <tr class="line-bottom">
                    <td width="25%">
                        <#if (moneyFormatter.isPositive(document.amount))>Gavėjas:</#if>
                        <#if (moneyFormatter.isNegative(document.amount))>Mokėtojas:</#if>
                    </td>
                    <td>
                        ${document.employee.name}
                    </td>
                </tr>

                <tr><td><br><br></td></tr>

                <tr class="line-bottom">
                    <td width="25%">
                    <#if (moneyFormatter.isPositive(document.amount))>Pinigus išmokėjo:</#if>
                    <#if (moneyFormatter.isNegative(document.amount))>Pinigus gavo:</#if>
                    </td>
                    <td></td>
                </tr>
            </table>

        </div>

        <div style="border-top:1px dashed #D3D3D3;height:1px"></div><!-- antra dalis -->

        <div class="subpage" style="min-height:125mm; margin-top:10mm">
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

            <br><br>

            <h2 class="text-center">
                PINIGŲ
            <#if (moneyFormatter.isPositive(document.amount))>PRIĖMIMO</#if>
            <#if (moneyFormatter.isNegative(document.amount))>IŠMOKĖJIMO</#if>
                KVITAS
            </h2>

            <br>

            <h3 class="text-center">Nr: ${document.number!""}</h3>
            <h3 class="text-center">Data: ${document.date}</h3>

            <br>

            <table border="0" cellspacing="0">
                <tr>
                    <td width="25%">
                        <#if (moneyFormatter.isPositive(document.amount))>Gauta už:</#if>
                        <#if (moneyFormatter.isNegative(document.amount))>Sumokėta už:</#if>
                    </td>
                    <td class="multiline">
                        ${document.note!""}
                    </td>
                </tr>
                <tr>
                    <td width="25%">
                        <#if (moneyFormatter.isNegative(document.amount))>Gavėjas:</#if>
                        <#if (moneyFormatter.isPositive(document.amount))>Mokėtojas:</#if>
                    </td>
                    <td>
                        <#if document.counterparty??>
                            ${document.counterparty.name!""},
                            Įm.kodas: ${document.counterparty.comCode!""}
                        </#if>
                    </td>
                </tr>
                <tr>
                    <td>Suma:</td>
                    <td><b>${moneyFormatter.text(moneyFormatter.abs(document.amount))}</b></td>
                </tr>

                <tr><td><br><br></td></tr>


                <tr class="line-bottom">
                    <td>
                    <#if (moneyFormatter.isPositive(document.amount))>Gavėjas:</#if>
                    <#if (moneyFormatter.isNegative(document.amount))>Mokėtojas:</#if>
                    </td>
                    <td>
                    ${document.employee.name}
                    </td>
                </tr>

                <tr><td><br><br></td></tr>

                <tr class="line-bottom">
                    <td>
                    <#if (moneyFormatter.isPositive(document.amount))>Pinigus išmokėjo:</#if>
                    <#if (moneyFormatter.isNegative(document.amount))>Pinigus gavo:</#if>
                    </td>
                    <td></td>
                </tr>
            </table>

        </div>
    </div>
</div>
</body>