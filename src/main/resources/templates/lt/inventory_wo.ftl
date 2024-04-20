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

            <div class="text-center">
                <h2 style="text-transform: uppercase;">Nurašymo aktas</h2>
                <h3>Nr: ${document.number!""}</h3>
                <h3>Data: ${document.date!""}</h3>
            </div>

            <br>
            <br>
            <#if document.inventoryNote?has_content>
                <p class="multiline">${document.inventoryNote!""}</p>
                <br>
            </#if>
            <br>

            <table border="0" cellspacing="0">
                <thead>
                    <tr class="line">
                        <th class="text-right">#</th>
                        <th>Sandėlis</th>
                        <th>Kodas</th>
                        <th class="wide">Pavadinimas</th>
                        <th>Vnt.</th>
                        <th class="text-right text-nowrap">Kiekis (+/-)</th>
                        <th class="text-right text-nowrap">Savikaina (+/-)</th>
                    </tr>
                </thead>
                <#if document.parts??>
                <tbody>
                    <#assign nr = 0>
                    <#list document.parts as part>
                    <tr class="line">
                        <#assign nr = nr + 1>
                        <td class="text-right">${nr}</td>
                        <td>${part.warehouse.name!""}</td>
                        <td class="text-nowrap">${part.sku!""}</td>
                        <td>${part.name!""}</td>
                        <td>${part.unit!""}</td>
                        <td class="text-right text-nowrap">${part.quantity!""}</td>
                        <td class="text-right text-nowrap">${moneyFormatter.formatPrice(part.costTotal)}</td>
                    </tr>
                    </#list>
                </tbody>
                <tfoot>
                    <tr>
                        <th colspan="5" class="text-right">Iš viso:</th>
                        <th class="text-right text-nowrap">${totalQuantity!""}</th>
                        <th class="text-right text-nowrap">${moneyFormatter.formatPrice(totalCostTotal)}</th>
                    </tr>
                </tfoot>
                </#if>
            </table>

            <table border="0" cellspacing="0">
                <tr><td></td></tr>
                <tr><td class="line">&nbsp;</td></tr>
                <tr><td class="small text-center">vardas, pavardė, pareigos, parašas</td></tr>
                <tr><td></td></tr>
                <tr><td class="line">&nbsp;</td></tr>
                <tr><td class="small text-center">vardas, pavardė, pareigos, parašas</td></tr>
            </table>

        </div>
    </div>
</div>
</body>
</html>
