/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.gui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author ff
 *
 */
public class PDFExporter {
	
	public static void writeToFile(File f, Component m) throws Exception {
        Graphics2D g2;
        
        Document document = new Document(new Rectangle(
                m.getBounds().x,
                m.getBounds().y,
                m.getBounds().width,
                m.getBounds().height
                ));
        PdfWriter writer;
        writer = PdfWriter.getInstance(document, new FileOutputStream(f));

        document.open();
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(m.getSize().width, m.getSize().height);

        g2 = tp.createGraphicsShapes(m.getSize().width, m.getSize().height);

        m.paint(g2);
        g2.dispose();
        cb.addTemplate(tp, 0, 0);
        document.close();
    }

}
