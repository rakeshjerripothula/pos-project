package com.increff.invoice.service;

import com.increff.invoice.exception.ApiException;
import com.increff.invoice.exception.ApiStatus;
import com.increff.invoice.model.internal.InvoiceModel;
import com.increff.invoice.util.XmlBuilderUtil;
import org.apache.fop.apps.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Base64;

@Service
public class PdfGenerationService {

    @Autowired
    private XmlBuilderUtil xmlBuilderUtil;

    public String generatePdf(InvoiceModel invoiceModel) {
        try {
            String xml = xmlBuilderUtil.buildInvoiceXml(invoiceModel);

            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            ByteArrayOutputStream pdfOutStream = new ByteArrayOutputStream();

            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOutStream);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Source xsl = new StreamSource(getClass().getClassLoader().getResourceAsStream("xsl/invoice.xsl"));
            Transformer transformer = transformerFactory.newTransformer(xsl);

            Source src = new StreamSource(new StringReader(xml));
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);

            byte[] pdfBytes = pdfOutStream.toByteArray();
            return Base64.getEncoder().encodeToString(pdfBytes);

        } catch (Exception e) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Could not create Invoice");
        }
    }
}
