<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
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
        .line-right{border-right:1px solid #000}
        .small{padding:0;font-size:.66em}.text-right{text-align:right}.text-center{text-align:center}.text-nowrap{white-space:nowrap}
        label{text-align:right;font-weight:700;white-space:nowrap}
        .multiline{white-space:pre-line}
        h1,h2,h3,h4,h5,p{margin:0;padding:0}
        .wide{width:100%}
        html,body{font-family:Helvetica,Arial,sans-serif}
        @page{@top-left{content:'${declaration.docHeader.docId!""}';font-size:12px;font-family:Helvetica,Arial,sans-serif;font-weight:700}}
        @page{@top-right{content:'Lapas ' counter(page) ' iš ' counter(pages);font-size:12px;font-family:Helvetica,Arial,sans-serif}}
    </style>
</head>
<body>
<div class="page">
    <div class="subpage">
        <table border="0" cellspacing="0">
            <tr>
                <td colspan="3" class="line">
                    <div class="text-center">
                        <h3>PRIDĖTINĖS VERTĖS MOKESČIO GRĄŽINIMO UŽSIENIO KELEIVIUI DEKLARACIJA</h3>
                        <h3>THE RETURN OF THE VALUE-ADDED TAX REFUND TO A FOREIGN TRAVELLER</h3>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <object type="image/barcode" style="width:300px;height:30px;" value="${declaration.docHeader.docId}" format="CODE_128"></object>
                </td>
                <td class="text-right wide"><h4 class="text-nowrap">Deklaracijos Nr:<br>Declaration No:</h4></td>
                <td><h3 class="text-nowrap">${declaration.docHeader.docId}</h3></td>
            </tr>
            <tr>
                <td></td>
                <td class="text-right wide"><h4 class="text-nowrap">Korekcijos Nr:<br>Correction No:</h4></td>
                <td><h3 class="text-nowrap">${declaration.docHeader.docCorrNo}</h3></td>
            </tr>
            <tr>
                <td></td>
                <td class="text-right wide"><h4 class="text-nowrap">Pildymo data:<br>Completion date:</h4></td>
                <td><h3 class="text-nowrap">${declaration.docHeader.completionDate!""}</h3></td>
            </tr>
        </table>
        <table border="0" cellspacing="0">
            <tr>
                <td width="40%" class="line-top line line-right">
                    <h4>Prekybinkas / Salesman:</h4>
                    <p><b>${company.businessName}</b></p>
                    <#if company.code?has_content><p>Įmonės kodas / Reg. code: <b>${company.code}</b></p></#if>
                    <#if company.vatCode?has_content><p>PVM kodas / VAT code: <b>${company.vatCode}</b></p></#if>
                    <#if company.address?has_content><p>Adresas / Address: ${company.address}</p></#if>
                </td>
                <td width="60%" class="line-top line">
                    <h4>Užsienio keleivis / Foreign traveller:</h4>
                    <p>
                        <span class="text-nowrap">Vardas, pavardė / Full name: </span>
                        <span class="text-nowrap"><b>${declaration.customer.firstName} ${declaration.customer.lastName}</b></span>
                    </p>
                    <p class="text-nowrap">Gimimo data / Birth date: <b>${declaration.customer.birthDate}</b></p>
                    <p class="text-nowrap">Rezidavimo valstybė / Country of residence: <b>${declaration.customer.resCountryCode}</b></p>
                    <p class="text-nowrap">Dokumentas / Document:
                        <#if declaration.customer.idDocType == "1"><b>Pasas / Passport</b></#if>
                        <#if declaration.customer.idDocType == "2"><b>Asmens tapatybės kortelė / Identity Card</b></#if>
                    </p>
                    <p class="text-nowrap">Dokumento Nr / Document No: <b>${declaration.customer.idDocNo}</b></p>
                    <p class="text-nowrap">Dokumento šalis / Issuing country: <b>${declaration.customer.issuedBy}</b></p>
                    <#if declaration.customer.otherDocType??>
                        <p>
                            <span class="text-nowrap">Rezidavimo dok. tipas / Residence doc. type: </span>
                            <span class="text-nowrap"><b>${declaration.customer.otherDocType!""}</b></span>
                        </p>
                        <p>
                            <span class="text-nowrap">Rezidavimo dok. Nr / Residence doc. No: </span>
                            <span class="text-nowrap"><b>${declaration.customer.otherDocNo!""}</b></span>
                        </p>
                        <p>
                            <span class="text-nowrap">Rezidavimo dok. šalis / Residence doc. country: </span>
                            <span class="text-nowrap"><b>${declaration.customer.otherIssuedBy!""}</b></span>
                        </p>
                    </#if>
                </td>
            </tr>
        </table>

        <br>
        <p>PVM sąskaitos-faktūros serija ir Nr / Invoice No: <b>${declaration.salesDoc.invoiceNo}</b></p>
        <p>PVM sąskaitos-faktūros data / Invoice date: <b>${declaration.salesDoc.date}</b></p>

        <table border="0" cellspacing="0" style="-fs-table-paginate: paginate;-fs-page-break-min-height: 1.5cm;">
            <thead>
                <tr class="line">
                    <th class="text-right">Eilės numeris / Seq. number</th>
                    <th class="wide">Pavadinimas / Description</th>
                    <th class="text-right">Kiekis / Quantity</th>
                    <th>Matavimo vienetas / Unit Of Measure</th>
                    <th class="text-right">Kaina be PVM / Taxable Amount</th>
                    <th class="text-right">PVM tarifas / VAT rate</th>
                    <th class="text-right">PVM suma / VAT amount</th>
                    <th class="text-right">Viso suma / Total amount</th>
                </tr>
            </thead>
            <tbody>
                <#if declaration.salesDoc.goods??>
                    <#list declaration.salesDoc.goods as good>
                        <tr class="line">
                            <td class="text-right">${good.sequenceNo}</td>
                            <td>${good.description}</td>
                            <td class="text-right text-nowrap">${good.quantity}</td>
                            <td>${good.unitOfMeasureCode!""}${good.unitOfMeasureOther!""}</td>
                            <td class="text-right text-nowrap">${moneyFormatter.format("EUR",good.taxableAmount)}</td>
                            <td class="text-right text-nowrap">${good.vatRate} %</td>
                            <td class="text-right text-nowrap">${moneyFormatter.format("EUR",good.vatAmount)}</td>
                            <td class="text-right text-nowrap">${moneyFormatter.format("EUR",good.totalAmount)}</td>
                        </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#if qrcodes??>
            <br>
            <table border="0" cellspacing="0" style="-fs-table-paginate: paginate;-fs-page-break-min-height: 1.5cm;">
            <#list qrcodes as qrcode>
                <tr>
                    <td>
                        <object type="image/barcode" style="width:400px;height:400px;" value="${qrcode}">
                            <encode-hint name="ERROR_CORRECTION" value="M"></encode-hint>
                        </object>
                    </td>
                </tr>
            </#list>
            </table>
        </#if>

    </div>
</div>
</body>
</html>
