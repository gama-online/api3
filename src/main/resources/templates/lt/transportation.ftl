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
        .wide{width:100%}
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
                        <h1>Važtaraštis</h1>
                    </td>
                    <td width="50%">
                        <div class="title">
                            <h2>
                                <#if document.seriesName?has_content>Serija <span style="font-size: 110%">${document.seriesName!""}</span></#if>
                                Nr. <span style="font-size: 110%">${document.seriesNumber!""}</span>
                            </h2>
                            <h3>Data: ${document.date!""}</h3>
                        </div>
                    </td>
                </tr>
            </table>

            <br>

            <table border="0" cellspacing="0">
                <thead>
                    <tr class="line">
                        <th class="text-right">#</th>
                        <th class="wide">Pavadinimas</th>
                        <th class="">Kodas</th>
                        <th class="">Vnt.</th>
                        <th class="text-right">Kiekis</th>
                    </tr>
                </thead>
                <tbody>
                    <#if document.partsFrom??>
                        <#assign nr = 0>
                        <#list document.partsFrom as part>
                        <tr class="line"><#assign nr = nr + 1>
                            <td class="text-right">${nr}</td>
                            <td>${part.name!""}</td>
                            <td class="text-nowrap">${part.sku!""}</td>
                            <td>${part.unit!""}</td>
                            <td class="text-right text-nowrap">${part.quantity!""}</td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>

            <br>
            <br>

            <table border="0" cellspacing="0">
                <#if document.partsFrom??>
                    <tr><td></td></tr>
                    <tr><td class="line"><b>Pastabos:</b> ${document.note!""}</td></tr>
                    <tr><td></td></tr>
                </#if>

                <tr><td></td></tr>
                <tr><td class="line"><b>Prekes išdavė:</b></td></tr>
                <tr><td class="small text-center">vardas, pavardė, parašas</td></tr>

                <tr><td></td></tr>
                <tr><td class="line"><b>Prekes priėmė:</b></td></tr>
                <tr><td class="small text-center">vardas, pavardė, parašas</td></tr>
             </table>

            <#if document.driver?has_content || document.transportId?has_content || document.transportMarque?has_content ||
                                       (document.loadAddress.address1)?has_content || (document.unloadAddress.address1)?has_content>
                <br>
                <br>
                <br>
                <table border="0" cellspacing="0">
                    <tr>
                        <td colspan="2" class="line"><b>Vairuotojas:</b> ${document.driver!""}</td>
                    </tr>
                    <tr>
                        <td class="line"><b>Automobilio valstybinis Nr:</b> ${document.transportId!""}</td>
                        <td class="line"><b>Automobilio markė:</b> ${document.transportMarque!""}</td>
                    </tr>
                    <tr>
                        <td width="50%">
                            <p><b>Pakrovimo sandėlis:</b></p>
                            <p>${document.warehouseFrom.name}</p>
                        </td>
                        <td width="50%">
                            <p><b>Iškrovimo sandėlis:</b></p>
                            <p>${document.warehouseTo.name}</p>
                        </td>
                    </tr>
                    <tr>
                        <td width="50%">
                            <p><b>Pakrovimo adresas:</b></p>
                            <#if (document.loadAddress.address1)?has_content><p>${document.loadAddress.address}</p></#if>
                        </td>
                        <td width="50%">
                            <p><b>Iškrovimo adresas:</b></p>
                            <#if (document.unloadAddress.address1)?has_content><p>${document.unloadAddress.address}</p></#if>
                        </td>
                    </tr>
                </table>

            </#if>
        </div>
    </div>
</div>
</body>