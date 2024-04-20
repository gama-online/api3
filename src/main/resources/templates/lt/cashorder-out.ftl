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

            <table border="0" cellspacing="0">
                <tr>
                    <td width="50%">
                        <h2 style="text-transform: uppercase;">Kasos išlaidų orderis</h2>
                    </td>
                    <td width="50%">
                        <div class="title">
                            <h2>
                                Serija <span style="font-size: 110%">${document.seriesName!""}</span>
                                Nr. <span style="font-size: 110%">${document.seriesNumber!""}</span>
                            </h2>
                            <h3>Data: ${document.date!""}</h3>
                        </div>
                    </td>
                </tr>
            </table>

            <br>
            <br>
            <br>

            <table border="0" cellspacing="0">
                <tr>
                    <td width="25%">Suma: </td>
                    <td width="75%"><b><span style="font-size: 110%">${moneyFormatter.text(moneyFormatter.negated(document.amount))}</span></b></td>
                </tr>

                <tr><td></td><td></td></tr>

                <tr>
                    <td>Pagrindas: </td>
                    <td class="multiline">
                       <b>${document.note!""}</b>
                    </td>
                </tr>

                <tr><td></td><td></td></tr>

                <tr>
                    <td>Gavau:</td>
                    <td class="line"></td>
                </tr>
                <tr><td></td><td class="small text-center">gauta suma žodžiais</td></tr>

                <tr><td></td><td></td></tr>

                <tr>
                    <td>Gavėjas: </td>
                    <td class="line">
                        <#if (document.employee)??><b>${document.employee.name!""}</b></#if>
                        <#if (document.counterparty)??>
                            <b>${document.counterparty.name!""}</b><#if document.counterparty.comCode?has_content>, Įmonės kodas: ${document.counterparty.comCode}</#if>
                            <br><br><br>
                            &nbsp;
                        </#if>
                    </td>
                </tr>
                <tr><td></td><td class="small text-center">vardas, pavardė, pareigos, parašas</td></tr>

            </table>

            <br>
            <br>
            <br>
            <br>

            <table border="0" cellspacing="0">
                <tr><td class="line"><b>Įmonės vadovas:</b></td></tr>
                <tr><td class="small text-center">vardas, pavardė, parašas</td></tr>
                <tr><td></td></tr>

                <tr><td class="line"><b>Vyr. buhalteris:</b></td></tr>
                <tr><td class="small text-center">vardas, pavardė, parašas</td></tr>
                <tr><td></td></tr>

                <tr>
                    <td class="line">
                        <b>Išmokėjo kasininkas:</b>
                        <#if document.cash??>
                            <#if document.cash.cashier?has_content> ${document.cash.cashier}</#if>
                        </#if>
                    </td>
                </tr>
                <tr><td class="small text-center">vardas, pavardė, parašas</td></tr>
            </table>

        </div>
    </div>
</div>
</body>